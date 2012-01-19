package pl.net.bluesoft.rnd.pt.ext.stepeditor.auto;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.AbstractStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.TaskConfig;
import pl.net.bluesoft.util.lang.Classes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoStepEditorWindow extends AbstractStepEditorWindow implements ClickListener {

    private static final long		serialVersionUID	= 2136349026207825108L;
	
	private Button					saveButton = new Button(Messages.getString("jse.button.save"), this);

	private Map<String,TextField>   textParams = new HashMap<String,TextField>();

    private StepDefinition          stepDef;

    public AutoStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
	}

    public Component getHeader() {
        VerticalLayout layout = new VerticalLayout();
        
        Label stepNameLabel = new Label();
        stepNameLabel.setContentMode(Label.CONTENT_XHTML);
        if (stepName == null) {
            stepNameLabel.setValue(Messages.getString("jse.noStepName"));
        } else {
            stepNameLabel.setValue(Messages.getString("jse.stepName", stepName));
        }

        Label definitionLabel = new Label();
        definitionLabel.setContentMode(Label.CONTENT_XHTML);
        if (stepDef == null) {
            definitionLabel.setValue(Messages.getString("jse.stepdef.notfound", stepDef.getName()));
        } else if (stepDef.getParameters().isEmpty()) {
            definitionLabel.setValue(Messages.getString("jse.params.notfound", stepDef.getName()));
        } else {
            definitionLabel.setValue(Messages.getString("jse.definition", stepDef.getName()));
        }
        
        layout.addComponent(stepNameLabel);
        layout.addComponent(definitionLabel);
		return layout;
    }
	
	private ComponentContainer buildLayout(Map<String,String> loadedMap) {

		VerticalLayout vll = new VerticalLayout();
		vll.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		
		if (stepDef != null) {
		
		   vll.addComponent(saveButton);
		   vll.setExpandRatio(saveButton, 0);
		   textParams.clear();
		   
		   for (StepParameter stepParam : stepDef.getParameters()) {
			  TextField tf = new TextField(stepParam.getName() + (stepParam.isRequired() ? "*" : ""));
			  tf.setWidth(100, Sizeable.UNITS_PERCENTAGE);
			  tf.setNullRepresentation("");
			  if (loadedMap != null) {
				  tf.setValue(loadedMap.get(stepParam.getName()));
			  }
			  
			  textParams.put(stepParam.getName(),tf);
			  vll.addComponent(tf);
			  vll.setExpandRatio(tf, 0);
		   }
		}

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		hl.setSpacing(true);
		hl.setMargin(true);

		hl.addComponent(vll);
		
		return hl;
	}
	

    public ComponentContainer init() {
    	Map<String, String> loadedMap = null;
    	
    	if (jsonConfig != null && jsonConfig.trim().length() > 0) {
    	   loadedMap = getLoadedJsonData(jsonConfig);
    	}
    	
    	if (stepType != null && stepType.trim().length() > 0) {
    		stepDef = getStepDefinition(stepType);
    	}
    	
		return buildLayout(loadedMap);
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
	
	private String validate() {
		StringBuffer msg = new StringBuffer();
		for (StepParameter stepParam : stepDef.getParameters()) {
			TextField tf = textParams.get(stepParam.getName());
			String value = (String)tf.getValue();
			if (stepParam.isRequired() && (value == null || value.trim().length() == 0)) {
		       msg.append(Messages.getString("jse.required", stepParam.getName()) + "<br/>");		
			}
		}
		return msg.toString();
	}
	
	private String getJsonToSave() {
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(stepDef.getName());
		
		for (StepParameter stepParam : stepDef.getParameters()) {
			TextField tf = textParams.get(stepParam.getName());
			tc.addParam(stepParam.getName(), tf.getValue());		
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
		String msg = validate();
		if (msg.trim().length() != 0) {
			application.getMainWindow().showNotification(msg);
		} else {
		  String json = getJsonToSave();
		  application.getJsHelper().postAndRedirect(url, json);
		}
	}

	
	private StepDefinition getStepDefinition(String stepName) {
		ProcessToolRegistry reg = StepEditorApplication.getRegistry(application);
        Map<String,ProcessToolProcessStep> availableSteps = reg.getAvailableSteps();
        for (ProcessToolProcessStep stepInstance : availableSteps.values()) {
            Class stepClass = stepInstance.getClass();
            AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
            if (stepName.equals(a.name())) {

              StepDefinition stepDef = new StepDefinition();
              stepDef.setName(a.name());

              List<Field> fields = Classes.getFieldsWithAnnotation(stepClass, AutoWiredProperty.class);

              if (fields != null) {
                for (Field field : fields) {
                    StepParameter param = new StepParameter();
                    param.setName(field.getName());
                    param.setType(field.getType());

                    AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);
                    param.setRequired(awp != null && awp.required());
                    stepDef.addParameter(param);
                }
              }
              return stepDef;
            }
        }
//        }

		return null;
	}

}
