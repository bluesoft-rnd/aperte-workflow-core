package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.form.FormAwareField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aperteworkflow.util.vaadin.ui.GenericValueFieldFactory;

public class WidgetConfigFormFieldFactory extends GenericValueFieldFactory {

    private static final Logger logger = Logger.getLogger(WidgetConfigFormFieldFactory.class.getName());

    
	public Field createField(Property<?> property, Form form) {


        Field field = createBaseField(property, form);

        if (property.getType() == Integer.class) {
            field.addValidator(new IntegerValidator("is.not.an.integer"));
        }

        if (field instanceof AbstractField) {
            AbstractField abstractField = (AbstractField) field;
            abstractField.setImmediate(true);
        }

        if (field instanceof AbstractTextField) {
            AbstractTextField textField = (AbstractTextField) field;
            textField.setNullRepresentation("");
        }

        if (field instanceof RichTextArea) {
            // RichTextArea does not extend AbstractTextField like TextArea or TextField
            RichTextArea textArea = (RichTextArea) field;
            textArea.setNullRepresentation("");
        }
        


        return field;
	}

	protected Field createBaseField(Property<?> property, Form form) {
		Field field = null;
		if (property.getPropertyFieldClass() != null) {
            try {
                field = property.getPropertyFieldClass().newInstance();
            } catch (InstantiationException e) {
                logger.log(Level.WARNING, "Failed to create field using class from property", e);
            } catch (IllegalAccessException e) {
                logger.log(Level.WARNING, "Failed to create field using class from property", e);
            }
        }

		if (field == null) {
			field = createFieldByPropertyType(property.getType());
		}

        if (field instanceof FormAwareField) {
            Collection<?> itemPropertyIds = form.getItemPropertyIds();
            Map<String, com.vaadin.data.Property> map = new HashMap<String, com.vaadin.data.Property>();
            for (Object o : itemPropertyIds){
                 Property p = (Property) o;
                map.put(p.getPropertyId(), p);
            }
                
            ((FormAwareField)field).setFormProperties(map);
        }


        field.setPropertyDataSource(property);
        field.setRequired(property.isRequired());
        field.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        if (property.getName() == null) {
            field.setCaption(createCaptionByPropertyId(property.getPropertyId()));
        } else {
            field.setCaption(property.getName());
        }

        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
        if (property.getDescription() == null) {
            field.setDescription(messages.getMessage(
                    "property.field.description.short.format",
                    new Object[] { property.getPropertyId() }
            ));
        } else {
            field.setDescription(messages.getMessage(
                    "property.field.description.format",
                    new Object[] { property.getDescription(), property.getPropertyId() }
            ));
        }

		return field;
	}

}
