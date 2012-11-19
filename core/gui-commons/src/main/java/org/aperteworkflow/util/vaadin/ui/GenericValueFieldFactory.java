package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;

public class GenericValueFieldFactory extends DefaultFieldFactory {
	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
		Class<?> type = item.getItemProperty(propertyId).getType();
		Field field = createFieldByPropertyType(type);
		field.setCaption(createCaptionByPropertyId(propertyId));
		return field;
	}

	public static Field createFieldByPropertyType(Class<?> type) {
		Field field = DefaultFieldFactory.createFieldByPropertyType(type);

		if (field instanceof TextField && !type.isAssignableFrom(String.class)) {
			if (type.isEnum()) {
				Select select = new Select();

				for (Object value : type.getEnumConstants()) {
					select.addItem(value);
				}
				field = select;
			} else {
				field = new GenericValueTextField();
			}
		}
		return field;
	}
}
