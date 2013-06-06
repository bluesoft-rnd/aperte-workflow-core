package org.aperteworkflow.integration.liferay;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.impl.LiferayAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.impl.LiferayUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.impl.LdapUsersSource;
import pl.net.bluesoft.rnd.processtool.usersource.impl.LiferayUserSource;
import pl.net.bluesoft.util.eventbus.EventListener;

/**
 * @author mpawlak@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> {
	
    private Logger logger = Logger.getLogger(Activator.class.getName());
	
	@Override
	public void start(BundleContext context) throws Exception 
	{
		injectImplementation();
	
	}
	
	/** Denpendency Injection */
	private void injectImplementation()
	{
		logger.info("Injecting Liferay dependencies...");
		
		/* Inject Liferay based user source */
		ClassDependencyManager.getInstance().injectImplementation(IUserSource.class, LiferayUserSource.class, 1);
		ClassDependencyManager.getInstance().injectImplementation(IPortalUserSource.class, LiferayUserSource.class, 1);
		ClassDependencyManager.getInstance().injectImplementation(IDirectoryService.class, LdapUsersSource.class, 1);
		
		/* Inject Liferay based role manager */
		ClassDependencyManager.getInstance().injectImplementation(IUserRolesManager.class, LiferayUserRolesManager.class, 1);
		
		/* Inject Liferay based authorization serice */
		ClassDependencyManager.getInstance().injectImplementation(IAuthorizationService.class, LiferayAuthorizationService.class, 1);
		
	}
	
	

	@Override
	public void stop(BundleContext context) throws Exception 
	{


	}

	@Override
	public void onEvent(BpmEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
