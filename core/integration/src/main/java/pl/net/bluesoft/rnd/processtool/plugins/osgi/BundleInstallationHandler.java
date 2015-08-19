package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import com.thoughtworks.xstream.XStream;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.*;
import pl.net.bluesoft.rnd.processtool.plugins.deployment.ProcessDeployer;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.beans.ScriptFileNameBean;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.domain.IContentProvider;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;
import pl.net.bluesoft.rnd.processtool.web.view.AbstractTaskListView;
import pl.net.bluesoft.rnd.processtool.web.view.ITasksListViewBeanFactory;
import pl.net.bluesoft.rnd.processtool.web.view.TaskListView;
import pl.net.bluesoft.rnd.processtool.web.view.TaskListViewProcessFactory;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetJavaScriptProvider;
import pl.net.bluesoft.rnd.util.AnnotationUtil;
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

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper.*;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:17
 */
public class BundleInstallationHandler {
    private static final String SEPARATOR = "/";

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Autowired
    private ApplicationContext applicationContext;

    private ErrorMonitor errorMonitor;
    private Logger logger;
    private DefaultListableBeanFactory beanFactory;

    public BundleInstallationHandler(ErrorMonitor errorMonitor, Logger logger) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        this.beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        this.errorMonitor = errorMonitor;
        this.logger = logger;
    }

    public synchronized void processBundleExtensions(Bundle bundle, int eventType) throws ClassNotFoundException {
        if (processToolRegistry.getDataRegistry().getProcessToolContextFactory() == null) {
            logger.severe("No default process tool context registered! - skipping process tool context-based processing of this OSGI bundle");
            return;
        }

        OSGiBundleHelper bundleHelper = new OSGiBundleHelper(bundle);

        if (bundleHelper.hasHeaderValues(SPRING_BEANS)) {
            handleSpringBeans(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(MAPPERS)) {
            handleMappers(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(VIEW)) {
            handleView(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(SCRIPT)) {
            handleScript(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(CONTROLLER)) {
            handleController(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(MODEL_ENHANCEMENT)) {
            handleModelEnhancement(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(WIDGET_ENHANCEMENT)) {
            handleWidgetEnhancement(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(BUTTON_ENHANCEMENT)) {
            handleButtonEnhancement(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(STEP_ENHANCEMENT)) {
            handleStepEnhancement(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(I18N_PROPERTY)) {
            handleMessageSources(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(PROCESS_DEPLOYMENT)) {
            handleProcessRoles(eventType, bundleHelper);
            handleProcessDeployment(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(GLOBAL_DICTIONARY)) {
            handleGlobalDictionaries(eventType, bundleHelper);
        }

        if (bundleHelper.hasHeaderValues(RESOURCES)) {
            handleBundleResources(eventType, bundleHelper);
        }


        if (bundleHelper.hasHeaderValues(TASK_LIST_VIEW)) {
            handleTasksListView(eventType, bundleHelper);
        }

		BundleExtensionHandlerParams params = new BundleExtensionHandlerParamsImpl(bundle, bundleHelper, eventType);

		for (BundleExtensionHandler handler : processToolRegistry.getBundleRegistry().getBundleExtensionHandlers()) {
			handler.handleBundleExtensions(params);
		}
	}

	private static class BundleExtensionHandlerParamsImpl implements BundleExtensionHandlerParams {
		private final Bundle bundle;
		private final OSGiBundleHelper bundleHelper;
		private final int eventType;

		private BundleExtensionHandlerParamsImpl(Bundle bundle, OSGiBundleHelper bundleHelper, int eventType) {
			this.bundle = bundle;
			this.bundleHelper = bundleHelper;
			this.eventType = eventType;
		}

		@Override
		public int getEventType() {
			return eventType;
		}

		@Override
		public boolean hasBundleHeader(String headerName) {
			return hasText(getHeaderText(headerName));
		}

		@Override
		public String getBundleHeaderValue(String headerName) {
			String headerText = getHeaderText(headerName);
			return hasText(headerText) ? headerText.trim() : null;
		}

		@Override
		public String[] getBundleHeaderValues(String headerName) {
			String headerText = getHeaderText(headerName);

			if (hasText(headerText)) {
				return headerText.replaceAll("\\s", "").split(",");
			}
			return new String[0];
		}

		private String getHeaderText(String headerName) {
			return bundle.getHeaders().get(headerName);
		}

		@Override
		public IBundleResourceProvider getBundleResourceProvider() {
			return bundleHelper;
		}

		@Override
		public Class loadClass(String className) {
			try {
				return bundle.loadClass(className);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

    private void handleMappers(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] mappers = bundleHelper.getHeaderValues(MAPPERS);

        for (String mapper : mappers) {
            try {
                //Class<? extends IAttributesMapper> mapperClass = (Class<? extends IAttributesMapper>) bundle.loadClass(mapper);
                Class<?> mapperClass = bundle.loadClass(mapper);

                if (IAttributesMapper.class.isAssignableFrom(mapperClass)) {
                    if (eventType == Bundle.ACTIVE) {
                        processToolRegistry.getDataRegistry().registerAttributesMapper((Class<? extends IAttributesMapper>) mapperClass);
                    } else {
                        processToolRegistry.getDataRegistry().unregisterAttributesMapper((Class<? extends IAttributesMapper>) mapperClass);
                    }
                } else if (IMapper.class.isAssignableFrom(mapperClass)) {
                    if (eventType == Bundle.ACTIVE) {
                        processToolRegistry.getDataRegistry().registerMapper((Class<? extends IMapper>) mapperClass);
                    } else {
                        processToolRegistry.getDataRegistry().unregisterMapper((Class<? extends IMapper>) mapperClass);
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }
    }

    private void handleSpringBeans(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] springBeans = bundleHelper.getHeaderValues(SPRING_BEANS);

        for (String springBean : springBeans) {
            try {
                Class<?> springBeanClass = bundle.loadClass(springBean);
                String springBeanSimpleClassName = springBeanClass.getSimpleName();

                if (eventType == Bundle.ACTIVE) {
                    beanFactory.registerBeanDefinition(springBeanSimpleClassName,
                            BeanDefinitionBuilder.
                                    genericBeanDefinition(springBeanClass).
                                    setScope(BeanDefinition.SCOPE_SINGLETON).
                                    getBeanDefinition()
                    );
                } else {
                    beanFactory.removeBeanDefinition(springBeanSimpleClassName);
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }
    }

    private void handleScript(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] javaScriptFiles = bundleHelper.getHeaderValues(SCRIPT);


        for (String javaScriptFileName : javaScriptFiles) {
            try {
                ScriptFileNameBean scriptFileNameBean = new ScriptFileNameBean(javaScriptFileName);
                IWidgetScriptProvider scriptProvider;

                if (scriptFileNameBean.getProviderClass().equals(FileWidgetJavaScriptProvider.class.getName())) {
                    scriptProvider =
                            new FileWidgetJavaScriptProvider(
                                    scriptFileNameBean.getFileName(),
                                    bundle.getResource(scriptFileNameBean.getFileName()));
                } else {
                    scriptProvider = (IWidgetScriptProvider) bundle
                            .loadClass(scriptFileNameBean.getProviderClass())
                            .getConstructor(String.class, URL.class)
                            .newInstance(scriptFileNameBean.getFileName());
                }


                if (eventType == Bundle.ACTIVE) {
                    processToolRegistry.getGuiRegistry().registerJavaScript(scriptFileNameBean.getFileName(), scriptProvider);
                } else {
                    processToolRegistry.getGuiRegistry().unregisterJavaScript(scriptFileNameBean.getFileName());
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }

    }

    private void handleView(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] widgetClasses = bundleHelper.getHeaderValues(VIEW);


        for (String widgetClass : widgetClasses) {
            try {
                Class<?> clazz = bundle.loadClass(widgetClass);
                String widgetName = AnnotationUtil.getAliasName(clazz);

                if (eventType == Bundle.ACTIVE) {
                    ProcessHtmlWidget htmlWidget = (ProcessHtmlWidget) clazz
                            .getConstructor(IBundleResourceProvider.class)
                            .newInstance(bundleHelper);

                    processToolRegistry.getGuiRegistry().registerHtmlView(widgetName, htmlWidget);
                } else {
                    processToolRegistry.getGuiRegistry().unregisterHtmlView(widgetName);
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }
    }

    private void handleController(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(CONTROLLER);

        for (String cls : classes) {
            try {
                Class<? extends IOsgiWebController> controllerClass =
                        (Class<? extends IOsgiWebController>) bundleHelper.getBundle().loadClass(cls);
                OsgiController controllerAnnotation = controllerClass.getAnnotation(OsgiController.class);

                String controllerName = controllerAnnotation.name();
                if (eventType == Bundle.ACTIVE) {
                    IOsgiWebController controller =
                            (IOsgiWebController) beanFactory.createBean(
                                    controllerClass,
                                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                    true);

                    processToolRegistry.getGuiRegistry().registerWebController(controllerName, controller);
                } else {
                    processToolRegistry.getGuiRegistry().unregisterWebController(controllerName);
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }

    }

    private void handleMessageSources(int eventType, OSGiBundleHelper bundleHelper) {
        final Bundle bundle = bundleHelper.getBundle();
        String[] properties = bundleHelper.getHeaderValues(I18N_PROPERTY);
        for (String propertyFileName : properties) {
            String providerId = bundle.getBundleId() + File.separator + propertyFileName;
            if (eventType == Bundle.ACTIVE) {
                processToolRegistry.getBundleRegistry().registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                    @Override
                    public InputStream loadProperty(String path) throws IOException {
                        return getBundleResourceStream(bundle, path);
                    }
                }, propertyFileName), providerId);
            } else {
                processToolRegistry.getBundleRegistry().unregisterI18NProvider(providerId);
            }
        }
    }

    private void handleStepEnhancement(int eventType, OSGiBundleHelper bundleHelper) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(STEP_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                processToolRegistry.getGuiRegistry().registerStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
            } else {
                processToolRegistry.getGuiRegistry().unregisterStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
            }
        }
    }

    private void handleButtonEnhancement(int eventType, OSGiBundleHelper bundleHelper) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(BUTTON_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                processToolRegistry.getGuiRegistry().registerButton((Class) bundle.loadClass(cls));
            } else {
                processToolRegistry.getGuiRegistry().unregisterButton((Class) bundle.loadClass(cls));
            }
        }
    }

    private void handleWidgetEnhancement(int eventType, OSGiBundleHelper bundleHelper) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(WIDGET_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                processToolRegistry.getGuiRegistry().registerWidget((Class) bundle.loadClass(cls));
            } else {
                processToolRegistry.getGuiRegistry().unregisterWidget((Class) bundle.loadClass(cls));
            }
        }
    }

    private void handleModelEnhancement(int eventType, OSGiBundleHelper bundleHelper) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(MODEL_ENHANCEMENT);
        Collection<Class> classSet = new HashSet<Class>();
        for (String cls : classes) {
            classSet.add(bundle.loadClass(cls));
        }
        if (!classSet.isEmpty()) {
            Class<?>[] extensions = classSet.toArray(new Class<?>[classSet.size()]);
            boolean needUpdate = eventType == Bundle.ACTIVE
                    ? processToolRegistry.getDataRegistry().registerModelExtension(extensions)
                    : processToolRegistry.getDataRegistry().unregisterModelExtension(extensions);
            if (needUpdate) {
                logger.fine("Rebuilding Hibernate session factory...");
                try {
                    processToolRegistry.getDataRegistry().commitModelExtensions();
                } catch (Exception e) {
                    logger.severe("Encountered problem while updating Hibernate mappings");
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    processToolRegistry.getDataRegistry().unregisterModelExtension(extensions);
                }
            } else {
                logger.warning("Skipping Hibernate session factory rebuild. Classes already processed.");
            }
        }
    }

    private void handleProcessDeployment(int eventType, final OSGiBundleHelper bundleHelper) {
        final Bundle bundle = bundleHelper.getBundle();
        String[] properties = bundleHelper.getHeaderValues(PROCESS_DEPLOYMENT);
        for (final String processPackage : properties) {
            final String providerId = bundle.getBundleId() + SEPARATOR + processPackage.replace(".", SEPARATOR) + "/messages";
            if (eventType == Bundle.ACTIVE) {
                final String basePath = SEPARATOR + processPackage.replace(".", SEPARATOR) + SEPARATOR;

                processToolRegistry.withExistingOrNewContext(new ProcessToolContextCallback() {
                    @Override
                    public void withContext(ProcessToolContext ctx) {
                        try {
                            /* Initialize process deployer */
                            ProcessDeployer processDeployer = new ProcessDeployer(ctx);

                            processDeployer.deployOrUpdateProcessDefinition(
                                    bundleHelper.getBundleResourceStream(basePath + "processdefinition." +
                                            processToolRegistry.getProcessToolSessionFactory().getBpmDefinitionLanguage()),
                                    bundleHelper.getBundleResourceStream(basePath + "processtool-config.xml"),
                                    bundleHelper.getBundleResourceStream(basePath + "queues-config.xml"),
                                    bundleHelper.getBundleResourceStream(basePath + "processdefinition.png"),
                                    bundleHelper.getBundleResourceStream(basePath + "processdefinition-logo.png")
                            );

                            getRegistry().getBundleRegistry().registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                                @Override
                                public InputStream loadProperty(String path) throws IOException {
                                    return getBundleResourceStream(bundle, path);
                                }
                            }, "/" + processPackage.replace(".", SEPARATOR) + "/messages"), providerId);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                            forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
                        }
                    }
                });
            } else { // ignore
                processToolRegistry.getBundleRegistry().unregisterI18NProvider(providerId);
            }
        }
    }

    private void handleProcessRoles(int eventType, OSGiBundleHelper bundleHelper) {
        if (eventType != Bundle.ACTIVE) {
            return;
        }

        Bundle bundle = bundleHelper.getBundle();

        if (bundleHelper.hasHeaderValues(ROLE_FILES)) {
            String[] files = bundleHelper.getHeaderValues(ROLE_FILES);
            for (String file : files) {
                try {
                    InputStream input = bundleHelper.getBundleResourceStream(file);
                    Collection<ProcessRoleConfig> roles = getRoles(input);
                    createRoles(roles);
                } catch (Exception e) {
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
                } catch (Exception e) {
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
                try {

					
					/* Check if role exist. If not, create it */
                    if (!userRolesManager.isRoleExist(role.getName())) {
                        userRolesManager.createRole(role.getName(), role.getDescription());
                        logger.log(Level.INFO, "Created role " + role.getName());
                    }

                } catch (RuntimeException e) {
                    forwardErrorInfoToMonitor("adding role " + role.getName(), e);
                    throw e;

                }
            }
        }
    }

    private void handleBundleResources(int eventType, OSGiBundleHelper bundleHelper) {
        Bundle bundle = bundleHelper.getBundle();
        String[] resources = bundleHelper.getHeaderValues(RESOURCES);
        for (String pack : resources) {
            if (eventType == Bundle.ACTIVE) {
                String basePath = SEPARATOR + pack.replace(".", SEPARATOR);
                if (!basePath.endsWith(SEPARATOR)) {
                    basePath += SEPARATOR;
                }
                Enumeration<URL> urls = bundle.findEntries(basePath, null, true);
                while (urls.hasMoreElements()) {
                    String path = urls.nextElement().getPath();
                    processToolRegistry.getBundleRegistry().registerResource(bundle.getSymbolicName(), path);
                }
            } else {
                processToolRegistry.getBundleRegistry().removeRegisteredResources(bundle.getSymbolicName());
            }
        }
    }

    private void handleGlobalDictionaries(int eventType, OSGiBundleHelper bundleHelper) {
        String[] properties = bundleHelper.getHeaderValues(GLOBAL_DICTIONARY);
        if (eventType == Bundle.ACTIVE) {
            for (String pack : properties) {
                try {
                    String basePath = SEPARATOR + pack.replace(".", SEPARATOR) + SEPARATOR;
                    InputStream is = bundleHelper.getBundleResourceStream(basePath + "global-dictionaries.xml");
                    if (is != null) {
                        processToolRegistry.registerGlobalDictionaries(is);
                    } else {
                        logger.log(Level.SEVERE, "No global dictionary stream found in package: " + pack);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    forwardErrorInfoToMonitor(bundleHelper.getBundleMetadata().getDescription() + " global-dictionary", e);
                }
            }
        }
    }
    private void handleTasksListView(int eventType, OSGiBundleHelper bundleHelper)
    {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(TASK_LIST_VIEW);

        for (String cls : classes)
        {
            try
            {
                Class<? extends AbstractTaskListView> viewClass =
                        (Class<? extends AbstractTaskListView>)bundleHelper.getBundle().loadClass(cls);
                TaskListView viewAnnotation = viewClass.getAnnotation(TaskListView.class);

                String fileName = viewAnnotation.fileName();
                if(fileName == null || fileName.isEmpty()) {
                    throw new RuntimeException("Error during task list factory registration: no file name declarated");
                }

                String queueId = viewAnnotation.queueId();
                if(queueId == null || queueId.isEmpty()) {
                    throw new RuntimeException("Error during task list factory registration: no queueId declarated");
                }

                String queueDisplayedName = viewAnnotation.queueDisplayedName();
                if(queueDisplayedName == null || queueDisplayedName.isEmpty()) {
                    throw new RuntimeException("Error during task list factory registration: no queueDisplayedName declarated");
                }

                String queueDisplayedDescription = viewAnnotation.queueDisplayedDescription();
                if(queueDisplayedDescription == null || queueDisplayedDescription.isEmpty()) {
                    throw new RuntimeException("Error during task list factory registration: no queueDisplayedDescription declarated");
                }

                Integer priority = viewAnnotation.priority();
                if(fileName == null || fileName.isEmpty()) {
                    throw new RuntimeException("Error during task list factory registration: no file name declarated");
                }

                AbstractTaskListView.QueueTypes queueType = viewAnnotation.queueType();
                if(queueType == null) {
                    throw new RuntimeException("Error during task list factory registration: no queueType declarated");
                }


                Class<? extends ITasksListViewBeanFactory> mainFactoryClass = viewAnnotation.mainFactory();
                if(mainFactoryClass == null) {
                    throw new RuntimeException("Error during task list factory registration: no mainFactoryClass declarated");
                }

                TaskListViewProcessFactory[] factories = viewAnnotation.processFactories();

                if (eventType == Bundle.ACTIVE)
                {

                    ITasksListViewBeanFactory mainFactoryInstance = mainFactoryClass
                            .getConstructor()
                            .newInstance();

                    IContentProvider contentProvider =
                            new FileWidgetContentProvider(fileName, bundleHelper);

                    AbstractTaskListView taskView = viewClass
                            .getConstructor(IContentProvider.class, ITasksListViewBeanFactory.class)
                            .newInstance(contentProvider, mainFactoryInstance);

                    /* Add process factories */
                    for(TaskListViewProcessFactory factoryAnnotation: factories) {
                        ITasksListViewBeanFactory processFactory = factoryAnnotation.factoryClass()
                                .getConstructor()
                                .newInstance();
                        taskView.setProcessFactory(factoryAnnotation.processName(), processFactory);
                    }

                    taskView.setPriority(priority);
                    taskView.setQueueId(queueId);
                    taskView.setQueueType(queueType);
                    taskView.setQueueDisplayedName(queueDisplayedName);
                    taskView.setQueueDisplayedDesc(queueDisplayedDescription);

                    processToolRegistry.getGuiRegistry().registerTasksListView(queueId, taskView);
                }
                else
                {
                    processToolRegistry.getGuiRegistry().unregisterTasksListView(queueId);
                }
            }
            catch (Throwable e)
            {
                logger.log(Level.SEVERE, e.getMessage(), e);
                forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
            }
        }

    }

    private void forwardErrorInfoToMonitor(String path, Throwable e) {
        errorMonitor.forwardErrorInfoToMonitor(path, e);
    }
}
