package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class NestedGetter extends AbstractNestedStub implements Getter {
    private final transient Method method;

    private Class<?> clazz;
    private List<Getter> getterChain = new ArrayList<Getter>();

    public NestedGetter(NestedPropertyAccessor parent, String nestedPropertyName) {
        super(parent, nestedPropertyName);
        Getter prevGetter = null;
        String propertyName = nestedPropertyName;
        do {
            String nextPart = resolver.next(propertyName);
            prevGetter = addGetter(getterChain, prevGetter, nextPart);
            propertyName = resolver.remove(propertyName);
        }
        while (resolver.hasNested(propertyName));
        this.method = prevGetter.getMethod();
        this.clazz = method.getDeclaringClass();
    }

    @Override
    public Object get(Object owner) throws HibernateException {
        Object result = null;
        Object base = owner;
        for (Getter getter : getterChain) {
            result = getter.get(base);
            if (result == null) {
                break;
            }
            base = result;
        }
        return result;
    }

    @Override
    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
        return get(owner);
    }

    @Override
    public Member getMember() {
        return method;
    }

    @Override
    public Class getReturnType() {
        return method.getReturnType();
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
        return "NestedGetter{" +
                "nestedPropertyName='" + nestedPropertyName + '\'' +
                ", clazz=" + clazz.getName() +
                ", method=" + method.getName() +
                '}';
    }
}
