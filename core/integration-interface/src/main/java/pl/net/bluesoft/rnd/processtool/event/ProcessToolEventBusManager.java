package pl.net.bluesoft.rnd.processtool.event;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.ConcurrentEventBusManager;

import java.util.concurrent.ExecutorService;

public class ProcessToolEventBusManager extends ConcurrentEventBusManager {
    protected ProcessToolRegistry registry;

    public ProcessToolEventBusManager(ProcessToolRegistry registry) {
        super();
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
                    public void withContext(ProcessToolContext ctx) {
                        ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                        try {
                            publish(event);
                        } finally {
                            ProcessToolContext.Util.removeThreadProcessToolContext();
                        }
                    }
                });
            }
        };
    }
}
