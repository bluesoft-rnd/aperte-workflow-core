package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolGuiCallback {
	void callback(ProcessToolContext ctx, ProcessToolBpmSession session);
}
