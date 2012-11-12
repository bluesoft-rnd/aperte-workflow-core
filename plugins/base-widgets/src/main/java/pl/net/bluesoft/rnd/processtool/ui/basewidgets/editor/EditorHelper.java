package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Utility functions, used across entire package
 *
 * @author tlipski@bluesoft.net.pl
 */
class EditorHelper {
    public static Annotation getAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        Annotation res = cls.getAnnotation(annotationCls);
        if (res != null) {
            return res;
        } else if (!cls.equals(Object.class)) {
            return getAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }

    public static Annotation getFieldAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        for (Field f : cls.getDeclaredFields()) {
            Annotation annotation = f.getAnnotation(annotationCls);
            if (annotation != null) return annotation;
        }
        if (!cls.equals(Object.class)) {
            return getFieldAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }

    public static Field getFieldWithAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        for (Field f : cls.getDeclaredFields()) {
            Annotation annotation = f.getAnnotation(annotationCls);
            if (annotation != null) return f;
        }
        if (!cls.equals(Object.class)) {
            return getFieldWithAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }

    public static Field findField(Object propertyId, Class classOfItem) {
        Field declaredField;
        try {
            declaredField = classOfItem.getDeclaredField(String.valueOf(propertyId));
        } catch (NoSuchFieldException e) {
            declaredField = null;
        }
        if (declaredField == null && !Object.class.equals(classOfItem))
            return findField(propertyId, classOfItem.getSuperclass());
        return declaredField;
    }

    public static String joinValidationErrors(List<XmlValidationError> xmlValidationErrors) {
        StringBuilder msg = new StringBuilder();
        for (XmlValidationError err : xmlValidationErrors) {
            msg.append(getLocalizedMessage(err.getMessageKey()).replace("%s", extractFieldNames(err))).append(" \n");
        }
        return msg.toString();
    }

	private static String extractFieldNames(XmlValidationError err) {
		if(err.getField().startsWith("[") && err.getField().endsWith("]")){
			String string = err.getField().substring(1, err.getField().length()-1);
			String[] parts = string.split(" +");
			StringBuilder sb = new StringBuilder();
			for(String part : parts){
				String key = "";
				if("&".equals(part)){
					key = XmlConstants.XML_FIELD_AND;
				} else if("|".equals(part)){
					key = XmlConstants.XML_FIELD_OR;
				} else {
					key = err.getParent() + "." + part;
				}
				sb.append(getLocalizedMessage(key));
				sb.append(" ");
			}

			return sb.toString().trim();
		} else {
			return getLocalizedMessage(err.getParent() + "." + err.getField());
		}
	}
    
    public static String getLocalizedMessage(String key) {
        return I18NSource.ThreadUtil.getLocalizedMessage(
                (key.startsWith("processdata") ? "" : "widget.process_data_block.editor.") + key);
    }
    
    public static String getParametrizedLocalizedMessage(String key, Object... parameters) {
        return I18NSource.ThreadUtil.getThreadI18nSource().getMessage(
                (key.startsWith("processdata") ? "" : "widget.process_data_block.editor.") + key,
                parameters
        );
    }
    
}
