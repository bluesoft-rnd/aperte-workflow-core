package pl.net.bluesoft.rnd.processtool.ui.dict.request;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;

/**
 * Action: cancel edition of selected dictionary item
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class CancelEditionOfDictionaryItemActionRequest implements IActionRequest 
{
	private ProcessDBDictionaryItem dicardChangesToItem;

	public CancelEditionOfDictionaryItemActionRequest(ProcessDBDictionaryItem itemToDelete) 
	{
		this.dicardChangesToItem = itemToDelete;
	}
	
	public ProcessDBDictionaryItem getItemToRollback()
	{
		return this.dicardChangesToItem;
	}

}
