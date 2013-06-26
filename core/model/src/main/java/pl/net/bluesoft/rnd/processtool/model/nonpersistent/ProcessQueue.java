package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.io.Serializable;
import java.util.Collection;

/**
 * Non-persistent Process Queue data
 *
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessQueue {
	String getName();
	boolean isBrowsable();
	int getProcessCount();
	String getDescription();
	boolean getUserAdded();
}
