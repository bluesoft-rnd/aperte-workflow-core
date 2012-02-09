package org.aperteworkflow.editor.ui.queue;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.aperteworkflow.editor.domain.QueueRolePermission;

public class QueueRolePermissionBox extends GridLayout {

    private QueueRolePermission queueRolePermission;
    private QueueRolePermissionBoxHandler handler;

    private Label roleNameLabel;
    private CheckBox browsingAllowedCheckbox;
    private Button deleteButton;

    public QueueRolePermissionBox(QueueRolePermission queueRolePermission, QueueRolePermissionBoxHandler handler) {
        super(2, 2);
        this.handler = handler;
        this.queueRolePermission = queueRolePermission;
        initComponent();
        initLayout();
    }

    private void initComponent() {
        roleNameLabel = new Label(queueRolePermission.getRoleName());

        browsingAllowedCheckbox = new CheckBox();
        browsingAllowedCheckbox.setCaption("browsing allowed");
        browsingAllowedCheckbox.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (Boolean.TRUE.equals(queueRolePermission.isBrowsingAllowed())) {
                    queueRolePermission.setBrowsingAllowed(false);
                } else {
                    queueRolePermission.setBrowsingAllowed(true); // we handle null as well here
                }
            }
        });
        
        if (Boolean.TRUE.equals(queueRolePermission.isBrowsingAllowed())) {
            browsingAllowedCheckbox.setValue(true);
        }

        deleteButton = new Button();
        deleteButton.setCaption("  X  ");
        deleteButton.setStyleName(BaseTheme.BUTTON_LINK);
        deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.removeQueueRolePermissionBox(QueueRolePermissionBox.this);
            }
        });
    }

    private void initLayout() {
        setSpacing(true);

        addComponent(roleNameLabel, 0, 0);
        addComponent(browsingAllowedCheckbox, 0, 1);
        addComponent(deleteButton, 1, 0, 1, 1);

        setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
    }

    public QueueRolePermission getQueueRolePermission() {
        return queueRolePermission;
    }
}
