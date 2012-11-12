package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.dict.fields.DictionaryItemValuesField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.HashSet;
import java.util.Set;

import static org.aperteworkflow.util.vaadin.VaadinUtility.addIcon;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.smallButton;

public class DictionaryItemForm extends Form {
    private BeanItem<ProcessDBDictionaryItem> item;
    private Button saveButton;
    private Button cancelButton;
    private I18NSource source;
    private Application application;
    private GridLayout layout;
    private Button addButton;

    private static final Set<String> FIELDS_VISIBLE = new HashSet<String>() {{
        add("stringKey");
        add("description");
        add("dbValues");
    }};
    private static final Set<String> FIELDS_EDITABLE = FIELDS_VISIBLE;
    private static final Set<String> FIELDS_REQUIRED = new HashSet<String>() {{
        add("stringKey");
    }};

    public DictionaryItemForm(Application application, I18NSource source, BeanItem<ProcessDBDictionaryItem> item) {
        super();
        this.application = application;
        this.source = source;
        this.item = item;
        saveButton = smallButton(getMessage("button.save"));
        cancelButton = smallButton(getMessage("button.cancel"));
        initForm();
    }

    public void addSaveClickListener(ClickListener clickListener) {
        saveButton.addListener(clickListener);
    }

    public void addCancelClickListener(ClickListener clickListener) {
        cancelButton.addListener(clickListener);
    }

    private void initForm() {
        layout = new GridLayout(2, 3);
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setWidth("100%");

        setLayout(layout);
        setLocale(application.getLocale());
        setFormFieldFactory(new DictionaryItemFormFieldFactory(application, source,
                FIELDS_VISIBLE, FIELDS_EDITABLE, FIELDS_REQUIRED));
        setItemDataSource(item);
        setVisibleItemProperties(FIELDS_VISIBLE);
        setValidationVisible(false);
        setValidationVisibleOnCommit(false);
        setImmediate(true);
        setWriteThrough(false);
        setFooter(horizontalLayout(Alignment.MIDDLE_RIGHT, cancelButton, saveButton));
    }

    protected void attachField(Object propertyId, Field field) {
        if (FIELDS_VISIBLE.contains(propertyId)) {
            if (field.getValue() == null && field.getType() == String.class) {
                field.setValue("");
            }
            if ("stringKey".equals(propertyId)) {
                layout.addComponent(field, 0, 0);
            }
            else if ("description".equals(propertyId)) {
                layout.addComponent(field, 1, 0);
            }
            else if ("dbValues".equals(propertyId)) {
                Label caption = new Label("<b>" + getMessage("dict.item.values") + "</b>", Label.CONTENT_XHTML);
                caption.setWidth("100%");

                HorizontalLayout captionLayout;
                if (field.isRequired()) {
                    captionLayout = horizontalLayout(Alignment.MIDDLE_LEFT, caption, new Label("*", Label.CONTENT_XHTML) {{
                        addStyleName("v-required-field-indicator");
                    }});
                }
                else {
                    captionLayout = horizontalLayout(Alignment.MIDDLE_LEFT, caption);
                }
                layout.removeComponent(0, 1);
                layout.addComponent(captionLayout, 0, 1);
                layout.setComponentAlignment(captionLayout, Alignment.MIDDLE_LEFT);

                addButton = addIcon(application);
                addButton.setCaption(getMessage("dict.add.value"));
                addButton.setDescription(getMessage("dict.add.value"));
                addButton.addListener(((DictionaryItemValuesField) field).getAddValueClickListener());

                layout.removeComponent(1, 1);
                layout.addComponent(addButton, 1, 1);
                layout.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT);

                layout.addComponent(field, 0, 2, 1, 2);
            }
            layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
        }
    }

    public String getMessage(String key) {
        return source.getMessage(key);
    }

    public String getMessage(String key, String defaultValue) {
        return source.getMessage(key, defaultValue);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        addButton.setReadOnly(readOnly);
        super.setReadOnly(readOnly);
    }
}
