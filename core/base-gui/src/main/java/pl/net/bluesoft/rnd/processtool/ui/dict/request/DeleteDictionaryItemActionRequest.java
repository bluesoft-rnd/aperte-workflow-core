package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import com.vaadin.data.util.BeanItemContainer;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: delete selected item
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DeleteDictionaryItemActionRequest implements IActionRequest 
{
	private ProcessDBDictionaryItem itemToDelete;
	private BeanItemContainer<ProcessDBDictionaryItem> container;

	public DeleteDictionaryItemActionRequest(ProcessDBDictionaryItem itemToDelete, BeanItemContainer<ProcessDBDictionaryItem> container) 
	{
		this.itemToDelete = itemToDelete;
		this.container = container;
	}
	
	public ProcessDBDictionaryItem getItemToDelete()
	{
		return this.itemToDelete;
	}
	
	public BeanItemContainer<ProcessDBDictionaryItem> getContainer()
	{
		return this.container;
	}

}
