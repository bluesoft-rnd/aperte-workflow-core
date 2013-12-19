package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
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
	protected Boolean skipSaving = false;

	@AutoWiredProperty
	protected Boolean autoHide = false;

    @AutoWiredProperty
    protected String iconName;

//	@AutoWiredProperty
	protected String bpmAction;

	@AutoWiredProperty
	protected Boolean markProcessImportant = false;

	@AutoWiredProperty
	protected String styleName;
	
	@AutoWiredProperty
	protected String notification;
	
	@AutoWiredProperty
	protected String actionType = ProcessStateAction.PRIMARY_ACTION;

    @AutoWiredProperty
    protected Integer priority = 1;

    protected Application application;
	protected I18NSource messageSource;
	protected boolean enabled = true;

	protected ProcessToolBpmSession bpmSession;
	protected ProcessStateAction definition;
	protected String loggedUser;
	protected String substitutingUser;

	protected ProcessToolActionCallback callback;

	public void setContext(ProcessStateAction processStateAction, ProcessToolBpmSession bpmSession,
			Application application, I18NSource messageSource) {
		this.application = application;
		this.messageSource = messageSource;
		this.bpmSession = bpmSession;
		this.definition = processStateAction;

		this.loggedUser = bpmSession.getUserLogin();
        this.substitutingUser = bpmSession.getSubstitutingUserLogin();

		PropertyAutoWiring.autowire(this, getAutowiredProperties());
	}

	protected Map<String, String> getAutowiredProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("buttonName", definition.getButtonName());
		map.put("autoHide", String.valueOf(definition.getAutohide()));
		map.put("description", definition.getDescription());
		map.put("label", definition.getLabel());
		map.put("actionType", definition.getActionType());
		map.put("bpmAction", definition.getBpmName());
		map.put("skipSaving", String.valueOf(definition.getSkipSaving()));
		map.put("markProcessImportant", String.valueOf(definition.getMarkProcessImportant()));
		map.put("priority", String.valueOf(definition.getPriority()));
		map.put("url", String.valueOf(definition.getUrl()));
		map.put("title", String.valueOf(definition.getTitle()));
		map.put("question", String.valueOf(definition.getQuestion()));
		map.put("notification", String.valueOf(definition.getNotification()));
		for (ProcessStateActionAttribute attr : definition.getAttributes()) {
			map.put(attr.getName(), attr.getValue());
		}
		return map;
	}

	protected String getComponentStyleName() {
		if(styleName == null){
			if(ProcessStateAction.PRIMARY_ACTION.equals(actionType)){
				return "default";
			}
		}
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSkipSaving() {
        return skipSaving;
    }

    public void setSkipSaving(boolean skipSaving) {
        this.skipSaving = skipSaving;
    }

    public boolean isAutoHide() {
        return autoHide;
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    public String getBpmAction() {
        return bpmAction;
    }

    public void setBpmAction(String bpmAction) {
        this.bpmAction = bpmAction;
    }

    public boolean getMarkProcessImportant() {
        return markProcessImportant;
    }

    public void setMarkProcessImportant(boolean markProcessImportant) {
        this.markProcessImportant = markProcessImportant;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ProcessToolBpmSession getBpmSession() {
        return bpmSession;
    }

    public void setBpmSession(ProcessToolBpmSession bpmSession) {
        this.bpmSession = bpmSession;
    }

    public ProcessStateAction getDefinition() {
        return definition;
    }

    public void setDefinition(ProcessStateAction definition) {
        this.definition = definition;
    }

    public ProcessToolActionCallback getCallback() {
        return callback;
    }

    public void setCallback(ProcessToolActionCallback callback) {
        this.callback = callback;
    }

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
