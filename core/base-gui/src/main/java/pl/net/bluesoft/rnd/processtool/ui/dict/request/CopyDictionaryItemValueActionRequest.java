package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import com.vaadin.data.util.BeanItemContainer;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: copy selected item's value
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class CopyDictionaryItemValueActionRequest implements IActionRequest 
{
	private ProcessDBDictionaryItemValue itemsValueToCopy;
	private BeanItemContainer<ProcessDBDictionaryItemValue> container;

	public CopyDictionaryItemValueActionRequest(ProcessDBDictionaryItemValue itemToDelete, BeanItemContainer<ProcessDBDictionaryItemValue> container) 
	{
		this.itemsValueToCopy = itemToDelete;
		this.container = container;
	}
	
	public ProcessDBDictionaryItemValue getItemValueToCopy()
	{
		return this.itemsValueToCopy;
	}
	
	public BeanItemContainer<ProcessDBDictionaryItemValue> getContainer()
	{
		return this.container;
	}

}
