package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: POlszewski
 * Date: 2011-11-25
 * Time: 14:21:26
 */
@Entity
@Table(name="pt_sequence")
public class ProcessToolSequence extends PersistentEntity {
	private String processDefinitionName;
	private String name;
	private long value;

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
