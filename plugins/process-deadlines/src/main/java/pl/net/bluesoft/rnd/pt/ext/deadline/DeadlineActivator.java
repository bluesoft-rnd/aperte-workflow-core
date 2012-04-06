package pl.net.bluesoft.rnd.pt.ext.deadline;

import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.pt.ext.deadline.model.DeadlineNotificationTemplate;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.Properties;

public class DeadlineActivator extends AbstractPluginActivator implements EventListener<BpmEvent> {
    private DeadlineEngine engine;

    @Override
    protected void init() throws Exception {
        Properties pluginProperties = loadProperties("plugin.properties");

        registry.registerModelExtension(DeadlineNotificationTemplate.class);
        registry.commitModelExtensions();

        engine = new DeadlineEngine(registry, pluginProperties);
        engine.init();

        registry.getEventBusManager().subscribe(BpmEvent.class, this);
    }

    @Override
    protected void destroy() throws Exception {
        registry.getEventBusManager().unsubscribe(BpmEvent.class, this);
        engine.destroy();
    }

    @Override
    public void onEvent(BpmEvent e) {
        engine.onProcessStateChange(e.getTask(), e.getProcessInstance(), e.getEventType() == Type.NEW_PROCESS);
    }
}
