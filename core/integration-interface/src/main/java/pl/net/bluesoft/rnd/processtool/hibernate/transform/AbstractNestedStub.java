package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;

import java.util.List;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class AbstractNestedStub {
    protected static final Resolver resolver = new DefaultResolver();

    protected final NestedPropertyAccessor parent;
    protected final String nestedPropertyName;

    protected AbstractNestedStub(NestedPropertyAccessor parent, String nestedPropertyName) {
        this.parent = parent;
        this.nestedPropertyName = nestedPropertyName;
    }

    protected Setter addSetter(List<Setter> setterChain, Getter prevGetter, String nextPart) {
        Class nextClass = prevGetter != null ? prevGetter.getReturnType() : parent.getBaseClass();
        Setter nextSetter = parent.getPropertySetter(nextClass, nextPart);
        setterChain.add(nextSetter);
        return nextSetter;
    }

    protected Getter addGetter(List<Getter> getterChain, Getter prevGetter, String nextPart) {
        Class nextClass = prevGetter != null ? prevGetter.getReturnType() : parent.getBaseClass();
        Getter nextGetter = parent.getPropertyGetter(nextClass, nextPart);
        getterChain.add(nextGetter);
        return nextGetter;
    }

    protected String getPropertyPath(int index) {
        int occurrences = 0;
        int end = 0;
        for (; end < nestedPropertyName.length() && occurrences != index; ++end) {
            char c = nestedPropertyName.charAt(end);
            if (c == '.') {
                ++occurrences;
            }
        }
        return nestedPropertyName.substring(0, end == 0 ? nestedPropertyName.length() : end);
    }
}
