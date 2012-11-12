package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import org.apache.commons.beanutils.ConvertUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;

public class PropertyAutoWiring {
    private static final Logger logger = Logger.getLogger(PropertyAutoWiring.class.getName());

    public static void autowire(Object obj, Map<String, String> attributes) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        Class clazz = obj.getClass();
        for (Field f : Classes.getDeclaredFields(clazz)) {
            String autoName = null;
            AutoWiredProperty awp = f.getAnnotation(AutoWiredProperty.class);
            if (awp != null) {
                autoName = AutoWiredProperty.DEFAULT.equals(awp.name()) ? f.getName() : awp.name();
            }
            String v = nvl(attributes.get(autoName), ctx.getAutowiredProperty("autowire." + autoName));
            if (autoName != null && v != null) {
                try {
                	logger.finer("Setting class " + clazz.getSimpleName() + " attribute " + autoName + " to " + v);

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
