package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.StringUtil;

import java.util.*;
import static org.aperteworkflow.util.vaadin.VaadinUtility.*;

public class DictionaryItemExtensionField extends CustomField {
    private I18NSource source;
    private Application application;
    private ProcessDBDictionaryItem dictItem;
    private Button addButton;

    private VerticalLayout itemsLayout;

    private Map<String, ProcessDBDictionaryItemExtension> originalValue;
    private List<ProcessDBDictionaryItemExtension> modifiedValue;

    public DictionaryItemExtensionField(Application application, I18NSource source, ProcessDBDictionaryItem dictItem) {
        this.source = source;
        this.application = application;
        this.dictItem = dictItem;
        initView();
    }

    private void initView() {
        Panel root = new Panel();
        root.setStyleName("borderless light");
        root.setWidth("100%");

        itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("100%");

        setCaption(source.getMessage("dict.item.extensions"));

        addButton = addIcon(application);
        addButton.setDescription(source.getMessage("dict.add.extension"));
        addButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                ProcessDBDictionaryItemExtension itemExtension = new ProcessDBDictionaryItemExtension();
                modifiedValue.add(itemExtension);
                createExtensionRow(itemExtension);
            }
        });

        root.addComponent(horizontalLayout(new Label(source.getMessage("dict.add.extension")), addButton));
        root.addComponent(itemsLayout);
        setCompositionRoot(root);
    }

    private class ItemExtensionFormFieldFactory extends DefaultFieldFactory {
        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            Field field = null;
            if (propertyId.equals("name") || propertyId.equals("value")) {
                field = new TextField();
                if ("name".equals(propertyId)) {
                    field.setRequired(true);
                    field.setCaption(source.getMessage("dict.item.extensions.name"));
                }
                else {
                    field.setCaption(source.getMessage("dict.item.extensions.value"));
                }
            }
            return field;
        }
    }

    private class ItemExtensionForm extends Form {
        private ProcessDBDictionaryItemExtension itemExtension;
        private HorizontalLayout layout;
        private Button deleteButton = null;

        private ItemExtensionForm(ProcessDBDictionaryItemExtension ext) {
            this.itemExtension = ext;

            layout = new HorizontalLayout();
            layout.setSpacing(true);
            layout.setWidth("100%");

            setLayout(layout);
            setValidationVisible(false);
            setValidationVisibleOnCommit(false);
            setWriteThrough(false);
            setImmediate(true);
            setInvalidCommitted(false);
            setFormFieldFactory(new ItemExtensionFormFieldFactory());
            setVisibleItemProperties(new String[] {"name", "value"});
            setItemDataSource(new BeanItem<ProcessDBDictionaryItemExtension>(ext));
        }

        public void addDeleteButton(ClickListener listener) {
            deleteButton = deleteIcon(application);
            layout.addComponent(deleteButton);
            layout.setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
            layout.setExpandRatio(deleteButton, 1.0F);
            deleteButton.addListener(listener);
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            if (deleteButton != null) {
                deleteButton.setVisible(!readOnly);
            }
        }

        @Override
        protected void attachField(Object propertyId, Field field) {
            if (field.getValue() == null && field.getType() == String.class) {
                field.setValue("");
            }
            super.attachField(propertyId, field);
        }

        public ProcessDBDictionaryItemExtension getItemExtension() {
            return itemExtension;
        }
    }

    private void loadData() {
        itemsLayout.removeAllComponents();
        modifiedValue = new ArrayList<ProcessDBDictionaryItemExtension>();
        for (ProcessDBDictionaryItemExtension ext : originalValue.values()) {
            modifiedValue.add(new ProcessDBDictionaryItemExtension(ext));
        }
        Collections.sort(modifiedValue, new Comparator<ProcessDBDictionaryItemExtension>() {
            @Override
            public int compare(ProcessDBDictionaryItemExtension o1, ProcessDBDictionaryItemExtension o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (ProcessDBDictionaryItemExtension itemExtension : modifiedValue) {
            createExtensionRow(itemExtension);
        }
    }

    private void createExtensionRow(final ProcessDBDictionaryItemExtension ext) {
        final ItemExtensionForm form = new ItemExtensionForm(ext);
        form.setWidth("100%");
        form.addDeleteButton(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                itemsLayout.removeComponent(form);
                modifiedValue.remove(form.getItemExtension());
            }
        });
        itemsLayout.addComponent(form);
    }

    @Override
    protected void setInternalValue(Object newValue) {
        if (newValue != null && !(newValue instanceof Map)) {
            throw new IllegalArgumentException("Unable to handle non-map values");
        }
        originalValue = (Map<String, ProcessDBDictionaryItemExtension>) newValue;
        loadData();
        super.setInternalValue(newValue);
    }

    @Override
    public Object getValue() {
        validateInternal();
        Map<String, ProcessDBDictionaryItemExtension> value = new HashMap<String, ProcessDBDictionaryItemExtension>();
        for (ProcessDBDictionaryItemExtension ext : modifiedValue) {
            value.put(ext.getName(), ext);
        }
        return value;
    }

    @Override
    public void validate() throws InvalidValueException {
        validateInternal();
        super.validate();
    }

    public void validateInternal() {
        for (Iterator<Component> it = itemsLayout.getComponentIterator(); it.hasNext();) {
            ItemExtensionForm form = (ItemExtensionForm) it.next();
            form.commit();
        }
        for (ProcessDBDictionaryItemExtension ext : modifiedValue) {
            String name = ext.getName();
            if (!StringUtil.hasText(name)) {
                throw new InvalidValueException(source.getMessage("validate.item.ext.name.empty"));
            }
            for (ProcessDBDictionaryItemExtension otherExt : modifiedValue) {
                if (ext != otherExt && ext.getName().equals(otherExt.getName())) {
                    throw new InvalidValueException(source.getMessage("validate.item.ext.name.duplicate").replaceFirst("%s", ext.getName()));
                }
            }
        }
    }

    @Override
    public Class<?> getType() {
        return Map.class;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        for (Iterator<Component> it = itemsLayout.getComponentIterator(); it.hasNext(); ) {
            it.next().setReadOnly(readOnly);
        }
        addButton.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }
}
