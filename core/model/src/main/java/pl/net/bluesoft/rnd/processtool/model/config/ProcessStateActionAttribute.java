package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_action_attr")
public class ProcessStateActionAttribute extends PersistentEntity {
//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="action_id")
	private ProcessStateAction action;
	
	private String name;
    @Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String value;

	public ProcessStateActionAttribute() {
	}

	public ProcessStateActionAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

    @XmlTransient
	public ProcessStateAction getAction() {
		return action;
	}

//    @XmlTransient
	public void setAction(ProcessStateAction action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
