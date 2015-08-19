package org.aperteworkflow.editor.actioneditor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.editor.stepeditor.JavaScriptHelper;
import org.aperteworkflow.editor.stepeditor.user.JSONHandler;
import org.aperteworkflow.editor.stepeditor.user.Property;
import org.aperteworkflow.editor.stepeditor.user.WidgetEditor;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction.getAutowiredPropertyNames;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.rnd.util.AnnotationUtil.getAliasName;

public class ActionEditorApplication extends GenericEditorApplication implements ParameterHandler, ClickListener {
	private static final long serialVersionUID = 2136349126207525109L;
	private static final Logger	logger = Logger.getLogger(ActionEditorApplication.class.getName());
	private PropertiesPanel propertiesPanel = new PropertiesPanel();
	private Button saveButton;
	private Select buttonList;
	private static final ObjectMapper mapper = new ObjectMapper();
	private Window mainWindow;
	private JavaScriptHelper jsHelper;
	private String url;
	private Map<String,Object> oldActionParameters = new LinkedHashMap<String,Object>();
	private Map<String,Object> oldActionAttributes = new LinkedHashMap<String,Object>();
	private Map<String,Object> oldWidgets = new LinkedHashMap<String,Object>();

	private static final String ACTION_PSEUDO_STATE_WIDGETS = "actionPseudoStateWidgets";

	private WidgetEditor widgetEditor;

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		if (parameters == null || parameters.isEmpty()) {
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

				if (oldActionAttributes.containsKey(ACTION_PSEUDO_STATE_WIDGETS)) {
					String widgets = (String)oldActionAttributes.get(ACTION_PSEUDO_STATE_WIDGETS);
					widgets = new String(Base64.decodeBase64(widgets), Charset.forName("UTF-8"));
					oldActionAttributes.remove(ACTION_PSEUDO_STATE_WIDGETS);

					if (!StringUtils.isEmpty(widgets)) {
						oldWidgets = mapper.readValue(widgets, new TypeReference<HashMap<String,Object>>(){});
					}
				}

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
			Class<? extends ProcessToolActionButton> buttonClass = getRegistry().getGuiRegistry().getAvailableButtons().get(buttonType);
			propertiesPanel.init(buttonClass);
			propertiesPanel.refreshForm(true, oldActionParameters);
		}
		saveButton = new Button("save", this);
		saveButton.setImmediate(true);

		initWidgetEditor();

		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

		TabSheet ts = new TabSheet();
		ts.setSizeFull();
		ts.addTab(propertiesPanel, messages.getMessage("action.editor.attributes.tabcaption"));
		ts.addTab(widgetEditor.buildWidgetEditorTabContent(), messages.getMessage("action.editor.widgets.tabcaption"));
		ts.setSelectedTab(propertiesPanel);
		main.addComponent(ts);
		main.setExpandRatio(ts, 1.0f);

		main.addComponent(saveButton);

		mainWindow.setContent(main);
	}

	private void initWidgetEditor() {
		widgetEditor = new WidgetEditor(this);
		widgetEditor.init();

		try {
			JSONHandler.analyzeChildren(
					oldWidgets,
					widgetEditor.getStepTreeContainer(),
					widgetEditor.getRootItem());
		}
		catch (JSONHandler.WidgetNotFoundException e) {
			I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

			logger.log(Level.SEVERE, "Widget not found", e);
			getMainWindow().showNotification(messages.getMessage("error.config_not_loaded.title"),
					messages.getMessage("error.config_not_loaded.widget_not_found.body", e.getWidgetItemName()),
					Window.Notification.TYPE_ERROR_MESSAGE);
		}
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
		Map<String, Class<? extends ProcessToolActionButton>> availableButtons = getRegistry().getGuiRegistry().getAvailableButtons();

		for (Class<? extends ProcessToolActionButton> stepClass : availableButtons.values()) {
			String aliasName = getAliasName(stepClass);

			items.add(new Item(stepClass, aliasName));

			if (aliasName.equals(buttonType)) {
				active = stepClass;
			}
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
			actionDef.setItems(getProperties());
			actionDef.setAttributes(getAttributes());

			try {
				addWidgetsToDefinition(actionDef);

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

	private void addWidgetsToDefinition(ActionDef actionDef) throws IOException {
		Map<String, Object> widgetMap = JSONHandler.treeToMap(widgetEditor.getStepTree(), widgetEditor.getRootItem());

		if (widgetMap == null || widgetMap.isEmpty()) {
			return;
		}

		Collection<Object> children = (Collection<Object>)widgetMap.get("children");

		if (children == null || children.isEmpty()) {
			return;
		}

		String widgetJSON = mapper.writeValueAsString(widgetMap);
		widgetJSON = Base64.encodeBase64String(widgetJSON.getBytes("UTF-8"));
		actionDef.getAttributes().put(ACTION_PSEUDO_STATE_WIDGETS, widgetJSON);
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
