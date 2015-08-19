package pl.net.bluesoft.rnd.processtool.event;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.ConcurrentEventBusManager;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.lang.Maps;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessToolEventBusManager extends ConcurrentEventBusManager {
	private static final Logger logger = Logger.getLogger(ProcessToolEventBusManager.class.getName());

	protected ProcessToolRegistry registry;

    public ProcessToolEventBusManager(ProcessToolRegistry registry) {
        this.registry = registry;
    }

    public ProcessToolEventBusManager(ProcessToolRegistry registry, ExecutorService executorService) {
        super(executorService);
        this.registry = registry;
    }

    @Override
    protected Runnable getEventRunnable(final Object event) {
        return new Runnable() {
            @Override
            public void run() {
                registry.withProcessToolContext(new ProcessToolContextCallback() {
                    @Override
                    public void withContext(ProcessToolContext ctx) 
                    {
                       publish(event);
                    }
                });
            }
        };
    }

	// Since there is no way to override only part of this method,
	// its implementation is copied here and changed where necessary

	@Override
	public void publish(Object event) {
		logger.finest("Publishing event: " + event.getClass());
		Map<Class, Set<WeakReference<EventListener>>> map = getListenerMap();
		Class cls = event.getClass();
		while (cls != null) {
			synchronized (map) {
				Set<WeakReference<EventListener>> set = Maps.getSetFromMap(map, cls);
				for (Iterator<WeakReference<EventListener>> it = set.iterator(); it.hasNext(); ) {
					WeakReference<EventListener> ref = it.next();
					if (ref != null) {
						EventListener listener = ref.get();
						if (listener != null) {
							logger.finest("Receiving event by listener: " + listener.getClass().getName());
							try {
								listener.onEvent(event);
							}
							catch (Exception e) {
								logger.log(Level.SEVERE, e.getMessage(), e);
							}
						}
					}
					else {
						it.remove();
					}
				}
			}
			cls = cls.getSuperclass();
		}
	}
}
