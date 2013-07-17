package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Process Tool Context factory
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory, ProcessToolBpmConstants {
    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());
    private ProcessToolRegistry registry;

    public ProcessToolContextFactoryImpl(ProcessToolRegistry registry) {
        this.registry = registry;
        initJbpmConfiguration();
    }

    @Override
    public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) 
    {
    	return withProcessToolContext(callback);
    }

    @Override
	public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) 
    {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ProcessToolRegistry.Util.getAwfClassLoader());

		try {
			ProcessToolRegistry.Util.getAwfClassLoader().loadClass(JbpmStepAction.class.getName());
		} catch (ClassNotFoundException e) {
			logger.warning("JbpmStepAction.class was not found");
		}
		
		
		try {
			ProcessToolContext ctx = getThreadProcessToolContext();

			/* Active context already exists, use it */
			if (ctx != null && ctx.isActive()) {
				return callback.processWithContext(ctx);
			}
    	
    		/* Context is set but its session is closed, remove it */
			if (ctx != null && !ctx.isActive()) {
				ProcessToolContext.Util.removeThreadProcessToolContext();
			}

			if (registry.isJta()) {
				return withProcessToolContextJta(callback);
			}
			else {
				return withProcessToolContextNonJta(callback);
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	public <T> T withProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback) 
	{
        T result = null;

		Session session = registry.getSessionFactory().openSession();
		try 
		{
			Transaction tx = session.beginTransaction();
			ProcessToolContext ctx = new ProcessToolContextImpl(session, registry);
			ProcessToolContext.Util.setThreadProcessToolContext(ctx);
			try
			{
				result = callback.processWithContext(ctx);
			}
			catch (RuntimeException e)
			{
				logger.log(Level.SEVERE, e.getMessage(), e);
				try {
					tx.rollback();
				}
				catch (Exception e1) {
					logger.log(Level.WARNING, e1.getMessage(), e1);
				}
				throw e;
			}
			finally
			{
				ctx.close();
				ProcessToolContext.Util.removeThreadProcessToolContext();
			}
			tx.commit();
		}
		finally 
		{
			if (session.isOpen()) session.close();
		}
        return result;
    }

    public <T> T withProcessToolContextJta(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

        try {
			UserTransaction ut = getUserTransaction();

            logger.fine("ut.getStatus() = " + ut.getStatus());

            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ut.rollback();
            }
            if (ut.getStatus() != Status.STATUS_ACTIVE) {
				ut.begin();
			}

			Session session = registry.getSessionFactory().getCurrentSession();

			try {
				ProcessToolContext ctx = new ProcessToolContextImpl(session, registry);
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				try
				{
					result = callback.processWithContext(ctx);
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					try
					{
						ut.rollback();
					}
					catch (Exception e1) {
						logger.log(Level.WARNING, e1.getMessage(), e1);
					}
					throw e;
				}
				finally
				{
					ctx.close();
					ProcessToolContext.Util.removeThreadProcessToolContext();
				}
			} finally {
                if (session.isOpen()) session.flush();
            }
            ut.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

	private UserTransaction getUserTransaction() throws NamingException {
		UserTransaction ut;
		try {
			ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		}
		catch (Exception e) {
			//it should work on jboss regardless. But it does not..
			logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
			ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
		}
		return ut;
	}

    @Override
    public ProcessToolRegistry getRegistry() {
        return registry;
    }

    @Override
	public void updateSessionFactory(SessionFactory sf) {
    }

	public void initJbpmConfiguration() {
		JbpmService.getInstance().init();
	}
}
