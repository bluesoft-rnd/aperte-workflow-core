package org.aperteworkflow.util.dict.ui.fields;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.*;

import static org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper._NAME;
import static org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper._VALUE;
import static org.aperteworkflow.util.vaadin.VaadinUtility.addIcon;
import static org.aperteworkflow.util.vaadin.VaadinUtility.deleteIcon;

public abstract class DictionaryItemExtensionField extends CustomField  {
	private I18NSource source;
    private Application application;
    private Button addButton;

    private VerticalLayout itemsLayout;
    private Label noExtensionsLabel;

    private Map<String, DictionaryItemExtensionWrapper> originalValue = new HashMap<String, DictionaryItemExtensionWrapper>();
    private List<DictionaryItemExtensionWrapper> modifiedValue;

    public DictionaryItemExtensionField(Application application, I18NSource source) {
        this.source = source;
        this.application = application;
        initView();
    }

    private void initView() {
        VerticalLayout root = new VerticalLayout();
        root.setStyleName("borderless light");
        root.setWidth("100%");
        root.setMargin(false);

        noExtensionsLabel = new Label("<i>" + getMessage("dict.item.noextensions") + "</i>", Label.CONTENT_XHTML);

        itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("100%");

        addButton = addIcon(application);
        addButton.setDescription(getMessage("dict.add.extension"));
        addButton.setCaption(getMessage("dict.add.extension"));
        addButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
				DictionaryItemExtensionWrapper itemExtension = createDictionaryItemExtensionWrapper();
                modifiedValue.add(itemExtension);
                createExtensionRow(itemExtension);
            }
        });

        Label caption = new Label("<b>" + getMessage("dict.item.extensions") + "</b>", Label.CONTENT_XHTML);
        caption.setWidth("100%");

        HorizontalLayout captionLayout = new HorizontalLayout();
        captionLayout.setWidth("100%");
        captionLayout.setSpacing(true);
        captionLayout.addComponent(caption);
        captionLayout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        captionLayout.addComponent(addButton);
        captionLayout.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT);

        root.addComponent(captionLayout);
        root.addComponent(itemsLayout);
        setCompositionRoot(root);
    }

	protected abstract DictionaryItemExtensionWrapper createDictionaryItemExtensionWrapper();

	public String getMessage(String key) {
        return source.getMessage(key);
    }

    public String getMessage(String key, String defaultValue) {
        return source.getMessage(key, defaultValue);
    }

    private void loadData() {
        itemsLayout.removeAllComponents();
        createModifiedListFromOriginal();
        if (modifiedValue.isEmpty()) {
            itemsLayout.addComponent(noExtensionsLabel);
        }
        else {
            for (DictionaryItemExtensionWrapper itemExtension : modifiedValue) {
                createExtensionRow(itemExtension);
            }
        }
    }

    private void createModifiedListFromOriginal() {
        modifiedValue = new ArrayList<DictionaryItemExtensionWrapper>();
        for (DictionaryItemExtensionWrapper ext : originalValue.values()) {
            modifiedValue.add(ext.exactCopy());
        }
        Collections.sort(modifiedValue, new Comparator<DictionaryItemExtensionWrapper>() {
            @Override
            public int compare(DictionaryItemExtensionWrapper o1, DictionaryItemExtensionWrapper o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void createExtensionRow(final DictionaryItemExtensionWrapper ext) {
        final ItemExtensionForm form = new ItemExtensionForm(ext);
        form.setWidth("100%");
        form.addDeleteButton(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                itemsLayout.removeComponent(form);
                modifiedValue.remove(ext);
                if (modifiedValue.isEmpty()) {
                    itemsLayout.addComponent(noExtensionsLabel);
                }
            }
        });
        if (itemsLayout.getComponentIndex(noExtensionsLabel) != -1) {
            itemsLayout.removeComponent(noExtensionsLabel);
        }
        itemsLayout.addComponent(form);
    }

    @Override
    protected void setInternalValue(Object newValue) {
        if (newValue != null && !(newValue instanceof Map)) {
            throw new IllegalArgumentException("Unable to handle non-map values");
        }
        originalValue = (Map<String, DictionaryItemExtensionWrapper>) newValue;
        loadData();
        super.setInternalValue(newValue);
    }

    @Override
    public Object getValue() {
        validateInternal();
        Map<String, DictionaryItemExtensionWrapper> value = new HashMap<String, DictionaryItemExtensionWrapper>();
        for (DictionaryItemExtensionWrapper ext : modifiedValue) {
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
        if (!modifiedValue.isEmpty()) {
            for (Iterator<Component> it = itemsLayout.getComponentIterator(); it.hasNext(); ) {
                ItemExtensionForm form = (ItemExtensionForm) it.next();
                form.commit();
            }
            for (DictionaryItemExtensionWrapper ext : modifiedValue) {
                for (DictionaryItemExtensionWrapper otherExt : modifiedValue) {
                    if (ext != otherExt && ext.getName().equals(otherExt.getName())) {
                        throw new InvalidValueException(getMessage("validate.item.ext.name.duplicate").replaceFirst("%s", ext.getName()));
                    }
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

    @Override
    public void discard() throws SourceException {
        super.discard();
        createModifiedListFromOriginal();
    }

    private class ItemExtensionForm extends Form {
        private HorizontalLayout layout;
        private Button deleteButton;

        private ItemExtensionForm(DictionaryItemExtensionWrapper ext) {
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
            setVisibleItemProperties(new String[] { _NAME, _VALUE });
            setItemDataSource(new BeanItem<DictionaryItemExtensionWrapper>(ext));

            deleteButton = deleteIcon(application);
            deleteButton.setCaption(getMessage("dict.delete.ext"));
            deleteButton.setDescription(getMessage("dict.delete.ext"));

            layout.addComponent(deleteButton);
            layout.setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
            layout.setExpandRatio(deleteButton, 1.0F);
        }

        public void addDeleteButton(ClickListener listener) {
            deleteButton.addListener(listener);
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            if (deleteButton != null) {
                deleteButton.setVisible(!readOnly);
            }
        }
    }

    private class ItemExtensionFormFieldFactory extends DefaultFieldFactory {
        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            Field field = null;
            if (propertyId.equals(_NAME) || propertyId.equals(_VALUE)) {
                TextField textField = new TextField();
                textField.setNullRepresentation("");
                if (_NAME.equals(propertyId)) {
                    textField.setRequired(true);
                    textField.setRequiredError(getMessage("validate.item.ext.name.empty"));
                    textField.setCaption(getMessage("dict.item.extensions.name"));
                }
                else {
                    textField.setCaption(getMessage("dict.item.extensions.value"));
                }
                field = textField;
            }
            return field;
        }
    }
}
