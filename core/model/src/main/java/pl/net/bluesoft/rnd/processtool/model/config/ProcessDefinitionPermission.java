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
@Table(name="pt_process_def_prms")
public class ProcessDefinitionPermission extends PersistentEntity implements IPermission
{
	

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

    @XmlTransient
    public ProcessDefinitionConfig getDefinition() {
        return definition;
    }


    public void setDefinition(ProcessDefinitionConfig definition) {
        this.definition = definition;
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
