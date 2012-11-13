package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;

import java.util.Collection;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolChildrenFilteringWidget {

	List<ProcessStateWidget> filterChildren(BpmTask task, List<ProcessStateWidget> sortedList);
	
}
 