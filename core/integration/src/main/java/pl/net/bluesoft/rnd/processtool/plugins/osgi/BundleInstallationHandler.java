package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import com.thoughtworks.xstream.XStream;
import org.aperteworkflow.util.liferay.LiferayBridge;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper.getBundleResourceStream;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:17
 */
public class BundleInstallationHandler {
	public static final String		MODEL_ENHANCEMENT	    = "ProcessTool-Model-Enhancement";
	public static final String		WIDGET_ENHANCEMENT	    = "ProcessTool-Widget-Enhancement";
	public static final String		BUTTON_ENHANCEMENT  	= "ProcessTool-Button-Enhancement";
	public static final String		STEP_ENHANCEMENT	    = "ProcessTool-Step-Enhancement";
	public static final String		I18N_PROPERTY		    = "ProcessTool-I18N-Property";
	public static final String		PROCESS_DEPLOYMENT	    = "ProcessTool-Process-Deployment";
	public static final String		GLOBAL_DICTIONARY	    = "ProcessTool-Global-Dictionary";
	public static final String		ICON_RESOURCES		    = "ProcessTool-Resources-Icons";
	public static final String		HUMAN_NAME			    = "Bundle-HumanName-Key";
	public static final String      DESCRIPTION_KEY         = "Bundle-Description-Key";
	public static final String		RESOURCES		        = "ProcessTool-Resources";
	public static final String		ROLE_FILES			    = "ProcessTool-Role-Files";
	public static final String 		IMPLEMENTATION_BUILD    = "Implementation-Build";
	public static final String      TASK_ITEM_ENHANCEMENT   = "ProcessTool-TaskItem-Enhancement";
	public static final String      DESCRIPTION             = Constants.BUNDLE_DESCRIPTION;
	public static final String      HOMEPAGE_URL            = Constants.BUNDLE_UPDATELOCATION;
	public static final String      DOCUMENTATION_URL       = Constants.BUNDLE_DOCURL;

	public static final String[]	HEADER_NAMES		    = {
			MODEL_ENHANCEMENT, WIDGET_ENHANCEMENT, BUTTON_ENHANCEMENT, STEP_ENHANCEMENT, I18N_PROPERTY,
			PROCESS_DEPLOYMENT, GLOBAL_DICTIONARY, ICON_RESOURCES, RESOURCES, HUMAN_NAME, DESCRIPTION_KEY,
			ROLE_FILES, IMPLEMENTATION_BUILD, TASK_ITEM_ENHANCEMENT, DESCRIPTION, HOMEPAGE_URL, DOCUMENTATION_URL
	};


	private static final String SEPARATOR = "/";

	private ProcessToolRegistry registry;
	private ErrorMonitor errorMonitor;
	private Logger logger;

	public BundleInstallationHandler(ErrorMonitor errorMonitor, Logger logger) {
		this.errorMonitor = errorMonitor;
		this.logger = logger;
	}

