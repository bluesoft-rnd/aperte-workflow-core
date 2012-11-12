package org.aperteworkflow.editor.actioneditor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.editor.stepeditor.JavaScriptHelper;
import org.aperteworkflow.editor.stepeditor.user.Property;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionEditorApplication extends GenericEditorApplication implements	ParameterHandler, ClickListener {

	private static final long serialVersionUID = 2136349126207525109L;
	private static final Logger	logger = Logger.getLogger(ActionEditorApplication.class.getName());
	private PropertiesPanel propertiesPanel = new PropertiesPanel();
	private Button saveButton;
	private Select buttonList;
	private static final ObjectMapper mapper = new ObjectMapper();
	private Window mainWindow;
	private JavaScriptHelper jsHelper;
	private String url;
	private Map<String,Object> oldActionParameters = new HashMap<String,Object>();

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		if (parameters == null || parameters.size() == 0) {
			// No parameters to handle, we are not interested in such a request
			// it may be a request for static resource e.g.
			// <servlet>/APP/323/root.gif
			return;
		}

		url = getStringParameterByName("callbackUrl", parameters);
		String buttonType = getStringParameterByName("buttonType", parameters); 
		String buttonName = getStringParameterByName("buttonName", parameters);
		String actionParameters = getStringParameterByName("actionParameters", parameters);
		
	
		if (!StringUtils.isEmpty(actionParameters)) {
			try {
						 oldActionParameters=mapper.readValue(actionParameters, new TypeReference<HashMap<String,Object>>(){});
			} catch (JsonParseException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
			} catch (JsonMappingException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
			}
		}
		refreshWindow(buttonType, buttonName);
	}

	private void refreshWindow(String buttonType, String buttonName) {
		mainWindow.removeAllComponents();
		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		Label header = new Label();
		if (StringUtils.isEmpty(buttonName))
			header.setCaption("[No button name]");
		else
			header.setCaption("Button name: " + buttonName);
		main.addComponent(header);
		buttonList = prepareButtonList(buttonType);
		main.addComponent(buttonList);
		if (!StringUtils.isEmpty(buttonType)) {
			Class<? extends ProcessToolActionButton> buttonClass = getRegistry().getAvailableButtons().get(buttonType);
			propertiesPanel.init(buttonClass);
			propertiesPanel.refreshForm(true, oldActionParameters);
			main.addComponent(propertiesPanel);
		}
		saveButton = new Button("save", this);
		saveButton.setImmediate(true);
		main.addComponent(saveButton);

		mainWindow.setContent(main);
	}

	

	@Override
	public void init() {
		super.init();
		mainWindow = new Window(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("application.title"));
		jsHelper = new JavaScriptHelper(mainWindow);
		jsHelper.preventWindowClosing();
		mainWindow.addParameterHandler(this);
		setMainWindow(mainWindow);
	}

	private Select prepareButtonList(String buttonType) {
		final Select buttonList = new Select();
		buttonList.setNullSelectionAllowed(false);
		buttonList.setImmediate(true);

		ProcessToolRegistry reg = getRegistry();

		Map<String, Class<? extends ProcessToolActionButton>> availableButtons = reg.getAvailableButtons();
		for (Class<? extends ProcessToolActionButton> stepClass : availableButtons.values()) {
			AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
			buttonList.addItem(stepClass);
			buttonList.setItemCaption(stepClass, a.name());
			if (a.name().equals(buttonType))
				buttonList.setValue(stepClass);
		}
		
		buttonList.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Class<?> buttonClass = (Class<?>) buttonList.getValue();
				propertiesPanel.init(buttonClass);
				propertiesPanel.refreshForm(true, oldActionParameters);
			}
		});

		return buttonList;
	}


	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent() == saveButton) {
			
			if (!propertiesPanel.getPropertiesForm().isValid()) {
				getCurrent().getMainWindow().showNotification(VaadinUtility.validationNotification("Validation error", "Correct data"));
				return;
			}
			ActionDef actionDef = new ActionDef();
			actionDef.setButtonType(buttonList.getItemCaption(buttonList.getValue()));
			//Map<String, Object> codedPropertiesValue = codePropertiesValue(propertiesPanel.getPropertiesMap());
			actionDef.setItems(propertiesPanel.getPropertiesMap());
			 
			
			try {
			  String s = mapper.writeValueAsString(actionDef);
			  jsHelper.postAndRedirectAction(url, s);
			} catch (JsonMappingException e) {
				logger.log(Level.SEVERE, "Error saving action", e);
			} catch (JsonGenerationException e) {
				logger.log(Level.SEVERE, "Error saving action", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error saving action", e);
			}
		}
	}


	
	}
