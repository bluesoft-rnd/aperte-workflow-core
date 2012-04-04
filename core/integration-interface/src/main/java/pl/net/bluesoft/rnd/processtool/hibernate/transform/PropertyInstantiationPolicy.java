package pl.net.bluesoft.rnd.processtool.hibernate.transform;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public interface PropertyInstantiationPolicy {
    boolean forceInstantiation(String propertyPath, Class parentClass, Class instantiatedClass);
    Object instantiate(String propertyPath, Class parentClass, Class instantiatedClass);
}
