package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WidgetItem implements Serializable {
	private static final long								serialVersionUID	= -8907544816596058014L;
    private static Map<String, WidgetItem>					widgetSet			= new HashMap<String, WidgetItem>();

	private String											widgetId;
	private String											name;
	private String											description;
	private Collection<Property<?>>							properties;
	private Collection<PermissionDefinition>							permissions;
	private Boolean											childrenAllowed;
	private String											icon;
	private BundleItem										bundle;

	public WidgetItem(String widgetId, String name, String description, String icon,
                      Collection<Property<?>> properties,
                      Collection<PermissionDefinition> permissions,
                      Boolean childrenAllowed, BundleItem bundle) {
		super();
		this.widgetId = widgetId;
		this.name = name;
		this.description = description;
		this.properties = properties;
		this.permissions = permissions;
		this.childrenAllowed = childrenAllowed;
		this.icon = icon;
		this.bundle = bundle;
		storeInWidgetset();
	}

	private void storeInWidgetset() {
		widgetSet.put(widgetId, this);
	}

	public static WidgetItem getWidgetItem(String widgetId) {
		return widgetSet.get(widgetId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<Property<?>> getProperties() {
		return properties;
	}

	public void setProperties(Collection<Property<?>> properties) {
		this.properties = properties;
	}
	
	public Collection<PermissionDefinition> getPermissions() {
		return permissions;
	}

	public void setPermissions(Collection<PermissionDefinition> permissions) {
		this.permissions = permissions;
	}

	public Boolean getChildrenAllowed() {
		return childrenAllowed;
	}

	public void setChildrenAllowed(Boolean childrenAllowed) {
		this.childrenAllowed = childrenAllowed;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public BundleItem getBundle() {
		return bundle;
	}

	public void setBundle(BundleItem bundle) {
		this.bundle = bundle;
	}
}
