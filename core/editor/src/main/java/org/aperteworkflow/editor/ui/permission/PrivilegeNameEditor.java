package org.aperteworkflow.editor.ui.permission;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.vaadin.DataHandler;
import org.aperteworkflow.util.liferay.LiferayBridge;
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
        setSpacing(true);
        this.permissionDefinition = permissionDefinition;
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        privilegeDescriptionLabel = new Label(getDescription(permissionDefinition));
        privilegeDescriptionLabel.setContentMode(Label.CONTENT_XHTML); // TODO don't use XHTML switch to style names

        roleNameDescriptionLabel = new Label(messages.getMessage("permission.editor.assigned.roles"));

        roleNameComboBox = new RoleNameComboBox();
        roleNameComboBox.setHandler(this);

        roleNameLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof PermissionWrapperBox) {
                    String basicCss = "float: left; margin: 3px; margin-bottom: 8px; padding: 3px; display: inline; font-weight: bold; border: 2px solid ";
                    return basicCss + "#287ece; -moz-border-radius: 5px; border-radius: 5px; padding-left: 6px; padding-right: 6px;";
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
            builder.append(I18NSource.ThreadUtil.getThreadI18nSource().getMessage(definition.getDescription()));
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
        Permission permission = new Permission();
        permission.setPrivilegeName(permissionWrapper.getPrivilegeName());
        permission.setRoleName(permissionWrapper.getRoleName());
        provider.addPermission(permission);
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
        Permission permission = new Permission();
        permission.setPrivilegeName(permissionWrapper.getPrivilegeName());
        permission.setRoleName(permissionWrapper.getRoleName());
        provider.removePermission(permission);
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
        roleNameComboBox.addItem(".*");
		for (String roleName : LiferayBridge.getRegularRoleNames()) {
			roleNameComboBox.addItem(roleName);
		}
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
