package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.exception.AquireOperationLockException;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;

/**
 *
 * Facade for operation locking in enterprise env.
 *
 * @author: mpawlak@bluesoft.net.pl
 */
public interface ILockFacade
{
    <T> T performWithLock(ProcessToolContext ctx, OperationWithLock<T> operation, OperationOptions options);

    /** Aquire lock, throw exception if lock exists. Set lockMaxMinutes as lock expiration time */
    OperationLock acquireLock(OperationOptions options) throws AquireOperationLockException;

    /** Release given lock */
    void releaseLock(OperationLock operationLock);

    /** Check if lock with given name exists, return null otherwise */
    OperationLock checkLock(String operationName);
}
