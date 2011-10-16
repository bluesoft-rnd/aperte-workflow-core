package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name = "pt_process_state_widget_prms")
public class ProcessStateWidgetPermission  extends AbstractPermission {

	@ManyToOne
	@JoinColumn(name = "widget_id")
	private ProcessStateWidget widget;

	public ProcessStateWidget getWidget() {
		return widget;
	}

	public void setWidget(ProcessStateWidget widget) {
		this.widget = widget;
	}
}
