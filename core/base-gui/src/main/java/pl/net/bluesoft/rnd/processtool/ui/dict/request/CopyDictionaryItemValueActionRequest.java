package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import com.vaadin.data.util.BeanItemContainer;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.DictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: copy selected item's value
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class CopyDictionaryItemValueActionRequest implements IActionRequest 
{
	private DictionaryModelView.ProcessDBDictionaryItemValueWrapper itemsValueToCopy;
	private BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> container;

	public CopyDictionaryItemValueActionRequest(DictionaryModelView.ProcessDBDictionaryItemValueWrapper itemToDelete,
												BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> container)
	{
		this.itemsValueToCopy = itemToDelete;
		this.container = container;
	}
	
	public DictionaryModelView.ProcessDBDictionaryItemValueWrapper getItemValueToCopy()
	{
		return this.itemsValueToCopy;
	}
	
	public BeanItemContainer<DictionaryModelView.ProcessDBDictionaryItemValueWrapper> getContainer()
	{
		return this.container;
	}

}
