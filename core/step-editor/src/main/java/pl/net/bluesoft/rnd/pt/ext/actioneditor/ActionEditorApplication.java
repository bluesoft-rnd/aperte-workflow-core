package pl.net.bluesoft.rnd.pt.ext.actioneditor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.JavaScriptHelper;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.Property;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.WidgetConfigFormFieldFactory;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.widget.property.PropertiesPanel;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;
import pl.net.bluesoft.util.lang.Classes;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

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
				oldActionParameters = mapper.readValue(actionParameters, new TypeReference<HashMap<String,Object>>(){});
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
		main.addComponent(propertiesPanel);
		if (!StringUtils.isEmpty(buttonType)) {
			Class<? extends ProcessToolActionButton> buttonClass = getRegistry().getAvailableButtons().get(buttonType);
			propertiesPanel.refreshForm(buttonClass);
			refreshFormValues();
		}
		saveButton = new Button("save", this);
		saveButton.setImmediate(true);
		main.addComponent(saveButton);

		mainWindow.setContent(main);
	}

	

	@Override
	public void init() {
		super.init();
		mainWindow = new Window(Messages.getString("application.title"));
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
				propertiesPanel.refreshForm(buttonClass);
				refreshFormValues();
			}
		});

		return buttonList;
	}

	private void refreshFormValues() {
		for (Object propertyId : propertiesPanel.getPropertiesForm().getItemPropertyIds()) {
            Property prop = (Property)propertyId;
			com.vaadin.ui.Field field = propertiesPanel.getPropertiesForm().getField(propertyId);
			field.setValue(oldActionParameters.get(prop.getName()));
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent() == saveButton) {
			
			if (!propertiesPanel.getPropertiesForm().isValid()) {
				getCurrent().getMainWindow().showNotification(VaadinUtility.validationNotification("Validation error","Correct data"));
				return;
			}
			ActionDef actionDef = new ActionDef();
			actionDef.setButtonType(buttonList.getItemCaption(buttonList.getValue()));
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
                
                actionDef.putItem(prop.getName(), obj);
			}
			
			
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
