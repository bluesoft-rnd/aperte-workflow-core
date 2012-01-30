package org.aperteworkflow.editor.ui.permission;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

public class PermissionWrapperBox extends HorizontalLayout {

    private PermissionWrapperHandler handler;
    private PermissionWrapper permissionWrapper;

    private Label roleNameLabel;
    private Button deleteButton;

    public PermissionWrapperBox(PermissionWrapper permissionWrapper, PermissionWrapperHandler handler) {
        this.handler = handler;
        this.permissionWrapper = permissionWrapper;
        initComponent();
        initLayout();
    }

    public PermissionWrapper getPermissionWrapper() {
        return permissionWrapper;
    }

    private void initComponent() {
        roleNameLabel = new Label(permissionWrapper.getRoleName());

        deleteButton = new Button();
        deleteButton.setCaption("  X  ");
        deleteButton.setStyleName(BaseTheme.BUTTON_LINK);
        deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.removePermissionWrapper(permissionWrapper);
            }
        });
    }

    private void initLayout() {
        setSpacing(true);
        addComponent(roleNameLabel);
        addComponent(deleteButton);
    }

}
