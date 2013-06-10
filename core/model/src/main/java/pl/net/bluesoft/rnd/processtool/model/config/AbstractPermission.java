package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_permission")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractPermission extends PersistentEntity {

	private static final long serialVersionUID = 1L;
	
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
