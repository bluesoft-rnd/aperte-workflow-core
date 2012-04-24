#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.Set;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.Component;

/**
 * Class responsible for widget's logic.
 */
@AliasName(name = "${widgetName}")
@ChildrenAllowed(value = false)
public class SampleWidget extends BaseProcessToolVaadinWidget {

	@Override
	public void setContext(ProcessStateConfiguration state,
			ProcessStateWidget configuration, I18NSource i18nSource,
			ProcessToolBpmSession bpmSession, Application application,
			Set<String> permissions, boolean isOwner) {

		// TODO: here you can access widget's context
	}

	@Override
	public Component render() {
		return new SampleWidgetComponent() {

			@Override
			protected void loadData() {
				// TODO: implement to describe widget's GUI behavior, populate it with data, etc.
			}
		};
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		// TODO: if children are allowed, replace it with your logic
		throw new UnsupportedOperationException();

	}

}
