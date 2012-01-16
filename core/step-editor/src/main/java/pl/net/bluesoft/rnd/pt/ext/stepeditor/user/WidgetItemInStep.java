package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class WidgetItemInStep implements Serializable {
	private static final long serialVersionUID = -4319558996871289010L;
	private WidgetItem widgetItem;
	private List<Property<?>> properties = new LinkedList<Property<?>>();
	private List<Property<?>> permissions = new LinkedList<Property<?>>();

	public WidgetItemInStep(WidgetItem widgetItem, List<Property<?>> properties, List<Property<?>> permissions) {
		super();
		this.widgetItem = widgetItem;
		this.properties = properties;
		this.permissions = permissions;
	}

	public WidgetItemInStep(WidgetItem widgetItem) {
		super();
		this.widgetItem = widgetItem;
		if (widgetItem.getProperties() != null) {
			for (Property<?> property : widgetItem.getProperties()) {
				try {
					properties.add((Property<?>) property.clone());
				} catch (CloneNotSupportedException e) {
					// should never happen
					// if happens, we just log the exception and skip property
					// that failed
					e.printStackTrace();
				}
			}
		}
		if (widgetItem.getPermissions() != null) {
			for (Property<?> perm : widgetItem.getPermissions()) {
				try {
					permissions.add((Property<?>) perm.clone());
				} catch (CloneNotSupportedException e) {
					// should never happen
					// if happens, we just log the exception and skip property
					// that failed
					e.printStackTrace();
				}
			}
		}
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
	
	public List<Property<?>> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Property<?>> permissions) {
		this.permissions = permissions;
	}

	public boolean hasPermissions() {
		return permissions != null && permissions.size() > 0;
	}
}
