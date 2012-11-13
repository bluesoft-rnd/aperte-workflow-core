package org.aperteworkflow.editor.stepeditor.auto;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.editor.stepeditor.AbstractStepEditorWindow;
import org.aperteworkflow.editor.stepeditor.StepEditorApplication;
import org.aperteworkflow.editor.stepeditor.TaskConfig;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aperteworkflow.util.vaadin.VaadinUtility.styled;

public class AutoStepEditorWindow extends AbstractStepEditorWindow {

    private static final long serialVersionUID = 2136349026207825108L;
    private static final Logger	logger = Logger.getLogger(AutoStepEditorWindow.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

	private PropertiesPanel propertiesPanel;
    private TabSheet tabSheet;
    private Label stepTypeLabel;

    public AutoStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
	}

	public ComponentContainer init() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        VerticalLayout vll = new VerticalLayout();
		vll.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        vll.addComponent(new Label(messages.getMessage("jse.instructions"), Label.CONTENT_XHTML));
		vll.setSpacing(true);

		if (stepType != null) {
            propertiesPanel = new PropertiesPanel();

            Class<?> stepClass = getStepClass(stepType);
		    propertiesPanel.init(stepClass);
		    propertiesPanel.refreshForm(false, getLoadedJsonData(jsonConfig));

            stepTypeLabel = styled(new Label(propertiesPanel.getClassInfo().getDocName()), "h2");
            
            tabSheet = new TabSheet();
            tabSheet.addTab(propertiesPanel, messages.getMessage("form.properties"));

            vll.addComponent(stepTypeLabel);
            vll.addComponent(tabSheet);
		}
		   
		return vll;
	}
		
	private Map<String,Object> getLoadedJsonData(String jsonConfig) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		if (StringUtils.isEmpty(jsonConfig))
			return new HashMap<String,Object>();
		try {
            Map<String,Object> propertiesMap =  mapper.readValue(
                    jsonConfig,
                    new TypeReference<HashMap<String, Object>>() {}
            );
            // decode base64 and drop empty properties
            if (propertiesMap != null && !propertiesMap.isEmpty()) {
				Iterator<Map.Entry<String, Object>> it = propertiesMap.entrySet().iterator();
                while (it.hasNext()) {
					Map.Entry<String, Object> e = it.next();
					String propertyName = e.getKey();
                    Object encodedValue = e.getValue();
                    if (encodedValue == null) {
                        it.remove();
                        continue;
                    }

                    if (encodedValue instanceof String) {
                        if (encodedValue.toString().trim().isEmpty()) {
                            it.remove();
                            continue;
                        }
                        byte[] decoded = Base64.decodeBase64(encodedValue.toString().getBytes());
                        propertiesMap.put(propertyName, new String(decoded));
                    }
                }
            }
            return propertiesMap;
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error parsing JSON data", e);
		}
		application.getMainWindow().showNotification(messages.getMessage("jse.error.read"));
		return null;
	}

//    private boolean isPropertyEmpty(Object value) {
//        return (value == null || value.toString().trim().isEmpty());
//    }

	private String getJsonToSave() {
        // encode the properties with base64 and drop the empty values
        Map<String, Object> propertiesMap = propertiesPanel.getPropertiesMap();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
			Iterator<Map.Entry<String, Object>> it = propertiesMap.entrySet().iterator();
            while (it.hasNext()) {
				Map.Entry<String, Object> e = it.next();
                String propertyName = e.getKey();
                Object propertyValue = e.getValue();
                if (propertyValue == null) {
                    it.remove();
                    continue;
                }

                if (propertyValue instanceof String) {
                    if (propertyValue.toString().trim().isEmpty()) {
                        it.remove();
                        continue;
                    }

                    String encodedValue = Base64.encodeBase64URLSafeString(propertyValue.toString().getBytes());
                    propertiesMap.put(propertyName, encodedValue);
                }
            }
        }

		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(propertiesPanel.getClassInfo().getAliasName());
		tc.setParams(propertiesMap);
		
		try {
			return mapper.writeValueAsString(tc);
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating JSON", e);
		}
		application.getMainWindow().showNotification(messages.getMessage("jse.error.write"));
		return "";
		
	}

    @Override
	public void save() {
		if (!propertiesPanel.getPropertiesForm().isValid()) {
			GenericEditorApplication.getCurrent()
                    .getMainWindow().showNotification(VaadinUtility.validationNotification("Validation error", "Correct data"));
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
