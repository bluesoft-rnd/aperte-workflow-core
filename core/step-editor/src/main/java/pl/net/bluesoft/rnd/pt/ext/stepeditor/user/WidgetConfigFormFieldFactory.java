package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WidgetConfigFormFieldFactory extends DefaultFieldFactory {
	private static final long	serialVersionUID	= 1840386215211557588L;
    private static final Logger logger              = Logger.getLogger(WidgetConfigFormFieldFactory.class.getName());

	public Field createField(Property<?> property) {
        Field field = createBaseField(property);

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

	protected Field createBaseField(Property<?> property) {
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

		field.setPropertyDataSource(property);
        field.setCaption(createCaptionByPropertyId(property.getName()));
        field.setDescription(property.getDescription());
        field.setRequired(property.isRequired());
        field.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		return field;
	}

}
