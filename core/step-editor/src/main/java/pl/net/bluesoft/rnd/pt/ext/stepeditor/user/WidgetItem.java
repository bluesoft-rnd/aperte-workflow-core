package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.io.Serializable;
import java.util.*;

import org.aperteworkflow.editor.ui.permission.PermissionDefinition;

import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.Permission;
import pl.net.bluesoft.rnd.pt.ext.widget.property.AperteProcessClassInfo;

public class WidgetItem implements Serializable {
	private static final long								serialVersionUID	= -8907544816596058014L;
    private static Map<String, WidgetItem>					widgetSet			= new HashMap<String, WidgetItem>();
	private BundleItem										bundle;
	private AperteProcessClassInfo classInfo;
	
	
	public WidgetItem(String widgetId, String name, String description, String icon,
                      List<Property<?>> properties,
                      List<PermissionDefinition> permissions,
                      Boolean childrenAllowed, BundleItem bundle) {
		
		classInfo = new AperteProcessClassInfo();
		classInfo.setAliasName(widgetId);
		classInfo.setDocName(name);
		classInfo.setDocDescription(description);
		classInfo.setDocIcon(icon);
		classInfo.setProperties(properties);
		classInfo.setChildrenAllowed(childrenAllowed);
		this.bundle = bundle;
		classInfo.setPermissions(permissions);
		storeInWidgetset(widgetId);
	}
	public WidgetItem(Class<?> aperteClass, Set<Permission> defaultPermissions, BundleItem bundle) {
		classInfo = new AperteProcessClassInfo(aperteClass, defaultPermissions);
		this.bundle = bundle;
		storeInWidgetset(classInfo.getAliasName());
	}

	private void storeInWidgetset(String widgetId) {
		widgetSet.put(widgetId, this);
	}

	public static WidgetItem getWidgetItem(String widgetId) {
		return widgetSet.get(widgetId);
	}

	public String getName() {
		return classInfo.getDocName();
	}

	public String getDescription() {
		return classInfo.getDocDescription();
	}

	public Collection<Property<?>> getProperties() {
		return classInfo.getProperties();
	}

	public Collection<PermissionDefinition> getPermissions() {
		return classInfo.getPermissions();
	}

	public Boolean getChildrenAllowed() {
		return classInfo.isChildrenAllowed();
	}

	public String getWidgetId() {
		return classInfo.getAliasName();
	}

	public String getIcon() {
		return classInfo.getDocIcon();
	}

	public BundleItem getBundle() {
		return bundle;
	}
	public AperteProcessClassInfo getClassInfo() {
		return classInfo;
	}
	
}
