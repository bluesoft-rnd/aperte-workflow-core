package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.io.Serializable;

import com.vaadin.data.util.ObjectProperty;

public class Property<T> extends ObjectProperty<T> implements Serializable, Cloneable {

	public enum PropertyType {PROPERTY, PERMISSION};
	
	private static final long serialVersionUID = -6913191546296165712L;
	private String name;
	private String description;
	private String[] allowedValues;
	private String propertyId;
	private boolean required;
	private PropertyType propertyType;  

	public Property(PropertyType propertyType, String propertyId, String name, String description, Class<T> type, String[] allowedValues, boolean required, T value) {
		super(value, type);
		this.propertyType = propertyType;
		this.propertyId = propertyId;
		this.name = name;
		this.description = description;
		this.allowedValues = allowedValues;
		this.required = required;
	}

	public Property() {
		super(null);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String[] getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public PropertyType getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}

}
