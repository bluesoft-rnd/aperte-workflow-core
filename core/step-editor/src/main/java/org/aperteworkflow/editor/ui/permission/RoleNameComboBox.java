package org.aperteworkflow.editor.ui.permission;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Select;

public class RoleNameComboBox extends Select implements ComboBox.NewItemHandler {

    private PermissionWrapperHandler handler;

    public RoleNameComboBox() {
        setImmediate(true);
        setNewItemHandler(this);
        setNewItemsAllowed(true);

        addListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (handler == null) {
                    return;
                }

                String roleName = (String) RoleNameComboBox.this.getValue();
                handleRoleChange(roleName);
            }
        });
    }

    @Override
    public void addNewItem(String newItemCaption) {
        addItem(newItemCaption);
        setValue(newItemCaption);
    }

    public void setHandler(PermissionWrapperHandler handler) {
        this.handler = handler;
    }
    
    private void handleRoleChange(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return;
        }

        PermissionWrapper wrapper = new PermissionWrapper();
        wrapper.setRoleName(roleName);
        handler.addPermissionWrapper(wrapper);
    }
}
