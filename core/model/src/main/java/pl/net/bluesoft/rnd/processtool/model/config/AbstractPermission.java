package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_permission")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractPermission extends PersistentEntity {
	private String roleName;
	private String priviledgeName;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPriviledgeName() {
		return priviledgeName;
	}

	public void setPriviledgeName(String priviledgeName) {
		this.priviledgeName = priviledgeName;
	}

}
