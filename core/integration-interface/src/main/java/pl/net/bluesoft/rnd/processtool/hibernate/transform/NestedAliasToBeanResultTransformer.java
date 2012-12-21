package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;
import org.hibernate.transform.ResultTransformer;
import pl.net.bluesoft.util.lang.Lang;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class NestedAliasToBeanResultTransformer implements ResultTransformer, Serializable {
    private final Class resultClass;
    private boolean isInitialized;
    private String[] aliases;
    private Setter[] setters;

    private PropertyInstantiationPolicy instantiationPolicy;

    public NestedAliasToBeanResultTransformer(Class resultClass) {
        if (resultClass == null) {
            throw new IllegalArgumentException("resultClass cannot be null");
        }
        isInitialized = false;
        this.resultClass = resultClass;
    }

    public NestedAliasToBeanResultTransformer setInstantiationPolicy(PropertyInstantiationPolicy instantiationPolicy) {
        if (isInitialized) {
            throw new IllegalStateException("Cannot set instantiation policy after initialization");
        }
        this.instantiationPolicy = instantiationPolicy;
        return this;
    }

    public Object transformTuple(Object[] tuple, String[] aliases) {
        if (!isInitialized) {
            initialize(aliases);
        }
        else {
            check(aliases);
        }

        Object result = instantiationPolicy.instantiate(null, null, resultClass);
        for (int i = 0; i < aliases.length; ++i) {
            if (setters[i] != null) {
                setters[i].set(result, tuple[i], null);
            }
        }
        return result;
    }

    private void initialize(String[] aliases) {
        if (instantiationPolicy == null) {
            instantiationPolicy = new DefaultPropertyInstantiationPolicy();
        }
        PropertyAccessor propertyAccessor = new NestedPropertyAccessor(resultClass, instantiationPolicy);
        this.aliases = new String[aliases.length];
        setters = new Setter[aliases.length];
        for (int i = 0; i < aliases.length; ++i) {
            String alias = aliases[i];
            if (alias != null) {
                this.aliases[i] = alias;
                setters[i] = propertyAccessor.getSetter(resultClass, alias);
            }
        }
        isInitialized = true;
    }

    private void check(String[] aliases) {
        if (!Arrays.equals(aliases, this.aliases)) {
            throw new IllegalStateException("aliases are different from what is cached; aliases=" + Arrays.asList(aliases)
                    + " cached=" + Arrays.asList(this.aliases));
        }
    }

    public List transformList(List collection) {
        return collection;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NestedAliasToBeanResultTransformer that = (NestedAliasToBeanResultTransformer) o;

        if (!resultClass.equals(that.resultClass)) {
            return false;
        }
        if (!Arrays.equals(aliases, that.aliases)) {
            return false;
        }
        if (!Lang.equals(instantiationPolicy, that.instantiationPolicy)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultClass != null ? resultClass.hashCode() : 0;
        result = 31 * result + (aliases != null ? Arrays.hashCode(aliases) : 0);
        result = 31 * result + (instantiationPolicy != null ? instantiationPolicy.hashCode() : 0);
        return result;
    }
}
