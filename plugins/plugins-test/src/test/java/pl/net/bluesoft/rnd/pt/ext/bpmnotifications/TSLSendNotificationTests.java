package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.hibernate.SQLQuery;
import org.hibernate.Transaction;
import org.hibernate.exception.GenericJDBCException;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade.NotificationsFacade;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.testabstract.AperteDataSourceTestCase;
import pl.net.bluesoft.util.lang.Strings;

public class TSLSendNotificationTests extends AperteDataSourceTestCase 
{
	Integer threadCount =0;
	
	public void testEngineForTSL()
	{
		doTest(new AperteTestMethod() 
		{
			@Override
			public void test() 
			{
				final BpmNotificationEngine engine = new BpmNotificationEngine(registry);
				registry.registerService(BpmNotificationService.class, engine, new Properties());
				
				try 
				{
					
					engine.addNotificationToSend("Default", "axa-mail@bluesoft.net.pl", "inz.pawlak@gmail.com", "test", "test", true);					
					engine.addNotificationToSend("Default", "axa-mail@bluesoft.net.pl", "inz.pawlak@gmail.com", "test2", "test2", true);
				} 
				catch (Exception e1) 
				{
					fail(e1.getMessage());

				}
				//ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession().buildLockRequest(LockOptions.UPGRADE);
//				
//				org.hibernate.Session session = registry.getSessionFactory().openSession();
//				session.beginTransaction();
//				SQLQuery query = session.createSQLQuery("select * from PT_EXT_BPM_NOTIFICATION for update nowait");
//				query.addEntity(BpmNotification.class);
				
				Collection<BpmNotification> notificationsToSend = getNotificationsToSend();
				Collection<BpmNotification> notificationsToSend2 = getNotificationsToSend();
				
				int i = 2;

			}
		});

	}
	
	public static Collection<BpmNotification> getNotificationsToSend()
	{
		org.hibernate.Session session = registry.getSessionFactory().openSession();
		Transaction transation = session.beginTransaction();
		
		SQLQuery query = session.createSQLQuery("select * from PT_EXT_BPM_NOTIFICATION for update NOWAIT");
		query.addEntity(BpmNotification.class);
		
		/* Try aquire lock for notifications */
		Collection<BpmNotification> notifications;
		
		try
		{
			notifications = query.list();
			
			//transation.commit();
		}
		/* Table is locked, return empty collection */
		catch(GenericJDBCException ex)
		{
			transation.rollback();
			
			return new ArrayList<BpmNotification>();
		}
		
		return notifications;
	}
	
	public void test_1()
	{
		final String userName = "axa-mail";
		final String password = "Blue105";
		
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", "192.168.2.12");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.user", userName);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", "588");
		props.put("mail.smtp.auth.plain.disable", "true");
		
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("ssl.SocketFactory.provider", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.class", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
		props.put("ssl.SocketFactory.provider", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
//		props.put("mail.transport.protocol", "smtp"); 
//		props.put("mail.debug", "true"); 
//		props.put("mail.smtp.starttls.enable", "false");
//		props.put("mail.smtp.host", "bluesoft.home.pl");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.user", "axa-mail");
//		props.put("mail.smtp.port", "465");
//		props.put("mail.smtp.password", "esod2011");
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.socketFactory.port", "465");

		try
		{
			Authenticator auth = new Authenticator() 
			{
				public PasswordAuthentication getPasswordAuthentication()
				{
				return new PasswordAuthentication(userName, password);
				}
			};
			
			Session session = Session.getInstance(props, auth);
			session.setDebug(true);
			
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress("axa-mail@bluesoft.net.pl"));
	        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("axa-mail@bluesoft.net.pl"));
	        message.setSubject("test");
	        message.setSentDate(new Date());
	        //body
	        MimeBodyPart messagePart = new MimeBodyPart();
	        messagePart.setContent("test <br><b>test gruby</b><br> test żołądków", (true ? "text/html" : "text/plain") + "; charset=\"UTF-8\"");
	        
	        Multipart multipart = new MimeMultipart("alternative");
	        multipart.addBodyPart(messagePart);

	        //zalaczniki
	        int counter = 0;
	        URL url;
	        

	        
	        message.setContent(multipart);
	        message.setSentDate(new Date());
			
    		Transport.send(message);
    		
//            Transport transport = session.getTransport("smtp");
//            transport.connect(secureHost, Integer.parseInt(securePort), userName, userPassword);
//            transport.sendMessage(msg, msg.getAllRecipients());
//            transport.close();

		}
		catch (Exception mex)
		{
			mex.printStackTrace();
			fail();
		}
	}

}
