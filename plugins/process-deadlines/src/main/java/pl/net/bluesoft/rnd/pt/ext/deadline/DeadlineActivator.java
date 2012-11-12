package pl.net.bluesoft.rnd.pt.ext.deadline;

import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadlineActivator extends AbstractPluginActivator implements EventListener<BpmEvent> {
    private DeadlineEngine engine;
    
    private Logger logger = Logger.getLogger(DeadlineActivator.class.getName());

    @Override
    protected void init() throws Exception {
        Properties pluginProperties = loadProperties("plugin.properties");

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
    public void onEvent(BpmEvent event) {
    	try {
    		engine.onProcessStateChange(event.getTask(), event.getProcessInstance(), event.getEventType() == Type.NEW_PROCESS);
    	} catch (Throwable t) {
    		logger.log(Level.WARNING, "Exception while processing deadline event " + event.toString(), t);
    	}
    }
}
