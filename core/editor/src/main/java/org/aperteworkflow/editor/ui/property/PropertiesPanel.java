package org.aperteworkflow.editor.ui.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.Field;
import org.aperteworkflow.editor.stepeditor.user.Property;
import org.aperteworkflow.editor.stepeditor.user.WidgetConfigFormFieldFactory;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

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

    public void ensureForm() {
        if (propertiesForm == null) {
            refreshForm(true);
        }
    }

	private void refreshForm(boolean setCaption) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		removeAllComponents();
        VerticalLayout layout = (VerticalLayout) getContent();
		
        if (setCaption) {
          setCaption(classInfo.getDocName());

		  layout.addComponent(new Label(classInfo.getDocDescription()));
        }

		List<Property<?>> properties = classInfo.getProperties();
		if (properties == null || properties.size() == 0) {
			layout.addComponent(new Label(messages.getMessage("form.no.parameters.defined")));
		} else {
			propertiesForm = new PropertiesForm();
			propertiesForm.setImmediate(true);
		}
		layout.addComponent(propertiesForm);
	}
	
	public void refreshForm(final boolean setCaption, final List<Property<?>> properties) 
	{
		getRegistry().withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				refreshForm(setCaption);

				for (Property<?> property : properties) {
					Field field = fieldFactory.createField(property, propertiesForm);
					propertiesForm.addField(property, field);
				}

			}

		});

	}
	
    public void refreshForm(boolean setCaption, Map<String,Object> valuesMap) { 
	    refreshForm(setCaption);
	    
    	List<Property<?>> properties = classInfo.getProperties();
		for (Property<?> property : properties) {
			final Field field = fieldFactory.createField(property, propertiesForm);
			propertiesForm.addField(property, field);
			Object value = valuesMap.get(property.getPropertyId());
			
			//BACKCOMPATIBILITY: this block is necessary for back-compatibility from 2.0 RC1 to 2.0.
			if (property.getPropertyId().equals("markProcessImportant") ){
				if(isValueNullOrEmpty(value)){
				value = false;
				}
			}
			
			
			field.setValue(value);
		}
    }
    private boolean isValueNullOrEmpty(Object value){
    	
    	if(value==null || (value instanceof String && ((String)value).isEmpty())){
			return true;
			}
    	return false;

    }
    
	public Map<String,Object> getPropertiesMap() {
		Map<String,Object> map = new HashMap<String,Object>(); 
		
		for (Object propertyId : propertiesForm.getItemPropertyIds()) {
            Property prop = (Property)propertyId;
			Field field = propertiesForm.getField(propertyId);
            Object obj = field.getValue();
            
        	if (obj == null) { // TODO this is at least strange and should be reconsidered
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
