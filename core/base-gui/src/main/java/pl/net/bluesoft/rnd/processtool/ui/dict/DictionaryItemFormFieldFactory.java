package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

public class DictionaryItemFormFieldFactory extends DefaultFieldFactory {
    private Object[] visiblePropertyIds;
    private Object[] editablePropertyIds;
    private Object[] requiredPropertyIds;
    private I18NSource source;
    private Application application;

    public DictionaryItemFormFieldFactory(Application application, I18NSource source, Object[] visiblePropertyIds,
                                          Object[] editablePropertyIds, Object[] requiredPropertyIds) {
        this.application = application;
        this.visiblePropertyIds = visiblePropertyIds;
        this.editablePropertyIds = editablePropertyIds;
        this.requiredPropertyIds = requiredPropertyIds;
        this.source = source;
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        if (!isPropertyVisible(propertyId)) {
            return null;
        }
        BeanItem<ProcessDBDictionaryItem> beanItem = (BeanItem<ProcessDBDictionaryItem>) item;
        ProcessDBDictionaryItem bean = beanItem.getBean();
        Field field = "extensions".equals(propertyId) ? new DictionaryItemExtensionField(application, source, bean)
                : new TextField(source.getMessage("dict.item." + propertyId));
        field.setWidth("100%");
        if (isPropertyEditable(propertyId)) {
            field.setRequired(isPropertyRequired(propertyId));
            field.setRequiredError(source.getMessage("dict.item." + propertyId + ".required"));
            if ("value".equals(propertyId)) {
                String type = bean.getValueType();
                if (type != null) {
                    if ("int".equalsIgnoreCase(type)) {
                        field.addValidator(new IntegerValidator(source.getMessage("validate.integer")));
                    }
                    else if ("dbl".equalsIgnoreCase(type)) {
                        field.addValidator(new DoubleValidator(source.getMessage("validate.double")));
                    }
                }
            }
        }
        else {
            field.setReadOnly(true);
        }
        return field;
    }

    private boolean isPropertyRequired(Object propertyId) {
        return findInArray(propertyId, requiredPropertyIds);
    }

    private boolean isPropertyVisible(Object propertyId) {
        return findInArray(propertyId, visiblePropertyIds);
    }

    private boolean isPropertyEditable(Object propertyId) {
        return findInArray(propertyId, editablePropertyIds);
    }

    private boolean findInArray(Object obj, Object[] array) {
        for (Object o : array) {
            if (obj.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public Object[] getVisiblePropertyIds() {
        return visiblePropertyIds;
    }

    public Object[] getEditablePropertyIds() {
        return editablePropertyIds;
    }

    public Object[] getRequiredPropertyIds() {
        return requiredPropertyIds;
    }
}
