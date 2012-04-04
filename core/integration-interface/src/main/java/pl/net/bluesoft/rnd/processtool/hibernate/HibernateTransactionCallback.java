package pl.net.bluesoft.rnd.processtool.hibernate;

import javax.transaction.Status;
import javax.transaction.Synchronization;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class HibernateTransactionCallback implements Synchronization {
    public abstract void onCommit();
    public abstract void onRollback();

    @Override
    public void beforeCompletion() {
    }
	
    @Override
    public void afterCompletion(int status) {
        switch (status) {
            case Status.STATUS_COMMITTED: onCommit(); break;
            case Status.STATUS_ROLLEDBACK: onRollback(); break;
        }
    }
}