	public synchronized void processBundleExtensions(final Bundle bundle, int eventType) throws ClassNotFoundException {
		if (registry.getProcessToolContextFactory() == null) {
			logger.severe("No default process tool context registered! - skipping process tool context-based processing of this OSGI bundle");
			return;
		}

		OSGiBundleHelper bundleHelper = new OSGiBundleHelper(bundle);

		if (bundleHelper.hasHeaderValues(MODEL_ENHANCEMENT)) {
			handleModelEnhancement(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(WIDGET_ENHANCEMENT)) {
			handleWidgetEnhancement(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(BUTTON_ENHANCEMENT)) {
			handleButtonEnhancement(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(STEP_ENHANCEMENT)) {
			handleStepEnhancement(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(I18N_PROPERTY)) {
			handleMessageSources(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(PROCESS_DEPLOYMENT)) {
			handleProcessRoles(eventType, bundleHelper, registry);
			handleProcessDeployment(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(GLOBAL_DICTIONARY)) {
			handleGlobalDictionaries(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(RESOURCES)) {
			handleBundleResources(eventType, bundleHelper, registry);
		}

		if (bundleHelper.hasHeaderValues(TASK_ITEM_ENHANCEMENT)) {
			handleTaskItemEnhancement(eventType, bundleHelper, registry);
		}
	}

	private void handleMessageSources(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
		final Bundle bundle = bundleHelper.getBundle();
		String[] properties = bundleHelper.getHeaderValues(I18N_PROPERTY);
		for (final String propertyFileName : properties) {
			String providerId = bundle.getBundleId() + File.separator + propertyFileName;
			if (eventType == Bundle.ACTIVE) {
				toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
					@Override
					public InputStream loadProperty(String path) throws IOException {
						return getBundleResourceStream(bundle, path);
					}
				}, propertyFileName), providerId);
			}
			else {
				toolRegistry.unregisterI18NProvider(providerId);
			}
		}
	}

	private void handleStepEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
		Bundle bundle = bundleHelper.getBundle();
		String[] classes = bundleHelper.getHeaderValues(STEP_ENHANCEMENT);
		for (String cls : classes) {
			if (eventType == Bundle.ACTIVE) {
				toolRegistry.registerStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
			}
			else {
				toolRegistry.unregisterStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
			}
		}
	}

	private void handleButtonEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
		Bundle bundle = bundleHelper.getBundle();
		String[] classes = bundleHelper.getHeaderValues(BUTTON_ENHANCEMENT);
		for (String cls : classes) {
			if (eventType == Bundle.ACTIVE) {
				toolRegistry.registerButton(bundle.loadClass(cls));
			}
			else {
				toolRegistry.unregisterButton(bundle.loadClass(cls));
			}
		}
	}

	private void handleWidgetEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
		Bundle bundle = bundleHelper.getBundle();
		String[] classes = bundleHelper.getHeaderValues(WIDGET_ENHANCEMENT);
		for (String cls : classes) {
			if (eventType == Bundle.ACTIVE) {
				toolRegistry.registerWidget(bundle.loadClass(cls));
			}
			else {
				toolRegistry.unregisterWidget(bundle.loadClass(cls));
			}
		}
	}

	private void handleModelEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
		Bundle bundle = bundleHelper.getBundle();
		String[] classes = bundleHelper.getHeaderValues(MODEL_ENHANCEMENT);
		Collection<Class> classSet = new HashSet<Class>();
		for (String cls : classes) {
			classSet.add(bundle.loadClass(cls));
		}
		if (!classSet.isEmpty()) {
			Class<?>[] extensions = classSet.toArray(new Class<?>[classSet.size()]);
			boolean needUpdate = eventType == Bundle.ACTIVE
					? toolRegistry.registerModelExtension(extensions)
					: toolRegistry.unregisterModelExtension(extensions);
			if (needUpdate) {
				logger.fine("Rebuilding Hibernate session factory...");
				try {
					toolRegistry.commitModelExtensions();
				}
				catch (Exception e) {
					logger.severe("Encountered problem while updating Hibernate mappings");
					logger.log(Level.SEVERE, e.getMessage(), e);
					toolRegistry.unregisterModelExtension(extensions);
				}
			}
			else {
				logger.warning("Skipping Hibernate session factory rebuild. Classes already processed.");
			}
		}
	}

