package org.aperteworkflow.util.vaadin.ui.date;

import com.vaadin.ui.*;

import java.util.Locale;

public class InlineDateFieldWithTextField extends DateFieldWithTextField<InlineDateField> {
	public InlineDateFieldWithTextField(String caption, String timeCaption, Locale locale, String timeErrorMessage, boolean initWithEmptyHour) {
    	super(new InlineDateField(), caption, timeCaption, locale, timeErrorMessage, initWithEmptyHour);
	}

	@Override
	protected Layout wrapFields() {
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(dateField);
		HorizontalLayout hl = new HorizontalLayout();
		hl.addComponent(timeField);
		hl.setComponentAlignment(timeField, Alignment.MIDDLE_CENTER);
		vl.addComponent(hl);
		return vl;
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
}
