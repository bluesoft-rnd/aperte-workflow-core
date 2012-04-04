package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Transformer;

import javax.persistence.MappedSuperclass;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: amichalak@bluesoft.net.pl
 */
@MappedSuperclass
public abstract class UserAttributesSupport extends PersistentEntity {
    public abstract UserData getUser();
    public abstract Set<UserAttribute> getAttributes();

    public Set<String> getAttributeKeys() {
        return Collections.collect(getAttributes(), new Transformer<UserAttribute, String>() {
            @Override
            public String transform(UserAttribute obj) {
                return obj.getKey();
            }
        }, new HashSet<String>());
    }

    public Map<String, UserAttribute> getAttributesMap() {
        return Collections.transform(getAttributes(), new Transformer<UserAttribute, String>() {
            @Override
            public String transform(UserAttribute obj) {
                return obj.getKey();
            }
        });
    }

    protected UserAttribute getChildParent() {
        return this instanceof UserAttribute ? (UserAttribute) this : null;
    }

    protected UserAttribute modifyOrAddAttribute(UserAttribute attribute, String key, String value) {
        if (attribute == null) {
            return addInternal(new UserAttribute(key, value));
        }
        attribute.setValue(value);
        return attribute;
    }

    protected UserAttribute modifyOrAddAttribute(final UserAttribute original, final UserAttribute current) {
        if (original == null) {
            return addInternal(current);
        }
        if (UserAttributePredicates.matchAttribute(original).apply(current)) {
            return original;
        }
        removeMatched(original);
        return addInternal(current);
    }

    UserAttribute addInternal(UserAttribute a) {
        if (a.getParent() != null) {
            a.getParent().getAttributes().remove(a);
        }
        getAttributes().add(a);
        if (getUser() != null) {
            getUser().addChild(a);
        }
        a.setUser(getUser());
        a.setParent(getChildParent());
        return a;
    }

    public UserAttribute addAttribute(final String key, final String value) {
        UserAttribute a = findAttribute(key, value);
        return a != null ? a : modifyOrAddAttribute(null, key, value);
    }

    public UserAttribute setAttribute(UserAttribute attribute, String oldValue) {
        return modifyOrAddAttribute(findAttribute(attribute.getKey(), oldValue), attribute);
    }

    public UserAttribute setAttribute(UserAttribute attribute) {
        return modifyOrAddAttribute(findAttribute(attribute.getKey()), attribute);
    }

    public UserAttribute setAttribute(final String key, final String value) {
        return modifyOrAddAttribute(findAttribute(key), key, value);
    }

    public UserAttribute setAttribute(final String key, final String oldValue, final String newValue) {
        return modifyOrAddAttribute(findAttribute(key, oldValue), key, newValue);
    }

    public boolean containsAttributes(final String... keys) {
        return containsAttributes(new HashSet<String>() {{
            for (String key : keys) {
                add(key);
            }
        }});
    }

    public boolean containsAttributes(Set<String> keys) {
        return getAttributeKeys().containsAll(keys);
    }

    protected UserAttribute findAttribute(Collection<UserAttribute> collection, Predicate<UserAttribute> predicate) {
        return Collections.firstMatching(collection, predicate);
    }

    protected Collection<UserAttribute> findAttributesByPredicate(Collection<UserAttribute> collection, Predicate<UserAttribute> predicate) {
        return Collections.filter(collection, predicate);
    }

    public Collection<UserAttribute> findAttributesMatchingKey(final String key) {
        return findAttributesByPredicate(getAttributes(), UserAttributePredicates.matchKey(key));
    }

    public UserAttribute findAttribute(UserAttribute attribute) {
        return findAttribute(getAttributes(), UserAttributePredicates.matchEntity(attribute));
    }

    public UserAttribute findAttribute(final String key) {
        return findAttribute(getAttributes(), UserAttributePredicates.matchKey(key));
    }

    public UserAttribute findAttribute(final String key, final String value) {
        return findAttribute(getAttributes(), UserAttributePredicates.matchKeyValue(key, value));
    }

    public UserAttribute findAttributeRecursive(final String key, final String value) {
        UserAttribute found = findAttribute(key, value);
        if (found == null) {
            for (UserAttribute a : getAttributes()) {
                found = a.findAttributeRecursive(key, value);
                if (found != null) {
                    break;
                }
            }
        }
        return found;
    }

    public UserAttribute findAttributeRecursive(final String key) {
        UserAttribute found = findAttribute(key);
        if (found == null) {
            for (UserAttribute a : getAttributes()) {
                found = a.findAttributeRecursive(key);
                if (found != null) {
                    break;
                }
            }
        }
        return found;
    }

    public String findAttributeValue(String key) {
        UserAttribute a = findAttribute(key);
        return a != null ? a.getValue() : null;
    }

    public String findAttributeValueRecursive(String key) {
        UserAttribute a = findAttributeRecursive(key);
        return a != null ? a.getValue() : null;
    }

    public void removeAttribute(Predicate<UserAttribute> predicate) {
        Set<UserAttribute> matchedToRemove = new HashSet<UserAttribute>();
        for (UserAttribute a : getAttributes()) {
            if (predicate.apply(a)) {
                matchedToRemove.add(a);
            }
        }
        removeMatched(matchedToRemove);
    }

    private void removeMatched(Set<UserAttribute> matchedToRemove) {
        getAttributes().removeAll(matchedToRemove);
        if (getUser() != null) {
            UserData user = getUser();
            for (UserAttribute a : matchedToRemove) {
                clearAttribute(user, a);
            }
        }
    }

    void clearAttribute(UserData user, UserAttribute a) {
        a.removeAllAttributes();
        a.setParent(null);
        a.setUser(null);
        user.addOrphan(a);
    }

    private void removeMatched(final UserAttribute a) {
        removeMatched(new HashSet<UserAttribute>() {{
            add(a);
        }});
    }

    public void removeAttribute(final UserAttribute a) {
        removeAttribute(UserAttributePredicates.matchEntity(a));
    }

    public void removeAttribute(final String key, final String value) {
        removeAttribute(UserAttributePredicates.matchKeyValue(key, value));
    }

    public void removeAttributeByKey(final String key) {
        removeAttribute(UserAttributePredicates.matchKey(key));
    }

    public void removeAttributeByValue(final String value) {
        removeAttribute(UserAttributePredicates.matchValue(value));
    }

    public void removeAllAttributes() {
        removeAttribute(UserAttributePredicates.matchAll());
    }

}
