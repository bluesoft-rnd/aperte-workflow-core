package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: save selected dictionary item
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SaveDictionaryItemActionRequest implements IActionRequest 
{
	private ProcessDBDictionaryItem itemToDelete;

	public SaveDictionaryItemActionRequest(ProcessDBDictionaryItem itemToDelete) 
	{
		this.itemToDelete = itemToDelete;
	}
	
	public ProcessDBDictionaryItem getItemToSave()
	{
		return this.itemToDelete;
	}

}
