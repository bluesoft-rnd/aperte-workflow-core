package pl.net.bluesoft.rnd.processtool.di;

import java.util.logging.Logger;


import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.impl.MockAuthorizationService;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.impl.AccessTokenFacade;
import pl.net.bluesoft.rnd.processtool.token.impl.AccessTokenFactory;

/**
 * This class initialize default api implementations
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DefaultDependencyInjectionInitializer implements IDependencyInjectionInitializer
{
	private static final Logger logger = Logger.getLogger(DefaultDependencyInjectionInitializer.class.getName());

    public static void injectDependencies()
    {
        logger.info("Setting up dependencies...");

        /* Service to authenticate and authorize clients */
        ClassDependencyManager.getInstance().injectImplementation(IAuthorizationService.class, MockAuthorizationService.class);

		/* Tokens */
        ClassDependencyManager.getInstance().injectImplementation(IAccessTokenFactory.class, AccessTokenFactory.class);
        ClassDependencyManager.getInstance().injectImplementation(ITokenService.class, AccessTokenFacade.class);

        logger.info("All dependencies injected");
    }


    @Override
    public void inject() {
        injectDependencies();
    }
}
