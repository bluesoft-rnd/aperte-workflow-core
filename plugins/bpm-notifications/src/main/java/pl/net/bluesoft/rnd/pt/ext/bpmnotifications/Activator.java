package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aperteworkflow.ui.view.ViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEvent;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEventListener;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.BpmAdminPortletRender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.util.eventbus.EventListener;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> {
	
    private Logger logger = Logger.getLogger(Activator.class.getName());

    private BpmNotificationEngine engine;
	MailEventListener mailEventListener;
	private SchedulersActivator schedulerActivator;
	
	@Override
	public void start(BundleContext context) throws Exception 
	{
		ProcessToolRegistry registry = getRegistry(context);
		
		
		schedulerActivator = new SchedulersActivator(registry);
		
		/* Init the bpm notification engine */
		engine = new BpmNotificationEngine(registry);
		
        registry.registerService(BpmNotificationService.class, engine, new Properties());
		registry.getEventBusManager().subscribe(BpmEvent.class, this);
		
		mailEventListener = new MailEventListener(engine);
		registry.getEventBusManager().subscribe(MailEvent.class, mailEventListener);
		
		/* Register scheduler for notifications sending */
		schedulerActivator.scheduleNotificationsSend(engine);
	
		getViewRegistry(registry).registerGenericPortletViewRenderer("admin", BpmAdminPortletRender.INSTANCE);
		getViewRegistry(registry).registerGenericPortletViewRenderer("user", BpmAdminPortletRender.INSTANCE);
	}
	
	

	@Override
	public void stop(BundleContext context) throws Exception {
		ProcessToolRegistry registry = getRegistry(context);
        registry.removeRegisteredService(BpmNotificationService.class);
		registry.getEventBusManager().unsubscribe(BpmEvent.class, this);
		registry.getEventBusManager().unsubscribe(MailEvent.class, mailEventListener);
		mailEventListener = null;

		getViewRegistry(registry).unregisterGenericPortletViewRenderer("admin", BpmAdminPortletRender.INSTANCE);
		getViewRegistry(registry).unregisterGenericPortletViewRenderer("user", BpmAdminPortletRender.INSTANCE);
	}

	private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}


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
                    e.getUserData(), processStarted, processEnded, enteringStep);
        }
	}
	
	private ViewRegistry getViewRegistry(ProcessToolRegistry registry) {
		return registry.getRegisteredService(ViewRegistry.class);
	}
}
