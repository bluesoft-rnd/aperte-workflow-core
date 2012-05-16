package pl.net.bluesoft.rnd.pt.ext.userdata.model;

import pl.net.bluesoft.rnd.processtool.model.BpmVariable;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;

import javax.persistence.Entity;
import javax.persistence.Table;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_ext_process_instance_usr_as")
public class ProcessInstanceUserAssignment extends ProcessInstanceAttribute implements BpmVariable {

	private String userLogin;
	private String bpmLogin;
	private String userDescription;
	private String role;

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserDescription() {
		return userDescription;
	}

	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getBpmVariableName() {
		return role;
	}

	@Override
	public Object getBpmVariableValue() {
		return bpmLogin;
	}

	public String getBpmLogin() {
		return bpmLogin;
	}

	public void setBpmLogin(String bpmLogin) {
		this.bpmLogin = bpmLogin;
	}

    @Override
    public String toString() {
        return nvl(bpmLogin, userLogin);
    }
}
