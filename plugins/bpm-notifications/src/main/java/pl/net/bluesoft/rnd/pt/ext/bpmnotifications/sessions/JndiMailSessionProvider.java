package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;



import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Session provider for e-mail basen on jndi
 * 
 * @author mpawlak
 *
 */
public class JndiMailSessionProvider implements IMailSessionProvider
{
	private Logger logger = Logger.getLogger(JndiMailSessionProvider.class.getName());

	@Override
	public Session getSession(String profileName)
	{
		Session mailSession = tryLookupForSession("mail/"+profileName);
		
		if(mailSession == null)
			mailSession = tryLookupForSession("java:comp/env/mail/"+profileName);
		
		if(mailSession == null)
		{
			logger.severe("Connection name for jndi resource not found: "+profileName);
			throw new IllegalArgumentException("Connection name for jndi resource not found: "+profileName);
		}
		
		/* Add smtp authentication */
		
		String userName = mailSession.getProperties().getProperty("mail.smtp.user");
		String userPassword = mailSession.getProperties().getProperty("mail.smtp.password");
		String isDebug = mailSession.getProperties().getProperty("mail.debug");
		
//		String portString = mailSession.getProperties().getProperty("mail.smtp.port");
//		String protocol = mailSession.getProperties().getProperty("mail.transport.protocol");
//		String host = mailSession.getProperties().getProperty("mail.smtp.host");
		
		
		if(isDebug != null && isDebug.equals("true"))
		{
			for(Object property: mailSession.getProperties().keySet())
			{
				Object value = mailSession.getProperties().get(property);
				
				logger.info("Property "+property+" = "+value);
			}
		}
		
		
		if(userPassword == null)
			userPassword = mailSession.getProperties().getProperty("password");
		
		Authenticator authenticator = new JndiAuthenticator(userName, userPassword);
		
		/*Session session = */mailSession.getInstance(mailSession.getProperties(), authenticator);
		
//		PasswordAuthentication authentication = new PasswordAuthentication(userName,userPassword);
//
//	    URLName url=  new URLName(protocol,host , Integer.parseInt(portString), null, userName, userPassword);
//	    
//	    mailSession.setPasswordAuthentication(url,authentication);
		
		return mailSession;
	}
	
	private class JndiAuthenticator extends Authenticator
	{
		 String userName;
		 String userPassword;
		 
		 public JndiAuthenticator(String userName, String userPassword)
		 {
			 this.userName = userName;
			 this.userPassword = userPassword;
		 }
		 
		public PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(userName, userPassword);
		}
	}
	
	private Session tryLookupForSession(String profileName)
	{
		try
		{
			return (Session) new InitialContext().lookup(profileName);
		}
		catch(NamingException e)
		{
			logger.fine("Connection name for jndi resource not found: "+profileName);
			return null;
		}
	}

	@Override
	public void refreshConfig()
	{
		// TODO Auto-generated method stub

	}

}
