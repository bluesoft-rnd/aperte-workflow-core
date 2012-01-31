package org.aperteworkflow.editor.domain;

import java.io.Serializable;

/**
 * Permission equivalent to {@link pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission}
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

}
