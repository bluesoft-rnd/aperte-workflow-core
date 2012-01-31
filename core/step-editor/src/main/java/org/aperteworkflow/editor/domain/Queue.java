package org.aperteworkflow.editor.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Queue definition
 */
public class Queue implements Serializable {

    private String name;
    private String description;
    private List<QueueRolePermission> rolePermissions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Queue queue = (Queue) o;

        if (name != null ? !name.equals(queue.name) : queue.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QueueRolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<QueueRolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
