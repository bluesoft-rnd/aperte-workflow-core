package pl.net.bluesoft.rnd.pt.ext.stepeditor.auto;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.AbstractStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.TaskConfig;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.widget.property.PropertiesPanel;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;
import pl.net.bluesoft.util.lang.Classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoStepEditorWindow extends AbstractStepEditorWindow {

    private static final long		serialVersionUID	= 2136349026207825108L;
    private static final Logger	logger = Logger.getLogger(AutoStepEditorWindow.class.getName());

	private PropertiesPanel propertiesPanel = new PropertiesPanel();
	private static final ObjectMapper mapper = new ObjectMapper();

    public AutoStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
	}

	public ComponentContainer init() {

		VerticalLayout vll = new VerticalLayout();
		vll.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        vll.addComponent(new Label(Messages.getString("jse.instructions"),
                Label.CONTENT_XHTML));
		vll.setSpacing(true);

		if (stepType != null) {
		   Class<?> stepClass = getStepClass(stepType);
		   propertiesPanel.init(stepClass);
		   propertiesPanel.refreshForm(true, getLoadedJsonData(jsonConfig));
		   vll.addComponent(propertiesPanel);
		}
		   
		return vll;
	}
		
	private Map<String,Object> getLoadedJsonData(String jsonConfig) {
		if (StringUtils.isEmpty(jsonConfig))
			return new HashMap<String,Object>();
		try {
			return mapper.readValue(jsonConfig, new TypeReference<HashMap<String, Object>>() {
            });
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		}
		application.getMainWindow().showNotification(Messages.getString("jse.error.read"));
		return null;
	}
	
	private String getJsonToSave() {
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(propertiesPanel.getClassInfo().getAliasName());
		tc.setParams(propertiesPanel.getPropertiesMap());
		
		try {
			return mapper.writeValueAsString(tc);
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		}
		application.getMainWindow().showNotification(Messages.getString("jse.error.write"));
		return "";
		
	}

    @Override
	public void save() {
		if (!propertiesPanel.getPropertiesForm().isValid()) {
			GenericEditorApplication.getCurrent()
                    .getMainWindow().showNotification(VaadinUtility.validationNotification("Validation error","Correct data"));
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
