package pl.net.bluesoft.rnd.pt.ext.processeditor.domain;

import java.io.Serializable;

/**
 * Role and it's permission for queue
 */
public class QueueRolePermission implements Serializable {

    private String roleName;
    private Boolean browsingAllowed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueueRolePermission that = (QueueRolePermission) o;

        if (roleName != null ? !roleName.equals(that.roleName) : that.roleName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return roleName != null ? roleName.hashCode() : 0;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Boolean isBrowsingAllowed() {
        return browsingAllowed;
    }

    public void setBrowsingAllowed(Boolean browsingAllowed) {
        this.browsingAllowed = browsingAllowed;
    }
}
