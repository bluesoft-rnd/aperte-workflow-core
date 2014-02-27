package pl.net.bluesoft.rnd.processtool.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author mpawlak@bluesoft.net.pl
 */
public class OperationLockDAOImpl extends SimpleHibernateBean<OperationLock> implements OperationLockDAO {
    public OperationLockDAOImpl(Session hibernateSession) {
        super(hibernateSession);
    }

    @Override
    public void createLock(OperationLock lock)
    {

        Session session = getSession();
        session.save(lock);
        session.flush();

    }

    @Override
    public OperationLock getLock(String operationName)
    {
        return (OperationLock) getSession().createCriteria(OperationLock.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(eq("lockName", operationName))
                .uniqueResult();
    }

    @Override
    public void removeLock(OperationLock operationLock)
    {

        /* Check if lock still exists */
        OperationLock lock = (OperationLock)getSession().get(OperationLock.class, operationLock.getId());
        if(lock == null)
            return;

        getSession().delete(lock);
        getSession().flush();
    }
}
