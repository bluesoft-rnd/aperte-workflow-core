package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.exceptions.ExceptionsUtils;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.concurrent.locks.LockSupport;
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
        session.setFlushMode(FlushMode.COMMIT);
        session.setCacheMode(CacheMode.IGNORE);
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
            session.close();
        }
        return result;
    }

    @Override
    public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback, ExecutionType type) {
        //logger.info(">>>>>>>>> withProcessToolContext, executionType: " + type.toString() + ", threadId: " + Thread.currentThread().getId());
        long start = System.currentTimeMillis();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ProcessToolRegistry.Util.getAwfClassLoader());

        try {
            ProcessToolRegistry.Util.getAwfClassLoader().loadClass(JbpmStepAction.class.getName());
        } catch (ClassNotFoundException e) {
            logger.warning("JbpmStepAction.class was not found");
        }

        ContextStats stats = null;

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

            stats = new ContextStats();

            if (ExecutionType.NO_TRANSACTION.equals(type)) {
                return executeWithProcessToolContext(callback, stats);
            } else if (ExecutionType.NO_TRANSACTION_SYNCH.equals(type)) {
                return executeWithProcessToolContextSynch(callback, stats);
            } else if (ExecutionType.TRANSACTION_SYNCH.equals(type)) {
                //jbpm doesn't support external user transactions
                //if (registry.isJta()) {
                //	return executeWithProcessToolContextJtaSynch(callback);
                //} else {
                return executeWithProcessToolContextNonJtaSynch(callback, stats);
                //}
            } else {
                //jbpm doesn't support external user transactions
                //if (registry.isJta()) {
                //	return executeWithProcessToolContextJta(callback);
                //} else {
                return executeWithProcessToolContextNonJta(callback, stats);
                //}
            }

        }
        finally {
            //logger.info("<<<<<<<<< withProcessToolContext: " +  Thread.currentThread().getId() + " time: " + (System.currentTimeMillis() - start) + (stats != null ? " stats:\n" + stats.toString() : ""));
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private synchronized <T> T executeWithProcessToolContextNonJtaSynch(ReturningProcessToolContextCallback<T> callback, ContextStats stats) {
        return executeWithProcessToolContextNonJta(callback, stats);
    }

    private <T> T executeWithProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback, ContextStats stats) {
        return executeWithProcessToolContextNonJta(callback, true, stats);
    }

    private <T> T executeWithProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback, boolean reload, ContextStats stats)
    {
        T result = null;

        stats.beforeOpenSession();
        Session session = registry.getDataRegistry().getSessionFactory().openSession();
        stats.afterOpenSession();

        ProcessToolContext ctx = new ProcessToolContextImpl(session);
        ProcessToolContext.Util.setThreadProcessToolContext(ctx);

        UserTransaction ut = null;
        try {
            stats.beforeBeginTransaction();
            ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
            ut.begin();
            stats.afterBeginTransaction();

            result = callback.processWithContext(ctx);

            if(ut.getStatus() != Status.STATUS_COMMITTED) {
                stats.beforeCommit();
                ut.commit();
                stats.afterCommit();
            }
        }
        catch (Throwable ex)
        {
            logger.log(Level.SEVERE, "Problem during context executing", ex);
            try {
                if(ut.getStatus() != Status.STATUS_ROLLEDBACK) {
                    stats.beforeRollback();
                    ut.rollback();
                    stats.afterRollback();
                }

            }
            catch (Exception e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
            }

            throw new RuntimeException(ex);
        }
        finally
        {
            LockSupport.unpark(Thread.currentThread());

            JbpmService.getInstance().destroy();
            if (session.isOpen()) {
                stats.beforeOpenSession();
                session.close();
                stats.afterOpenSession();
            }

            ctx.close();
            ProcessToolContext.Util.removeThreadProcessToolContext();
        }

        return result;
    }




    private synchronized <T> T executeWithProcessToolContextSynch(ReturningProcessToolContextCallback<T> callback, ContextStats stats) {
        return executeWithProcessToolContext(callback, stats);
    }

    private <T> T executeWithProcessToolContext(ReturningProcessToolContextCallback<T> callback, ContextStats stats) {
        T result = null;

        stats.beforeOpenSession();
        Session session = registry.getDataRegistry().getSessionFactory().openSession();
        session.setDefaultReadOnly(true);
        session.setFlushMode(FlushMode.MANUAL);
        stats.afterOpenSession();

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
            if (session.isOpen()) {
                stats.beforeCloseSession();
                session.close();
                stats.afterCloseSession();
            }
        }
        return result;
    }

    private static class ContextStats {
        private static class Stat {
            private final String name;
            private long start;
            private int count;
            private long min = Long.MAX_VALUE, max = Long.MIN_VALUE, total;

            private Stat(String name) {
                this.name = name;
            }

            public void start() {
                start = System.currentTimeMillis();
            }

            public void end() {
                long t = System.currentTimeMillis() - start;
                ++count;
                min = Math.min(min, t);
                max = Math.max(max, t);
                total += t;
            }

            @Override
            public String toString() {
                return count > 0 ? name + " = {" +
                        "count=" + count +
                        ", min=" + min +
                        ", max=" + max +
                        ", avg=" + total/count +
                        '}' : "";
            }
        }

        private Stat openSession = new Stat("openSession");
        private Stat closeSession = new Stat("closeSession");
        private Stat beginTransaction = new Stat("beginTransaction");
        private Stat commit = new Stat("commit");
        private Stat rollback = new Stat("rollback");
        private Stat reloadJbpm = new Stat("reloadJbpm");

        public void beforeOpenSession() {
            openSession.start();
        }

        public void afterOpenSession() {
            openSession.end();
        }

        public void beforeBeginTransaction() {
            beginTransaction.start();
        }

        public void afterBeginTransaction() {
            beginTransaction.end();
        }

        public void beforeCommit() {
            commit.start();
        }

        public void afterCommit() {
            commit.end();
        }

        public void beforeRollback() {
            rollback.start();
        }

        public void afterRollback() {
            rollback.end();
        }

        public void beforeCloseSession() {
            closeSession.start();
        }

        public void afterCloseSession() {
            closeSession.end();
        }

        public void beforeReloadJbpm() {
            reloadJbpm.start();
        }

        public void afterReloadJbpm() {
            reloadJbpm.end();
        }

        @Override
        public String toString() {
            String s1 = openSession.toString();
            String s2 = closeSession.toString();
            String s3 = beginTransaction.toString();
            String s4 = commit.toString();
            String s5 = rollback.toString();
            String s6 = reloadJbpm.toString();

            StringBuilder sb = new StringBuilder(128);
            if (!s1.isEmpty()) {
                sb.append(s1).append('\n');
            }
            if (!s2.isEmpty()) {
                sb.append(s2).append('\n');
            }
            if (!s3.isEmpty()) {
                sb.append(s3).append('\n');
            }
            if (!s4.isEmpty()) {
                sb.append(s4).append('\n');
            }
            if (!s5.isEmpty()) {
                sb.append(s5).append('\n');
            }
            if (!s6.isEmpty()) {
                sb.append(s6).append('\n');
            }
            return sb.toString();
        }
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
        JbpmService.getInstance().destroy();
    }
}
