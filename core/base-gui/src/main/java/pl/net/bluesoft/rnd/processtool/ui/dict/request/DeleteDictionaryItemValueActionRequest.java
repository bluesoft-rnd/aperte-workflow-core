package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import com.vaadin.data.util.BeanItemContainer;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.DictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: delete selected item
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DeleteDictionaryItemValueActionRequest implements IActionRequest 
{
	private DictionaryModelView.ProcessDBDictionaryItemValueWrapper itemToDelete;
	private BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> container;

	public DeleteDictionaryItemValueActionRequest(DictionaryModelView.ProcessDBDictionaryItemValueWrapper itemToDelete,
												  BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> container)
	{
		this.itemToDelete = itemToDelete;
		this.container = container;
	}
	
	public DictionaryModelView.ProcessDBDictionaryItemValueWrapper getItemValueToDelete()
	{
		return this.itemToDelete;
	}
	
	public BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> getContainer()
	{
		return this.container;
	}

}
