package pl.net.bluesoft.rnd.processtool.plugins.osgi.oldfelix;

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
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;


/**
 * User: POlszewski
 * Date: 2012-11-29
 * Time: 21:02
 *
 * This is original implementation, proved to work yet in not all cases.
 */
public class OldFelixBundleService implements FelixBundleService {
	private static class BundleInfo {
		private Long lastModified;
		private Long installDuration;
		private Set<String> exportedPackages = new HashSet<String>();
		private Set<String> importedPackages = new HashSet<String>();

		public Long getLastModified() {
			return lastModified;
		}

		public void setLastModified(Long lastModified) {
			this.lastModified = lastModified;
		}

		public Long getInstallDuration() {
			return installDuration;
		}

		public void setInstallDuration(Long installDuration) {
			this.installDuration = installDuration;
		}

		public Set<String> getExportedPackages() {
			return exportedPackages;
		}

		public void setExportedPackages(Set<String> exportedPackages) {
			this.exportedPackages = exportedPackages;
		}

		public Set<String> getImportedPackages() {
			return importedPackages;
		}

		public void setImportedPackages(Set<String> importedPackages) {
			this.importedPackages = importedPackages;
		}
	}

	private Felix felix;
	private ProcessToolRegistry registry;
	private BundleInstallationHandler handler;
	private Map<String, BundleInfo> bundleInfos;
	private ErrorMonitor errorMonitor;
	private Logger logger;
	private String pluginsDir;

	public OldFelixBundleService(ErrorMonitor errorMonitor, Logger logger) {
		this.errorMonitor = errorMonitor;
		this.logger = logger;
		this.handler = new BundleInstallationHandler(errorMonitor, logger);
		this.bundleInfos = new HashMap<String, BundleInfo>();
	}

