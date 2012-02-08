package org.aperteworkflow.editor.ui.queue;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Select;
import org.aperteworkflow.editor.domain.QueueRolePermission;

//TODO merge with RoleNameComboBox in ui.permissions

public class RoleNameComboBox extends Select implements ComboBox.NewItemHandler {

    private QueueRolePermissionBoxHandler handler;

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

    public QueueRolePermissionBoxHandler getHandler() {
        return handler;
    }

    public void setHandler(QueueRolePermissionBoxHandler handler) {
        this.handler = handler;
    }

    @Override
    public void addNewItem(String newItemCaption) {
        addItem(newItemCaption);
        setValue(newItemCaption);
    }

    private void handleRoleChange(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return;
        }

        QueueRolePermission permission = new QueueRolePermission();
        permission.setRoleName(roleName);

        QueueRolePermissionBox box = new QueueRolePermissionBox(permission, handler);
        handler.addQueueRolePermissionBox(box);
    }
}
