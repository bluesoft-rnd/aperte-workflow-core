package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Simple attribute with String value.
 * 
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance_s_attr")
public class ProcessInstanceSimpleAttribute extends ProcessInstanceAttribute implements BpmVariable{

	private String value;

	private String bpmVariableName;

    public ProcessInstanceSimpleAttribute() {
    }

    public ProcessInstanceSimpleAttribute(String key, String value) {
        setKey(key);
        setValue(value);
    }

	@Override
	public String getBpmVariableName() {
		return bpmVariableName != null ? bpmVariableName : getKey();
	}

	@Override
	public Object getBpmVariableValue() {
		return value;
	}

	public void setBpmVariableName(String bpmVariableName) {
		this.bpmVariableName = bpmVariableName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    @Override
    public String toString() {
        return value;
    }
}
