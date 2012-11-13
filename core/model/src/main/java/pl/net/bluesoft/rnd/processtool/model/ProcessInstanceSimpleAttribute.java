package pl.net.bluesoft.rnd.processtool.model;

import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.Searchable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Collection;

/**
 * Simple attribute with String value.
 * 
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance_s_attr")
public class ProcessInstanceSimpleAttribute extends ProcessInstanceAttribute implements BpmVariable, Searchable {

    @Column(name="value_")
	private String value;

	private String bpmVariableName;

    public ProcessInstanceSimpleAttribute() {
    }

    public ProcessInstanceSimpleAttribute(String key, String value) {
        setKey(key);
        setValue(value);
        setBpmVariableName(key);
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

    @Override
    public Collection<ProcessInstanceSearchAttribute> getAttributes() {
        return Arrays.asList(
                new ProcessInstanceSearchAttribute(getKey(), value, false),
                new ProcessInstanceSearchAttribute(getBpmVariableName(), value, false)
        );
    }
}
