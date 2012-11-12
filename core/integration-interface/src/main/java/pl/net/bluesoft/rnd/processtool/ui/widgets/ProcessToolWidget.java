package pl.net.bluesoft.rnd.processtool.ui.widgets;

import com.vaadin.Application;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEvent;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEventBus;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolWidget {

	void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration,
	                I18NSource i18NSource, ProcessToolBpmSession bpmSession,
	                Application application,
	                Set<String> permissions,
	                boolean isOwner);
	
	void setParent(ProcessToolWidget parent);
	void addChild(ProcessToolWidget child);
	void setGeneratorKey(String key);
	String getGeneratorKey();
	ProcessStateWidget getConfiguration();
	String getAttributeValue(String key);

	Application getApplication();
    boolean hasVisibleData();

	void setWidgetEventBus(WidgetEventBus widgetEventBus);
	void handleWidgetEvent(WidgetEvent event);
}
