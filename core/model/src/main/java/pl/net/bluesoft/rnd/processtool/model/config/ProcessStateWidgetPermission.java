package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name = "pt_process_state_widget_prms")
public class ProcessStateWidgetPermission  extends PersistentEntity implements IPermission {


	@ManyToOne
	@JoinColumn(name = "widget_id")
	private ProcessStateWidget widget;

    @XmlTransient
	public ProcessStateWidget getWidget() {
		return widget;
	}


	public void setWidget(ProcessStateWidget widget) {
		this.widget = widget;
	}
	
	private String roleName;
	private String privilegeName;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPrivilegeName() {
		return privilegeName;
	}

	public void setPrivilegeName(String privilegeName) {
		this.privilegeName = privilegeName;
	}
}
