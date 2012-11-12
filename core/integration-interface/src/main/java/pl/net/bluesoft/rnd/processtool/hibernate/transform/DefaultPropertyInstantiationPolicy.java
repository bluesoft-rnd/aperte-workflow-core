package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.hibernate.HibernateException;

import java.io.Serializable;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class DefaultPropertyInstantiationPolicy implements PropertyInstantiationPolicy, Serializable {
    @Override
    public boolean forceInstantiation(String propertyPath, Class parentClass, Class instantiatedClass) {
        return false;
    }

    @Override
    public Object instantiate(String propertyPath, Class parentClass, Class instantiatedClass) {
        try {
            return instantiatedClass.newInstance();
        }
        catch (InstantiationException e) {
            throw new HibernateException("Could not instantiate result class: " + instantiatedClass.getName());
        }
        catch (IllegalAccessException e) {
            throw new HibernateException("Could not instantiate result class: " + instantiatedClass.getName());
        }
    }
}
