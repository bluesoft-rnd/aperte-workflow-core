package org.aperteworkflow.editor.ui.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

/**
 * Definition of the permission, provides metadata to create the actual permission object
 */
public class PermissionDefinition implements Comparable<PermissionDefinition> {

    private String key;
    private String description;

    public PermissionDefinition() {
    }

    public PermissionDefinition(String key) {
        this.key = key;
    }

    public PermissionDefinition(String key, String description) {
        this.key = key;
        this.description = description;
    }
    
    public PermissionDefinition(AbstractPermission permission) {
        this.key = permission.getPrivilegeName();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionDefinition)) return false;

        PermissionDefinition that = (PermissionDefinition) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public int compareTo(PermissionDefinition o) {
        if (o == null) return 1;
        if (key == null) {
            if (o.key == null) {
                return 0;
            } else{
                return -1;
            }
        }
        return key.compareTo(o.key);
    }
}
