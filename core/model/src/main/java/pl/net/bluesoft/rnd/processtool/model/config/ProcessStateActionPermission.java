package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_state_act_prms")
public class ProcessStateActionPermission extends AbstractPermission {

//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="action_id")
	private ProcessStateAction action;

    @XmlTransient
	public ProcessStateAction getAction() {
		return action;
	}

//    @XmlTransient
	public void setAction(ProcessStateAction action) {
		this.action = action;
	}
}
