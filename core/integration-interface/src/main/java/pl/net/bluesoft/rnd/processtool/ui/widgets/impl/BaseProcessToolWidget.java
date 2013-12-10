package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import com.vaadin.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEvent;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEventBus;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Arrays;
import java.util.HashMap;
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
	protected String taskId;
	
    @Autowired
    protected IPortalUserSource userSource;
    
    @Autowired
    protected ProcessToolRegistry processToolRegistry;
	
	protected BaseProcessToolWidget()
	{
    	/* init user source */
		ObjectFactory.inject(this);
		
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

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

	@Override
	public boolean hasPermission(String... names) {
		boolean canView = isOwner || Arrays.asList(names).contains("VIEW");
		for (String name : names) {
			if (permissions.contains(name) && canView) {
				return true;
			}
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

	@Override
	public String getGeneratorKey() {
		return generatorKey;
	}

	@Override
	public void setGeneratorKey(String generatorKey) {
		this.generatorKey = generatorKey;
	}
	
	public String getName()
	{
		return configuration.getName();
	}
}
