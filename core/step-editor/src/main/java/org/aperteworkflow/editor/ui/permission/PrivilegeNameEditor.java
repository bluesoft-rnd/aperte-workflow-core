package org.aperteworkflow.editor.ui.permission;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Permission;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Component used to edit role names inside single privilege name
 */
public class PrivilegeNameEditor extends GridLayout implements PermissionWrapperHandler, DataHandler {

    private PermissionDefinition permissionDefinition;
    private PermissionProvider provider;

    private Label privilegeDescriptionLabel;
    private Label roleNameDescriptionLabel;
    private RoleNameComboBox roleNameComboBox;
    private Layout roleNameLayout;

    public PrivilegeNameEditor(PermissionDefinition permissionDefinition) {
        super(2, 3);
        this.permissionDefinition = permissionDefinition;
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        privilegeDescriptionLabel = new Label(getDescription(permissionDefinition));
        privilegeDescriptionLabel.setContentMode(Label.CONTENT_XHTML);

        roleNameDescriptionLabel = new Label(messages.getMessage("permission.editor.assigned.roles"));

        roleNameComboBox = new RoleNameComboBox();
        roleNameComboBox.setHandler(this);

        roleNameLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof PermissionWrapperBox) {
                    String basicCss = "float: left; margin: 3px; padding: 3px;  display: inline; font-weight: bold; border: 2px solid ";
                    return basicCss + "#287ece;";
                }

                return super.getCss(c);
            }
        };
        roleNameLayout.setWidth("100%");
    }

    private String getDescription(PermissionDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h2>");
        builder.append(definition.getKey());
        builder.append("</h2>");
        if (definition.getDescription() != null && !definition.getDescription().trim().isEmpty()) {
            builder.append("<i>");
            builder.append(definition.getDescription());
            builder.append("</i>");
        }
        return builder.toString();
    }
    
    private void initLayout() {
        setWidth("100%");
        setSpacing(true);

        addComponent(privilegeDescriptionLabel, 0, 0);
        addComponent(roleNameComboBox, 1, 0);
        addComponent(roleNameDescriptionLabel, 0, 1, 1, 1);
        addComponent(roleNameLayout, 0, 2, 1, 2);

        setComponentAlignment(privilegeDescriptionLabel, Alignment.MIDDLE_LEFT);
        setComponentAlignment(roleNameComboBox, Alignment.BOTTOM_RIGHT);
        
        setColumnExpandRatio(0, 1);
        setColumnExpandRatio(1, 0);
    }
    
    @Override
    public void addPermissionWrapper(PermissionWrapper permissionWrapper) {
        // ensure the privilege name
        permissionWrapper.setPrivilegeName(permissionDefinition.getKey());

        PermissionWrapperBox box = getPermissionWrapperBoxByRoleName(permissionWrapper.getRoleName());
        if (box == null) {
            box = new PermissionWrapperBox(permissionWrapper, this);
            roleNameLayout.addComponent(box);
        }

        if (roleNameComboBox.containsId(permissionWrapper.getRoleName())) {
            roleNameComboBox.removeItem(permissionWrapper.getRoleName());
        }
    }

    @Override
    public boolean removePermissionWrapper(PermissionWrapper permissionWrapper) {
        PermissionWrapperBox box = getPermissionWrapperBoxByRoleName(permissionWrapper.getRoleName());
        if (box == null) {
            // Nothing to remove
            return false;
        }

        roleNameLayout.removeComponent(box);
        roleNameLayout.requestRepaint();
        roleNameComboBox.addItem(permissionWrapper.getRoleName());
        return true;
    }
    
    private PermissionWrapperBox getPermissionWrapperBoxByRoleName(String roleName) {
        Iterator<Component> it = roleNameLayout.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if ((c instanceof PermissionWrapperBox)) {
                PermissionWrapperBox box = (PermissionWrapperBox) c;
                if (roleName.equals(box.getPermissionWrapper().getRoleName())) {
                    return box;
                }
            }
        }
        return null;
    }

    @Override
    public void loadData() {
        roleNameComboBox.removeAllItems();
        // TODO get roles from liferay

        roleNameLayout.removeAllComponents();
        if (provider.getPermissions() != null) {
            for (Permission permission : provider.getPermissions()) {
                addPermissionWrapper(new PermissionWrapper(permission));
            }
        }
    }

    @Override
    public void saveData() {

    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public PermissionProvider getProvider() {
        return provider;
    }

    public void setProvider(PermissionProvider provider) {
        this.provider = provider;
    }

    public PermissionDefinition getPermissionDefinition() {
        return permissionDefinition;
    }

    public List<Permission> getPermissions() {
        List<Permission> list = new ArrayList<Permission>();

        Iterator<Component> it = roleNameLayout.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if ((c instanceof PermissionWrapperBox)) {
                PermissionWrapperBox box = (PermissionWrapperBox) c;
                list.add(box.getPermissionWrapper().toPermission());
            }
        }

        return list;
    }

}
