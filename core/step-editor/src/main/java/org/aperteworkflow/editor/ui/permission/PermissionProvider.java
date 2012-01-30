package org.aperteworkflow.editor.ui.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import java.util.Collection;

public interface PermissionProvider {

    /**
     * Get all the permissions defined by the user
     * @return Permissions
     */
    Collection<AbstractPermission> getPermissions();

    /**
     * Get all the permission definitions allowed by the user
     * @return Permission definitions
     */
    Collection<PermissionDefinition> getPermissionDefinitions();

    /**
     * Can user create own {@link PermissionDefinition} regardless of what was returned by the provider.
     * This allows to create custom privilege name inside the permission set.
     * @return True if it's allowed.
     */
    boolean isNewPermissionDefinitionAllowed();

}
