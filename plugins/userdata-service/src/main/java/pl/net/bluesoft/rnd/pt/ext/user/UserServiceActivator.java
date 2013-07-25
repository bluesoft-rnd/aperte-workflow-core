package pl.net.bluesoft.rnd.pt.ext.user;

import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryServicePropertiesProvider;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.pt.ext.user.service.AperteDirectoryService;
import pl.net.bluesoft.rnd.pt.ext.user.service.AperteDirectoryServiceProperties;
import pl.net.bluesoft.rnd.pt.ext.user.service.AperteUserRolesManager;
import pl.net.bluesoft.rnd.pt.ext.user.service.AperteUserSource;

public class UserServiceActivator extends AbstractPluginActivator {
    @Override
    protected void init() throws Exception {
		ClassDependencyManager.getInstance().injectImplementation(IUserSource.class, AperteUserSource.class);
		ClassDependencyManager.getInstance().injectImplementation(IPortalUserSource.class, AperteUserSource.class);
		ClassDependencyManager.getInstance().injectImplementation(IDirectoryServicePropertiesProvider.class, AperteDirectoryServiceProperties.class);
		ClassDependencyManager.getInstance().injectImplementation(IDirectoryService.class, AperteDirectoryService.class);

		ClassDependencyManager.getInstance().injectImplementation(IUserRolesManager.class, AperteUserRolesManager.class);
	}

    @Override
    protected void destroy() throws Exception {
    }
}
