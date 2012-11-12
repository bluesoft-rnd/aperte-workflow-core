package pl.net.bluesoft.rnd.processtool.hibernate;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class SimpleHibernateBean<T> implements HibernateBean<T> {
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected Session session;
    protected Class<T> entityType;

    public SimpleHibernateBean() {
        this(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession());
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
    public T loadById(Object id) {
        return id != null ? findUnique(Restrictions.idEq(id)) : null;
    }

    @Override
    public T findUnique(Criterion... queries) {
        return findUnique(getDetachedCriteria(), queries);
    }

    protected T findUnique(DetachedCriteria criteria, Criterion... queries) {
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
        for (T object : objects) {
            saveOrUpdate(object);
    	}
    }

    @Override
    public void saveOrUpdate(T object) {
        getSession().saveOrUpdate(object);
    }

    @Override
    public void delete(Collection<T> objects) {
        for (T object : objects) {
            delete(object);
        }
    }

    @Override
    public void delete(T object) {
        getSession().delete(object);
    }

	@Override
	public T refresh(T object) {
		return object != null ? loadById(getId(object)) : null;
	}

	protected Object getId(T object) {
		if (object instanceof PersistentEntity) {
			return ((PersistentEntity)object).getId();
		}
		throw new RuntimeException("Could not determine id for " + object.getClass().getName());
	}
}
