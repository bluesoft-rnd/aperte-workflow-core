package pl.net.bluesoft.rnd.pt.ext.user.service;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;
import pl.net.bluesoft.rnd.pt.ext.user.dao.UserDataDAO;
import pl.net.bluesoft.rnd.pt.ext.user.dao.UserDataDAOImpl;
import pl.net.bluesoft.rnd.pt.ext.user.model.PersistentUserData;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Standard implementation of {@link IUserSource} 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AperteUserSource implements IPortalUserSource {
	@Override
	public UserData getUserByLogin(String login) throws UserSourceException 
	{
		UserDataDAO userDao = new UserDataDAOImpl(getSession());

		PersistentUserData userData = userDao.loadUserByLogin(login);
		return userData;
	}

	@Override
	public UserData getUserByLogin(String login, Long companyId) throws UserSourceException {
		return getUserByLogin(login);
	}

	@Override
	public UserData getUserByEmail(String email) 
	{
		UserDataDAO userDao = new UserDataDAOImpl(getSession());
		
		return userDao.loadUserByEmail(email);
	}

	@Override
	public List<UserData> getAllUsers()
	{
		UserDataDAO userDao = new UserDataDAOImpl(getSession());
		
		return new ArrayList<UserData>(userDao.findAll());
	}

	
	private Session getSession()
	{
		Session session = getRegistry().getDataRegistry().getSessionFactory().getCurrentSession();
		if(session == null)
			session = getRegistry().getDataRegistry().getSessionFactory().openSession();
		
		return session;
	}

	@Override
	public UserData getUserByRequest(HttpServletRequest request) {
		throw new UnsupportedOperationException();
	}

    @Override
    public UserData getUserByRequest(PortletRequest request) {
        return null;
    }

    @Override
    public HttpServletRequest getHttpServletRequest(PortletRequest request) {
        return null;
    }

    @Override
    public HttpServletRequest getOriginalHttpServletRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    public HttpServletResponse getHttpServletResponse(PortletResponse response) {
        return null;
    }

}
