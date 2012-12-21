package pl.net.bluesoft.rnd.pt.ext.user;

import pl.net.bluesoft.rnd.processtool.service.ProcessToolUserService;
import pl.net.bluesoft.rnd.pt.ext.user.service.UserDataService;

import java.util.Properties;

public class UserServiceActivator extends AbstractPluginActivator {
    @Override
    protected void init() throws Exception {
        UserDataService service = new UserDataService(registry);
        registry.registerService(ProcessToolUserService.class, service, new Properties());
    }

    @Override
    protected void destroy() throws Exception {
        registry.removeRegisteredService(ProcessToolUserService.class);
    }
}
