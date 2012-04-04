package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class NestedSetter extends AbstractNestedStub implements Setter {
    private final transient Method method;
    private final PropertyInstantiationPolicy instantiationPolicy;

    private Class<?> clazz;
    private List<Getter> getterChain = new ArrayList<Getter>();

    private List<Setter> setterChain = new ArrayList<Setter>();

    public NestedSetter(NestedPropertyAccessor parent, String nestedPropertyName) {
        super(parent, nestedPropertyName);
        Getter prevGetter = null;
        String propertyName = nestedPropertyName;
        while (resolver.hasNested(propertyName)) {
            String nextPart = resolver.next(propertyName);
            addSetter(setterChain, prevGetter, nextPart);
            prevGetter = addGetter(getterChain, prevGetter, nextPart);
            propertyName = resolver.remove(propertyName);
        }
        addSetter(setterChain, prevGetter, propertyName);
        this.method = getFinalSetter().getMethod();
        this.clazz = method.getDeclaringClass();
        this.instantiationPolicy = parent.getInstantiationPolicy();
    }

    private Setter getFinalSetter() {
        return setterChain.get(setterChain.size() - 1);
    }

    @Override
    public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
        Object prevTarget = target;
        Object subTarget = target;
        for (int i = 0; i < getterChain.size(); ++i) {
            Getter getter = getterChain.get(i);
            subTarget = getter.get(prevTarget);
            if (subTarget == null) {
                String propertyPath = getPropertyPath(i + 1);
                Class parentClass = prevTarget.getClass();
                Class instantiatedClass = getter.getReturnType();
                if (value != null || instantiationPolicy.forceInstantiation(propertyPath, parentClass, instantiatedClass)) {
                    subTarget = instantiationPolicy.instantiate(propertyPath, parentClass, instantiatedClass);
                }
                if (subTarget == null) {
                    return;
                }
                Setter setter = setterChain.get(i);
                setter.set(prevTarget, subTarget, factory);
            }
            prevTarget = subTarget;
        }
        getFinalSetter().set(subTarget, value, factory);
    }

    @Override
    public String getMethodName() {
        return method.getName();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "NestedSetter{" +
                "nestedPropertyName='" + nestedPropertyName + '\'' +
                ", clazz=" + clazz.getName() +
                ", method=" + method.getName() +
                '}';
    }
}
