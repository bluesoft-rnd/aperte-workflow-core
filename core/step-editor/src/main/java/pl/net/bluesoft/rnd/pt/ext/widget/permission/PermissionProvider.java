package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import java.util.Collection;

public interface PermissionProvider {

    Collection<AbstractPermission> getPermissions();
    
    Collection<PermissionDefinition> getPermissionDefinitions();

    boolean isNewDefinitionAllowed();

}
