package org.aperteworkflow.util.vaadin.ui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.TextField;

public class GenericValueTextField extends TextField {
	
	public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
	static {
	    map.put(boolean.class, Boolean.class);
	    map.put(byte.class, Byte.class);
	    map.put(short.class, Short.class);
	    map.put(char.class, Character.class);
	    map.put(int.class, Integer.class);
	    map.put(long.class, Long.class);
	    map.put(float.class, Float.class);
	    map.put(double.class, Double.class);
	}
	
	@Override
	public void setPropertyDataSource(final Property newDataSource) {
		super.setPropertyDataSource(getPropertyFormatter(newDataSource));
	}

	protected PropertyFormatter getPropertyFormatter(final Property newDataSource) {
		PropertyFormatter propertyFormatter = new PropertyFormatter(newDataSource) {
			@Override
			public String format(Object value) {
				if (value == null) {
					return getNullRepresentation();
				}
				return (String) ConvertUtils.convert(value, String.class);
			}

			@Override
			public Object parse(String formattedValue) throws Exception {
				if ("".equals(formattedValue)){
					return null;
				}
				return ConvertUtils.convert(formattedValue, map.get(newDataSource.getType()));
			}
		};
		// primitive value problem workaround
		// ObjectProperty is unable to set wrapped value if it's type is primitive
		// this code changes type to wrapper, transparently solving the problem
		if (newDataSource.getType().isPrimitive() && newDataSource instanceof ObjectProperty) {
			try {
				Field[] fields = newDataSource.getClass().getSuperclass().getDeclaredFields();
				for (Field field:fields){
					if (field.getName().equals("type")){
						field.setAccessible(true);
						field.set(newDataSource, map.get(field.get(newDataSource)));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return propertyFormatter;
		}
		else {
			return propertyFormatter;
		}
	}
}
