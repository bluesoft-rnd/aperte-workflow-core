package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Field;

import java.io.Serializable;

public class Property<T> extends ObjectProperty<T> implements Serializable, Cloneable, Comparable<Property<?>> {

    private static final long serialVersionUID = -6913191546296165712L;
	private String name;
	private String description;
	private String propertyId;
	private boolean required;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Property property = (Property)o;

		if (propertyId != null ? !propertyId.equals(property.propertyId) : property.propertyId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return propertyId != null ? propertyId.hashCode() : 0;
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

    public Class<? extends Field> getPropertyFieldClass() {
        return propertyField;
    }

    public void setPropertyFieldClass(Class<? extends Field> propertyField) {
        this.propertyField = propertyField;
    }
}
