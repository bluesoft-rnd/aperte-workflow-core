package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.*;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_widget_attr")
public class ProcessStateWidgetAttribute extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_STATE_WIDGET_ATTR")
			}
	)
	@Column(name = "id")
	protected Long id;

	//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="widget_id")
	private ProcessStateWidget widget;
	
	private String name;
    @Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String value;

	public ProcessStateWidgetAttribute() {
	}

	public ProcessStateWidgetAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlTransient
	public ProcessStateWidget getWidget() {
		return widget;
	}

//    @XmlTransient
	public void setWidget(ProcessStateWidget widget) {
		this.widget = widget;
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
