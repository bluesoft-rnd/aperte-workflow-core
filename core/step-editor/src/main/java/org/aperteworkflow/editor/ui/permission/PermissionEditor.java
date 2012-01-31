package org.aperteworkflow.editor.ui.permission;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Permission;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.*;

public class PermissionEditor extends VerticalLayout implements DataHandler {

    private PermissionProvider provider;
    private List<PrivilegeNameEditor> privilegeNameEditors;
    
    private TextField newPrivilegeNameField;
    private Button newPrivilegeNameButton;
    private HorizontalLayout newPrivilegeNameLayout;
    private Label descriptionLabel;

    public PermissionEditor() {
        privilegeNameEditors = new ArrayList<PrivilegeNameEditor>();
        initComponent();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
        
        descriptionLabel = new Label(messages.getMessage("permission.editor.description"));
    
        newPrivilegeNameField = new TextField();
        newPrivilegeNameField.setNullRepresentation("");
        
        newPrivilegeNameButton = VaadinUtility.button(
                messages.getMessage("permission.editor.new.privilege"),
                new Runnable() {
                    @Override
                    public void run() {
                        String privilegeName = (String) newPrivilegeNameField.getValue();
                        PermissionDefinition definition = new PermissionDefinition(privilegeName);
                        if (getUsedPermissionDefinition().contains(definition)) {
                            // this definition is already used
                            return;
                        }

                        newPrivilegeNameField.setValue(null);
                        addNewPrivilegeEditor(definition);
                    }
                }
        );
        
        newPrivilegeNameLayout = new HorizontalLayout();
        newPrivilegeNameLayout.setSpacing(true);
        newPrivilegeNameLayout.addComponent(newPrivilegeNameField);
        newPrivilegeNameLayout.addComponent(newPrivilegeNameButton);

        setSpacing(true);
    }

    public PermissionProvider getProvider() {
        return provider;
    }

    public void setProvider(PermissionProvider provider) {
        this.provider = provider;
    }

    @Override
    public void loadData() {
        removeAllComponents();
        privilegeNameEditors.clear();

        addComponent(descriptionLabel);
        if (provider.isNewPermissionDefinitionAllowed()) {
            addComponent(newPrivilegeNameLayout);
        }

        for (PermissionDefinition definition : getUniqueProvidedPermissionDefinitions()) {
            addNewPrivilegeEditor(definition);
        }
    }

    private void addNewPrivilegeEditor(PermissionDefinition definition) {
        PrivilegeNameEditor editor = new PrivilegeNameEditor(definition);
        editor.setProvider(new PrivilegeNamePermissionProvider(definition.getKey(), provider));
        editor.loadData();
        addComponent(editor);
        privilegeNameEditors.add(editor);
    }

    private List<PermissionDefinition> getUsedPermissionDefinition() {
        List<PermissionDefinition> list = new ArrayList<PermissionDefinition>();

        Iterator<Component> it = getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof PrivilegeNameEditor) {
                PrivilegeNameEditor editor = (PrivilegeNameEditor) c;
                list.add(editor.getPermissionDefinition());
            }
        }

        Collections.sort(list);
        return list;
    }
    
    private Set<PermissionDefinition> getUniqueProvidedPermissionDefinitions() {
        // use set to get unique permission definitions
        Set<PermissionDefinition> set = new TreeSet<PermissionDefinition>();
        if (provider.getPermissions() != null) {
            for (Permission permission : provider.getPermissions()) {
                set.add(new PermissionDefinition(permission));
            }
        }

        if (provider.getPermissionDefinitions() != null) {
            set.addAll(provider.getPermissionDefinitions());
        }


        return set;
    }

    @Override
    public void saveData() {

    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public List<Permission> getPermissions() {
        List<Permission> list = new ArrayList<Permission>();
        for (PrivilegeNameEditor privilegeNameEditor : privilegeNameEditors) {
            list.addAll(privilegeNameEditor.getPermissions());
        }
        return list;
    }
}
