package pl.net.bluesoft.rnd.processtool.ui.dict.fields;

import static org.aperteworkflow.util.vaadin.VaadinUtility.addIcon;
import static org.aperteworkflow.util.vaadin.VaadinUtility.deleteIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.customfield.CustomField;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class DictionaryItemExtensionField extends CustomField  {
    private I18NSource source;
    private Application application;
    private Button addButton;

    private VerticalLayout itemsLayout;
    private Label noExtensionsLabel;
    
    private Collection<ItemExtensionForm> forms;

    public DictionaryItemExtensionField(Application application, I18NSource source) {
        this.source = source;
        this.application = application;
        this.forms = new ArrayList<DictionaryItemExtensionField.ItemExtensionForm>();
        initView();
    }

    private void initView() {
        VerticalLayout root = new VerticalLayout();
        root.setStyleName("borderless light");
        root.setSizeFull();
        root.setMargin(false);

        noExtensionsLabel = new Label("<i>" + getMessage("dict.item.noextensions") + "</i>", Label.CONTENT_XHTML);
        noExtensionsLabel.setSizeFull();

        itemsLayout = new VerticalLayout();
        itemsLayout.setSizeFull();

        addButton = addIcon(application);
        addButton.setDescription(getMessage("dict.add.extension"));
        addButton.setCaption(getMessage("dict.add.extension"));
        addButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) 
            {
                ProcessDBDictionaryItemExtension itemExtension = new ProcessDBDictionaryItemExtension();
                getExtensions().add(itemExtension);
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

    public String getMessage(String key) {
        return source.getMessage(key);
    }

    public String getMessage(String key, String defaultValue) {
        return source.getMessage(key, defaultValue);
    }

    private void loadData() 
    {
        itemsLayout.removeAllComponents();
        if (getExtensions().isEmpty()) {
            itemsLayout.addComponent(noExtensionsLabel);
        }
        else 
        {
        	List<ProcessDBDictionaryItemExtension> extensions = new ArrayList<ProcessDBDictionaryItemExtension>(getExtensions());
            /* Some garbage, structured-style code... */
            Collections.sort(extensions, new Comparator<ProcessDBDictionaryItemExtension>() {
                @Override
                public int compare(ProcessDBDictionaryItemExtension o1, ProcessDBDictionaryItemExtension o2) 
                {
                	if(o1.getName() == null)
                		return -1;
                	else if(o2.getName() == null)
                		return 1;
                	else
                		return o1.getName().compareTo(o2.getName());

                }
            });
            for (ProcessDBDictionaryItemExtension itemExtension : extensions) 
                createExtensionRow(itemExtension);
        }
    }


    private void createExtensionRow(final ProcessDBDictionaryItemExtension ext) 
    {
        final ItemExtensionForm form = new ItemExtensionForm(ext);
        form.setWidth("100%");
        form.addDeleteButton(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) 
            {
            	ext.setItemValue(null);
            	getExtensions().remove(ext);
                itemsLayout.removeComponent(form);
                if (getExtensions().isEmpty()) {
                    itemsLayout.addComponent(noExtensionsLabel);
                }
                forms.remove(form);
            }
        });
        if (itemsLayout.getComponentIndex(noExtensionsLabel) != -1) {
            itemsLayout.removeComponent(noExtensionsLabel);
        }
        forms.add(form);
        itemsLayout.addComponent(form);
    }


    @Override
    public void validate() throws InvalidValueException {
        validateInternal();
        super.validate();
    }

    public void validateInternal() {
        if (!getExtensions().isEmpty()) {
            for (Iterator<Component> it = itemsLayout.getComponentIterator(); it.hasNext(); ) {
                ItemExtensionForm form = (ItemExtensionForm) it.next();
                form.commit();
            }
            for (ProcessDBDictionaryItemExtension ext : getExtensions()) {
                for (ProcessDBDictionaryItemExtension otherExt : getExtensions()) {
                    if (ext != otherExt && ext.getName().equals(otherExt.getName())) {
                        throw new InvalidValueException(getMessage("validate.item.ext.name.duplicate").replaceFirst("%s", ext.getName()));
                    }
                }
            }
        }
    }
    
    @Override
    public void setPropertyDataSource(Property newDataSource) 
    {
    	loadData();
    	
    	super.setPropertyDataSource(newDataSource);
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

    private class ItemExtensionForm extends Form {
        private HorizontalLayout layout;
        private Button deleteButton;

        private ItemExtensionForm(ProcessDBDictionaryItemExtension ext) {
            layout = new HorizontalLayout();
            layout.setSpacing(true);
            layout.setWidth("100%");
            
            setWriteThrough(DictionaryItemExtensionField.this.isWriteThrough());

            setLayout(layout);
            setValidationVisible(false);
            setValidationVisibleOnCommit(false);

            setImmediate(true);
            setInvalidCommitted(false);
            setFormFieldFactory(new ItemExtensionFormFieldFactory());
            setVisibleItemProperties(new String[] {"name", "stringValue"});
            setItemDataSource(new BeanItem<ProcessDBDictionaryItemExtension>(ext));

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
        public Field createField(Item item, Object propertyId, Component uiContext) 
        {
            if (propertyId.equals("name"))
    		{
                TextField textField = new TextField();
                textField.setNullRepresentation("");
                textField.setRequired(true);
                textField.setRequiredError(getMessage("validate.item.ext.name.empty"));
                textField.setCaption(getMessage("dict.item.extensions.name"));
                textField.setPropertyDataSource(item.getItemProperty("name"));
                textField.setWriteThrough(DictionaryItemExtensionField.this.isWriteThrough());
                return textField;
    		}
            else if(propertyId.equals("stringValue"))
            {
                TextField textField = new TextField();
                textField.setNullRepresentation("");
                textField.setCaption(getMessage("dict.item.extensions.value"));
                textField.setPropertyDataSource(item.getItemProperty("stringValue"));
                textField.setWriteThrough(DictionaryItemExtensionField.this.isWriteThrough());
                return textField;
            }

            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
	public Set<ProcessDBDictionaryItemExtension> getExtensions()
    {
    	if(getValue() == null)
    		return new HashSet<ProcessDBDictionaryItemExtension>();
    	
    	Set<ProcessDBDictionaryItemExtension> extensions = (Set<ProcessDBDictionaryItemExtension>)getValue();
    	return extensions;
    }
}
