package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Field;

import java.io.Serializable;

public class Property<T> extends ObjectProperty<T> implements Serializable, Cloneable, Comparable<Property<?>> {

    public enum PropertyType {
        PROPERTY,
        PERMISSION
    }
	
	private static final long serialVersionUID = -6913191546296165712L;
	private String name;
	private String description;
	private String[] allowedValues;
	private String propertyId;
	private boolean required;
	private PropertyType propertyType;
    private Class<? extends Field> propertyField;

    public Property(Class<T> type) {
        this(null, type);
    }
    
    public Property(T value, Class<T> type) {
        super(value, type);
    }

    @Override
    public int compareTo(Property<?> other) {
        if (other == null) {
            // Null object shall be first, always
            return 0;
        }

        // Handle possible null values
        if (propertyId == null) {
            return other.propertyId == null ? 0 : 1;
        }
        if (other.propertyId == null) {
            return 1;
        }

        // Compare name literals
        return propertyId.compareTo(other.propertyId);
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

    public Class<? extends Field> getPropertyFieldClass() {
        return propertyField;
    }

    public void setPropertyFieldClass(Class<? extends Field> propertyField) {
        this.propertyField = propertyField;
    }
}
