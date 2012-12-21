package pl.net.bluesoft.rnd.pt.ext.sched;

import pl.net.bluesoft.rnd.pt.ext.sched.event.ScheduleJobEvent;
import pl.net.bluesoft.rnd.pt.ext.sched.impl.QuartzSchedulerService;
import pl.net.bluesoft.rnd.pt.ext.sched.model.SchedulerProperty;
import pl.net.bluesoft.rnd.pt.ext.sched.service.ProcessToolSchedulerService;

import java.util.Properties;

public class SchedulerActivator extends AbstractPluginActivator {
    private QuartzSchedulerService service;

    @Override
    protected void init() throws Exception {
        Properties schedulerProperties = loadProperties("quartz.properties");

        registry.registerModelExtension(SchedulerProperty.class);
        registry.commitModelExtensions();

        service = new QuartzSchedulerService(registry, schedulerProperties);
        service.init();

        registry.registerService(ProcessToolSchedulerService.class, service, null);
        registry.getEventBusManager().subscribe(ScheduleJobEvent.class, service);
    }

    @Override
    protected void destroy() throws Exception {
        registry.getEventBusManager().unsubscribe(ScheduleJobEvent.class, service);
        registry.removeRegisteredService(ProcessToolSchedulerService.class);
        service.destroy();
    }
}
