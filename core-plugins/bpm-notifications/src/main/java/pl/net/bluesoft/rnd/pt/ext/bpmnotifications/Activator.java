package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.util.eventbus.EventListener;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> {

	BpmNotificationEngine engine = new BpmNotificationEngine();
	@Override
	public void start(BundleContext context) throws Exception {
		ProcessToolRegistry registry = getRegistry(context);
		registry.registerModelExtension(BpmNotificationConfig.class);
		registry.registerModelExtension(BpmNotificationTemplate.class);
		registry.commitModelExtensions();

		registry.getEventBusManager().subscribe(BpmEvent.class, this);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		ProcessToolRegistry registry = getRegistry(context);
		registry.getEventBusManager().unsubscribe(BpmEvent.class, this);

	}

	private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}

	public void onEvent(BpmEvent e) {
		if (BpmEvent.Type.SIGNAL_PROCESS == e.getEventType() ||
			BpmEvent.Type.NEW_PROCESS == e.getEventType())
	 	engine.onProcessStateChange(e.getProcessInstance(), e.getUserData());
	}
}
