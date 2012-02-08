package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class BaseProcessToolWidget implements ProcessToolWidget {
	protected ProcessStateConfiguration state;
	protected ProcessStateWidget configuration;
	protected ProcessToolWidget parent;
	protected I18NSource i18NSource;
	protected ProcessToolBpmSession bpmSession;
	protected Set<String> permissions;
	protected Map<String, String> attributes = new HashMap();
	protected boolean isOwner;
	private Application application;

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
	}

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

	public ProcessStateWidget getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ProcessStateWidget configuration) {
		this.configuration = configuration;
	}

	public ProcessToolWidget getParent() {
		return parent;
	}

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
				if (((ProcessInstanceAttribute)t).getKey().equals(key))
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
}
