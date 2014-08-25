package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import org.aperteworkflow.ui.view.IViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.NotificationsConstants.ProviderType;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.addons.INotificationsAddonsManager;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.addons.mock.impl.NotificationAddonsMockManager;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEvent;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEventListener;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.BpmAdminPortletRender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ITemplateDataProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateDataProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.DatabaseMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.JndiMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.settings.NotificationsSettingsProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.IMailTemplateLoader;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.MailTemplateProvider;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper.getBundleResourceString;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> 
{

    @Autowired
    private ProcessToolRegistry processToolRegistry;
	
    private Logger logger = Logger.getLogger(Activator.class.getName());

    private BpmNotificationEngine engine;

	MailEventListener mailEventListener;
	private SchedulersActivator schedulerActivator;
	
	@Override
	public void start(final BundleContext context) throws Exception
	{
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        processToolRegistry.withProcessToolContext(new ProcessToolContextCallback()
        {
			@Override
			public void withContext(ProcessToolContext ctx)
			{
				injectImplementation();
				
				/* Init the bpm notification engine */
				engine = new BpmNotificationEngine();

                schedulerActivator = new SchedulersActivator(processToolRegistry);

                processToolRegistry.getBundleRegistry().registerService(IBpmNotificationService.class, engine, new Properties());

                processToolRegistry.getEventBusManager().subscribe(BpmEvent.class, Activator.this);

                mailEventListener = new MailEventListener(engine);
                processToolRegistry.getEventBusManager().subscribe(MailEvent.class, mailEventListener);

		        /* Register scheduler for notifications sending */
                schedulerActivator.scheduleNotificationsSend(engine);

                String path = "/pl/net/bluesoft/rnd/pt/ext/bpmnotifications/html/file.html";
                String html = getBundleResourceString(context.getBundle(), path);

                BpmAdminPortletRender.init(html);

                getViewRegistry(processToolRegistry).registerGenericPortletViewRenderer("admin", BpmAdminPortletRender.INSTANCE);
                getViewRegistry(processToolRegistry).registerGenericPortletViewRenderer("user", BpmAdminPortletRender.INSTANCE);
			}
   		     });


	}
	
	/** Denpendency Injection */
	private void injectImplementation()
	{
		logger.info("Injecting Liferay dependencies...");

    	String providerName = NotificationsSettingsProvider.getProviderType();
    	if(providerName == null)         {
            logger.info("Mail session provider set to database");
        ClassDependencyManager.getInstance().injectImplementation(IMailSessionProvider.class, DatabaseMailSessionProvider.class, 1);
        }
    else if(providerName.equals(ProviderType.JNDI.getParamterName()))
    	{
    		logger.info("Mail session provider set to jndi resources");
    		ClassDependencyManager.getInstance().injectImplementation(IMailSessionProvider.class, JndiMailSessionProvider.class, 1);
    	}
    	else if(providerName.equals(ProviderType.DATABASE.getParamterName()))
    	{
    		logger.info("Mail session provider set to database");
    		ClassDependencyManager.getInstance().injectImplementation(IMailSessionProvider.class, DatabaseMailSessionProvider.class, 1);
    	}
    	else
    	{
    		logger.severe("Unknown provider ["+providerName+"]!");
    		ClassDependencyManager.getInstance().injectImplementation(IMailSessionProvider.class, DatabaseMailSessionProvider.class, 1);
    	}
		
		/* Inject Liferay based user source */
		ClassDependencyManager.getInstance().injectImplementation(ITemplateDataProvider.class, TemplateDataProvider.class, 1);
		ClassDependencyManager.getInstance().injectImplementation(IMailTemplateLoader.class, MailTemplateProvider.class, 1);
		ClassDependencyManager.getInstance().injectImplementation(INotificationsAddonsManager.class, NotificationAddonsMockManager.class);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {

        processToolRegistry.getBundleRegistry().removeRegisteredService(IBpmNotificationService.class);
        processToolRegistry.getEventBusManager().unsubscribe(BpmEvent.class, this);
        processToolRegistry.getEventBusManager().unsubscribe(MailEvent.class, mailEventListener);
		mailEventListener = null;

		getViewRegistry(processToolRegistry).unregisterGenericPortletViewRenderer("admin", BpmAdminPortletRender.INSTANCE);
		getViewRegistry(processToolRegistry).unregisterGenericPortletViewRenderer("user", BpmAdminPortletRender.INSTANCE);
	}


	@Override
	public void onEvent(BpmEvent e)
	{
		if(Type.NEW_PROCESS == e.getEventType() || Type.END_PROCESS == e.getEventType())
			logger.log(Level.INFO, "Received event " + e.getEventType() + " for process " + e.getProcessInstance().getId());
		else if(Type.ASSIGN_TASK == e.getEventType() || Type.SIGNAL_PROCESS == e.getEventType())
			logger.log(Level.INFO, "Received event " + e.getEventType() + " for task " + e.getProcessInstance().getExternalKey() + "/" + e.getTask().getTaskName());
		
        if (Type.ASSIGN_TASK == e.getEventType() || Type.NEW_PROCESS == e.getEventType() ||
			Type.SIGNAL_PROCESS == e.getEventType() || Type.END_PROCESS == e.getEventType())
        {
            boolean processStarted = BpmEvent.Type.NEW_PROCESS == e.getEventType();
			boolean processEnded = BpmEvent.Type.END_PROCESS == e.getEventType();
            boolean enteringStep = Type.ASSIGN_TASK == e.getEventType() || Type.NEW_PROCESS == e.getEventType();
			engine.onProcessStateChange(e.getTask(), e.getProcessInstance(),
                    e.getUserLogin(), processStarted, processEnded, enteringStep);
        }
	}
	
	private IViewRegistry getViewRegistry(ProcessToolRegistry registry) {
		return registry.getRegisteredService(IViewRegistry.class);
	}
}
