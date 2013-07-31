package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_widget_attr")
public class ProcessStateWidgetAttribute extends PersistentEntity 
{
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="widget_id")
	private ProcessStateWidget widget;
	
	private String name;
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String value;

	public ProcessStateWidgetAttribute() {
	}

	public ProcessStateWidgetAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

    @XmlTransient
	public ProcessStateWidget getWidget() {
		return widget;
	}

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
