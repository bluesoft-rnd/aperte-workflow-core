package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import com.vaadin.Application;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEvent;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEventBus;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 */
public abstract class BaseProcessToolWidget implements ProcessToolWidget {
	protected ProcessStateConfiguration state;
	protected ProcessStateWidget configuration;
	protected ProcessToolWidget parent;
	protected I18NSource i18NSource;
	protected ProcessToolBpmSession bpmSession;
	protected Set<String> permissions;
	protected String generatorKey;
	protected Map<String, String> attributes = new HashMap<String, String>();
	protected boolean isOwner;
	private Application application;
	protected WidgetEventBus widgetEventBus;

	@Override
	public void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration, I18NSource i18NSource,
			ProcessToolBpmSession bpmSession, Application application, Set<String> permissions, boolean isOwner) {
		this.state = state;
		this.configuration = configuration;
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
		this.permissions = permissions;
		this.isOwner = isOwner;
		this.application = application;
		for (ProcessStateWidgetAttribute attr : configuration.getAttributes()) {
			attributes.put(attr.getName(), attr.getValue());
		}
        PropertyAutoWiring.autowire(this, attributes);
	}

	@Override
	public Application getApplication() {
		return application;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getAttributeValue(String key) {
		return attributes.get(key);
	}

    public UserData getLiferayUser() {
        return (UserData) application.getUser();
    }

    public UserData getBpmUser() {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        return bpmSession.getUser(ctx);
    }

    public UserData getBpmUser(UserData userData) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        return bpmSession.loadOrCreateUser(ctx, userData);
    }

    public Map<String, UserAttribute> getUserAttributes() {
        return getBpmUser().getMainAttributesMap();
    }

    public Map<String, UserAttribute> getUserAttributes(UserData userData) {
        return getBpmUser(userData).getMainAttributesMap();
    }

    public UserAttribute getUserAttribute(String key) {
        return getBpmUser().findAttribute(key);
    }

    public UserAttribute getUserAttribute(UserData userData, String key) {
        return getBpmUser(userData).findAttribute(key);
    }

    public void setUserAttributes(UserAttribute... attributes) {
        UserData bpmUser = getBpmUser();
        bpmUser.removeAllAttributes();
        prepareAndSaveUserAttributes(bpmUser, attributes);
    }

    public void setUserAttributes(UserData userData, UserAttribute... attributes) {
        UserData bpmUser = getBpmUser(userData);
        bpmUser.removeAllAttributes();
        prepareAndSaveUserAttributes(bpmUser, attributes);
    }

    public void addUserAttributes(UserAttribute... attributes) {
        prepareAndSaveUserAttributes(getBpmUser(), attributes);
    }

    public void addUserAttributes(UserData userData, UserAttribute... attributes) {
        prepareAndSaveUserAttributes(getBpmUser(userData), attributes);
    }

    private void prepareAndSaveUserAttributes(UserData bpmUser, UserAttribute... attributes) {
        for (UserAttribute a : attributes) {
            bpmUser.setAttribute(a);
        }
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        ctx.getUserDataDAO().saveOrUpdate(bpmUser);
    }

	protected boolean hasPermission(String... names) {
		boolean canView = !isOwner && Arrays.asList(names).contains("VIEW");
		for (String name : names) {
			if (permissions.contains(name) && (isOwner || canView))
				return true;
		}
		return permissions.contains("*");
	}

	public ProcessStateConfiguration getState() {
		return state;
	}

	public void setState(ProcessStateConfiguration state) {
		this.state = state;
	}

	@Override
	public ProcessStateWidget getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ProcessStateWidget configuration) {
		this.configuration = configuration;
	}

	public ProcessToolWidget getParent() {
		return parent;
	}

	@Override
	public void setParent(ProcessToolWidget parent) {
		this.parent = parent;
	}

	public String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public void setI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	public ProcessToolBpmSession getBpmSession() {
		return bpmSession;
	}

	public void setBpmSession(ProcessToolBpmSession bpmSession) {
		this.bpmSession = bpmSession;
	}

	public <T extends ProcessInstanceAttribute> Collection<T> getAttributes(Class<T> cls, ProcessInstance pi) {
		Collection<T> res = new HashSet<T>();
		for (ProcessInstanceAttribute attr : pi.getProcessAttributes()) {
			if (attr.getClass().isAssignableFrom(cls)) {
				res.add((T) attr);
			}
		}
		return res;
	}

	public <T extends ProcessInstanceAttribute> T getAttribute(Class<T> cls, ProcessInstance pi) {
		Collection<T> collection = getAttributes(cls, pi);
		if (collection.isEmpty()) {
			return null;
		} else {
			return collection.iterator().next();
		}
	}

	public<T extends ProcessInstanceAttribute> T getAttribute(Class<T> cls, String key, ProcessInstance pi) {
		Collection<T> collection = getAttributes(cls, pi);
		if (collection.isEmpty()) {
			return null;
		} else {
			for (T t : collection) {
				if (t.getKey().equals(key))
				return t;
			}
			return null;
		}
	}

	public String getSimpleAttribute(String key, ProcessInstance pi) {
		Collection<ProcessInstanceSimpleAttribute> collection = getAttributes(ProcessInstanceSimpleAttribute.class, pi);
		for (ProcessInstanceSimpleAttribute a : collection) {
			if (a.getKey() != null && a.getKey().equals(key)) {
				return a.getValue();
			}
		}
		return null;
	}

	public void setSimpleAttribute(String key, String value, ProcessInstance pi) {
		Collection<ProcessInstanceSimpleAttribute> collection = getAttributes(ProcessInstanceSimpleAttribute.class, pi);
		boolean found = false;
		for (ProcessInstanceSimpleAttribute a : collection) {
			if (a.getKey().equals(key)) {
				a.setValue(value);
				found = true;
			}
		}
		if (!found) {
			ProcessInstanceSimpleAttribute processInstanceSimpleAttribute = new ProcessInstanceSimpleAttribute();
			processInstanceSimpleAttribute.setValue(value);
			processInstanceSimpleAttribute.setKey(key);
			pi.addAttribute(processInstanceSimpleAttribute);
		}
	}

    @Override
    public boolean hasVisibleData() {
        return true;
    }

	@Override
	public void setWidgetEventBus(WidgetEventBus widgetEventBus) {
		this.widgetEventBus = widgetEventBus;
	}

	@Override
	public void handleWidgetEvent(WidgetEvent event) {
	}

	public String getGeneratorKey() {
		return generatorKey;
	}

	@Override
	public void setGeneratorKey(String generatorKey) {
		this.generatorKey = generatorKey;
	}
}
