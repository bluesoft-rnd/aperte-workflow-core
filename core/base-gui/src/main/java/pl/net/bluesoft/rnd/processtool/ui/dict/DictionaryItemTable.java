package pl.net.bluesoft.rnd.processtool.ui.dict;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CancelEditionOfDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.EditDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveNewDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.generic.exception.PropertyNameNotDefinedException;
import pl.net.bluesoft.rnd.processtool.ui.table.GenericTable;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Strings;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.TextField;

/**
 * Table to display dictionary items 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryItemTable extends GenericTable<ProcessDBDictionaryItem>
{
	
    private static final String EMPTY_VALID_DATE = "...";
    
	public static final String KEY_COLUMN_NAME = "key";
	public static final String DESCRIPTION_COLUMN_NAME = "description";
	public static final String VALUE_COLUMN_NAME = "value";
	public static final String EXTENSIONS_COLUMN_NAME = "extensions";
	public static final String DELETE_COLUMN_NAME = "delete";
	public static final String EDIT_SAVE_COLUMN_NAME = "edit_save";
	
	private static final String[] VISIBLE_COLUMNS = 
	{
		KEY_COLUMN_NAME, DESCRIPTION_COLUMN_NAME, 
		VALUE_COLUMN_NAME, EXTENSIONS_COLUMN_NAME, 
		EDIT_SAVE_COLUMN_NAME, DELETE_COLUMN_NAME
	};
	
	private static final String[] EDITABLE_COLUMNS =
		{
			KEY_COLUMN_NAME, DESCRIPTION_COLUMN_NAME, 
		};
	

	public DictionaryItemTable(BeanItemContainer<ProcessDBDictionaryItem> container, I18NSource i18NSource, GenericVaadinPortlet2BpmApplication application) 
	{
		super(container, i18NSource, application);
		
		setColumnHeader(KEY_COLUMN_NAME, getMessage("dict.item.key"));
		setColumnHeader(DESCRIPTION_COLUMN_NAME, getMessage("dict.item.description"));
		setColumnHeader(VALUE_COLUMN_NAME, getMessage("dict.item.values"));
		setColumnHeader(EXTENSIONS_COLUMN_NAME, getMessage("dict.item.extensions"));
		setColumnHeader(EDIT_SAVE_COLUMN_NAME, getMessage("pagedtable.edit"));
		setColumnHeader(DELETE_COLUMN_NAME, getMessage("pagedtable.delete"));
		
		setSortContainerPropertyId(KEY_COLUMN_NAME);
		sort(new Object[] {KEY_COLUMN_NAME}, new boolean[] {true});

	}

	@Override
	protected Component generateCell(ProcessDBDictionaryItem entry,String columnId) 
	{
		BeanItem<ProcessDBDictionaryItem> bean =  getContainer().getItem(entry);
		if(bean == null)
			return null;
		
		if(columnId.equals(KEY_COLUMN_NAME))
		{
			TextField textField = new TextField();
			textField.setPropertyDataSource(bean.getItemProperty(KEY_COLUMN_NAME));
			textField.setReadOnly(true);

			return textField;
		}
		
		else if(columnId.equals(DESCRIPTION_COLUMN_NAME))
		{
			TextField textField = new TextField();
			textField.setPropertyDataSource(bean.getItemProperty(DESCRIPTION_COLUMN_NAME));
			textField.setReadOnly(true);

			return textField;
		}
		
		else if(columnId.equals(VALUE_COLUMN_NAME))
		{
            return new DictPopupView(getMessage("dict.showValues"), new ValuesPopupVisibilityListener(entry));
		}
		
		else if(columnId.equals(EXTENSIONS_COLUMN_NAME))
		{
            return new DictPopupView(getMessage("dict.showValues"), new ExtensionsPopupVisibilityListener(entry));
		}
		
		else if(columnId.equals(EDIT_SAVE_COLUMN_NAME))
		{
			if(isEditable())
			{
				if(getValue() == entry)
				{				
					CssLayout layout = new CssLayout();
					SaveItemButton saveItemButton = new SaveItemButton(entry);
					CancelItemButton cancelItemButton = new CancelItemButton(entry);
					
					layout.addComponent(saveItemButton);
					layout.addComponent(cancelItemButton);
					return layout;
				}
				else
				{
					return null;
				}
			}
			else
			{
				EditItemButton editItemButton = new EditItemButton(entry);
				return editItemButton;
			}
		}
		
		else if(columnId.equals(DELETE_COLUMN_NAME))
		{
			if(!isEditable())
			{
				DeleteItemButton deleteItemButton = new DeleteItemButton(entry);
				
				return deleteItemButton;
			}
			else
			{
				return null;
			}
		}
		
		throw new PropertyNameNotDefinedException("Column name not defined: "+columnId);
	}
	
	@Override
	protected Field generateField(ProcessDBDictionaryItem entry, String columnId)
	{
		BeanItem<ProcessDBDictionaryItem> bean =  getContainer().getItem(entry);
		if(bean == null)
			return null;
		
		if(columnId.equals(KEY_COLUMN_NAME))
		{
			TextField textField = new TextField();
			textField.setPropertyDataSource(bean.getItemProperty(KEY_COLUMN_NAME));
			textField.setNullRepresentation("");
			textField.setNullSettingAllowed(false);
			textField.setRequired(true);
			textField.setCaption("");
			textField.setRequiredError(getMessage("validate.item.val.empty"));
			textField.setImmediate(true);
			textField.setValidationVisible(true);
			
			/* if this is selected item, enable its edition */
			textField.setReadOnly(this.getValue() != entry);
			
			return textField;
		}
		
		else if(columnId.equals(DESCRIPTION_COLUMN_NAME))
		{
			TextField textField = new TextField();
			textField.setNullRepresentation("");
			textField.setNullSettingAllowed(false);
			textField.setPropertyDataSource(bean.getItemProperty(DESCRIPTION_COLUMN_NAME));
			textField.setValidationVisible(true);
			
			/* if this is selected item, enable its edition */
			textField.setReadOnly(this.getValue() != entry);
			
			return textField;
		}
		
		return null;
	}

	@Override
	protected String[] getVisibleFields() 
	{
		return VISIBLE_COLUMNS;
	}
	
	@Override
	protected String[] getEditableFields() {
		return EDITABLE_COLUMNS;
	}
	

	/** Delete item request */
	private class EditItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItem entryToEdit;
		
		public EditItemButton(ProcessDBDictionaryItem entryToEdit)
		{
			this.entryToEdit = entryToEdit;
			
			setImmediate(true);
			setStyleName("default small");
			setCaption(getMessage("pagedtable.edit"));
			addListener((ClickListener)this);
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			EditDictionaryItemActionRequest actionRequest = new EditDictionaryItemActionRequest(entryToEdit);
			DictionaryItemTable.this.notifyListeners(actionRequest);
		}
	}
	
	private class SaveItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItem entryToSave;
		
		public SaveItemButton(ProcessDBDictionaryItem entryToEdit)
		{
			this.entryToSave = entryToEdit;
			
			setImmediate(true);
			setStyleName("default small");
			setCaption(getMessage("pagedtable.save"));
			addListener((ClickListener)SaveItemButton.this);
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			if(!isEntryValid(entryToSave))
				return;
			
			if(entryToSave.getId() == null)
			{
				SaveNewDictionaryItemActionRequest actionRequest = new SaveNewDictionaryItemActionRequest(entryToSave);
				DictionaryItemTable.this.notifyListeners(actionRequest);
			}
			else
			{
				SaveDictionaryItemActionRequest actionRequest = new SaveDictionaryItemActionRequest(entryToSave);
				DictionaryItemTable.this.notifyListeners(actionRequest);
			}
		}
	}
	
	private class CancelItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItem entryToSave;
		
		public CancelItemButton(ProcessDBDictionaryItem entryToEdit)
		{
			this.entryToSave = entryToEdit;
			
			setImmediate(true);
			setStyleName("default small");
			setCaption(getMessage("pagedtable.cancel"));
			addListener((ClickListener)CancelItemButton.this);
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			CancelEditionOfDictionaryItemActionRequest actionRequest = new CancelEditionOfDictionaryItemActionRequest(entryToSave);
			DictionaryItemTable.this.notifyListeners(actionRequest);

		}
	}
	
	/** Delete item request */
	private class DeleteItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItem entryToDelete;
		
		public DeleteItemButton(ProcessDBDictionaryItem entryToDelete)
		{
			this.entryToDelete = entryToDelete;
			
			setImmediate(true);
			setStyleName("default small");
			setCaption(getMessage("pagedtable.delete"));
			addListener((ClickListener)this);
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			/* Create new delete entry request */
			DeleteDictionaryItemActionRequest actionRequest = new DeleteDictionaryItemActionRequest(entryToDelete, getContainer());
			
			DictionaryItemTable.this.notifyListeners(actionRequest);
		}
	}
	
    private class DictPopupView extends PopupView {
        private Label info;

        public DictPopupView(final String smallTitle, DictPopupVisibilityListener listener) 
        {
            super(smallTitle, null);
            this.info = new Label("", Label.CONTENT_XHTML);
            this.info.setWidth(400, UNITS_PIXELS);
            setContent(new Content() {
                @Override
                public String getMinimizedValueAsHTML() {
                    return smallTitle;
                }

                @Override
                public Component getPopupComponent() {
                    return info;
                }
            });
            listener.setLargeView(info);
            addListener(listener);
            setHideOnMouseOut(true);
            addStyleName("bubble");
        }
    }
    
    private class ExtensionsPopupVisibilityListener extends DictPopupVisibilityListener
    {
        protected ExtensionsPopupVisibilityListener(ProcessDBDictionaryItem entry) {
            super(entry);
        }
        
        @Override
        public String getEmptyDescription() {
            return getMessage("dict.item.noextensions");
        }

        @Override
        public String getItemRepresentation(ProcessDBDictionaryItemValue item) {
            StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append("<ul>");
            
            if(item.getExtensions().isEmpty())
            {
            	sb.append("<li>").append(getMessage("dict.item.noextensions")).append("</li>");
            }

            else 
            {
                for(ProcessDBDictionaryItemExtension ext: item.getExtensions())
                {
                    sb.append("<li>")
                    .append("<b>").append(ext.getName()).append("</b>")
                    .append(Strings.hasText(ext.getDescription())
                            ? " (" + ext.getDescription() + ")" : "")
                    .append(": ")
                    .append(Strings.hasText(ext.getValue()) ? "<b>" + ext.getValue() + "</b>"
                            : getMessage("dict.item.extensions.novalue"))
                    .append("</li>");
                }
            }
            sb.append("</ul>");
            return sb.toString();
        }
    }
    
    private class ValuesPopupVisibilityListener extends DictPopupVisibilityListener
    {
        protected ValuesPopupVisibilityListener(ProcessDBDictionaryItem entry) {
            super(entry);
        }

        @Override
        public String getEmptyDescription() {
            return getMessage("dict.item.novalues");
        }
        @Override
        public String getItemRepresentation(ProcessDBDictionaryItemValue item) {
            DateFormat dateFormat = VaadinUtility.simpleDateFormat();
            StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append(" (").append("<i>");
            if (item.hasFullDatesRange()) {
                sb.append(getMessage("dict.full.range"));
            }
            else {
                sb.append(item.getValidStartDate() != null ? dateFormat.format(item.getValidStartDate()) : EMPTY_VALID_DATE)
                        .append(" - ")
                        .append(item.getValidEndDate() != null ? dateFormat.format(item.getValidEndDate()) : EMPTY_VALID_DATE);
            }
            sb.append("</i>)");
            return sb.toString();
        }
    	
    }
    
    private abstract class DictPopupVisibilityListener implements PopupVisibilityListener 
    {
        private ProcessDBDictionaryItem entry;
        private Label largeView;

        protected DictPopupVisibilityListener(ProcessDBDictionaryItem entry) {
            this.entry = entry;
        }

        public void setLargeView(Label largeView) {
            this.largeView = largeView;
        }

        public abstract String getEmptyDescription();

        public abstract String getItemRepresentation(ProcessDBDictionaryItemValue item);

        @Override
        public void popupVisibilityChange(PopupVisibilityEvent event) {
            if (event.isPopupVisible()) 
            {
                List<ProcessDBDictionaryItemValue> values = new ArrayList<ProcessDBDictionaryItemValue>(entry.getValues());
                StringBuilder sb = new StringBuilder();
                if (values.isEmpty()) {
                    sb.append(getEmptyDescription());
                }
                else {
                    sb.append("<ul>");
                    java.util.Collections.sort(values, new Comparator<ProcessDBDictionaryItemValue>() 
                    {
                        @Override
                        public int compare(ProcessDBDictionaryItemValue o1, ProcessDBDictionaryItemValue o2) 
                        {
                        	
                        	/* The null value is higher then anything else */
                        	if(o1.getValidStartDate() == null)
                        		return Integer.MAX_VALUE;
                        	
                        	else if(o1.getValidEndDate() == null)
                        		return Integer.MIN_VALUE;
                        	
                        	else if(o2.getValidStartDate() == null)
                        		return Integer.MIN_VALUE;
                        	
                        	
                			/* Fix na IBMowa impelementacje TimeStampa, który próbuje rzutować
                			 * obiekt Date na Timestamp i przez to leci wyjątek. 
                			 */
                			Date paymentDate1 = new Date(o1.getValidStartDate().getTime());
                			Date paymentDate2 = new Date(o2.getValidStartDate().getTime());
                        	
                        	/* The newer the date is the position of value is higher in collection */
                            return paymentDate2.compareTo(paymentDate1);
                        }
                    });
                    for (ProcessDBDictionaryItemValue value : values) {
                        sb.append("<li>").append(getItemRepresentation(value)).append("</li>");
                    }
                    sb.append("</ul>");
                }
                sb.append("</b>");
                largeView.setValue(sb.toString());
            }
        }
    }
}
