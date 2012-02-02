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
	
	private PropertiesForm propertiesForm = new PropertiesForm();
	private AperteProcessClassInfo classInfo;
	
	public void init(AperteProcessClassInfo classInfo) {
		this.classInfo = classInfo;
	}
	public void init(Class<?> aperteClass) {
		this.classInfo = new AperteProcessClassInfo(aperteClass);
	}
	
	private void refreshForm(boolean setCaption) {
        removeAllComponents();
        VerticalLayout layout = (VerticalLayout) getContent();
		
        if (setCaption) {
          setCaption(classInfo.getDocName());

		  layout.addComponent(new Label(classInfo.getDocDescription()));
        }

		List<Property<?>> properties = classInfo.getProperties();
		if (properties == null || properties.size() == 0) {
			layout.addComponent(new Label(Messages.getString("form.no.parameters.defined")));
		} else {
			propertiesForm = new PropertiesForm();
			propertiesForm.setImmediate(true);
		}
		layout.addComponent(propertiesForm);
	}
	
	public void refreshForm(boolean setCaption, List<Property<?>> properties) {
		refreshForm(setCaption);
		
		for (Property<?> property : properties) {
			final com.vaadin.ui.Field field = fieldFactory.createField(property);
			propertiesForm.addField(property, field);
		}
	}
	
    public void refreshForm(boolean setCaption, Map<String,Object> valuesMap) {
	    refreshForm(setCaption);
	    
    	List<Property<?>> properties = classInfo.getProperties();
		for (Property<?> property : properties) {
			final com.vaadin.ui.Field field = fieldFactory.createField(property);
			propertiesForm.addField(property, field);
			field.setValue(valuesMap.get(property.getPropertyId()));
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
	public AperteProcessClassInfo getClassInfo() {
		return classInfo;
	}
	public void setClassInfo(AperteProcessClassInfo classInfo) {
		this.classInfo = classInfo;
	}

    
}
