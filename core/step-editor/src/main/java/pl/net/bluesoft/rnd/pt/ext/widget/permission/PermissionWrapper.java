package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

public class PermissionWrapper extends AbstractPermission {

    private boolean privilagedNameEditable;

    public PermissionWrapper(AbstractPermission abstractPermission) {
        setPriviledgeName(abstractPermission.getPriviledgeName());
        setRoleName(abstractPermission.getRoleName());
    }

    public boolean isPrivilagedNameEditable() {
        return privilagedNameEditable;
    }

    public void setPrivilagedNameEditable(boolean privilagedNameEditable) {
        this.privilagedNameEditable = privilagedNameEditable;
    }


}
