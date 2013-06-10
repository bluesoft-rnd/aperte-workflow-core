package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_act_prms")
public class ProcessStateActionPermission extends PersistentEntity implements IPermission{


	private static final long serialVersionUID = -1416824019270643292L;
	
	@ManyToOne
	@JoinColumn(name="action_id")
	private ProcessStateAction action;

    @XmlTransient
	public ProcessStateAction getAction() {
		return action;
	}


	public void setAction(ProcessStateAction action) {
		this.action = action;
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
