package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
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
@Component
@Scope(value = "singleton")
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory
{
    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());


    @Autowired
    private ProcessToolRegistry registry;
    private static int counter = 0;

    private int ver = 0;



    public ProcessToolContextFactoryImpl()
    {
        initJbpmConfiguration();
        ver = ++counter;
    }

    @Override
    public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) {
        return withProcessToolContext(callback);
    }

    @Override
    public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) {
        return withProcessToolContext(callback,ExecutionType.TRANSACTION);
    }

    @Override
    public <T> T withProcessToolContextManualTransaction(ReturningProcessToolContextCallback<T> callback)
    {
        T result = null;

        Session session = registry.getDataRegistry().getSessionFactory().openSession();
        try {

            try {
                ProcessToolContext ctx = new ProcessToolContextImpl(session);
                ProcessToolContext.Util.setThreadProcessToolContext(ctx);

                result = callback.processWithContext(ctx);

                ProcessToolContext.Util.removeThreadProcessToolContext();
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }

        } finally {
            session.clear();
            session.close();
        }
        return result;
    }

    @Override
    public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback, ExecutionType type) {
        logger.info(">>>>>>>>> withProcessToolContext, executionType: " + type.toString() + ", threadId: " +  Thread.currentThread().getId());
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

            if (ExecutionType.NO_TRANSACTION.equals(type)) {
                return executeWithProcessToolContext(callback);
            } else if (ExecutionType.NO_TRANSACTION_SYNCH.equals(type)) {
                return executeWithProcessToolContextSynch(callback);
            } else if (ExecutionType.TRANSACTION_SYNCH.equals(type)) {
                //jbpm doesn't support external user transactions
                //if (registry.isJta()) {
                //	return executeWithProcessToolContextJtaSynch(callback);
                //} else {
                return executeWithProcessToolContextNonJtaSynch(callback);
                //}
            } else {
                //jbpm doesn't support external user transactions
                //if (registry.isJta()) {
                //	return executeWithProcessToolContextJta(callback);
                //} else {
                return executeWithProcessToolContextNonJta(callback);
                //}
            }

        }
        finally {
            logger.info("<<<<<<<<< withProcessToolContext: " +  Thread.currentThread().getId());
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private synchronized <T> T executeWithProcessToolContextNonJtaSynch(ReturningProcessToolContextCallback<T> callback) {
        return executeWithProcessToolContextNonJta(callback);
    }

    private <T> T executeWithProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback) {
        return executeWithProcessToolContextNonJta(callback, true);
    }

    private <T> T executeWithProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback, boolean reload)
    {
        T result = null;

        Session session = registry.getDataRegistry().getSessionFactory().openSession();
        try
        {
            Transaction tx = session.beginTransaction();
            ProcessToolContext ctx = new ProcessToolContextImpl(session);
            ProcessToolContext.Util.setThreadProcessToolContext(ctx);
            try
            {
                result = callback.processWithContext(ctx);

                try {
                    //throw new RuntimeException();
                    tx.commit();
                } catch (Exception ex) {
                    /* Hardcore fix //TODO change */
                    logger.severe("Ksession problem, retry: "+reload);
                    if (reload) {
                        reloadJbpm();
                        executeWithProcessToolContextNonJta(callback,false);
                    }
                }
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

        }
        finally
        {
            if (session.isOpen()) session.close();
        }
        return result;
    }

    private <T> T executeWithProcessToolContextJta(final ReturningProcessToolContextCallback<T> callback, boolean reload) {
        T result = null;
        UserTransaction ut = null;
        try {
            ut = getUserTransaction();
            logger.fine("ut.getStatus() = " + ut.getStatus());

            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ut.rollback();
            }
            if (ut.getStatus() != Status.STATUS_ACTIVE) {
                ut.begin();
            }

            Session session = registry.getDataRegistry().getSessionFactory().getCurrentSession();

            try {
                ProcessToolContext ctx = new ProcessToolContextImpl(session);
                ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                try
                {
                    result = callback.processWithContext(ctx);

                    try {
                        //throw new RuntimeException();
                        ut.commit();
                    } catch (Exception ex) {
                    /* Hardcore fix //TODO change */
                        logger.fine("Ksession problem, retry: "+reload);
                        if (reload) {
                            reloadJbpm();
                            executeWithProcessToolContextNonJta(callback,false);
                        }
                    }
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
            //if (ut.getStatus() == Status.STATUS_ACTIVE) 
            ut.commit();
        } catch (Exception e) {
            if (ut!=null) {
                try {
                    ut.rollback();
                } catch (IllegalStateException e1) {
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    e1.printStackTrace();
                } catch (SystemException e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        }
        return result;
    }

    private synchronized <T> T executeWithProcessToolContextSynch(ReturningProcessToolContextCallback<T> callback) {
        return executeWithProcessToolContext(callback);
    }

    private <T> T executeWithProcessToolContext(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

        Session session = registry.getDataRegistry().getSessionFactory().openSession();
        session.setDefaultReadOnly(true);
        try
        {
            ProcessToolContext ctx = new ProcessToolContextImpl(session);
            ProcessToolContext.Util.setThreadProcessToolContext(ctx);
            try {
                result = callback.processWithContext(ctx);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                ctx.close();
                ProcessToolContext.Util.removeThreadProcessToolContext();
            }
        } finally  {
            if (session.isOpen()) session.close();
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
    private void reloadJbpm() {
        JbpmService.getInstance().reloadSession();
    }
}
