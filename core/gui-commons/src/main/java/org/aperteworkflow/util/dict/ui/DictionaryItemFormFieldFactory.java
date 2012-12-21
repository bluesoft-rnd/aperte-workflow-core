package org.aperteworkflow.util.dict.ui;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import org.aperteworkflow.util.dict.ui.fields.DictionaryItemValuesField;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemWrapper;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Set;

import static org.aperteworkflow.util.dict.wrappers.DictionaryItemWrapper._VALUES;

public abstract class DictionaryItemFormFieldFactory extends DefaultFieldFactory {
    private Set<String> visiblePropertyIds;
    private Set<String> editablePropertyIds;
    private Set<String> requiredPropertyIds;
    private I18NSource source;
    private Application application;

    public DictionaryItemFormFieldFactory(Application application, I18NSource source, Set<String> visiblePropertyIds,
                                          Set<String> editablePropertyIds, Set<String> requiredPropertyIds) {
        super();
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
        BeanItem<DictionaryItemWrapper> beanItem = Lang2.assumeType(item, BeanItem.class);
        DictionaryItemWrapper bean = beanItem.getBean();
        Field field = _VALUES.equals(propertyId)
				? createItemValuesField(application, source, bean.getValueType())
                : new TextField(source.getMessage("dict.item." + propertyId));
        field.setWidth("100%");
        if (isPropertyEditable(propertyId)) {
            field.setRequired(isPropertyRequired(propertyId));
            field.setRequiredError(source.getMessage("dict.item." + propertyId + ".required"));
        }
        else {
            field.setReadOnly(true);
        }
        return field;
    }

	protected abstract DictionaryItemValuesField createItemValuesField(Application application, I18NSource source, String valueType);

	private boolean isPropertyRequired(Object propertyId) {
        return requiredPropertyIds.contains(propertyId);
    }

    private boolean isPropertyVisible(Object propertyId) {
        return visiblePropertyIds.contains(propertyId);
    }

    private boolean isPropertyEditable(Object propertyId) {
        return editablePropertyIds.contains(propertyId);
    }

    public Object[] getVisiblePropertyIds() {
        return visiblePropertyIds.toArray();
    }

    public Object[] getEditablePropertyIds() {
        return editablePropertyIds.toArray();
    }

    public Object[] getRequiredPropertyIds() {
        return requiredPropertyIds.toArray();
    }
}
