package org.aperteworkflow.editor.ui.permission;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.*;

public class PermissionEditor extends VerticalLayout implements DataHandler {

    private PermissionProvider provider;

    private TextField newPrivilegeNameField;
    private Button newPrivilegeNameButton;
    private HorizontalLayout newPrivilegeNameLayout;

    private Label descriptionLabel;

    public PermissionEditor() {
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

        addComponent(descriptionLabel);

        if (provider.isNewPermissionDefinitionAllowed()) {
            addComponent(newPrivilegeNameLayout);
        }

        List<PermissionDefinition> definitions = getProvidedPermissionDefinitions();
        for (PermissionDefinition definition : definitions) {
            addNewPrivilegeEditor(definition);
        }
    }

    private void addNewPrivilegeEditor(PermissionDefinition definition) {
        PrivilegeNameEditor editor = new PrivilegeNameEditor(definition);
        editor.setProvider(new PrivilegeNamePermissionProvider(definition.getKey(), provider));
        editor.loadData();
        addComponent(editor);
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
    
    private List<PermissionDefinition> getProvidedPermissionDefinitions() {
        List<PermissionDefinition> list = new ArrayList<PermissionDefinition>();

        if (provider.getPermissions() != null) {
            for (AbstractPermission permission : provider.getPermissions()) {
                list.add(new PermissionDefinition(permission));
            }
        }

        if (provider.getPermissionDefinitions() != null) {
            list.addAll(provider.getPermissionDefinitions());
        }

        Collections.sort(list);
        return list;
    }

    @Override
    public void saveData() {

    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
