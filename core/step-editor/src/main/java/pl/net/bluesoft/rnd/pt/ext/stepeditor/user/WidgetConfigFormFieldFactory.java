package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import pl.net.bluesoft.rnd.processtool.i18n.DefaultI18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;

public class WidgetConfigFormFieldFactory extends DefaultFieldFactory {
	private static final long	serialVersionUID	= 1840386215211557588L;

	private DefaultI18NSource	i18NSource			= new DefaultI18NSource();	;

	private List<I18NProvider>	i18NProviders;
	private Locale				locale;

	protected Field createField(Property<?> property, Class<? extends Field> klass) {
		Field field = createBaseField(property, klass);
		field.setDescription(property.getDescription());

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

		field.setRequired(property.isRequired());

		return field;
	}

	public Field createField(Property<?> property) {
		return createField(property, null);
	}

	protected Field createBaseField(Property<?> property, Class<? extends Field> klass) {
		Field field = null;
		if (klass != null) {
			try {
				field = klass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (field == null) {
			field = createFieldByPropertyType(property.getType());
		}
		field.setPropertyDataSource(property);
		field.setCaption(createCaptionByPropertyId(property.getName()));
		return field;
	}

	protected String getMessage(String key) {
		return getMessage(key, new Object[] {});
	}

	protected String getMessage(String key, Object... parameters) {
		try {
			return i18NSource.getMessage(key, i18NProviders);
		} catch (MissingResourceException e) {
			System.err.println("NOT DEFINED KEY: " + key);
			return '!' + key + '!';
		}
	}

	List<I18NProvider> getI18NProviders() {
		return i18NProviders;
	}

	void setI18NProviders(List<I18NProvider> i18nProviders) {
		i18NProviders = i18nProviders;
	}

	Locale getLocale() {
		return locale;
	}

	void setLocale(Locale locale) {
		this.locale = locale;
		i18NSource.setLocale(locale);
	}

}