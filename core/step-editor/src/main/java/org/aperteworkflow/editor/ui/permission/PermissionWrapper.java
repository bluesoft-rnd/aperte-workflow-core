package org.aperteworkflow.editor.ui.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

public class PermissionWrapper extends AbstractPermission {

    // Properties
    public static String PROPERTY_PRIVILEDGE_NAME = "priviledgeName";
    public static String PROPERTY_ROLE_NAME = "roleName";
    public static String PROPERTY_SHORT_NAME = "shortName";

    /**
     * This field is used to display the short, readable format of the bean.
     */
    private String shortName;

    public PermissionWrapper() {
        setShortName("undefined");
    }

    public PermissionWrapper(PermissionDefinition permissionDefinition) {
        this();
        if (permissionDefinition != null) {
            setPrivilegeName(permissionDefinition.getKey());
        }
    }

    public PermissionWrapper(AbstractPermission abstractPermission) {
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

    public AbstractPermission toAbstractPermission() {
        AbstractPermission perm = new AbstractPermission();
        perm.setPrivilegeName(getPrivilegeName());
        perm.setRoleName(getRoleName());
        return perm;
    }

}
