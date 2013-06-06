package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_action_attr")
public class ProcessStateActionAttribute extends PersistentEntity {


	private static final long serialVersionUID = -4169704547904662323L;

	@ManyToOne
	@JoinColumn(name="action_id")
	private ProcessStateAction action;
	
	private String name;
	
    @Lob
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
