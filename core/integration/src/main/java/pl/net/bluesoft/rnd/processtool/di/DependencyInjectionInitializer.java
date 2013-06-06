package pl.net.bluesoft.rnd.processtool.di;

import java.util.logging.Logger;

import org.aperteworkflow.ui.view.impl.GitWidgetVersionProvider;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.impl.MockAuthorizationService;
import pl.net.bluesoft.rnd.processtool.plugins.IWidgetVersionProvider;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.impl.AperteUserRolesManager;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.impl.AccessTokenFacade;
import pl.net.bluesoft.rnd.processtool.token.impl.AccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryServicePropertiesProvider;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.impl.AperteDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.impl.AperteDirectoryServiceProperties;
import pl.net.bluesoft.rnd.processtool.usersource.impl.AperteUserSource;

/**
 * This class initialize default interface implementations
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DependencyInjectionInitializer 
{
	private static final Logger logger = Logger.getLogger(DependencyInjectionInitializer.class.getName());
	
	/** Inject standard implementations */
	public static void injectDefaultDependecies()
	{
		logger.info("Setting up dependencies...");
		
		/* User source to provide operations on users */
		ClassDependencyManager.getInstance().injectImplementation(IUserSource.class, AperteUserSource.class);
		ClassDependencyManager.getInstance().injectImplementation(IPortalUserSource.class, AperteUserSource.class);
		ClassDependencyManager.getInstance().injectImplementation(IDirectoryServicePropertiesProvider.class, AperteDirectoryServiceProperties.class);
		ClassDependencyManager.getInstance().injectImplementation(IDirectoryService.class, AperteDirectoryService.class);
		
		/* Roles manager */
		ClassDependencyManager.getInstance().injectImplementation(IUserRolesManager.class, AperteUserRolesManager.class);
		
		/* Service to authenticate and authorize clients */
		ClassDependencyManager.getInstance().injectImplementation(IAuthorizationService.class, MockAuthorizationService.class);
		
		/* Tokens */
		ClassDependencyManager.getInstance().injectImplementation(IAccessTokenFactory.class, AccessTokenFactory.class);
		ClassDependencyManager.getInstance().injectImplementation(ITokenService.class, AccessTokenFacade.class);
		
		/* Widget version provider */
		ClassDependencyManager.getInstance().injectImplementation(IWidgetVersionProvider.class, GitWidgetVersionProvider.class);
		
		logger.info("All dependencies injected");
	}

}
