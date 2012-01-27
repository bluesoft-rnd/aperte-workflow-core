package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

public class PermissionWrapper extends AbstractPermission {

    private boolean priviledgeNameEditable;

    public PermissionWrapper() {
    }

    public PermissionWrapper(PermissionDefinition permissionDefinition) {
        setPriviledgeName(permissionDefinition.getKey());
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
