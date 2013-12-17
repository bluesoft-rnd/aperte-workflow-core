package pl.net.bluesoft.rnd.processtool.usersource.impl;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.Portal;
import com.liferay.portal.util.PortalUtil;
import org.aperteworkflow.integration.liferay.utils.LiferayUserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.cache.CacheProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;
import pl.net.bluesoft.util.lang.ExpiringCache;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.ADMIN_USER;
import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.SYSTEM_USER;

/**
 * {@link IUserSource} implementation for Liferay Portal
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferayUserSource implements IPortalUserSource, CacheProvider
{
	private static final ExpiringCache<String, List<UserData>> allUsers = new ExpiringCache<String, List<UserData>>(15 * 60 * 1000);
	private static final ExpiringCache<String, UserData> usersByLogin = new ExpiringCache<String, UserData>(15 * 60 * 1000);

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    @Autowired
    private IAuthorizationService authorizationService;

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
            return authorizationService.getUserByRequest(request);
        }
        catch (Exception e) 
        {
        	throw new UserSourceException("User not found", e);
        }
	}
	
	@Override
	public UserData getUserByRequest(PortletRequest request)
	{
		/* Get HttpServletRequest from RenderRequest */
        Portal portal = PortalUtil.getPortal();
		HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request);
		
		return getUserByRequest(httpRequest);
	}

    @Override
    public HttpServletRequest getHttpServletRequest(PortletRequest request)
    {
        return PortalUtil.getHttpServletRequest(request);
    }

    @Override
    public HttpServletRequest getOriginalHttpServletRequest(HttpServletRequest request) {
        return PortalUtil.getOriginalServletRequest(request);
    }

    @Override
    public HttpServletResponse getHttpServletResponse(PortletResponse response) {
        return PortalUtil.getHttpServletResponse(response);
    }


    @Override
	public void invalidateCache() {
		allUsers.clear();
		usersByLogin.clear();
	}
}
