package pl.net.bluesoft.rnd.processtool.usersource.impl;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import org.aperteworkflow.integration.liferay.utils.LiferayUserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;
import pl.net.bluesoft.util.lang.ExpiringCache;

import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.*;

/**
 * {@link IUserSource} implementation for Liferay Portal
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferayUserSource implements IPortalUserSource 
{
	private static final ExpiringCache<String, List<UserData>> allUsers = new ExpiringCache<String, List<UserData>>(15 * 60 * 1000);
	private static final ExpiringCache<String, UserData> usersByLogin = new ExpiringCache<String, UserData>(15 * 60 * 1000);

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    public LiferayUserSource()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }


	@Override
	public UserData getUserByLogin(final String login) throws UserSourceException
	{
        if (login == null) {
            return null;
        }

		return usersByLogin.get(login, new ExpiringCache.NewValueCallback<String, UserData>() {
			@Override
			public UserData getNewValue(String key) {
                long[] companyIds = PortalUtil.getCompanyIds();

				for (long companyId : companyIds) {
					try {
						User u = UserLocalServiceUtil.getUserByScreenName(companyId, login);
						if (u != null) {
							return LiferayUserConverter.convertLiferayUser(u);
						}
					}
					catch (PortalException e) {
						// continue
					}
					catch (Exception e) {
						throw new UserSourceException(e);
					}
				}
				if (ADMIN_USER.getLogin().equals(login)) {
					return ADMIN_USER;
				}
				if (SYSTEM_USER.getLogin().equals(login)) {
					return SYSTEM_USER;
				}
				return null;
			}
		});
	}

	@Override
	public UserData getUserByLogin(String login, Long companyId) throws UserSourceException 
	{
        try {
            User user = UserLocalServiceUtil.getUserByScreenName(companyId, login);
            return LiferayUserConverter.convertLiferayUser(user);
        }
        catch (Exception e) {
            throw new UserSourceException(e);
        }
	}

	@Override
	public UserData getUserByEmail(String email) 
	{
        if (email == null) 
            return null;
        
        try {
            long[] companyIds = PortalUtil.getCompanyIds();
            for (int i = 0; i < companyIds.length; ++i) {
                long ci = companyIds[i];
                try {
                    User u = UserLocalServiceUtil.getUserByEmailAddress(ci, email);
                    if (u != null) {
                        return LiferayUserConverter.convertLiferayUser(u);
                    }
                }
                catch (NoSuchUserException e) {
                    // continue
                }
            }
            return null;
        }
        catch (Exception e) {
            throw new UserSourceException(e);
        }
	}

	@Override
	public List<UserData> getAllUsers()
	{
		return allUsers.get(null, new ExpiringCache.NewValueCallback<String, List<UserData>>() {
			@Override
			public List<UserData> getNewValue(String key) {
				try {
					List<UserData> users = LiferayUserConverter.convertLiferayUsers(UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount()));

					for (UserData user : users) {
						Logger.getLogger(LiferayUserSource.class.getName()).warning("--------> Caching user " + user.getLogin());
						usersByLogin.put(user.getLogin(), user);
					}
					return users;
				}
				catch (SystemException e) {
					throw new UserSourceException(e);
				}
			}
		});
	}

	@Override
	public UserData getUserByRequest(HttpServletRequest request) 
	{
        try
        {
            IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
            return authorizationService.getUserByRequest(request);
        }
        catch (Exception e) 
        {
        	throw new UserSourceException("User not found", e);
        }
	}
	
	@Override
	public UserData getUserByRequest(RenderRequest request) 
	{
		/* Get HttpServletRequest from RenderRequest */
		HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request);
		
		return getUserByRequest(httpRequest);
	}
}
