package pl.net.bluesoft.rnd.pt.ext.testabstract;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.postgresql.ds.PGPoolingDataSource;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;

/**
 * Klasa bazowa dla wszystkich testów wymagających dostępu do bazy
 * 
 * @author Maciej Pawlak, Bluesoft
 *
 */
public class AperteDataSourceTestCase extends TestCase
{
	private static String DATABASE_NAME = "awf-jbpm-old";
	private static String USER_NAME = "esod";
	private static String USER_PASSWORD = "esod";
	
	protected static ProcessToolRegistry registry;
	protected static Session session;
	
	@Override
	protected void setUp() throws Exception 
	{
        // Create initial context
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, 
            "org.apache.naming");            
        InitialContext ic = new InitialContext();

        ic.createSubcontext("java:");
        ic.createSubcontext("java:/comp");
        ic.createSubcontext("java:/comp/env");
        ic.createSubcontext("java:/comp/env/jdbc");
        
        ic.createSubcontext("mail");
       
        // DataSource dla postgresa
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        
        dataSource.setDataSourceName("aperte-workflow-ds");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName(DATABASE_NAME);
        dataSource.setUser(USER_NAME);
        dataSource.setPassword(USER_PASSWORD);
        
        
        ic.bind("java:/comp/env/jdbc/aperte-workflow-ds", dataSource);
        
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
		
		props.put("mail.smtp.socketFactory.class", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
		props.put("ssl.SocketFactory.provider", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
		
		Authenticator auth = new Authenticator() 
		{
			public PasswordAuthentication getPasswordAuthentication()
			{
			return new PasswordAuthentication(userName, password);
			}
		};
		
		javax.mail.Session session = javax.mail.Session.getInstance(props, auth);
		session.setDebug(true);
        
		ic.bind("mail/Default", session);
               
        System.setProperty("org.aperteworkflow.datasource", "java:/comp/env/jdbc/aperte-workflow-ds");
        
        DataSource lookup = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/aperte-workflow-ds");
        
    	registry = new ProcessToolRegistryImpl();
        
        ProcessToolContextFactory contextFactory = new ProcessToolContextFactoryImpl(registry);
        registry.setProcessToolContextFactory(contextFactory);
        
		super.setUp();
	}
	
	protected void doTest(final AperteTestMethod testMethod)
	{
		registry.withProcessToolContext(new ProcessToolContextCallback() 
		{
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ctx.getHibernateSession().beginTransaction();
				
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				
				testMethod.test();
				
				ctx.getHibernateSession().cancelQuery();
			}


		});
	}
	
	protected interface AperteTestMethod
	{
		void test();
	}

}
