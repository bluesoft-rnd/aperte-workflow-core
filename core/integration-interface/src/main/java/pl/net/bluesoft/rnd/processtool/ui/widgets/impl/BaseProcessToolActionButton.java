package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateActionAttribute;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionCallback;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.util.lang.Formats.nvl;

public abstract class BaseProcessToolActionButton implements ProcessToolActionButton {
	@AutoWiredProperty
	protected String label;

	@AutoWiredProperty
	protected String description;

	@AutoWiredProperty
	protected boolean skipSaving = false;

	@AutoWiredProperty
	protected boolean autoHide = false;

	@AutoWiredProperty
	protected String bpmAction;

	@AutoWiredProperty
	protected String markProcessImportant;

	@AutoWiredProperty
	protected String styleName = "default";

    protected Application application;
	protected I18NSource messageSource;
	protected boolean enabled = true;

	protected ProcessToolBpmSession bpmSession;
	protected ProcessStateAction definition;
	protected UserData loggedUser;
	protected UserData substitutingUser;

	protected ProcessToolActionCallback callback;

	public void setContext(ProcessStateAction processStateAction, ProcessToolBpmSession bpmSession,
			Application application, I18NSource messageSource) {
		this.application = application;
		this.messageSource = messageSource;
		this.bpmSession = bpmSession;
		this.definition = processStateAction;
		ProcessToolContext ctx = getCurrentContext();
        this.substitutingUser = bpmSession.getSubstitutingUser(ctx);
        this.loggedUser = bpmSession.getUser(ctx);
		PropertyAutoWiring.autowire(this, getAutowiredProperties());
	}

	protected Map<String, String> getAutowiredProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("buttonName", definition.getButtonName());
		map.put("autoHide", String.valueOf(definition.getAutohide()));
		map.put("description", definition.getDescription());
		map.put("label", definition.getLabel());
		map.put("bpmAction", definition.getBpmName());
		map.put("skipSaving", String.valueOf(definition.getSkipSaving()));
		map.put("markProcessImportant", String.valueOf(definition.getMarkProcessImportant()));
		map.put("priority", String.valueOf(definition.getPriority()));
		for (ProcessStateActionAttribute attr : definition.getAttributes()) {
			map.put(attr.getName(), attr.getValue());
		}
		return map;
	}

	protected String getComponentStyleName() {
		return styleName;
	}

	protected String getMessage(String key) {
		return messageSource.getMessage(key);
	}

	public I18NSource getMessageSource() {
		return messageSource;
	}

	protected String getVisibleLabel() {
		return getMessage(nvl(label, description));
	}

	protected String getVisibleDescription() {
		return getMessage(nvl(description, label));
	}

	protected ProcessToolContext getCurrentContext() {
		if (callback != null && callback.getWidgetContextSupport() != null) {
			return callback.getWidgetContextSupport().getCurrentContext();
		}
		return ProcessToolContext.Util.getThreadProcessToolContext();
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setActionCallback(ProcessToolActionCallback callback) {
		this.callback = callback;
	}

	public I18NSource getApplicationI18NSource() {
		return (I18NSource)application;
	}
}
