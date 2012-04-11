package pl.net.bluesoft.rnd.pt.utils.jdbc.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Classes {
    public static <A extends Annotation> A getClassAnnotation(Class clazz, Class<A> annotation) {
        while (clazz != null && Object.class != clazz) {
            if (clazz.isAnnotationPresent(annotation)) {
                return (A) clazz.getAnnotation(annotation);
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static <A extends Annotation> java.lang.reflect.Field getFieldWithAnnotation(Class clazz, Class<A> annotation) {
        List<Field> fields = new LinkedList<Field>(Arrays.asList(clazz.getDeclaredFields()));
        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            java.lang.reflect.Field field = it.next();
            if (field.isAnnotationPresent(annotation)) {
                return field;
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getFieldWithAnnotation(clazz.getSuperclass(), annotation);
        }
        return null;
    }
}
