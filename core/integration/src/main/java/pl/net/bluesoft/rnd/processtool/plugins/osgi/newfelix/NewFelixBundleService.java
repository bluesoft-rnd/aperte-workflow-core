package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.ui.view.impl.DefaultViewRegistryImpl;
import org.osgi.framework.*;
import pl.net.bluesoft.rnd.processtool.plugins.*;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.BundleInstallationHandler;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.ErrorMonitor;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.FelixBundleService;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.FelixServiceBridge;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:53
 */
public class NewFelixBundleService implements FelixBundleService {
	private Felix felix;
	private ProcessToolRegistry registry;
	private BundleInstallationHandler handler;
	private BundleInfo bundleInfo;
	private ErrorMonitor errorMonitor;
	private Logger logger;
	private String pluginsDir;

	public NewFelixBundleService(ErrorMonitor errorMonitor, Logger logger) {
		this.errorMonitor = errorMonitor;
		this.logger = logger;
		this.handler = new BundleInstallationHandler(errorMonitor, logger);
		this.bundleInfo = new BundleInfo(logger);
	}

	@Override
	public void initialize(String felixDir, ProcessToolRegistryImpl registry) throws BundleException {
		stopFelix();

		setRegistry(registry);

		Map<String, Object> configMap = new HashMap<String, Object>();
		putBasicConfig(configMap);
		putStorageConfig(felixDir, configMap);
		putActivatorConfig(registry, configMap);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(Felix.class.getClassLoader());
			felix = new Felix(configMap);
			felix.init();
			felix.start();
		}
		finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
	}

	@Override
	public void setPluginsDir(String pluginsDir) {
		this.pluginsDir = pluginsDir;
		bundleInfo.setPluginsDir(pluginsDir);
	}

	private void setRegistry(ProcessToolRegistry registry) {
		this.registry = registry;
		handler.setRegistry(registry);
	}

	/**
	 * Sets Felix storage properties
	 *
	 * @param storageDir
	 * @param configMap
	 */
	private void putStorageConfig(String storageDir, Map<String, Object> configMap) {
		configMap.put(FelixConstants.FRAMEWORK_STORAGE, storageDir);
		configMap.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
	}

	/**
	 * Sets basic Felix properties
	 *
	 * @param configMap
	 */
	private void putBasicConfig(Map<String, Object> configMap) {
		configMap.put(FelixConstants.LOG_LEVEL_PROP, "4");
		configMap.put(FelixConstants.LOG_LOGGER_PROP, new org.apache.felix.framework.Logger() {
			@Override
			protected void doLog(Bundle bundle, ServiceReference sr, int level,
								 String msg, Throwable throwable) {
				if (throwable != null) {
					logger.log(Level.SEVERE, "Felix: " + msg + ", Throwable: " + throwable.getMessage(), throwable);
				} else {
					logger.log(Level.FINE, "Felix: " + msg);
				}
			}
		});

		configMap.put(FelixConstants.SERVICE_URLHANDLERS_PROP, true);
		configMap.put(FelixConstants.FRAMEWORK_BUNDLE_PARENT, FelixConstants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
		configMap.put("felix.auto.deploy.action", "install,update,start");
		configMap.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, getSystemPackages());
	}

	/**
	 * Sets Felix activator properties
	 *
	 * @param registry
	 * @param configMap
	 */
	private void putActivatorConfig(final ProcessToolRegistryImpl registry, Map<String, Object> configMap) {
		ArrayList<BundleActivator> activators = new ArrayList<BundleActivator>();
		activators.add(new BundleActivator() {
			private ProcessToolServiceBridge serviceBridge;

			@Override
			public void start(BundleContext context) throws Exception {
				if (registry != null) {
					registry.setOsgiBundleContext(context);
					serviceBridge = new FelixServiceBridge(felix);
					registry.addServiceLoader(serviceBridge);
					registerDefaultServices(context);
				}
			}

			@Override
			public void stop(BundleContext context) throws Exception {
				registry.removeServiceLoader(serviceBridge);
			}

			private void registerDefaultServices(BundleContext context) {
				context.registerService(ProcessToolRegistry.class.getName(), registry, new Hashtable<String, Object>());
				context.registerService(ViewRegistry.class.getName(), new DefaultViewRegistryImpl(), new Hashtable<String, Object>());
			}
		});

		configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
	}

	private String getSystemPackages() {
		try {
			InputStream is = getPackagesExportFileStream();
			String systemPackages = getPackagesExportFileContent(is);
			bundleInfo.setSystemPackages(systemPackages);
			return systemPackages;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error occurred while reading " + getPackagesExportFilePath(), e);
			return "";
		}
	}

	private InputStream getPackagesExportFileStream() {
		InputStream is;
		try {
			is = new FileInputStream(getPackagesExportFilePath());
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, "Error occurred while reading " + getPackagesExportFilePath(), e);
			logger.log(Level.SEVERE, "Falling back to bundled version");
			is = getClass().getResourceAsStream("/packages.export");
		}
		return is;
	}

	private String getPackagesExportFileContent(InputStream is) throws IOException {
		try {
			int c;
			StringBuilder sb = new StringBuilder();
			while ((c = is.read()) >= 0) {
				if (c == 10 || c == 13 || (char) c == ' ' || (char) c == '\t') {
					continue;
				}
				sb.append((char) c);
			}
			return sb.toString().replaceAll("\\s*", "").replaceAll(",+", ",");
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private String getPackagesExportFilePath() {
		return pluginsDir + File.separatorChar + "packages.export";
	}

	@Override
	public void stopFelix() throws BundleException {
		if (felix != null) {
			felix.stop();
			felix = null;
		}
	}

	boolean doInstallBundle(String bundlePath) {
		bundleInfo.installationStarted(bundlePath);
		Bundle bundle = installBundle(bundlePath);

		if (bundle != null) {
			try {
				handler.processBundleExtensions(bundle, Bundle.ACTIVE);

				bundle.start();

				bundleInfo.installationFinished(bundlePath);
				logger.info("STARTED: " + bundlePath);
				return true;
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "Exception processing bundle", e);
				errorMonitor.forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
			}
		}
		bundleInfo.installationFailed(bundlePath);
		return false;
	}

	boolean doUninstallBundle(Bundle bundle) {
		String bundlePath = getBundlePath(bundle);
		bundleInfo.uninstallationStarted(bundlePath);

		if (uninstallBundle(bundle, bundlePath)) {
			try {
				// in this state no extension have been installed
				if (bundle.getState() != Bundle.INSTALLED) {
					handler.processBundleExtensions(bundle, Bundle.STOPPING);
				}
				bundle.uninstall();
				bundleInfo.uninstallationFinished(bundlePath);
				logger.info("UNINSTALLED: " + bundlePath);
				return true;
			}
			catch(Exception e) {
				logger.log(Level.SEVERE, "Exception processing bundle", e);
				errorMonitor.forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
			}
		}
		bundleInfo.uninstallationFailed(bundlePath);
		return false;
	}

	private Bundle installBundle(String path) {
		try {
			logger.info("INSTALLING: " + path);
			Bundle bundle = felix.getBundleContext().installBundle("file://" + path.replace('\\','/'), new FileInputStream(path));
			bundle.update(new FileInputStream(path));
			logger.info("INSTALLED: " + path);
			return bundle;
		}
		catch (Throwable e) {
			logger.warning("BLOCKING: " + path);
			logger.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
	}

	private boolean uninstallBundle(Bundle bundle, String path) {
		try {
			logger.info("STOPPING: " + path);
			bundle.stop();
			logger.info("STOPPED: " + path);
			return true;
		}
		catch (Exception e) {
			logger.warning("UNABLE TO UNINSTALL BUNDLE: " + path);
			logger.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
	}

	@Override
	public synchronized void registerPlugin(String filename, InputStream is) {
		File fileRef = null;
		try {
			//create temp file
			File tempFile = fileRef = File.createTempFile(filename, Long.toString(System.nanoTime()));
			tempFile.setReadable(true, true);
			tempFile.setWritable(true, true);

			is.reset();
			FileOutputStream fos = new FileOutputStream(tempFile);
			try {
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) >= 0) {
					fos.write(buf, 0, len);
				}
				fos.flush();
			}
			finally {
				fos.close();
			}

			File dest = new File(pluginsDir, filename);
			if (!tempFile.renameTo(dest)) {
				throw new IOException("Failed to rename " + tempFile.getAbsolutePath() + " to " + dest.getAbsolutePath() +
						", as File.renameTo returns only boolean, the reason is unknown.");
			} else {
				logger.fine("Renamed " + tempFile.getAbsolutePath() + " to " + dest.getAbsolutePath());
			}
			fileRef = dest;
			logger.info("Installing bundle: " + dest.getAbsolutePath());
			bundleInfo.installationStarted(dest.getAbsolutePath());

			Bundle bundle = installBundle(dest.getAbsolutePath());
			bundle.start();

			bundleInfo.installationFinished(dest.getAbsolutePath());
			logger.info("STARTED: " + dest.getAbsolutePath());

			fileRef = null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to deploy plugin " + filename, e);
			throw new PluginManagementException(e);
		} finally {
			if (fileRef != null) {
				logger.fine("trying to remove leftover file " + fileRef.getAbsolutePath());
				fileRef.delete();
			}
		}
	}

	@Override
	public synchronized Collection<PluginMetadata> getRegisteredPlugins() {
		List<ProcessToolServiceBridge> serviceLoaders = registry.getServiceLoaders();
		List<PluginMetadata> registeredPlugins = new ArrayList<PluginMetadata>();
		for (ProcessToolServiceBridge serviceBridge : serviceLoaders) {
			try {
				registeredPlugins.addAll(serviceBridge.getInstalledPlugins());
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Failed to get registered plugins");
				throw new PluginManagementException(e);
			}
		}
		return registeredPlugins;
	}

	@Override
	public synchronized void enablePlugin(PluginMetadata pluginMetadata) {
		try {
			felix.getBundleContext().getBundle(pluginMetadata.getId()).start();
			logger.warning("Started bundle " + pluginMetadata.getName());
		} catch (BundleException e) {
			logger.log(Level.SEVERE, "Failed to start plugin " + pluginMetadata.getName(), e);
			throw new PluginManagementException(e);
		}
	}

	@Override
	public synchronized void disablePlugin(PluginMetadata pluginMetadata) {
		try {
			felix.getBundleContext().getBundle(pluginMetadata.getId()).stop();
			logger.warning("Stopped bundle " + pluginMetadata.getName());
		} catch (BundleException e) {
			logger.log(Level.SEVERE, "Failed to stop plugin " + pluginMetadata.getName(), e);
			throw new PluginManagementException(e);
		}
	}

	@Override
	public synchronized void uninstallPlugin(PluginMetadata pluginMetadata) {
		try {
			String file = pluginMetadata.getBundleLocation();
			file = file.replaceAll("file://", "");
			File f = new File(file);
			felix.getBundleContext().getBundle(pluginMetadata.getId()).uninstall();
			if (!f.delete()) {
				throw new PluginManagementException("Failed to remove file: " + file);
			} else {
				logger.warning("Uninstalled bundle " + pluginMetadata.getName() + ", removed file: " + file);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to uninstall plugin " + pluginMetadata.getName(), e);
			throw new PluginManagementException(e);
		}
	}

	@Override
	public synchronized void scheduledBundleInstall() {
		if (felix != null) {
			getBundleInstaller().syncBundlesWithRepository();
		}
		else {
			logger.warning("Felix not initialized yet");
		}
	}

	private BundleInstaller getBundleInstaller() {
		return new DependencyWiseBundleInstaller(this, bundleInfo, logger);
	}

	public List<Bundle> getInstalledBundles() {
		List<Bundle> bundles = new ArrayList<Bundle>();

		for (Bundle bundle : felix.getBundleContext().getBundles()) {
			if (!isSystemBundle(bundle) &&
				(bundle.getState() & (Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0) {
				bundles.add(bundle);
			}
		}
		return bundles;
	}

	private boolean isSystemBundle(Bundle bundle) {
		return "System Bundle".equals(bundle.getLocation());
	}

	List<String> getInstalledBundlePaths() {
		List<String> bundlePaths = new ArrayList<String>();

		for (Bundle bundle : getInstalledBundles()) {
			String bundlePath = getBundlePath(bundle);
			bundlePaths.add(bundlePath);
		}
		return bundlePaths;
	}

	String getBundlePath(Bundle bundle) {
		return bundle.getLocation().replaceFirst("file://", "").replace("/", File.separator);
	}

	Bundle getBundle(String bundlePath) {
		for (Bundle bundle : getInstalledBundles()) {
			if (getBundlePath(bundle).equals(bundlePath)) {
				return bundle;
			}
		}
		return null;
	}
}
