package org.aperteworkflow.editor.ui.permission;

import org.aperteworkflow.editor.domain.Permission;
import pl.net.bluesoft.rnd.processtool.model.config.IPermission;

public class PermissionWrapper implements IPermission {

    // Properties
    public static String PROPERTY_PRIVILEDGE_NAME = "priviledgeName";
    public static String PROPERTY_ROLE_NAME = "roleName";
    public static String PROPERTY_SHORT_NAME = "shortName";

    /**
     * This field is used to display the short, readable format of the bean.
     */
    private String shortName;
    private String roleName;
    private String privilegeName;

    public PermissionWrapper() {
        setShortName("undefined");
    }

    public PermissionWrapper(PermissionDefinition permissionDefinition) {
        this();
        if (permissionDefinition != null) {
            setPrivilegeName(permissionDefinition.getKey());
        }
    }



	public PermissionWrapper(Permission permission) {
        this();
        if (permission != null) {
            setPrivilegeName(permission.getPrivilegeName());
            setRoleName(permission.getRoleName());
            setShortName(getShortNameFromAttributes());
        }
    }
    


	public PermissionWrapper(IPermission abstractPermission) {
        this();
        if (abstractPermission != null) {
            setPrivilegeName(abstractPermission.getPrivilegeName());
            setRoleName(abstractPermission.getRoleName());
            setShortName(getShortNameFromAttributes());
        }
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortNameFromAttributes() {
        if (getPrivilegeName() == null || getPrivilegeName().trim().isEmpty()) {
            return "undefined";
        } else if (getRoleName() != null && !getRoleName().trim().isEmpty()) {
            return getPrivilegeName() + " : " + getRoleName();
        } else {
            return getPrivilegeName();
        }
    }
    
    public Permission toPermission() {
        Permission perm = new Permission();
        perm.setPrivilegeName(getPrivilegeName());
        perm.setRoleName(getRoleName());
        return perm;
    }

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
