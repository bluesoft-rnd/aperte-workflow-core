package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission;

import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionDefinition;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionPanel;
import pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionProvider;

import java.util.ArrayList;
import java.util.Collection;

public class ProcessPermissionTab extends VerticalLayout implements PermissionProvider {

    private PermissionPanel permissionPanel;

    public ProcessPermissionTab() {
        initComponent();
    }

    private void initComponent() {
        permissionPanel = new PermissionPanel();
        permissionPanel.setPermissionProvider(this);
        permissionPanel.loadData();

        addComponent(permissionPanel);
    }

    @Override
    public Collection<AbstractPermission> getPermissions() {
        return null;
    }

    @Override
    public Collection<PermissionDefinition> getPermissionDefinitions() {
        Collection<PermissionDefinition> definitions = new ArrayList<PermissionDefinition>();

        PermissionDefinition pd = new PermissionDefinition();
        pd.setKey("EDIT");
        pd.setDescription("aaaa");

        definitions.add(pd);
        
        pd = new PermissionDefinition();
        pd.setKey("VIEW");

        definitions.add(pd);
        
        return definitions;
    }

    @Override
    public boolean isNewDefinitionAllowed() {
        return true;
    }
}
