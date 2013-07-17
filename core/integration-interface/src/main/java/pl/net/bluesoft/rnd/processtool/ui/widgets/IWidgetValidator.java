package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;

/** 
 * Widget validation interface
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetValidator 
{
	/** Validate widget */
	Collection<String> validate(BpmTask task, Map<String, String> data);

}
