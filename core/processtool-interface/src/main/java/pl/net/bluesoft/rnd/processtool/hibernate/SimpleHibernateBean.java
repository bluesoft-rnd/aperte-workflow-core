package pl.net.bluesoft.rnd.processtool.hibernate;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class SimpleHibernateBean<T> implements HibernateBean<T> {
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected Session session;
    protected Class<T> entityType;

    public SimpleHibernateBean() {
        this(ProcessToolContext.Util.getProcessToolContextFromThread().getHibernateSession());
    }

	public SimpleHibernateBean(Session session) {
		this.session = session;
        this.entityType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public DetachedCriteria getDetachedCriteria() {
        return DetachedCriteria.forClass(entityType);
    }

    @Override
    public T findUnique(Criterion... queries) {
        DetachedCriteria criteria = getDetachedCriteria();
        if (queries != null) {
            for (Criterion c : queries) {
                criteria.add(c);
            }
        }
        return (T) criteria.getExecutableCriteria(getSession()).uniqueResult();
    }

    @Override
    public List<T> findAll() {
        return findAll(null);
    }

    @Override
    public List<T> findAll(Order order) {
        DetachedCriteria criteria = getDetachedCriteria();
        if (order != null) {
            criteria.addOrder(order);
        }
        return findByCriteria(criteria);
    }

    @Override
    public List<T> findByCriteria(DetachedCriteria criteria) {
        return criteria.getExecutableCriteria(getSession()).list();
    }

    @Override
    public void saveOrUpdate(Collection<T> objects) {
        getSession().saveOrUpdate(objects);
    }

    @Override
    public void saveOrUpdate(T object) {
        getSession().saveOrUpdate(object);
    }

    @Override
    public void delete(Collection<T> objects) {
        getSession().delete(objects);
    }

    @Override
    public void delete(T object) {
        getSession().delete(object);
    }
}
