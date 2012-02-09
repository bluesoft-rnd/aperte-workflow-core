package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolActionCallback {
	boolean saveProcessData();
	void performAction(ProcessStateAction a);
}
