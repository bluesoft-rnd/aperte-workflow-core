package org.aperteworkflow.integration.liferay;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.impl.LiferayAuthorizationService;
import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.di.IDependencyInjectionInitializer;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.impl.LiferayUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.impl.LdapUsersSource;
import pl.net.bluesoft.rnd.processtool.usersource.impl.LiferayUserSource;

import java.util.logging.Logger;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class LiferayDependencyInjectionInitializer implements IDependencyInjectionInitializer
{
    private static final Logger logger = Logger.getLogger(LiferayDependencyInjectionInitializer.class.getName());

    @Override
    public void inject() {
        logger.info("Injecting Liferay dependencies...");

		/* Inject Liferay based user source */
        ClassDependencyManager.getInstance().injectImplementation(IUserSource.class, LiferayUserSource.class, 1, true);
        ClassDependencyManager.getInstance().injectImplementation(IPortalUserSource.class, LiferayUserSource.class, 1, true);
        ClassDependencyManager.getInstance().injectImplementation(IDirectoryService.class, LdapUsersSource.class, 1);

		/* Inject Liferay based role manager */
        ClassDependencyManager.getInstance().injectImplementation(IUserRolesManager.class, LiferayUserRolesManager.class, 1);

		/* Inject Liferay based authorization serice */
        ClassDependencyManager.getInstance().injectImplementation(IAuthorizationService.class, LiferayAuthorizationService.class, 1);
    }
}
