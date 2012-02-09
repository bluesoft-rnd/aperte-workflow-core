package org.aperteworkflow.editor.domain;

import java.io.Serializable;

/**
 * Permission equivalent to pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission
 * however it does not refer this class which allows to clean separation of domain objects for
 * signavio code
 */
public class Permission implements Serializable {

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

    @Override
    public String toString() {
        return "Permission{" +
                "roleName='" + roleName + '\'' +
                ", privilegeName='" + privilegeName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (privilegeName != null ? !privilegeName.equals(that.privilegeName) : that.privilegeName != null)
            return false;
        if (roleName != null ? !roleName.equals(that.roleName) : that.roleName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName != null ? roleName.hashCode() : 0;
        result = 31 * result + (privilegeName != null ? privilegeName.hashCode() : 0);
        return result;
    }
}
