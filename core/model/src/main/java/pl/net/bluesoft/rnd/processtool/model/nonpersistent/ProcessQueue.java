package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

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
