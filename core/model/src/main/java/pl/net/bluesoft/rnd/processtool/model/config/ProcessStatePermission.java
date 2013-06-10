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
@Table(name = "pt_process_state_prms")
public class ProcessStatePermission extends PersistentEntity implements IPermission {

	private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private ProcessStateConfiguration config;

    @XmlTransient
    public ProcessStateConfiguration getConfig() {
        return config;
    }


    public void setConfig(ProcessStateConfiguration config) {
        this.config = config;
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
