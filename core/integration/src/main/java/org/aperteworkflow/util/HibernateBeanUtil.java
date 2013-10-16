package org.aperteworkflow.util;

import org.hibernate.proxy.HibernateProxy;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.model.processdata.AbstractProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttribute;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HibernateBeanUtil {
    public static <T> T fetchHibernateData(T o) {
		if (o == null) {
			return null;
		}
        try {
			return new DeepCloner().clone(o);
		}
		catch (Exception e) {
			Logger.getLogger(HibernateBeanUtil.class.toString()).log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
    }

	private static class DeepCloner {
		private final Map<Object, Object> cloned = new HashMap<Object, Object>();

		public <T> T clone(T t) {
			if (isNotCloneable(t)) {
				return t;
			}

			if (cloned.containsKey(t)) {
				return (T)cloned.get(t);
			}

			if (t instanceof Set) {
				return cloneSet(t);
			}
			if (t instanceof Map) {
				return cloneMap(t);
			}
			if (t instanceof Collection) {
				return cloneList(t);
			}
			if (t.getClass().isArray()) {
				return cloneArray(t);
			}
			if (t instanceof Cloneable) {
				return t;
			}

			t = replaceObject(t);

			return manualClone(t);
		}

		private <T> boolean isNotCloneable(T t) {
			return t == null || t instanceof Number || t instanceof Boolean || t instanceof String ||
					t instanceof Date || t.getClass().isEnum();
		}

		private <T> T cloneArray(T t) {
			Class<?> componentType = t.getClass().getComponentType();

			if (!componentType.isPrimitive()) {
				Object[] t1 = (Object[])t;
				Object[] clone = Arrays.copyOf(t1, t1.length);

				cloned.put(t, clone);

				for (int i = 0; i < t1.length; ++i) {
					clone[i] = clone(clone[i]);
				}
				return (T)clone;
			}
			return t;
		}

		private <T> T cloneList(T t) {
			List clone = new ArrayList();
			cloned.put(t, clone);

			for (Object elem : (Collection)t) {
				clone.add(clone(elem));
			}
			return (T)clone;
		}

		private <T> T cloneMap(T t) {
			Map clone = new HashMap();
			cloned.put(t, clone);

			for (Map.Entry e : ((Map<?, ?>)t).entrySet()) {
				clone.put(clone(e.getKey()), clone(e.getValue()));
			}
			return (T)clone;
		}

		private <T> T cloneSet(T t) {
			Set clone = new HashSet();
			cloned.put(t, clone);

			for (Object elem : (Collection)t) {
				clone.add(clone(elem));
			}
			return (T)clone;
		}

		private <T> T manualClone(T t) {
			Object clone;

			try {
				clone = t.getClass().newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

			cloned.put(t, clone);

			for (Field field : Lang.getFieldsFromClassAndAncestors(t.getClass())) {
				if (isCloneableField(field, t.getClass())) {
					Object value = Lang.get(t, field.getName());
					Lang.set(clone, field.getName(), clone(value));
				}
			}

			nullifyTroublesomeFields(clone);
			return (T)clone;
		}

		private static boolean isCloneableField(Field field, Class<?> clazz) {
			return (field.getModifiers() & Modifier.STATIC) == 0 && (field.getModifiers() & Modifier.TRANSIENT) == 0 &&
					field.getAnnotation(Transient.class) == null && field.getAnnotation(XmlTransient.class) == null &&
					!(clazz == BpmTaskBean.class && field.getName().equals("processDefinition"));
		}

		private static <T> T replaceObject(T object) {
			if (object instanceof HibernateProxy) {
				if (object.getClass().getName().contains("javassist")) {
					Class assistClass = object.getClass();
					try {
						Method m = assistClass.getMethod("writeReplace");
						return (T)m.invoke(object);

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			return object;
		}

		private static void nullifyTroublesomeFields(Object t) {
			if (t instanceof ProcessDefinitionPermission) {
				ProcessDefinitionPermission permission = (ProcessDefinitionPermission)t;

				permission.setDefinition(null);
			}
			else if (t instanceof ProcessStateConfiguration) {
				ProcessStateConfiguration state = (ProcessStateConfiguration)t;

				state.setDefinition(null);
			}
			else if (t instanceof ProcessStatePermission) {
				ProcessStatePermission permission = (ProcessStatePermission)t;

				permission.setConfig(null);
			}
			else if (t instanceof ProcessStateWidget) {
				ProcessStateWidget widget = (ProcessStateWidget)t;

				widget.setConfig(null);
				widget.setParent(null);
			}
			else if (t instanceof ProcessStateWidgetAttribute) {
				ProcessStateWidgetAttribute attribute = (ProcessStateWidgetAttribute)t;

				attribute.setWidget(null);
			}
			else if (t instanceof ProcessStateWidgetPermission) {
				ProcessStateWidgetPermission permission = (ProcessStateWidgetPermission)t;

				permission.setWidget(null);
			}
			else if (t instanceof ProcessStateAction) {
				ProcessStateAction action = (ProcessStateAction)t;

				action.setConfig(null);
			}
			else if (t instanceof ProcessStateActionAttribute) {
				ProcessStateActionAttribute attribute = (ProcessStateActionAttribute)t;

				attribute.setAction(null);
			}
			else if (t instanceof ProcessStateActionPermission) {
				ProcessStateActionPermission permission = (ProcessStateActionPermission)t;

				permission.setAction(null);
			}
			else if (t instanceof AbstractProcessInstanceAttribute) {
				ProcessInstanceAttribute attribute = (ProcessInstanceAttribute)t;

				attribute.setProcessInstance(null);
			}
			else if (t instanceof ProcessComment) {
				((ProcessComment)t).setProcessInstance(null);
			}
			else if (t instanceof ProcessDeadline) {
				((ProcessDeadline)t).setProcessInstance(null);
			}
			else if (t instanceof BpmTaskBean) {
				BpmTaskBean bpmTask = (BpmTaskBean)t;

				bpmTask.setProcessDefinition(bpmTask.getProcessInstance().getDefinition());
				bpmTask.getProcessInstance().setDefinition(null);
			}
		}
	}
}
