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
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction.getAutowiredPropertyNames;

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
	private Map<String,Object> oldActionAttributes = new HashMap<String,Object>();

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
		String actionAttributes = getStringParameterByName("actionAttributes", parameters);
		
	
		
			try {
				if (!StringUtils.isEmpty(actionParameters)) {
						 oldActionParameters=mapper.readValue(actionParameters, new TypeReference<HashMap<String,Object>>(){});
			}
			if (!StringUtils.isEmpty(actionAttributes)) {
						 oldActionAttributes=mapper.readValue(actionAttributes, new TypeReference<HashMap<String,Object>>(){});
						 if(!oldActionParameters.isEmpty()){							 
							 oldActionParameters.putAll(oldActionAttributes);
						 }
						 
			}
			} catch (JsonParseException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
			} catch (JsonMappingException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error reading action parameters", e);
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
		
		 // method-level class used for sorting
        class Item implements Comparable<Item> {
        	public Class<? extends ProcessToolActionButton> stepClass;
        	public String caption;
        	
			public Item(Class<? extends ProcessToolActionButton> stepClass, String caption) {
				this.stepClass = stepClass;
				this.caption = caption;
			}
			
			@Override
			public int compareTo(Item o) {
				return caption.compareTo(o.caption);
			}
        }
        
        List<Item> items = new LinkedList<Item>();
        Class<? extends ProcessToolActionButton> active = null;
		Map<String, Class<? extends ProcessToolActionButton>> availableButtons = getRegistry().getAvailableButtons();
		for (Class<? extends ProcessToolActionButton> stepClass : availableButtons.values()) {
			AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
			items.add(new Item(stepClass,a.name()));
			
			if (a.name().equals(buttonType))
				active=stepClass;
		}
		
		
		Collections.sort(items);
	        
	    for (Item item:items){
	    	buttonList.addItem(item.stepClass);
	    	buttonList.setItemCaption(item.stepClass, item.caption);
	    }
	    buttonList.setValue(active);
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
			actionDef.setItems(getProperties());
			actionDef.setAttributes(getAttributes());
			
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

	private Map<String, Object> getProperties() {
		return getStringObjectMap(true);
	}

	private Map<String, Object> getAttributes() {
		return getStringObjectMap(false);
	}

	private Map<String, Object> getStringObjectMap(boolean copyAutowiredProperties) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (Map.Entry<String, Object> entry : propertiesPanel.getPropertiesMap().entrySet()) {
			if (isAutowiredProperty(entry.getKey()) == copyAutowiredProperties) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private boolean isAutowiredProperty(String key) {
		return getAutowiredPropertyNames().contains(key);
	}
}
