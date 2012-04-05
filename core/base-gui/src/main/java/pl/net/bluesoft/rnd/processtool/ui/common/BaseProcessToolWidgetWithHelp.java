package pl.net.bluesoft.rnd.processtool.ui.common;

import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.help.HelpFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class BaseProcessToolWidgetWithHelp extends BaseProcessToolWidget implements UserFinder {
	protected ContextHelp contextHelp;
	protected HelpFactory helpFactory;
	private String helpDictionaryName;

	protected BaseProcessToolWidgetWithHelp(String helpDictionaryName) {
		this.helpDictionaryName = helpDictionaryName;
	}

	private void initHelpFactory() {
		if (helpFactory == null) {
			contextHelp = new ContextHelp();
			getApplication().getMainWindow().getContent().addComponent(contextHelp);
			helpFactory = new HelpFactory(getProcessDefinition(), getApplication(), getI18NSource(), getHelpDictionaryName(), contextHelp);
		}
	}

	public Component getHelpIcon(String key) {
		if (cannotEdit()) return new Label("");
		initHelpFactory();
		return helpFactory.helpIcon(key);
	}

	public Component getHelpIcon(String key, String message) {
		if (cannotEdit()) return new Label("");
		initHelpFactory();
		return helpFactory.helpIcon(key, message);
	}

	private boolean cannotEdit() {
		return !hasPermission("EDIT");
	}

	public Field wrapFieldWithHelp(Field field, String key) {
		if (cannotEdit()) return field;
		initHelpFactory();
		return helpFactory.wrapField(field, key);
	}

	public Component wrapComponentWithHelp(Component component, String key) {
		if (cannotEdit()) return component;
		initHelpFactory();
		return helpFactory.wrapComponentWithHelp(component, key);
	}

	public Component wrapComponentWithHelp(Component component, String key, Placement iconPlacement, Placement popupPlacement) {
		if (cannotEdit()) return component;
		initHelpFactory();
		return helpFactory.wrapComponentWithHelp(component, key, iconPlacement, popupPlacement);
	}

	public String getHelpDictionaryName() {
		return helpDictionaryName;
	}

	public HelpFactory getHelpFactory() {
		initHelpFactory();
		return helpFactory;
	}

	/**
	 * @deprecated contextHelpIsAttachedToMainWindow
	 */
	@Deprecated
	protected void attachContextHelpToLayout(){};

	protected abstract ProcessDefinitionConfig getProcessDefinition();

	public UserData getUserByLogin(String login) {
		if (login != null && getApplication() instanceof GenericVaadinPortlet2BpmApplication) {
			return ((GenericVaadinPortlet2BpmApplication)getApplication()).getUser(login);
		}
		return null;
	}

	public UserData getUserByEmail(String email) {
		if (email != null && getApplication() instanceof GenericVaadinPortlet2BpmApplication) {
			return ((GenericVaadinPortlet2BpmApplication)getApplication()).getUserByEmail(email);
		}
		return null;
	}
}
