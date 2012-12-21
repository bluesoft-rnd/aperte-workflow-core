package org.aperteworkflow.util.vaadin.ui.date;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.CompositeErrorMessage;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.addon.customfield.FieldWrapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * User: POlszewski
 * Date: 2012-01-12
 * Time: 16:43:29
 */
public abstract class DateFieldWithTextField<DF extends DateField> extends FieldWrapper<Date> {
	public static final int RESOLUTION_SEC = 1;
	public static final int RESOLUTION_MIN = 2;
	public static final int RESOLUTION_HOUR = 3;

	protected TextField timeField;
	protected DF dateField;
	private SimpleDateFormat dateFormat;
	private static final String[] formatString = new String[] { "HH:mm:ss", "HH:mm", "HH" };
	private int resolution = RESOLUTION_MIN;
	protected boolean updating;
	private boolean initWithEmptyHour;

	public DateFieldWithTextField(DF dateField, String caption, String timeCaption, Locale locale, String timeErrorMessage, boolean initWithEmptyHour) {
		super(dateField, Date.class);
		this.dateField = (DF) getWrappedField();
		setCaption(caption);
		this.initWithEmptyHour = initWithEmptyHour;

		dateFormat = new SimpleDateFormat(formatString[resolution - 1], locale);

		dateField.setResolution(DateField.RESOLUTION_DAY);

		timeField = createTimeField(timeCaption, timeErrorMessage);

		setCompositionRoot(wrapFields());
	}

	protected abstract Layout wrapFields();

	private TextField createTimeField(String timeCaption, String timeErrorMessage) {
		TextField timeField = new TextField(timeCaption);
		timeField.addValidator(new RegexpValidator("(2[0-3]|[0-1]?[0-9])(:[0-5][0-9]){0,2}", timeErrorMessage));
		timeField.setImmediate(true);
		timeField.setWidth(getAdvisedWidth(timeField), Sizeable.UNITS_PIXELS);
		timeField.setNullRepresentation("");
		timeField.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				updating = true;
				updateDateWithTime((Date) dateField.getValue());
				updating = false;
			}
		});
		return timeField;
	}

	private int getAdvisedWidth(TextField timeField) {
		if (timeField.getCaption() != null) {
			return 80;
		}
		return resolution == RESOLUTION_HOUR ? 30 : resolution == RESOLUTION_MIN ? 44 : 60;
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		super.setRequiredError(requiredMessage);
		timeField.setRequiredError(requiredMessage);
	}

	@Override
	public void setRequired(boolean required) {
		super.setRequired(required);
		timeField.setRequired(required);
	}

	@Override
	public ErrorMessage getErrorMessage() {
		CompositeErrorMessage dateErrors = (CompositeErrorMessage)dateField.getErrorMessage();
		CompositeErrorMessage timeErrors = (CompositeErrorMessage)timeField.getErrorMessage();
		LinkedList<CompositeErrorMessage> allErrors = new LinkedList<CompositeErrorMessage>();
		if(!dateField.isValid()){
			allErrors.add(dateErrors);
		}
		if(!timeField.isValid()){
			allErrors.add(timeErrors);
		}
		if(!allErrors.isEmpty())
			return new CompositeErrorMessage(allErrors);
		return null;
	}

	@Override
	public void validate() throws Validator.InvalidValueException {
		super.validate();
		//		timeField.setValidationVisible(false);
		timeField.validate();
		//		timeField.setValidationVisible(true);
	}

	@Override
	public boolean isValid() {
		return super.isValid() && timeField.isValid();
	}

	@Override
	public Object format(Date propertyValue) {
		if(!updating)
			updateTimeField(propertyValue);
		return propertyValue;
	}

	private void updateDateWithTime(Date fieldValue) {
		if (fieldValue != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(fieldValue);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			extractTime(cal);

			updateTimeField(cal.getTime());
			updateDateField(cal.getTime());
		}
		else {
			updateTimeField(null);
			updateDateField(null);
		}
	}

	private void extractTime(Calendar cal) {
		if (timeField.getValue() != null && timeField.getValue().toString() != null) {
			String timeString = timeField.getValue().toString().replaceAll("[,. -]", ":").replaceAll("[^0-9:]", "");
			if (!"".equals(timeString)) {
				String[] timeArray = timeString.split(":");
				if (timeArray.length >= 1 && timeArray[0].length() > 0 && resolution <= RESOLUTION_HOUR) {
					cal.set(Calendar.HOUR_OF_DAY, Math.min(23, Integer.valueOf(timeArray[0])));
				} else {
					return;
				}
				if (timeArray.length >= 2 && timeArray[1].length() > 0 && resolution <= RESOLUTION_MIN) {
					cal.set(Calendar.MINUTE, Math.min(59, Integer.valueOf(timeArray[1])));
				} else {
					return;
				}
				if (timeArray.length >= 3 && timeArray[2].length() > 0 && resolution <= RESOLUTION_SEC) {
					cal.set(Calendar.SECOND, Math.min(59, Integer.valueOf(timeArray[2])));
				} else {
					return;
				}
			}
		}
	}

	private void updateDateField(Date date) {
		dateField.setValue(date);
	}

	private void updateTimeField(Date date) {
		if(StringUtils.isEmpty((String)timeField.getValue()) && initWithEmptyHour)
			return;
		timeField.setValue(date != null ? dateFormat.format(date) : null);
	}

	public int getResolution() {
		return resolution;
	}

	public void setResolution(int resolution) {
		this.resolution = Math.min(3, Math.max(1, resolution));
	}

	public TextField getTimeField() {
		return timeField;
	}

	public void setTimeField(TextField timeField) {
		this.timeField = timeField;
	}

	public DF getDateField() {
		return dateField;
	}

	public void setDateField(DF dateField) {
		this.dateField = dateField;
	}
}
