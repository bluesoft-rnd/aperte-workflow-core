package org.aperteworkflow.help.impl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Created by IntelliJ IDEA. User: mwysocki_bls Date: 8/29/11 Time: 12:33 PM To
 * change this template use File | Settings | File Templates.
 * 
 * @author mpawlak
 */
public class HelpUtility {
	public static Component helpIcon(final Application application, final I18NSource i18NSource, ContextHelp contextHelp, String dictionary, String key,
			Boolean showDebugKey, Component optionalComponent, Placement popupPlacement) {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessDictionaryRegistry processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
		ProcessDictionary dict = processDictionaryRegistry.getSpecificOrDefaultGlobalDictionary("db", dictionary, i18NSource.getLocale().toString());

		return helpIcon(dict, application, i18NSource, contextHelp, key, showDebugKey, optionalComponent, popupPlacement);
	}

	public static Button helpIcon(final ProcessDictionary dict, final Application application, final I18NSource i18NSource, final ContextHelp contextHelp,
			final String key, Boolean showDebugKey, final Component optionalComponent, Placement popupPlacement) {
		String message = getMessageFromDictionary(dict, i18NSource, key, canUserEditDictionaries(application), showDebugKey);

		if (popupPlacement == null) {
			popupPlacement = Placement.RIGHT;
		}

		final Button icon = helpIconButton(application);
		icon.setWidth(20, Sizeable.UNITS_PIXELS);
		icon.setHeight(20, Sizeable.UNITS_PIXELS);
		if (optionalComponent != null) {
			contextHelp.addHelpForComponent(optionalComponent, message, popupPlacement);
		} else {
			contextHelp.addHelpForComponent(icon, message, popupPlacement);
		}

		icon.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {

				if (optionalComponent != null) {
					contextHelp.showHelpFor(optionalComponent);
					// showHelpFor(dict, application, i18NSource, contextHelp,
					// key, optionalComponent, Placement.RIGHT);
				} else {
					contextHelp.showHelpFor(icon);
				}
				// displayHelpNotification(application, i18NSource, message);
			}
		});

		return icon;
	}

	private static String getMessageFromDictionary(ProcessDictionary dict, I18NSource i18NSource, String key, boolean showEditButton, boolean showDebugKey) 
	{
		ProcessDictionaryItem dictItem = dict.lookup(key);
		
		StringBuilder messageBuilder = new StringBuilder();
		
		messageBuilder.append("<div id=\"editor1\">");
		
		if(dictItem == null)
			messageBuilder.append(i18NSource.getMessage("help.empty").replaceAll("\\{0\\}", key));
		else
			messageBuilder.append(dictItem.getValueForCurrentDate().getValue());
		
		messageBuilder.append("</div>");
		
		/* Add edit icon */
		if(showEditButton)
		{
			String processDefinitionName = dict.getProcessDefinition() == null ? null : dict.getProcessDefinition().getBpmDefinitionKey();
			String dictionaryId = dict.getDictionaryId();
			String languageCode = i18NSource.getLocale().toString();
			String dictionaryItemKey = key;
			String dictionaryItemValue = dictItem == null ? "" : dictItem.getValueForCurrentDate().getValue().toString();
			
			String escapedValue = StringEscapeUtils.escapeHtml4(dictionaryItemValue);
			
			
			messageBuilder.append("<br/>");
			messageBuilder.append("<input type=\"button\" value=\""+i18NSource.getMessage("help.popup.edit")+"\" name=\"editButton\" onClick=\"showEditHelpContextPopup('");
			messageBuilder.append(processDefinitionName);
			messageBuilder.append("','");
			messageBuilder.append(dictionaryId);
			messageBuilder.append("','");
			messageBuilder.append(languageCode);
			messageBuilder.append("','");
			messageBuilder.append(dictionaryItemKey);
			messageBuilder.append("','");
			messageBuilder.append(escapedValue);
			messageBuilder.append("')\" />");
			
			
		}
		

		if (showDebugKey) 
		{
			messageBuilder.append("<br/><br/><small>");
			messageBuilder.append(i18NSource.getMessage("help.dictionary"));
			messageBuilder.append(": ");
			messageBuilder.append(dict.getDictionaryName());
			messageBuilder.append("</small><br/><small>");
			messageBuilder.append(i18NSource.getMessage("help.key"));
			messageBuilder.append(": ");
			messageBuilder.append(key);
			messageBuilder.append("</small>");
		}
		


		return messageBuilder.toString();
	}

	private static Button helpIconButton(Application application) {
		Button b = new Button();
		b.addStyleName(BaseTheme.BUTTON_LINK);
		b.addStyleName("help_button");
		b.setWidth(12, Sizeable.UNITS_PIXELS);
		b.setHeight(12, Sizeable.UNITS_PIXELS);
		b.setImmediate(true);
		return b;
	}

	public static void showHelpFor(ProcessDictionary dict, Application application, I18NSource i18NSource, ContextHelp contextHelp, String key,
			Boolean showDebugKey, Component component, Placement placement)
	{

		if (placement == null)
			placement = Placement.RIGHT;

		String message = getMessageFromDictionary(dict, i18NSource, key, canUserEditDictionaries(application), showDebugKey);
		contextHelp.addHelpForComponent(component, message, placement);

		// map.put("hidden", "true");
		// contextHelp.changeVariables(null, map);
		contextHelp.showHelpFor(component);
		// map.put("hidden", Boolean.FALSE);
		// contextHelp.changeVariables(null, map);
	}
	
	private static boolean canUserEditDictionaries(Application application)
	{
		UserData user = (UserData)application.getUser();
		
		boolean canEdit = user.getRoleNames().contains("CHANGE_HELP_TOOLTIPS");
		
		return canEdit;
	}
}