	private void handleProcessDeployment(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
		final Bundle bundle = bundleHelper.getBundle();
		String[] properties = bundleHelper.getHeaderValues(PROCESS_DEPLOYMENT);
		for (String processPackage : properties) {
			String providerId = bundle.getBundleId() + SEPARATOR + processPackage.replace(".", SEPARATOR) + "/messages";
			if (eventType == Bundle.ACTIVE) {
				try {
					String basePath = SEPARATOR + processPackage.replace(".", SEPARATOR) + SEPARATOR;
					toolRegistry.deployOrUpdateProcessDefinition(
							bundleHelper.getBundleResourceStream(basePath + "processdefinition." +
									toolRegistry.getBpmDefinitionLanguage() + ".xml"),
							bundleHelper.getBundleResourceStream(basePath + "processtool-config.xml"),
							bundleHelper.getBundleResourceStream(basePath + "queues-config.xml"),
							bundleHelper.getBundleResourceStream(basePath + "processdefinition.png"),
							bundleHelper.getBundleResourceStream(basePath + "processdefinition-logo.png"));

					toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
						@Override
						public InputStream loadProperty(String path) throws IOException {
							return getBundleResourceStream(bundle, path);
						}
					}, "/" + processPackage.replace(".", SEPARATOR) + "/messages"), providerId);

					toolRegistry.registerProcessDictionaries(bundleHelper.getBundleResourceStream(basePath + "process-dictionaries.xml"));

				}
				catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
			}
			else { // ignore
				toolRegistry.unregisterI18NProvider(providerId);
			}
		}
	}

	private void handleProcessRoles(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry registry) {
		if (eventType != Bundle.ACTIVE) {
			return;
		}

		final Bundle bundle = bundleHelper.getBundle();

		if (bundleHelper.hasHeaderValues(ROLE_FILES)) {
			String[] files = bundleHelper.getHeaderValues(ROLE_FILES);
			for (String file : files) {
				try {
					InputStream input = bundleHelper.getBundleResourceStream(file);
					Collection<ProcessRoleConfig> roles = getRoles(input);
					createRoles(roles);
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
			}
		}

		if (bundleHelper.hasHeaderValues(PROCESS_DEPLOYMENT)) {
			String[] properties = bundleHelper.getHeaderValues(PROCESS_DEPLOYMENT);
			for (String processPackage : properties) {
				String basePath = SEPARATOR + processPackage.replace(".", SEPARATOR) + SEPARATOR;
				try {
					InputStream input = bundleHelper.getBundleResourceStream(basePath + "roles-config.xml");
					Collection<ProcessRoleConfig> roles = getRoles(input);
					createRoles(roles);
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
			}
		}
	}


	private Collection<ProcessRoleConfig> getRoles(InputStream input) {
		if (input == null) {
			return null;
		}
		XStream xstream = new XStream();
		xstream.aliasPackage("config", ProcessRoleConfig.class.getPackage().getName());
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(Boolean.class);
		xstream.useAttributeFor(Integer.class);
		return (Collection<ProcessRoleConfig>) xstream.fromXML(input);
	}

	private void createRoles(Collection<ProcessRoleConfig> roles) {
		if (roles != null) {
			for (ProcessRoleConfig role : roles) {
				try
				{
					boolean roleCreated = LiferayBridge.createRoleIfNotExists(role.getName(), role.getDescription());
					if (roleCreated)
						logger.log(Level.INFO, "Created role " + role.getName());

				} catch (RuntimeException e) {
					forwardErrorInfoToMonitor("adding role " + role.getName(), e);
					throw e;

				}
			}
		}
	}

	private void handleBundleResources(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
		Bundle bundle = bundleHelper.getBundle();
		String[] resources = bundleHelper.getHeaderValues(RESOURCES);
		for (String pack : resources) {
			if (eventType == Bundle.ACTIVE)
			{
				String basePath = SEPARATOR + pack.replace(".", SEPARATOR);
				if (!basePath.endsWith(SEPARATOR)) {
					basePath += SEPARATOR;
				}
				Enumeration<URL> urls = bundle.findEntries(basePath, null, true);
				while (urls.hasMoreElements()) {
					String path = urls.nextElement().getPath();
					toolRegistry.registerResource(bundle.getSymbolicName(), path);
				}
			}
			else {
				toolRegistry.removeRegisteredResources(bundle.getSymbolicName());
			}
		}
	}

	private void handleGlobalDictionaries(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
		String[] properties = bundleHelper.getHeaderValues(GLOBAL_DICTIONARY);
		if (eventType == Bundle.ACTIVE) {
			for (String pack : properties) {
				try {
					String basePath = SEPARATOR + pack.replace(".", SEPARATOR) + SEPARATOR;
					InputStream is = bundleHelper.getBundleResourceStream(basePath + "global-dictionaries.xml");
					if (is != null) {
						toolRegistry.registerGlobalDictionaries(is);
					}
					else {
						logger.log(Level.SEVERE, "No global dictionary stream found in package: " + pack);
					}
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundleHelper.getBundleMetadata().getDescription() + " global-dictionary", e);
				}
			}
		}
	}

	private void handleTaskItemEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
		Bundle bundle = bundleHelper.getBundle();
		String[] classes = bundleHelper.getHeaderValues(TASK_ITEM_ENHANCEMENT);
		for (String cls : classes) {
			if (eventType == Bundle.ACTIVE) {
				toolRegistry.registerTaskItemProvider(bundle.loadClass(cls));
			}
			else {
				toolRegistry.unregisterTaskItemProvider(bundle.loadClass(cls));
			}
		}
	}

	public void setRegistry(ProcessToolRegistry registry) {
		this.registry = registry;
	}

	private void forwardErrorInfoToMonitor(String path, Exception e) {
		errorMonitor.forwardErrorInfoToMonitor(path, e);
	}
}