	@Override
	public void initialize(String storageDir, final ProcessToolRegistryImpl registry) throws BundleException {
		stopFelix();

		setRegistry(registry);

		Map<String, Object> configMap = new HashMap<String, Object>();
		putBasicConfig(pluginsDir, configMap);
		putStorageConfig(storageDir, configMap);
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
	private void putBasicConfig(String pluginsDir, Map<String, Object> configMap) {
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
		configMap.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, getSystemPackages(pluginsDir));
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
					context.registerService(ProcessToolRegistry.class.getName(), registry, new Hashtable());
					context.registerService(ViewRegistry.class.getName(), new DefaultViewRegistryImpl(),
							new Hashtable<String, Object>());
				}
			}

			@Override
			public void stop(BundleContext context) throws Exception {
				registry.removeServiceLoader(serviceBridge);
			}
		});

		configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
	}

	@Override
	public void stopFelix() throws BundleException {
		if (felix != null) {
			felix.stop();
			felix = null;
		}
	}

	public synchronized void scheduledBundleInstall() {
		if (felix == null) {
			logger.warning("Felix not initialized yet");
			return;
		}

		Set<String> jarFilePathsInPluginsDir = new HashSet<String>();
		List<String> installableBundlePaths = getInstallableBundlePaths(pluginsDir, jarFilePathsInPluginsDir);
		removeBundles(jarFilePathsInPluginsDir, installableBundlePaths);
		if (installableBundlePaths == null || installableBundlePaths.isEmpty()) {
			return;
		}

		List<String> toInstall = new ArrayList<String>(installableBundlePaths);

		long start = new Date().getTime();
		installBundles(installableBundlePaths);
		long end = new Date().getTime() - start;

		logger.info(from(toInstall).select(new F<String, String>() {
			@Override
			public String invoke(String path) {
				return String.format("Bundle %s installed in %s seconds", path, nvl(getBundleInfo(path).getInstallDuration(), 0L) / 1000.0);
			}
		}).toString("\n"));

		logger.info(String.format("Bundles installed in %s seconds", end / 1000.0));

		if (!installableBundlePaths.isEmpty()) {
			logger.warning("UNABLE TO INSTALL BUNDLES: " + installableBundlePaths.toString());
		}
	}

	private List<String> getInstallableBundlePaths(String pluginsDir, Set<String> jarFilePathsInPluginsDir) {
		File f = new File(pluginsDir);
		if (!f.exists()) {
			logger.warning("Plugins dir not found: " + pluginsDir + " attempting to create...");
			if (!f.mkdir()) {
				logger.severe("Failed to create plugins directory: " + pluginsDir + ", please reconfigure!!!");
				return null;
			} else {
				logger.info("Created plugins directory: " + pluginsDir);
			}
		}
		String[] list = f.list();
		Arrays.sort(list);
		List<String> installableBundlePaths = new ArrayList<String>();
		for (String filename : list) {
			File subFile = new File(f.getAbsolutePath() + File.separator + filename);
			String path = subFile.getAbsolutePath();
			if (!subFile.isDirectory() && path.matches(".*jar$")) {
				Long lastModified = getBundleInfo(path).getLastModified();
				if (lastModified == null || lastModified < subFile.lastModified()) {
					installableBundlePaths.add(path);
					getBundleInfo(path).setLastModified(subFile.lastModified());
				}
				jarFilePathsInPluginsDir.add(path);
			}
		}
		return installableBundlePaths;
	}

	private BundleInfo getBundleInfo(String filename) {
		BundleInfo info = bundleInfos.get(filename);
		if (info == null) {
			bundleInfos.put(filename, info = new BundleInfo());
		}
		return info;
	}

	private void removeBundles(Set<String> jarFilePathsInPluginsDir, List<String> installableBundlePaths) {
		Set<String> removablePaths = new HashSet<String>(bundleInfos.keySet());
		removablePaths.removeAll(jarFilePathsInPluginsDir);
		if (!removablePaths.isEmpty()) {
			for (String path : removablePaths) {
				bundleInfos.remove(path);
			}
			removablePaths.addAll(installableBundlePaths);
			Set<Bundle> removedBundles = uninstallBundles(removablePaths);
			for (Bundle bundle : removedBundles) {
				try
				{
					handler.processBundleExtensions(bundle, Bundle.STOPPING);
				}
				catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "Exception processing bundle", e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
				/* Zabezpiecznie na wypadku bledu w kodzie, aby nie wywalal innych
								 * pakietow przy starcie
								 */
				catch(Exception ex)
				{
					logger.log(Level.SEVERE, "Exception processing bundle", ex);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), ex);
				}
			}
		}
	}

	private Set<Bundle> uninstallBundles(Set<String> removablePaths) {
		Set<Bundle> removedBundles = new HashSet<Bundle>();
		Bundle[] installedBundles = felix.getBundleContext().getBundles();
		for (Bundle bundle : installedBundles) {
			String path = bundle.getLocation().replaceFirst("file://", "");
			if (removablePaths.contains(path) && (bundle.getState() &
					(Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0) {
				try {
					logger.info("STOPPING: " + path);
					bundle.stop();
					logger.info("STOPPED: " + path);
					removedBundles.add(bundle);
				}
				catch (Exception e) {
					logger.warning("UNABLE TO UNINSTALL BUNDLE: " + path);
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return removedBundles;
	}

	private void installBundles(List<String> installableBundlePaths) {
		boolean installed = true;
		while (!installableBundlePaths.isEmpty() && installed) {
			installed = false;
			for (Iterator<String> it = installableBundlePaths.iterator(); it.hasNext(); ) {
				Bundle bundle = installBundle(it.next());
				if (bundle != null) {
					try {
						handler.processBundleExtensions(bundle, Bundle.ACTIVE);

						bundle.start();
						logger.info("STARTED: " + it);

						it.remove();
						installed = true;
					}
					catch (ClassNotFoundException e) {
						logger.log(Level.SEVERE, "Exception processing bundle", e);
					}
					/* Zabezpiecznie na wypadku bledu w kodzie, aby nie wywalal innych
										 * pakietow przy starcie
										 */
					catch(Exception ex)
					{
						logger.log(Level.SEVERE, "Exception processing bundle", ex);
						forwardErrorInfoToMonitor(bundle.getSymbolicName(), ex);
					}
				}
			}
		}
	}

	private synchronized Bundle installBundle(String path) {
		Bundle bundle;
		long start = new Date().getTime();
		try {
			logger.info("INSTALLING: " + path);
			bundle = felix.getBundleContext().installBundle("file://" + path.replace('\\','/'), new FileInputStream(path));
			bundle.update(new FileInputStream(path));
			logger.info("INSTALLED: " + path);
		}
		catch (Throwable e) {
			logger.warning("BLOCKING: " + path);
			logger.log(Level.WARNING, e.getMessage(), e);
			bundle = null;
		}
		getBundleInfo(path).setInstallDuration(new Date().getTime() - start);
		return bundle;
	}

	public String getSystemPackages(String basedir) {
		try {
			InputStream is;
			try {
				is = new FileInputStream(pluginsDir + File.separatorChar + "packages.export");
			}
			catch (IOException e) {
				logger.log(Level.SEVERE, "Error occurred while reading " + pluginsDir + File.separatorChar + "packages.export", e);
				logger.log(Level.SEVERE, "Falling back to bundled version");
				is = getClass().getResourceAsStream("/packages.export");
			}
			try {
				int c = 0;
				StringBuffer sb = new StringBuffer();
				while ((c = is.read()) >= 0) {
					if (c == 10 || c == 13 || (char) c == ' ' || (char) c == '\t') {
						continue;
					}
					sb.append((char) c);
				}
				return sb.toString().replaceAll("\\s*", "").replaceAll(",+", ",");
			} finally {
				if (is != null) {
					is.close();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error occurred while reading " + pluginsDir + File.separatorChar + "packages.export", e);
		}
		return "";
	}

	@Override
	public void registerPlugin(String filename, InputStream is) {
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
			Bundle bundle = installBundle(dest.getAbsolutePath());

			bundle.start();
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
	public Collection<PluginMetadata> getRegisteredPlugins() {
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
	public void enablePlugin(PluginMetadata pluginMetadata) {
		try {
			felix.getBundleContext().getBundle(pluginMetadata.getId()).start();
			logger.warning("Started bundle " + pluginMetadata.getName());
		} catch (BundleException e) {
			logger.log(Level.SEVERE, "Failed to start plugin " + pluginMetadata.getName(), e);
			throw new PluginManagementException(e);
		}
	}

	@Override
	public void disablePlugin(PluginMetadata pluginMetadata) {
		try {
			felix.getBundleContext().getBundle(pluginMetadata.getId()).stop();
			logger.warning("Stopped bundle " + pluginMetadata.getName());
		} catch (BundleException e) {
			logger.log(Level.SEVERE, "Failed to stop plugin " + pluginMetadata.getName(), e);
			throw new PluginManagementException(e);
		}
	}

	@Override
	public void uninstallPlugin(PluginMetadata pluginMetadata) {
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

	protected void forwardErrorInfoToMonitor(String path, Exception e)
	{
		errorMonitor.forwardErrorInfoToMonitor(path, e);
	}

	public String getMonitorInfo() {
		return errorMonitor.getMonitorInfo();
	}
}
