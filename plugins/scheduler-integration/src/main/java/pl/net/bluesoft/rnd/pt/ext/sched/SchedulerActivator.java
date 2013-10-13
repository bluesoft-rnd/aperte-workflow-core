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

        registry.getDataRegistry().registerModelExtension(SchedulerProperty.class);
        registry.getDataRegistry().commitModelExtensions();

        service = new QuartzSchedulerService(registry, schedulerProperties);
        service.init();

        registry.getBundleRegistry().registerService(ProcessToolSchedulerService.class, service, null);
        registry.getEventBusManager().subscribe(ScheduleJobEvent.class, service);
    }

    @Override
    protected void destroy() throws Exception {
        registry.getEventBusManager().unsubscribe(ScheduleJobEvent.class, service);
        registry.getBundleRegistry().removeRegisteredService(ProcessToolSchedulerService.class);
        service.destroy();
    }
}
