package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

public class PermissionWrapper extends AbstractPermission {

    // TODO change for Enum with custom annotation to simplify usage in Vaadin
    public static String PROPERTY_PRIVILEDGE_NAME = "priviledgeName";
    public static String PROPERTY_PRIVILEDGE_NAME_EDITABLE = "priviledgeNameEditable";
    public static String PROPERTY_ROLE_NAME = "roleName";

    private boolean priviledgeNameEditable;

    public PermissionWrapper() {
    }

    public PermissionWrapper(PermissionDefinition permissionDefinition) {
        if (permissionDefinition != null) {
            setPriviledgeName(permissionDefinition.getKey());
        }
    }

    public PermissionWrapper(AbstractPermission abstractPermission) {
        setPriviledgeName(abstractPermission.getPriviledgeName());
        setRoleName(abstractPermission.getRoleName());
    }

    public boolean isPriviledgeNameEditable() {
        return priviledgeNameEditable;
    }

    public void setPriviledgeNameEditable(boolean priviledgeNameEditable) {
        this.priviledgeNameEditable = priviledgeNameEditable;
    }




}
