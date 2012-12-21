package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name = "pt_process_state_widget_prms")
public class ProcessStateWidgetPermission  extends AbstractPermission {

//    @XmlTransient
	@ManyToOne
	@JoinColumn(name = "widget_id")
	private ProcessStateWidget widget;

    @XmlTransient
	public ProcessStateWidget getWidget() {
		return widget;
	}

//    @XmlTransient
	public void setWidget(ProcessStateWidget widget) {
		this.widget = widget;
	}
}
