package org.aperteworkflow.util.vaadin.ui.date;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupDateField;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Date;

public class OptionalDateField extends CustomField {
    private CheckBox maxRangeCheckBox;
    private PopupDateField dateField;

    public OptionalDateField(I18NSource messageSource) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setWidth("100%");

        maxRangeCheckBox = new CheckBox(messageSource.getMessage("date.field.max.range"));
        maxRangeCheckBox.setValue(false);
        maxRangeCheckBox.setImmediate(true);
        maxRangeCheckBox.setWidth("100%");
        maxRangeCheckBox.addListener(new ValueChangeListener() {
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                toggleDateField(maxRangeCheckBox.booleanValue() ? null : new Date());
            }
        });

        dateField = new PopupDateField();
        dateField.setImmediate(true);
        dateField.setDateFormat(VaadinUtility.SIMPLE_DATE_FORMAT_STRING);
        dateField.setWidth("100px");
        dateField.setResolution(DateField.RESOLUTION_DAY);

        layout.addComponent(dateField);
        layout.addComponent(maxRangeCheckBox);
        layout.setExpandRatio(maxRangeCheckBox, 1.0F);

        setCompositionRoot(layout);
    }

    public void setDateFormat(String dateFormat) {
        dateField.setDateFormat(dateFormat);
    }

    public void setMaximumRangeCaption(String caption) {
        maxRangeCheckBox.setCaption(caption);
    }

    public void setDateFieldPrompt(String prompt) {
        dateField.setInputPrompt(prompt);
    }

    public void toggleMaximumRange(boolean select) {
        maxRangeCheckBox.setValue(select);
    }

    @Override
    protected void setInternalValue(Object newValue) {
        maxRangeCheckBox.setValue(newValue == null);
        toggleDateField(newValue);
        super.setInternalValue(newValue);
    }

    private void toggleDateField(Object value) {
        dateField.setValue(value);
        dateField.setEnabled(value != null);
    }

    public void addDateChangedListener(ValueChangeListener listener) {
        dateField.addListener(listener);
    }

    @Override
    public Object getValue() {
        return dateField.getValue();
    }

    public Date dateValue() {
        return (Date) getValue();
    }

    @Override
    public Class<?> getType() {
        return Date.class;
    }
}
