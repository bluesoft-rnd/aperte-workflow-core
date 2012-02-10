package org.aperteworkflow.editor.stepeditor.user;


import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import org.aperteworkflow.editor.ui.permission.PermissionProvider;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class WidgetFormWindow extends Panel  {

    private WidgetItemInStep widget;

    // TODO this class and method needs a a major rework
	public void loadWidget(final WidgetItemInStep widget, boolean reloadProperties) {
        this.widget = widget;
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		removeAllComponents();
        setStyleName(Reindeer.PANEL_LIGHT);
		if (widget == null) {
		    setCaption("");
		    return;
        }
        VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(new Label(widget.getWidgetItem().getDescription()));        
		if ((widget.getProperties() == null || widget.getProperties().size() == 0) && (widget.getPermissions() == null || widget.getPermissions().size() == 0)) {
			layout.addComponent(new Label(messages.getMessage("form.no.parameters.defined")));
		} else {
            TabSheet ts = new TabSheet();
            ts.setWidth("100%");
            if (widget.hasProperties()) {
                if (reloadProperties) {
                    PropertiesPanel propertiesPanel = new PropertiesPanel();
                    propertiesPanel.init(widget.getWidgetItem().getClassInfo());
                    propertiesPanel.refreshForm(false, widget.getProperties());
                    widget.setWidgetPropertiesPanel(propertiesPanel);
                }
                ts.addTab(widget.getWidgetPropertiesPanel(), messages.getMessage("form.properties"));
            }
            if (widget.hasPermissions()) {
                PermissionEditor permissionEditor = new PermissionEditor();
                permissionEditor.setMargin(true);
                permissionEditor.setProvider(new PermissionProvider() {
                    @Override
                    public Collection<Permission> getPermissions() {
                        return new LinkedHashSet<Permission>(widget.getPermissions());
                    }

                    @Override
                    public Collection<PermissionDefinition> getPermissionDefinitions() {
                        return widget.getPermissionDefinitions();
                    }

                    @Override
                    public boolean isNewPermissionDefinitionAllowed() {
                        return false;
                    }

                    @Override
                    public void addPermission(Permission permission) {
                        Set<Permission> newPermissions = new LinkedHashSet<Permission>(widget.getPermissions());
                        newPermissions.add(permission);
                        widget.setPermissions(new ArrayList<Permission>(newPermissions));
                    }

                    @Override
                    public void removePermission(Permission permission) {
                        Set<Permission> newPermissions = new LinkedHashSet<Permission>(widget.getPermissions());
                        newPermissions.remove(permission);
                        widget.setPermissions(new ArrayList<Permission>(newPermissions));
                    }
                });
                permissionEditor.loadData();
                ts.addTab(permissionEditor, messages.getMessage("form.permissions"));

            }
            layout.addComponent(ts);
		}
	}

    public WidgetItemInStep getWidget() {
        return widget;
    }
}
