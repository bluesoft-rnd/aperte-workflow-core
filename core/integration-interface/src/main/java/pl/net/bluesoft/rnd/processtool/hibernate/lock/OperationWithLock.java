package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * @author: Maciej
 */
public interface OperationWithLock<T>
{
    T action();
}
