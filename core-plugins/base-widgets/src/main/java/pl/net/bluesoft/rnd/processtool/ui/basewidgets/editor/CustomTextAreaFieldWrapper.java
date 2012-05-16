package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.ui.TextArea;
import org.vaadin.addon.customfield.CustomField;

import java.util.Collection;

/**
 * Boilerplate code, let's not mix it with real logic.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class CustomTextAreaFieldWrapper extends CustomField {
    protected TextArea rawText = new TextArea();

    @Override
    public Class<?> getType() {
        return rawText.getType();
    }

    @Override
    public boolean isReadOnly() {
        return rawText.isReadOnly();
    }

    @Override
    public boolean isInvalidCommitted() {
        return rawText.isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        rawText.setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        rawText.commit();
    }

    @Override
    public void discard() throws SourceException {
        rawText.discard();
    }

    @Override
    public boolean isModified() {
        return rawText.isModified();
    }

    @Override
    public boolean isWriteThrough() {
        return rawText.isWriteThrough();
    }

    @Override
    public void setWriteThrough(boolean writeTrough) throws SourceException, Validator.InvalidValueException {
        rawText.setWriteThrough(writeTrough);
    }

    @Override
    public boolean isReadThrough() {
        return rawText.isReadThrough();
    }

    @Override
    public void setReadThrough(boolean readTrough) throws SourceException {
        rawText.setReadThrough(readTrough);
    }

    @Override
    public String toString() {
        return rawText.toString();
    }

    @Override
    public Object getValue() {
        return rawText.getValue();
    }

    @Override
    public void addValidator(Validator validator) {
        rawText.addValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return rawText.getValidators();
    }

    @Override
    public void removeValidator(Validator validator) {
        rawText.removeValidator(validator);
    }

    @Override
    public boolean isValid() {
        return rawText.isValid();
    }

    @Override
    public boolean isInvalidAllowed() {
        return rawText.isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidAllowed) throws UnsupportedOperationException {
        rawText.setInvalidAllowed(invalidAllowed);
    }

    @Override
    public ErrorMessage getErrorMessage() {
        return rawText.getErrorMessage();
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        rawText.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        rawText.removeListener(listener);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        rawText.valueChange(event);
    }

    @Override
    public void focus() {
        rawText.focus();
    }

    @Override
    public boolean isRequired() {
        return rawText.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        rawText.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        rawText.setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return rawText.getRequiredError();
    }

    @Override
    public boolean isValidationVisible() {
        return rawText.isValidationVisible();
    }

    @Override
    public void setValidationVisible(boolean validateAutomatically) {
        rawText.setValidationVisible(validateAutomatically);
    }

    @Override
    public void setCurrentBufferedSourceException(SourceException currentBufferedSourceException) {
        rawText.setCurrentBufferedSourceException(currentBufferedSourceException);
    }
}
