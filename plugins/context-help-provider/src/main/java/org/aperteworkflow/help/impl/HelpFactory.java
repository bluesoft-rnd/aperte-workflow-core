package org.aperteworkflow.help.impl;


import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.MultiLevelDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpFactory {
	private Application				application;
	private I18NSource				i18NSource;
	private ProcessDictionary		dict;
	private ContextHelp				contextHelp;
	private boolean					showKeys;
	private String 					helpKeysOutputFileName = "keys.txt";
	private BufferedWriter 			helpKeysOutputFileStream;
	private Map<Integer, Resource> 	helpIcons = new HashMap<Integer, Resource>();
	private String 					dictionaryName;

	public HelpFactory(List<ProcessDefinitionConfig> definitions, Application application, I18NSource i18NSource, String dictionary, ContextHelp contextHelp) {
		this.application = application;
		this.i18NSource = i18NSource;
		this.contextHelp = contextHelp;
		this.dictionaryName = dictionary;

		if (application instanceof GenericVaadinPortlet2BpmApplication) {
			GenericVaadinPortlet2BpmApplication o = (GenericVaadinPortlet2BpmApplication) application;
			showKeys = o.showKeys();
		}

		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessDictionaryRegistry registry = ctx.getProcessDictionaryRegistry();

		List<ProcessDictionary> dictionaries = new ArrayList<ProcessDictionary>();

		for (ProcessDefinitionConfig definition : definitions) {
			ProcessDictionary dictProcess = registry.getSpecificOrDefaultProcessDictionary(definition, "db", dictionary, i18NSource.getLocale().toString());

			if (dictProcess != null) {
				dictionaries.add(dictProcess);
			}
		}

		ProcessDictionary dictGlobal = registry.getSpecificOrDefaultGlobalDictionary("db", dictionary, i18NSource.getLocale().toString());

		if (dictGlobal != null) {
			dictionaries.add(dictGlobal);
		}

		dict = createDict(registry, dictionaries);
	}

	private ProcessDictionary createDict(ProcessDictionaryRegistry registry, List<ProcessDictionary> dictionaries) {
		if (dictionaries.isEmpty()) {
			return registry.getEmptyDictionary();
		}
		else if (dictionaries.size() == 1) {
			return dictionaries.get(0);
		}
		else {
			return new MultiLevelDictionary(dictionaries);
		}
	}

	public Field wrapField(Field field, String key) {
		Button icon = helpIcon(key, field);
		return new FieldWithHelp(field, icon);
	}

	public Resource helpIcon(Integer i) {
		if(!helpIcons.containsKey(i)){
			helpIcons.put(i, new ClassResource(HelpUtility.class, "/img/help" + (i == null ? "" : i) + ".png", application));
		}
		return helpIcons.get(i);
	}

	public Button helpIcon(String key, String message) {
		Button button = helpIcon(key);
		button.setCaption(i18NSource.getMessage(message));
		button.setWidth("");
		button.addStyleName("with_message");
		return button;
	}

	public Button helpIcon(String key) {
		return helpIcon(key, (Component)null);
	}

	public Button helpIcon(String key, Component component) {
		logHelpKey(key);
		return HelpUtility.helpIcon(dict, application, i18NSource, contextHelp, key, showKeys, component, Placement.RIGHT);
	}

	public void showHelp(Component component, String key, Placement placement) {
		logHelpKey(key);
		HelpUtility.showHelpFor(dict, application, i18NSource, contextHelp, key, showKeys, component, placement);
	}

	public ComponentContainer wrapComponentWithHelp(Component component, String key) {
		return wrapComponentWithHelp(component, key, Placement.RIGHT, Placement.RIGHT);
	}

	public ComponentContainer wrapComponentWithHelp(Component component, String key, Placement iconPlacement, Placement popupPlacement) {
		logHelpKey(key);
		Button icon = HelpUtility.helpIcon(dict, application, i18NSource, contextHelp, key, showKeys, component, popupPlacement == null ? Placement.RIGHT
				: popupPlacement);
		AbstractOrderedLayout container;
		if (iconPlacement == Placement.ABOVE || iconPlacement == Placement.BELOW) {
			container = new VerticalLayout();
		} else {
			container = new HorizontalLayout();
		}
		container.addStyleName("help-wrapper");
		if (iconPlacement == Placement.ABOVE || iconPlacement == Placement.LEFT) {
			container.addComponent(icon);
			container.addComponent(component);
		} else {
			container.addComponent(component);
			container.addComponent(icon);
		}

		container.setExpandRatio(icon, 0.0f);
		container.setExpandRatio(component, 1.0f);

		return container;
	}

	public ContextHelp getContextHelp() {
		return contextHelp;
	}

	public boolean isShowKeys() {
		return showKeys;
	}
	
	public void logHelpKey(String key) {
		logHelpKey(dictionaryName, key);
	}
	
	public void logHelpKey(String dictionary, String key){
		if(isShowKeys()){
			initializeLogger();
			try {
				helpKeysOutputFileStream.append(dictionary).append(";").append(key).append("\r\n");
				helpKeysOutputFileStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void initializeLogger() {
		if(helpKeysOutputFileStream == null){
			try {
				helpKeysOutputFileStream = new BufferedWriter(new FileWriter(helpKeysOutputFileName, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			helpKeysOutputFileStream.flush();
			helpKeysOutputFileStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
