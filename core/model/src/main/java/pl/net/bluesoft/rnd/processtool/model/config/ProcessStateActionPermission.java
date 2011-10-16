package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_act_prms")
public class ProcessStateActionPermission extends AbstractPermission {

	@ManyToOne
	@JoinColumn(name="action_id")
	private ProcessStateAction action;

	public ProcessStateAction getAction() {
		return action;
	}

	public void setAction(ProcessStateAction action) {
		this.action = action;
	}
}
