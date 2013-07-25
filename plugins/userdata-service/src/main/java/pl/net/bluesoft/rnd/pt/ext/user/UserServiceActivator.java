package pl.net.bluesoft.rnd.pt.ext.user;

import pl.net.bluesoft.rnd.processtool.service.ProcessToolUserService;

public class UserServiceActivator extends AbstractPluginActivator {
    @Override
    protected void init() throws Exception {
    }

    @Override
    protected void destroy() throws Exception {
        registry.removeRegisteredService(ProcessToolUserService.class);
    }
}
