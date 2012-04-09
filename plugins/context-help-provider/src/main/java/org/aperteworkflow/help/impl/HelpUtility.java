package org.aperteworkflow.help.impl;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.BaseTheme;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Created by IntelliJ IDEA. User: mwysocki_bls Date: 8/29/11 Time: 12:33 PM To
 * change this template use File | Settings | File Templates.
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
		String message = getMessageFromDictionary(dict, i18NSource, key, showDebugKey);

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

	private static String getMessageFromDictionary(ProcessDictionary dict, I18NSource i18NSource, String key, Boolean showDebugKey) {
		ProcessDictionaryItem dictItem = dict.lookup(key);
		String message = dictItem == null ? i18NSource.getMessage("help.empty").replaceAll("\\{0\\}", key) : (String) dictItem.getValueForCurrentDate()
				.getValue();

		if (showDebugKey) {
			message += "<br/>" + "<br/>" + "<small>" + i18NSource.getMessage("help.dictionary") + ": " + dict.getDictionaryName() + "</small>" + "<br/>"
					+ "<small>" + i18NSource.getMessage("help.key") + ": " + key + "</small>";
		}

		return message;
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
			Boolean showDebugKey, Component component, Placement placement) {
		// Map<String, Boolean> map = new HashMap<String, Boolean>();
		// map.put("hidden", Boolean.TRUE);
		// contextHelp.changeVariables(null, map);
		if (placement == null)
			placement = Placement.RIGHT;

		String message = getMessageFromDictionary(dict, i18NSource, key, showDebugKey);
		contextHelp.addHelpForComponent(component, message, placement);

		// map.put("hidden", "true");
		// contextHelp.changeVariables(null, map);
		contextHelp.showHelpFor(component);
		// map.put("hidden", Boolean.FALSE);
		// contextHelp.changeVariables(null, map);
	}
}
