package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;

/**
 * Widget factory class. Class name is provided by process tool registry
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WidgetFactory 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	private ProcessToolBpmSession bpmSession;
	private Application application;
	private I18NSource i18NSource;
	
	public WidgetFactory(ProcessToolBpmSession bpmSession, Application application, I18NSource i18NSource)
	{
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
		this.application = application;
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	public <T extends ProcessToolWidget> T makeWidget(String name, ProcessStateWidget processStateWidget, Set<String> permissions, boolean isOwner) 
			throws IllegalAccessException, InstantiationException 
	{
		Class<? extends ProcessToolWidget> aClass = processToolRegistry.getWidgetClassName(name);
		if (aClass == null) {
			throw new IllegalAccessException("No class nicknamed by: " + name);
		}
		T newWidget = (T) aClass.newInstance();
		
		newWidget.setContext(processStateWidget.getConfig(), processStateWidget, i18NSource, bpmSession, application, permissions, isOwner);
		
		
		return newWidget;

	}
	
    public <T extends ProcessToolWidget> T makeWidget(Class<? extends ProcessToolWidget> aClass) throws IllegalAccessException, InstantiationException {
		return (T) aClass.newInstance();
	}

}
