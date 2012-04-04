package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.util.lang.Lang;
import pl.net.bluesoft.util.lang.Predicate;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public abstract class UserAttributePredicates implements Serializable {
    public static Predicate<UserAttribute> matchEntity(final UserAttribute a) {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return Lang.equals(a, input) || (a != null && input != null && Lang.equals(a.getId(), input.getId()));
            }
        };
    }

    public static Predicate<UserAttribute> matchParent(final UserAttribute a) {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return matchEntity(a).apply(input.getParent());
            }
        };
    }

    public static Predicate<UserAttribute> matchKey(final String key) {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return Lang.equals(input.getKey(), key);
            }
        };
    }

    public static Predicate<UserAttribute> matchValue(final String value) {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return Lang.equals(input.getValue(), value);
            }
        };
    }

    public static Predicate<UserAttribute> matchAll() {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return true;
            }
        };
    }

    public static Predicate<UserAttribute> matchKeyValue(final String key, final String value) {
        return new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return Lang.equals(input.getKey(), key) && Lang.equals(input.getValue(), value);
            }
        };
    }

    public static Predicate<UserAttribute> matchAttribute(final UserAttribute a) {
        return new Predicate<UserAttribute>() {
            Predicate<UserAttribute> predicate = matchKeyValue(a.getKey(), a.getValue());

            @Override
            public boolean apply(UserAttribute b) {
                if (!predicate.apply(b)) {
                    return false;
                }
                Map<String, UserAttribute> aMap = a.getAttributesMap(), bMap = b.getAttributesMap();
                Set<String> aKeys = aMap.keySet(), bKeys = bMap.keySet();
                if (!aKeys.containsAll(bKeys) || !bKeys.containsAll(aKeys)) {
                    return false;
                }
                for (String key : aKeys) {
                    if (!matchAttribute(aMap.get(key)).apply(bMap.get(key))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
