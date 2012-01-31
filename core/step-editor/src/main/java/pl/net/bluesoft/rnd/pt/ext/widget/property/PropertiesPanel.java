package pl.net.bluesoft.rnd.pt.ext.widget.property;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.Property;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.WidgetConfigFormFieldFactory;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.reflect.Field;
import java.util.*;

public class PropertiesPanel extends Panel {

	
	
	private static WidgetConfigFormFieldFactory fieldFactory = new WidgetConfigFormFieldFactory();
	
	private PropertiesForm propertiesForm = new PropertiesForm();;
	private List<Property> properties;
	private String docName;
	private String docDescription;
	private String aliasName;
	private boolean childrenAllowed;
	
	public void refreshForm(Class<?> aperteClass, Map<String,Object> valuesMap) {
		
		AliasName an = Classes.getClassAnnotation(aperteClass, AliasName.class);
		ChildrenAllowed ca = aperteClass.getAnnotation(ChildrenAllowed.class);
		AperteDoc classDoc = Classes.getClassAnnotation(aperteClass, AperteDoc.class);
		List<Field> fields = Classes.getFieldsWithAnnotation(aperteClass, AutoWiredProperty.class);
		
		aliasName = an == null ? "" : an.name();
		childrenAllowed = ca == null ? false : ca.value();
        docName = null;
        docDescription = null;
        properties = new ArrayList<Property>();
        I18NSource i18NSource = I18NSource.ThreadUtil.getThreadI18nSource();
        
		if (classDoc != null) {
			docName = i18NSource.getMessage(classDoc.humanNameKey());
			docDescription = i18NSource.getMessage(classDoc.descriptionKey());
		}
		if (StringUtils.isEmpty(docName)) {
			docName = aliasName;
		}
		if (StringUtils.isEmpty(docDescription)) {
			docDescription = aperteClass.getName();
		}
		
        if (fields != null && !fields.isEmpty()) {
        	for (Field field : fields) {
        		properties.add(getProperty(field));
        	}
        	Collections.sort(properties);
            
        }
		
		refreshForm();
		refreshFormValues(valuesMap);
	}
	
	private Property getProperty(Field field) {
		AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);
        AutoWiredPropertyConfigurator awpConfigurator = field.getAnnotation(AutoWiredPropertyConfigurator.class);
        AperteDoc fieldDoc = field.getAnnotation(AperteDoc.class);
        I18NSource i18NSource = I18NSource.ThreadUtil.getThreadI18nSource();
        
        String fieldDocName = null;
        String fieldDocDescription = null;
        
        if (fieldDoc != null) {
            fieldDocName = i18NSource.getMessage(fieldDoc.humanNameKey());
            fieldDocDescription = i18NSource.getMessage(fieldDoc.descriptionKey());
        }
        if (StringUtils.isEmpty(fieldDocName)) {
        	fieldDocName = field.getName();
        }
        if (StringUtils.isEmpty(fieldDocDescription)) {
        	fieldDocDescription = field.getName();
        }
        Property property = new Property(null, field.getType());
        property.setPropertyType(Property.PropertyType.PROPERTY);
        property.setPropertyId(field.getName());
        property.setName(fieldDocName);
        property.setDescription(fieldDocDescription);
        
        if (awp != null) {
            property.setRequired(awp.required());
        }
        if (awpConfigurator != null) {
            property.setPropertyFieldClass(awpConfigurator.fieldClass());
        }
        return property;
	}
	
    private void refreshForm() {
    	removeAllComponents();
		
        setCaption(docName);

        VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(new Label(docDescription));

		if (properties == null || properties.size() == 0) {
			layout.addComponent(new Label(Messages.getString("form.no.parameters.defined")));
		} else {
			propertiesForm = new PropertiesForm();
			propertiesForm.setImmediate(true);
            propertiesForm.addComponent(new Label("<b>" + Messages.getString("form.properties") + "</b>", Label.CONTENT_XHTML));
            
			for (Property<?> property : properties) {
				final com.vaadin.ui.Field field = fieldFactory.createField(property);
				propertiesForm.addField(property, field);
			}
			layout.addComponent(propertiesForm);
		}

    }
    
	private void refreshFormValues(Map<String,Object> valuesMap) {
		for (Object propertyId : propertiesForm.getItemPropertyIds()) {
            Property prop = (Property)propertyId;
			com.vaadin.ui.Field field = propertiesForm.getField(propertyId);
			field.setValue(valuesMap.get(prop.getPropertyId()));
		}
	}
	
	public Map<String,Object> getPropertiesMap() {
		Map<String,Object> map = new HashMap<String,Object>(); 
		
		for (Object propertyId : propertiesForm.getItemPropertyIds()) {
            Property prop = (Property)propertyId;
			com.vaadin.ui.Field field = propertiesForm.getField(propertyId);
            Object obj = field.getValue();
            
        	if (obj == null) {
        		if (Boolean.class.equals(prop.getType()))
        			obj = Boolean.FALSE;
        		else if (String.class.equals(prop.getType()))
        			obj = "";
        	}
            
            map.put(prop.getPropertyId(), obj);
		}

		return map;
	}

	public PropertiesForm getPropertiesForm() {
		return propertiesForm;
	}

	public void setPropertiesForm(PropertiesForm propertiesForm) {
		this.propertiesForm = propertiesForm;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getDocDescription() {
		return docDescription;
	}

	public void setDocDescription(String docDescription) {
		this.docDescription = docDescription;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public boolean isChildrenAllowed() {
		return childrenAllowed;
	}

	public void setChildrenAllowed(boolean childrenAllowed) {
		this.childrenAllowed = childrenAllowed;
	}
    
    
}
