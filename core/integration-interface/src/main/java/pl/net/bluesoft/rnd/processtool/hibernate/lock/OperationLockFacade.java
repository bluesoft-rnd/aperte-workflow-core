package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.OperationLockDAO;
import pl.net.bluesoft.rnd.processtool.dao.OperationLockDAOImpl;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.exception.AquireOperationLockException;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.DataRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import javax.persistence.UniqueConstraint;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class OperationLockFacade implements ILockFacade
{
    private static final Logger logger = Logger.getLogger(OperationLockFacade.class.getName());

    @Autowired
    private DataRegistry dataRegistry;

    public OperationLockFacade()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);


    }

    @Override
    public <T> T performWithLock(final OperationWithLock<T> operation, OperationOptions options)
    {
        OperationLock lock = null;

        try
        {
            TransactionAwareDataSourceProxy dataSourceProxy =  dataRegistry.getDataSourceProxy();
            Connection connection = dataSourceProxy.getConnection();
            connection.setAutoCommit(false);

            try {
                OperationLockDAO lockDAO = new OperationLockDAOImpl(connection);

                lock = acquireLock(options, lockDAO);
                connection.commit();


            }
            catch(Throwable ex)
            {
                connection.rollback();
                throw new RuntimeException("Problem during lock obtaining", ex);
            }
            finally {
                connection.close();
            }

            return operation.action();

        }
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "Problem during acquring lock", ex);
            return null;
        }
        finally
        {
            if(lock != null)
            {
                try {
                    TransactionAwareDataSourceProxy dataSourceProxy =  dataRegistry.getDataSourceProxy();
                    Connection connection = dataSourceProxy.getConnection();
                    connection.setAutoCommit(false);

                    try {
                        OperationLockDAO lockDAO = new OperationLockDAOImpl(connection);
                        lockDAO.removeLock(lock);
                        connection.commit();
                    }
                    catch(Throwable ex)
                    {
                        connection.rollback();
                    }
                    finally {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Problem during acquring lock", e);
                    return null;
                }

            }
        }
    }

    private OperationLock acquireLock(OperationOptions options, OperationLockDAO lockDAO) throws AquireOperationLockException
    {

        logger.info("Acquiring operation lock: "+options.getLockName()+"...");
        try
        {
            OperationLock existingLock = lockDAO.getLock(options.getLockName());

            /* Check if there is already a lock */
            if(existingLock != null)
            {
                if(!shouldReleaseLock(existingLock))
                    throw new AquireOperationLockException(options.getLockName() + " lock still valid");

                lockDAO.removeLock(existingLock);
            }


            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, options.getExpireAfterMinutes());

            OperationLock operationLock = new OperationLock();
            operationLock.setLockDate(new Date());
            operationLock.setLockMode(options.getMode());
            operationLock.setLockName(options.getLockName());
            operationLock.setLockReleaseDate(cal.getTime());


            lockDAO.createLock(operationLock);

            logger.info("Lock "+options.getLockName()+" acquired");

            return operationLock;
        }
        catch(AquireOperationLockException ex)
        {
            logger.info("Skipping action, operation lock ["+options.getLockName()+"] still valid...");

            throw new AquireOperationLockException(ex);
        }
        catch(Throwable ex)
        {
            logger.log(Level.SEVERE, "Problem with locks", ex);

            throw new AquireOperationLockException(ex);
        }
    }

    private boolean shouldReleaseLock(OperationLock existingLock)
    {

        if((new Date()).after(existingLock.getLockReleaseDate()))
            return true;
        else
            return false;
    }


}
