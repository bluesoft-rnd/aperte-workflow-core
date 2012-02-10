package org.aperteworkflow.editor.stepeditor.user;

import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WidgetItemInStep implements Serializable {
	private static final long serialVersionUID = -4319558996871289010L;
    private static final Logger logger = Logger.getLogger(WidgetItemInStep.class.getName());

	private WidgetItem widgetItem;
    private PropertiesPanel widgetPropertiesPanel;
	private List<Property<?>> properties = new LinkedList<Property<?>>();
	private List<Permission> permissions = new LinkedList<Permission>();
	private List<PermissionDefinition> permissionDefinitions = new LinkedList<PermissionDefinition>();

	public WidgetItemInStep(WidgetItem widgetItem, PropertiesPanel widgetPropertiesPanel, List<Property<?>> properties, List<PermissionDefinition> permissionDefinitions) {
		this.widgetItem = widgetItem;
		this.properties = properties;
		this.permissionDefinitions = permissionDefinitions;
        this.widgetPropertiesPanel = widgetPropertiesPanel;
	}

	public WidgetItemInStep(WidgetItem widgetItem) {
		this.widgetItem = widgetItem;
		if (widgetItem.getProperties() != null) {
			for (Property<?> property : widgetItem.getProperties()) {
				try {
					properties.add((Property<?>) property.clone());
				} catch (CloneNotSupportedException e) {
					// should never happen
					// if happens, we just log the exception and skip property
					// that failed
					logger.log(Level.SEVERE, "Clone not supported for property", e);
				}
			}
		}

		if (widgetItem.getPermissions() != null) {
			for (PermissionDefinition perm : widgetItem.getPermissions()) {
                permissionDefinitions.add(perm);
			}
		}

        widgetPropertiesPanel = new PropertiesPanel();
        widgetPropertiesPanel.init(widgetItem.getClassInfo());
        widgetPropertiesPanel.refreshForm(false, properties);
	}

	public WidgetItem getWidgetItem() {
		return widgetItem;
	}

	public void setWidgetItem(WidgetItem widgetItem) {
		this.widgetItem = widgetItem;
	}

	public List<Property<?>> getProperties() {
		return properties;
	}

	public void setProperties(List<Property<?>> properties) {
		this.properties = properties;
	}

	public boolean hasProperties() {
		return properties != null && properties.size() > 0;
	}

    public List<PermissionDefinition> getPermissionDefinitions() {
        return permissionDefinitions;
    }

    public void setPermissionDefinitions(List<PermissionDefinition> permissionDefinitions) {
        this.permissionDefinitions = permissionDefinitions;
    }

    public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	public boolean hasPermissions() {
		return permissionDefinitions != null && !permissionDefinitions.isEmpty();
	}

    public PropertiesPanel getWidgetPropertiesPanel() {
        return widgetPropertiesPanel;
    }

    public void setWidgetPropertiesPanel(PropertiesPanel widgetPropertiesPanel) {
        this.widgetPropertiesPanel = widgetPropertiesPanel;
    }
}
