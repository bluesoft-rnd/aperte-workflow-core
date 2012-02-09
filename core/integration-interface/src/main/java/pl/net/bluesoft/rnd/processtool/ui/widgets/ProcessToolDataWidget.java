package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolDataWidget {
	Collection<String> validateData(ProcessInstance processInstance);
	void saveData(ProcessInstance processInstance);
	void loadData(ProcessInstance processInstance);

}
