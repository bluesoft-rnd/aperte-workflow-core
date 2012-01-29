package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.*;
import org.apache.commons.beanutils.BeanUtils;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.WidgetElement;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import java.util.Arrays;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.findField;
import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.joinValidationErrors;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class WidgetPropertiesEditorFormComponent extends VerticalLayout {

    private Form form;
    private Object clone;
    private Object itemId;
    private ProcessDataHierarchyEditor editor;

    public WidgetPropertiesEditorFormComponent(final Object itemId, final ProcessDataHierarchyEditor editor) {
        setWidth("100%");
        setSpacing(true);
        this.itemId = itemId;
        this.editor = editor;

        final Class classOfItem = itemId.getClass();
        clone = clone(itemId);
        form = new Form();
        form.setFormFieldFactory(getFieldFactory(classOfItem));
        form.setWidth("100%");
        form.setWriteThrough(false);
        form.setCaption(itemId.getClass().getSimpleName());
        form.setItemDataSource(new BeanItem(clone));

        final Button commit = new Button("Commit");   //TODO i18n
        commit.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                commit();
            }
        });

        addComponent(form);
        addComponent(commit);
    }

    private void commit() {
        if (!form.isValid()) {
            getApplication().getMainWindow().showNotification("Please review validation errors",  //TODO i18n
                    Window.Notification.TYPE_WARNING_MESSAGE);
            return;
        }
        form.commit();
        if (clone instanceof WidgetElement) {
            List<XmlValidationError> xmlValidationErrors = ((WidgetElement) clone).validateElement();
            if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
                String msg = joinValidationErrors(xmlValidationErrors);
                getApplication().getMainWindow().showNotification("Validation errors",  //TODO i18n
                        msg, Window.Notification.TYPE_WARNING_MESSAGE);         //TODO i18n
                return;
            }
        }
        copyProperties(itemId, clone);
        editor.refreshRawXmlAndPreview();
    }

    private void copyProperties(Object dest, Object src) {
        try {
            BeanUtils.copyProperties(dest, src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object clone(Object itemId) {
        final Object clone;
        try {
            clone = BeanUtils.cloneBean(itemId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clone;
    }

    private DefaultFieldFactory getFieldFactory(final Class classOfItem) {
        return new DefaultFieldFactory() {

            @Override
            public Field createField(Item item, Object propertyId, Component uiContext) {
                Property property = item.getItemProperty(propertyId);
                Class<?> cls = property.getType();
                Class[] supported = new Class[]{String.class, Boolean.class, Integer.class};
                if (!Arrays.asList(supported).contains(cls)) {
                    return null;
                }
                java.lang.reflect.Field reflectField = findField(propertyId, classOfItem);
                if (reflectField != null) {
                    Field field = super.createField(item, propertyId, uiContext);
                    AvailableOptions opts = reflectField.getAnnotation(AvailableOptions.class);
                    if (opts != null && opts.value() != null) {
                        NativeSelect ns = new NativeSelect();
                        field = ns;
                        field.setCaption(createCaptionByPropertyId(propertyId));
                        for (String opt : opts.value()) {
                            ns.addItem(opt);
                        }
                    }
                    if (field instanceof AbstractField) {
                        AbstractField abstractField = (AbstractField) field;
                        abstractField.setImmediate(true);
                    }

                    if (field instanceof AbstractTextField) {
                        AbstractTextField textField = (AbstractTextField) field;
                        textField.setNullRepresentation("");
                    }
                    if (cls.equals(Integer.class)) {
                        field.addValidator(new IntegerValidator("is.not.an.integer"));
                        field.setWidth("100px");
                    } else {
                        field.setWidth("100%");
                    }
                    if (reflectField.getAnnotation(RequiredAttribute.class) != null) {
                        field.setRequired(true);
                    }
                    return field;
                }
                return null;

            }
        };
    }


    public Form getForm() {
        return form;
    }
}
