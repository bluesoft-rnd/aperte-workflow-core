package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.*;

import org.apache.commons.beanutils.BeanUtils;
import org.aperteworkflow.util.vaadin.ui.GenericValueFieldFactory;
import org.aperteworkflow.util.vaadin.ui.GenericValueTextField;

import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.WidgetElement;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;

import java.util.Arrays;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.*;

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
        BeanItem<?> item = new BeanItem<Object>(clone);

        form = new Form();
        form.setFormFieldFactory(getFieldFactory(classOfItem));
        form.setWidth("100%");
        form.setWriteThrough(false);
        form.setCaption(itemId.getClass().getSimpleName());
        form.setItemDataSource(item);

        final Button commit = new Button(getLocalizedMessage("commit"));
        commit.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                commit();
            }
        });

        addComponent(form);
        addComponent(commit);
    }

    public void commit() {
        if (!form.isValid()) {
            getApplication().getMainWindow().showNotification(getLocalizedMessage("validation-errors"),
                    Window.Notification.TYPE_WARNING_MESSAGE);
            return;
        }
        form.commit();
        if (clone instanceof WidgetElement) {
            List<XmlValidationError> xmlValidationErrors = ((WidgetElement) clone).validateElement();
            if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
                String msg = joinValidationErrors(xmlValidationErrors);
                getApplication().getMainWindow().showNotification(getLocalizedMessage("validation-errors"),
                        msg, Window.Notification.TYPE_WARNING_MESSAGE);
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
        return new GenericValueFieldFactory() {

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
                	Field field=null;
                	
                	field = super.createField(item, propertyId, uiContext);
                	
                    AvailableOptions opts = reflectField.getAnnotation(AvailableOptions.class);
                    if (opts != null && opts.value() != null) {
                        NativeSelect ns = new NativeSelect();
                        field = ns;
                        for (String opt : opts.value()) {
                            ns.addItem(opt);
                            ns.setItemCaption(opt, getLocalizedMessage(propertyId + "." + opt));
                        }
                    }

                    AperteDoc doc = reflectField.getAnnotation(AperteDoc.class);
                    if (doc != null) {
                        field.setCaption(getLocalizedMessage(doc.humanNameKey()));
                        field.setDescription(getParametrizedLocalizedMessage(
                                "description.format",
                                getLocalizedMessage(doc.descriptionKey()),
                                propertyId
                        ));
                    } else {
                        field.setCaption(getLocalizedMessage((String) propertyId));
                        field.setDescription(getParametrizedLocalizedMessage(
                                "description.short.format",
                                propertyId
                        ));
                    }

                    if (field instanceof AbstractField) {
                        AbstractField abstractField = (AbstractField) field;
                        abstractField.setImmediate(true);
                    }
                    if (field instanceof AbstractTextField) {
                        AbstractTextField textField = (AbstractTextField) field;
                        textField.setNullRepresentation("");
                    }
                    if (field instanceof RichTextArea) {
                        RichTextArea textArea = (RichTextArea) field;
                        textArea.setNullRepresentation("");
                    }

                    if (cls.equals(Integer.class)) {
                        field.addValidator(new IntegerValidator(getLocalizedMessage("is.not.an.integer")));
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

    public Object getItemId() {
        return itemId;
    }
}
