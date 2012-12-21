package org.aperteworkflow.util.vaadin.text;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

import static pl.net.bluesoft.util.lang.Formats.nvlAsString;

/**
 * @author amichalak@bluesoft.net.pl
 */
public abstract class TextValueChangeListener implements ValueChangeListener, TextChangeListener {
    private final String nullValue;

    protected TextValueChangeListener() {
        this(null);
    }

    public TextValueChangeListener(String nullValue) {
        this.nullValue = nullValue;
    }

    public abstract void handleTextChange(String changedText);

    @Override
    public void textChange(TextChangeEvent event) {
        handleTextChange(event.getText());
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        handleTextChange(nvlAsString(event.getProperty().getValue(), nullValue));
    }
}
