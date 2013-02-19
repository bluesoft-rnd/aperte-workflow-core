package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import com.vaadin.data.util.BeanItemContainer;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: delete selected item
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DeleteDictionaryItemValueActionRequest implements IActionRequest 
{
	private ProcessDBDictionaryItemValue itemToDelete;
	private BeanItemContainer<ProcessDBDictionaryItemValue> container;

	public DeleteDictionaryItemValueActionRequest(ProcessDBDictionaryItemValue itemToDelete, BeanItemContainer<ProcessDBDictionaryItemValue> container) 
	{
		this.itemToDelete = itemToDelete;
		this.container = container;
	}
	
	public ProcessDBDictionaryItemValue getItemValueToDelete()
	{
		return this.itemToDelete;
	}
	
	public BeanItemContainer<ProcessDBDictionaryItemValue> getContainer()
	{
		return this.container;
	}

}
