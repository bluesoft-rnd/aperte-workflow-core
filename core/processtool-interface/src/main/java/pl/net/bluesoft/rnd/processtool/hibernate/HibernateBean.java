package pl.net.bluesoft.rnd.processtool.hibernate;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import java.util.Collection;
import java.util.List;

public interface HibernateBean<T> {
    Class<T> getEntityType();

    DetachedCriteria getDetachedCriteria();

    T findUnique(Criterion... queries);

    List<T> findAll();

    List<T> findAll(Order order);

    List<T> findByCriteria(DetachedCriteria criteria);

    void saveOrUpdate(Collection<T> objects);

    void saveOrUpdate(T object);

    void delete(Collection<T> objects);

    void delete(T object);
}
