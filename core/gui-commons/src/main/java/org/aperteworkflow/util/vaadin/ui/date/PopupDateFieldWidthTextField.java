package org.aperteworkflow.util.vaadin.ui.date;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PopupDateField;

import java.util.Locale;

/**
 * User: POlszewski
 * Date: 2012-01-12
 * Time: 16:42:06
 */
public class PopupDateFieldWidthTextField extends DateFieldWithTextField<PopupDateField> {
	public PopupDateFieldWidthTextField(String caption, String timeCaption, Locale locale, String timeErrorMessage, boolean initWithEmptyHour) {
		super(new PopupDateField(), caption, timeCaption, locale, timeErrorMessage, initWithEmptyHour);
	}

	@Override
	protected Layout wrapFields() {
		GridLayout hl = new GridLayout(2,1);
		hl.addComponent(dateField);
		hl.addComponent(timeField);
		hl.setComponentAlignment(dateField, Alignment.MIDDLE_LEFT);
		hl.setComponentAlignment(timeField, Alignment.MIDDLE_LEFT);
		return hl;
	}
}
