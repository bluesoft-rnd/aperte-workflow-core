package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission;

import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionDefinition;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionPanel;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionProvider;

import java.util.Collection;

public class ProcessPermissionTab extends VerticalLayout implements PermissionProvider {

    private PermissionPanel permissionPanel;

    @Override
    public Collection<AbstractPermission> getPermissions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<PermissionDefinition> getPermissionDefinitions() {
        return null;
    }

    @Override
    public boolean isNewDefinitionAllowed() {
        return true;
    }
}
