package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.testabstract.AperteDataSourceTestCase;

public class TSLSendNotificationTests extends AperteDataSourceTestCase 
{
	public void testEngineForTSL()
	{
		doTest(new AperteTestMethod() 
		{
			@Override
			public void test() 
			{
				BpmNotificationEngine engine = new BpmNotificationEngine();
				registry.registerService(BpmNotificationService.class, engine, new Properties());
				
				try 
				{
					engine.sendNotification("Default", "awf@bluesoft.net.pl", "awf@bluesoft.net.pl", "test", "testujemy");
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					fail("Error during mail processing: "+e.getMessage());
				}
			}
		});

	}
	
	public void test_1()
	{
		Properties props = new Properties();

		
		props.put("mail.transport.protocol", "smtp"); 
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.user", "awf@bluesoft.net.pl");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.socketFactory.port", "587");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");


		SecurityManager security = System.getSecurityManager();

		try
		{
		Authenticator auth = new Authenticator() 
		{
			public PasswordAuthentication getPasswordAuthentication()
			{
			return new PasswordAuthentication("awf@bluesoft.net.pl", "awf");
			}
		};
		
		Session session = Session.getInstance(props, auth);
		session.setDebug(true);

		MimeMessage msg = new MimeMessage(session);
		msg.setText("test");
		msg.setSubject("test");
		msg.setFrom(new InternetAddress("awf@bluesoft.net.pl"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress("awf@bluesoft.net.pl"));
		Transport.send(msg);
		}
		catch (Exception mex)
		{
			mex.printStackTrace();
			fail();
		}
	}

}
