package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import org.apache.commons.beanutils.ConvertUtils;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.util.StepUtil;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyAutoWiring {
    private static final Logger logger = Logger.getLogger(PropertyAutoWiring.class.getName());

	public static void autowire(Object obj, Map<String, String> attributes) {
		autowire(obj, attributes, null);
	}

    public static void autowire(Object obj, Map<String, String> attributes, IAttributesProvider pi) {
        Class clazz = obj.getClass();

        for (Field f : Classes.getDeclaredFields(clazz)) {
        	AutoWiredProperty awp = f.getAnnotation(AutoWiredProperty.class);

			if (awp != null) {
				String autoName = AutoWiredProperty.DEFAULT.equals(awp.name()) ? f.getName() : awp.name();

				if (autoName != null) {
					String v = attributes.get(autoName);

					if (v != null) {
						try {
							if (awp.substitute() && pi != null) {
								v = StepUtil.substituteVariables(v, pi);
							}

							if (logger.isLoggable(Level.FINER)) {
								logger.finer("Setting class " + clazz.getSimpleName() + " attribute " + autoName + " to " + v);
							}

							Object value = ConvertUtils.convert(v, f.getType());
							Classes.setFieldValue(obj, f, value);
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
					}
				}
			}
        }
    }
}
