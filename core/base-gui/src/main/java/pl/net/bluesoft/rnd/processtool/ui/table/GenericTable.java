package pl.net.bluesoft.rnd.processtool.ui.table;

import java.util.Collection;
import java.util.HashSet;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.ui.dict.IEntryValidator;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequestListener;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.Table.ColumnGenerator;

/**
 * Fast, generic table. 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class GenericTable<ENTRY> extends Table implements ColumnGenerator
{
	/** Implement this method to genenerate cells by column name */
	protected abstract Component generateCell(ENTRY entry, String columnId);
	
	protected abstract Field generateField(ENTRY entry, String columnId);
	
	/** Get visible columns ids */
	protected abstract String[] getVisibleFields();
	
	protected abstract String[] getEditableFields();
	
	protected I18NSource i18NSource;
	protected GenericVaadinPortlet2BpmApplication application;
	private Collection<IActionRequestListener> actionRequestListeners;
	private Collection<IEntryValidator<ENTRY>> validators;

	public GenericTable(BeanItemContainer<ENTRY> container, I18NSource i18NSource, GenericVaadinPortlet2BpmApplication application) 
	{
		this.i18NSource = i18NSource;
		this.application = application;
		this.actionRequestListeners = new HashSet<IActionRequestListener>();
		this.validators = new HashSet<IEntryValidator<ENTRY>>();
		
		init(container);
	}
	
	/** Add new action request listener */
	public void addActionRequestListener(IActionRequestListener listener)
	{
		this.actionRequestListeners.add(listener);
	}
	
	/** Add new validation source */
	public void addEntryValidator(IEntryValidator<ENTRY> validationSource)
	{
		this.validators.add(validationSource);
	}
	
	protected void init(BeanItemContainer<ENTRY> container)
	{
		setContainerDataSource(container);
        
        setPageLength(10);
        setSizeFull();
	}
	
	/** Validate item */
	protected boolean isEntryValid(ENTRY entry)
	{
		for(IEntryValidator<ENTRY> validator: validators)
			if(!validator.isEntryValid(entry))
				return false;
		
		return true;
	}
	
	public void setContainerDataSource(BeanItemContainer<ENTRY> newDataSource) 
	{
		super.setContainerDataSource(newDataSource);
		this.setTableFieldFactory(new GenericTableFieldFactory());

        for(String propertyId: getVisibleFields())
        	if(getColumnGenerator(propertyId) == null && !isEditableField(propertyId))
        		addGeneratedColumn(propertyId, this);
		
        setVisibleColumns(getVisibleFields());
	}
	
	private class GenericTableFieldFactory implements TableFieldFactory
	{
		public GenericTableFieldFactory()
		{
			
		}

		@SuppressWarnings("unchecked")
		@Override
		public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) 
		{
			return generateField((ENTRY)itemId, (String)propertyId);
		}
		
	}
	
	private boolean isEditableField(String fieldName)
	{
		for(int i=0;i<getEditableFields().length;i++)
			if(getEditableFields()[i].equals(fieldName))
				return true;
		
		return false;
	}
	
	/** Notify all listeners abount action request */
	protected void notifyListeners(IActionRequest actionRequest)
	{
		for(IActionRequestListener listener: actionRequestListeners)
			listener.handleActionRequest(actionRequest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) 
	{
		return generateCell((ENTRY)itemId, (String)columnId);
	}
	
    protected String getMessage(String key, Object ... params) {
        return i18NSource.getMessage(key, key, params);
    }

	@SuppressWarnings("unchecked")
	protected BeanItemContainer<ENTRY> getContainer() {
		return (BeanItemContainer<ENTRY>)getContainerDataSource();
	}


}
