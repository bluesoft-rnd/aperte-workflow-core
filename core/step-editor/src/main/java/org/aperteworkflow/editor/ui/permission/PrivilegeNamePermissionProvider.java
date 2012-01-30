package org.aperteworkflow.editor.ui.permission;


import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link PermissionProvider} that provides the permissions only for specific privilege name
 */
public class PrivilegeNamePermissionProvider implements PermissionProvider{
    
    private String privilegeName;
    private PermissionProvider permissionProvider;

    public PrivilegeNamePermissionProvider(String privilegeName, PermissionProvider permissionProvider) {
        this.privilegeName = privilegeName;
        this.permissionProvider = permissionProvider;
    }

    @Override
    public Collection<AbstractPermission> getPermissions() {
        if (permissionProvider.getPermissions() == null) {
            return null;
        }

        List<AbstractPermission> privilegeNamePermissions = new ArrayList<AbstractPermission>();
        for (AbstractPermission permission : permissionProvider.getPermissions()) {
            if (privilegeName.equals(permission.getPrivilegeName())) {
                privilegeNamePermissions.add(permission);
            }
        }

        return privilegeNamePermissions;
    }

    @Override
    public Collection<PermissionDefinition> getPermissionDefinitions() {
        if (permissionProvider.getPermissionDefinitions() == null) {
            return null;
        }

        List<PermissionDefinition> privilegeNameDefinitions = new ArrayList<PermissionDefinition>();
        for (PermissionDefinition definition : permissionProvider.getPermissionDefinitions()) {
            if (privilegeName.equals(definition.getKey())) {
                privilegeNameDefinitions.add(definition);
            }
        }

        return privilegeNameDefinitions;
    }

    @Override
    public boolean isNewPermissionDefinitionAllowed() {
        return permissionProvider.isNewPermissionDefinitionAllowed();
    }
}
