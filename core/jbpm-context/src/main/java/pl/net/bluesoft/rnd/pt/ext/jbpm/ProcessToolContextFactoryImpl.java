package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

/**
 * Process Tool Context factory
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory, ProcessToolBpmConstants {

    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());
    private Configuration configuration;
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
    	ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
    	/* Active context already exists, use it */
    	if(ctx != null && ctx.isActive())
    		return callback.processWithContext(ctx);
    	
    	/* Context is set but its session is closed, remove it */
    	if(ctx != null && !ctx.isActive())
    		ProcessToolContext.Util.removeThreadProcessToolContext();
    	
    	ProcessToolRegistry.ThreadUtil.setThreadRegistry(registry);
    	
        if (registry.isJta()) {
            return withProcessToolContextJta(callback);
        } else {
            return withProcessToolContextNonJta(callback);
        }
    }

	public <T> T withProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback) 
	{
        T result = null;

		Session session = registry.getSessionFactory().openSession();
		try 
		{
			ProcessEngine pi = getProcessEngine();
			try 
			{
				Transaction tx = session.beginTransaction();
				ProcessToolContext ctx = new ProcessToolContextImpl(session, registry, pi);
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
						ctx.rollback();
					} catch (Exception e1) {
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
			finally {
				pi.close();
			}
		} 
		finally 
		{
			session.close();
		}
        return result;
    }

    public <T> T withProcessToolContextJta(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

        try {
            UserTransaction ut=null;
            try {
                ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            } catch (Exception e) {
                //it should work on jboss regardless. But it does not..
                logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
                ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
            }

            logger.fine("ut.getStatus() = " + ut.getStatus());
            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ut.rollback();
            }
            if (ut.getStatus() != Status.STATUS_ACTIVE)
                ut.begin();
            
            Session session = registry.getSessionFactory().getCurrentSession();
            
            try {
                ProcessEngine pi = getProcessEngine();
                try 
                {
                	ProcessToolContext ctx = new ProcessToolContextImpl(session, registry, pi);
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
        					ctx.rollback();
        					
                        } catch (Exception e1) {
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
                    pi.close();
                }
            } finally {
                session.flush();
            }
            ut.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;


    }

    private ProcessEngine getProcessEngine() {
        Thread t = Thread.currentThread();
        
        /* We need this to use correct ANTLR lib in Hibernate */
        ClassLoader previousLoader = registry.getClass().getClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            t.setContextClassLoader(newClassLoader);
            return configuration.buildProcessEngine();
        } finally {
            t.setContextClassLoader(previousLoader);
        }
    }

    @Override
    public ProcessToolRegistry getRegistry() {
        return registry;
    }

    public void updateSessionFactory(SessionFactory sf) {
        if (configuration != null) {
            configuration.setHibernateSessionFactory(sf);
        }
    }

    public void initJbpmConfiguration() {
        Thread t = Thread.currentThread();
        ClassLoader previousLoader = registry.getClass().getClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            t.setContextClassLoader(newClassLoader);
            configuration = new Configuration();
            configuration.setHibernateSessionFactory(registry.getSessionFactory());
        } finally {
            t.setContextClassLoader(previousLoader);
        }
    }

}
