package org.aperteworkflow.help.impl;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import org.vaadin.addon.customfield.FieldWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: mwysocki_bls
 * Date: 8/29/11
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class FieldWithHelp<T> extends FieldWrapper {
	public FieldWithHelp(final Field wrappedField, Component helpButton) {

		super(wrappedField, null, wrappedField.getType(),
                new HorizontalLayoutWrapper(wrappedField.getWidth(), wrappedField.getWidthUnits()));
		addStyleName("help-wrapper");

		setCaption(wrappedField.getCaption());
		wrappedField.setCaption(null);
		wrappedField.addStyleName("fieldhelp wrappedfield");
		wrappedField.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		HorizontalLayout layout = (HorizontalLayout) getCompositionRoot();
		layout.setMargin(false);
		layout.setSpacing(true);
		//layout.setSizeUndefined();

		layout.addComponent(wrappedField);

		layout.addComponent(helpButton);
		layout.setComponentAlignment(helpButton, Alignment.MIDDLE_LEFT);
		layout.setExpandRatio(helpButton, 0);
		layout.setExpandRatio(wrappedField, 1);

	}

    public Field getField() {
        return getWrappedField();
    }
}
