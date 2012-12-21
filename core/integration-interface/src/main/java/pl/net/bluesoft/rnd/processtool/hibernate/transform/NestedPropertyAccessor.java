package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class NestedPropertyAccessor implements PropertyAccessor {
    private final Class baseClass;
    private final PropertyInstantiationPolicy instantiationPolicy;
    private final Map<Class, PropertyAccessor> propertyAccessors = new HashMap<Class, PropertyAccessor>();
    private final Map<AccessorCacheKey, Object> accessorCache = new HashMap<AccessorCacheKey, Object>();

    public NestedPropertyAccessor(Class resultClass, PropertyInstantiationPolicy instantiationPolicy) {
        if (instantiationPolicy == null || resultClass == null) {
            throw new IllegalArgumentException("Result class and instantiation policy should be non-null");
        }
        this.baseClass = resultClass;
        this.instantiationPolicy = instantiationPolicy;
        this.propertyAccessors.put(baseClass, createAccessor(baseClass));
    }

    private PropertyAccessor createAccessor(Class theClass) {
        return new ChainedPropertyAccessor(
                new PropertyAccessor[] {
                        PropertyAccessorFactory.getPropertyAccessor(theClass, null),
                        PropertyAccessorFactory.getPropertyAccessor("field"),
                }
        );
    }

    public PropertyAccessor getAccessor(Class theClass) {
        PropertyAccessor accessor = propertyAccessors.get(theClass);
        if (accessor == null) {
            propertyAccessors.put(theClass, accessor = createAccessor(theClass));
        }
        return accessor;
    }

    public Getter getPropertyGetter(Class clazz, String propertyName) {
        AccessorCacheKey key = new AccessorCacheKey(clazz, propertyName, true);
        Getter getter = (Getter) accessorCache.get(key);
        if (getter == null) {
            getter = getAccessor(clazz).getGetter(clazz, propertyName);
            accessorCache.put(key, getter);
        }
        return getter;
    }

    public Setter getPropertySetter(Class clazz, String propertyName) {
        AccessorCacheKey key = new AccessorCacheKey(clazz, propertyName, false);
        Setter setter = (Setter) accessorCache.get(key);
        if (setter == null) {
            setter = getAccessor(clazz).getSetter(clazz, propertyName);
            accessorCache.put(key, setter);
        }
        return setter;
    }

    public Class getBaseClass() {
        return baseClass;
    }

    public PropertyInstantiationPolicy getInstantiationPolicy() {
        return instantiationPolicy;
    }

    private class AccessorCacheKey {
        private Class clazz;
        private String propertyName;
        private boolean getter;

        private AccessorCacheKey(Class clazz, String propertyName, boolean getter) {
            this.clazz = clazz;
            this.propertyName = propertyName;
            this.getter = getter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AccessorCacheKey)) {
                return false;
            }

            AccessorCacheKey that = (AccessorCacheKey) o;

            if (getter != that.getter) {
                return false;
            }
            if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) {
                return false;
            }
            if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = clazz != null ? clazz.hashCode() : 0;
            result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
            result = 31 * result + (getter ? 1 : 0);
            return result;
        }
    }

    @Override
    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new NestedGetter(this, propertyName);
    }

    @Override
    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new NestedSetter(this, propertyName);
    }


}
