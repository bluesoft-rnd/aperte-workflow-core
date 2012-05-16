package pl.net.bluesoft.rnd.pt.ext.deadline;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.deadline.model.DeadlineNotificationTemplate;
import pl.net.bluesoft.util.eventbus.EventListener;

public class DeadlineActivator implements BundleActivator, EventListener<BpmEvent> {
    private DeadlineEngine engine;

    private ProcessToolRegistry getRegistry(BundleContext context) {
        ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
        return (ProcessToolRegistry) context.getService(ref);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        final ProcessToolRegistry registry = getRegistry(context);
        registry.registerModelExtension(DeadlineNotificationTemplate.class);
        registry.commitModelExtensions();
        registry.getEventBusManager().subscribe(BpmEvent.class, this);

        engine = new DeadlineEngine(registry);

        registry.withProcessToolContext(new ProcessToolContextCallback() {
		    @Override
		    public void withContext(ProcessToolContext ctx) {
			    ProcessToolContext.Util.setThreadProcessToolContext(ctx);
			    try {
				    engine.setupProcessDeadlines();
			    } finally {
				    ProcessToolContext.Util.removeThreadProcessToolContext();
			    }
		    }
	    });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ProcessToolRegistry registry = getRegistry(context);
        registry.getEventBusManager().unsubscribe(BpmEvent.class, this);
        engine.stopScheduler();
    }

    @Override
    public void onEvent(BpmEvent e) {
        if (BpmEvent.Type.SIGNAL_PROCESS == e.getEventType() || BpmEvent.Type.NEW_PROCESS == e.getEventType()) {
            engine.onProcessStateChange(e.getProcessInstance(), e.getUserData());
        }
    }
}
