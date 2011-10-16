package pl.net.bluesoft.rnd.processtool.model;

/**
 * Interface which allows an object to propagate to BPM variables
 *
 * @author tlipski@bluesoft.net.pl
 */
public interface BpmVariable {

	String getBpmVariableName();
	Object getBpmVariableValue();
}
