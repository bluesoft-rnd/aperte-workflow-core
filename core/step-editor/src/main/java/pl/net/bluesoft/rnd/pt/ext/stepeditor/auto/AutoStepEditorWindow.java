package pl.net.bluesoft.rnd.pt.ext.stepeditor.auto;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.AbstractStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.TaskConfig;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.Property;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.widget.property.PropertiesPanel;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;
import pl.net.bluesoft.util.lang.Classes;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class AutoStepEditorWindow extends AbstractStepEditorWindow implements ClickListener {

    private static final long		serialVersionUID	= 2136349026207825108L;
	
	private Button					saveButton = new Button(Messages.getString("jse.button.save"), this);

	private PropertiesPanel propertiesPanel = new PropertiesPanel();

    public AutoStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
	}

    public Component getHeader() {
        VerticalLayout layout = new VerticalLayout();
        
        Label stepNameLabel = new Label();
        stepNameLabel.setContentMode(Label.CONTENT_XHTML);
        if (StringUtils.isEmpty(stepName)) {
            stepNameLabel.setValue("<h2>" + Messages.getString("jse.noStepName") + "</h2>");
        } else {
            stepNameLabel.setValue("<h2>" + Messages.getString("jse.stepName", stepName) + "</h2>");
        }

        layout.addComponent(stepNameLabel);
     
		return layout;
    }
	
    private void refreshFormValues() {
    	if (!StringUtils.isEmpty(jsonConfig)) {
    	  Map map = getLoadedJsonData(jsonConfig);
		  for (Object propertyId : propertiesPanel.getPropertiesForm().getItemPropertyIds()) {
              Property prop = (Property)propertyId;
			  com.vaadin.ui.Field field = propertiesPanel.getPropertiesForm().getField(propertyId);
			  field.setValue(map.get(prop.getPropertyId()));
		  }
    	}
	}
    
	public ComponentContainer init() {

		VerticalLayout vll = new VerticalLayout();
		vll.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		vll.setSpacing(true);
		vll.setMargin(true);
		
		if (stepType != null) {
		
		   vll.addComponent(saveButton);
		   vll.setExpandRatio(saveButton, 0);
		   vll.addComponent(propertiesPanel);
		   
		   Class<?> stepClass = getStepClass(stepType);
		   propertiesPanel.refreshForm(stepClass);
		   refreshFormValues();
		}
		   
		return vll;
	}
		

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent() == saveButton) {
			save();
		}
	}
	
	private Map<String,String> getLoadedJsonData(String jsonConfig) {
		try {
		  return new ObjectMapper().readValue(jsonConfig, Map.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		application.getMainWindow().showNotification(Messages.getString("jse.error.read"));
		return null;
	}
	
	
	private String getJsonToSave() {
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(propertiesPanel.getAliasName());
		
		for (Object propertyId : propertiesPanel.getPropertiesForm().getItemPropertyIds()) {
            Property prop = (Property)propertyId;
			com.vaadin.ui.Field field = propertiesPanel.getPropertiesForm().getField(propertyId);
            Object obj = field.getValue();
            
        	if (obj == null) {
        		if (Boolean.class.equals(prop.getType()))
        			obj = Boolean.FALSE;
        		else if (String.class.equals(prop.getType()))
        			obj = "";
        	}
            
            tc.addParam(prop.getPropertyId(), obj);
		}
		
		try {
			return new ObjectMapper().writeValueAsString(tc);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		application.getMainWindow().showNotification(Messages.getString("jse.error.write"));
		return "";
		
	}
	
	private void save() {
		if (!propertiesPanel.getPropertiesForm().isValid()) {
			GenericEditorApplication.getCurrent().getMainWindow().showNotification(VaadinUtility.validationNotification("Validation error","Correct data"));
			return;
		}
		String json = getJsonToSave();
		application.getJsHelper().postAndRedirectStep(url, json);
	}

	
	private Class<?> getStepClass(String stepType) {
		ProcessToolRegistry reg = GenericEditorApplication.getRegistry();
        Map<String,ProcessToolProcessStep> availableSteps = reg.getAvailableSteps();
        for (ProcessToolProcessStep stepInstance : availableSteps.values()) {
            Class stepClass = stepInstance.getClass();
            AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
            if (stepType.equals(a.name())) {
            	return stepClass;
            }
        }
        return null;
	}
	
}
