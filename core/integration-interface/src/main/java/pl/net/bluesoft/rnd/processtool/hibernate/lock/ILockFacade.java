package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.OperationLockDAO;
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
    <T> T performWithLock(OperationWithLock<T> operation, OperationOptions options);

}
