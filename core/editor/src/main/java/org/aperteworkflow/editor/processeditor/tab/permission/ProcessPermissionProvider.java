package org.aperteworkflow.editor.processeditor.tab.permission;

import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.permission.PermissionProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProcessPermissionProvider implements PermissionProvider {

    private List<PermissionDefinition> definitions;
    private List<Permission> permissions;

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Collection<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<PermissionDefinition> getPermissionDefinitions() {
        if (definitions == null) {
            I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

            definitions = new ArrayList<PermissionDefinition>();
            definitions.add(new PermissionDefinition("RUN", messages.getMessage("process.permission.RUN")));
            definitions.add(new PermissionDefinition("SEARCH", messages.getMessage("process.permission.SEARCH")));
        }
        return definitions;
    }

    @Override
    public boolean isNewPermissionDefinitionAllowed() {
        return false;
    }

    @Override
    public void addPermission(Permission permission) {
        //TODO
    }

    @Override
    public void removePermission(Permission permission) {
        //TODO
    }
}
