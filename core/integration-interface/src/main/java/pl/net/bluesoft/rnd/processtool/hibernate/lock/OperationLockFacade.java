package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.OperationLockDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.exception.AquireOperationLockException;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import javax.persistence.UniqueConstraint;
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

    private OperationLockDAO lockDAO;

    public OperationLockFacade(OperationLockDAO lockDAO)
    {
        this.lockDAO = lockDAO;
    }

    @Override
    public <T> T performWithLock(ProcessToolContext ctx, OperationWithLock<T> operation, OperationOptions options)
    {
        OperationLock lock = null;

        Session session =  lockDAO.getSession();

        try
        {
            Transaction transaction = session.beginTransaction();
            lock =  acquireLock(options);
            transaction.commit();

            return operation.action(ctx);

        }
        catch(AquireOperationLockException ex)
        {
            return null;
        }
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "Problem during acquring lock for Teta Sync", ex);
            return null;
        }
        finally
        {
            if(lock != null)
            {
                Transaction transaction = session.beginTransaction();
                releaseLock(lock);
                transaction.commit();
            }
        }
    }

    @Override
    public OperationLock acquireLock(OperationOptions options) throws AquireOperationLockException
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
        catch(RuntimeException ex)
        {
            logger.info("Skipping action, operation lock ["+options.getLockName()+"] still valid...");

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

    @Override
    public void releaseLock(OperationLock operationLock)
    {
        lockDAO.removeLock(operationLock);
    }

    @Override
    public OperationLock checkLock(String operationName)
    {
        return lockDAO.getLock(operationName);
    }
}
