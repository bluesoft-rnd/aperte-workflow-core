package pl.net.bluesoft.rnd.processtool.hibernate.transform;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class PropertyPathBasedInstantiationPolicy extends DefaultPropertyInstantiationPolicy {
    private final Map<String, Class> pathToClassMap = new HashMap<String, Class>();

    public PropertyPathBasedInstantiationPolicy() {
    }

    public PropertyPathBasedInstantiationPolicy(Map<String, Class> pathMappings) {
        this.pathToClassMap.putAll(pathMappings);
    }

    public PropertyPathBasedInstantiationPolicy addPath(String path, Class clazz) {
        pathToClassMap.put(path, clazz);
        return this;
    }

    @Override
    public Object instantiate(String propertyPath, Class parentClass, Class instantiatedClass) {
        Class clazz = pathToClassMap.get(propertyPath);
        if (clazz == null) {
            clazz = instantiatedClass;
        }
        return super.instantiate(propertyPath, parentClass, clazz);
    }
}
