package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolDataWidget {
	Collection<String> validateData(BpmTask task, boolean skipRequired);
	void saveData(BpmTask task);
	void loadData(BpmTask task);
}
