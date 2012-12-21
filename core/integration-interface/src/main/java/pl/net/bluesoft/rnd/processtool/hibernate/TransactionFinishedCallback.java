package pl.net.bluesoft.rnd.processtool.hibernate;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class TransactionFinishedCallback extends HibernateTransactionCallback {
    public abstract void onFinished();

    @Override
    public void onCommit() {
        onFinished();
    }

    @Override
    public void onRollback() {
        onFinished();
    }
}
