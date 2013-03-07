package pl.net.bluesoft.rnd.processtool.ui.dict;

import static org.aperteworkflow.util.vaadin.VaadinUtility.SIMPLE_DATE_FORMAT_STRING;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.ui.date.OptionalDateField;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.fields.DictionaryItemExtensionField;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CopyDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.generic.exception.PropertyNameNotDefinedException;
import pl.net.bluesoft.rnd.processtool.ui.table.GenericTable;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Table to display dictionary items 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryItemValueTable extends GenericTable<ProcessDBDictionaryItemValue> 
{
	public static final String VALUE_COLUMN_NAME = "stringValue";
	public static final String START_DATE_COLUMN_NAME = "validStartDate";
	public static final String END_DATE_COLUMN_NAME = "validEndDate";
	public static final String EXTENSIONS_COLUMN_NAME = "extensions";
	public static final String DELETE_COLUMN_NAME = "delete";
	public static final String COPY_COLUMN_NAME = "copy";
	
	private static final String[] VISIBLE_COLUMNS = 
	{
		VALUE_COLUMN_NAME, START_DATE_COLUMN_NAME, END_DATE_COLUMN_NAME,
		EXTENSIONS_COLUMN_NAME, COPY_COLUMN_NAME, DELETE_COLUMN_NAME
	};
	
	private static final String[] EDITABLE_COLUMNS =
		{
		VALUE_COLUMN_NAME, START_DATE_COLUMN_NAME, END_DATE_COLUMN_NAME,
		EXTENSIONS_COLUMN_NAME
		};

	public DictionaryItemValueTable(BeanItemContainer<ProcessDBDictionaryItemValue> container, I18NSource i18NSource, GenericVaadinPortlet2BpmApplication application) 
	{
		super(container, i18NSource, application);
		
		setWriteThrough(false);
		
		setColumnHeader(VALUE_COLUMN_NAME, getMessage("dict.item.values"));
		
		setColumnHeader(START_DATE_COLUMN_NAME, getMessage("dict.item.value.valid.from"));
		setColumnHeader(END_DATE_COLUMN_NAME, getMessage("dict.item.value.valid.to"));
		setColumnHeader(EXTENSIONS_COLUMN_NAME, getMessage("dict.item.extensions"));
		setColumnHeader(COPY_COLUMN_NAME, getMessage("pagedtable.copy"));
		setColumnHeader(DELETE_COLUMN_NAME, getMessage("pagedtable.delete"));
		
		setSortContainerPropertyId(START_DATE_COLUMN_NAME);
		setSortAscending(false);
		sort();

	}

	@Override
	protected Component generateCell(ProcessDBDictionaryItemValue entry,String columnId) 
	{
		if(columnId.equals(DELETE_COLUMN_NAME))
			return new DeleteItemButton(entry);

		else if(columnId.equals(COPY_COLUMN_NAME))
			return new CopyItemButton(entry);

		throw new PropertyNameNotDefinedException("Column name not defined: "+columnId);
	}
	
	@Override
	protected Field generateField(ProcessDBDictionaryItemValue entry,String columnId) 
	{
		BeanItem<ProcessDBDictionaryItemValue> bean =  getContainer().getItem(entry);
		
		if(columnId.equals(VALUE_COLUMN_NAME))
		{
			TextField textField = new TextField();
			textField.setRequired(true);
			textField.setNullRepresentation("");
			textField.setCaption("");
			textField.setRequiredError(getMessage("validate.item.val.empty"));
			textField.setPropertyDataSource(bean.getItemProperty(VALUE_COLUMN_NAME));
			
			textField.setWidth(100, UNITS_PERCENTAGE);
            return textField;
		}
		if(columnId.equals(START_DATE_COLUMN_NAME))
		{
            OptionalDateField dateField = new OptionalDateField(i18NSource);
            dateField.setDateFormat(SIMPLE_DATE_FORMAT_STRING);
            dateField.setPropertyDataSource(bean.getItemProperty(START_DATE_COLUMN_NAME));
			dateField.setWidth(100, UNITS_PERCENTAGE);
            return dateField;
		}
		
		if(columnId.equals(END_DATE_COLUMN_NAME))
		{
            OptionalDateField dateField = new OptionalDateField(i18NSource);
            dateField.setDateFormat(SIMPLE_DATE_FORMAT_STRING);
            dateField.setPropertyDataSource(bean.getItemProperty(END_DATE_COLUMN_NAME));
			dateField.setWidth(100, UNITS_PERCENTAGE);
            return dateField;
		}
		
		if(columnId.equals(EXTENSIONS_COLUMN_NAME))
		{
			DictionaryItemExtensionField itemExtensionField = new DictionaryItemExtensionField(application, i18NSource);
			itemExtensionField.setPropertyDataSource(bean.getItemProperty(EXTENSIONS_COLUMN_NAME));
			itemExtensionField.setWriteThrough(true);
			
            return itemExtensionField;
		}
		
		throw new PropertyNameNotDefinedException("Column name not defined: "+columnId);
		
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
	
	/** Copy item's value request */
	private class CopyItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItemValue itemsValueToCopy;
		
		public CopyItemButton(ProcessDBDictionaryItemValue itemsValueToCopy)
		{
			this.itemsValueToCopy = itemsValueToCopy;
			
			setImmediate(true);
			setStyleName("default small");
			setCaption(getMessage("pagedtable.copy"));
			addListener((ClickListener)this);
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			/* Create new delete entry request */
			CopyDictionaryItemValueActionRequest actionRequest = new CopyDictionaryItemValueActionRequest(itemsValueToCopy, getContainer());
			
			DictionaryItemValueTable.this.notifyListeners(actionRequest);
		}
	}
	
	/** Delete item's value request */
	private class DeleteItemButton extends Button implements ClickListener
	{
		private ProcessDBDictionaryItemValue entryToDelete;
		
		public DeleteItemButton(ProcessDBDictionaryItemValue entryToDelete)
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
			DeleteDictionaryItemValueActionRequest actionRequest = new DeleteDictionaryItemValueActionRequest(entryToDelete, getContainer());
			
			DictionaryItemValueTable.this.notifyListeners(actionRequest);
		}
	}

}
