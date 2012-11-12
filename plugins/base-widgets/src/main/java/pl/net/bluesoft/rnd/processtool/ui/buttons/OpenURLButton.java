package pl.net.bluesoft.rnd.processtool.ui.buttons;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.YesNoDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;

@AliasName(name = "OpenURLButton")
public class OpenURLButton extends StandardValidatingButton {

	@AutoWiredProperty
	private String url = "";
	
    @Override
    protected void showValidationErrorsOrSave(final WidgetContextSupport support, final Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
		if(url.matches("#\\{.*\\}")){
        	String urlKey = url.replaceAll("#\\{(.*)\\}", "$1");
        	ProcessInstanceAttribute attr = task.getProcessInstance().findAttributeByKey(urlKey);
        	if(attr != null)
        		url = ((ProcessInstanceSimpleAttribute)attr).getValue();
        }
        getApplication().getMainWindow().open(new ExternalResource(url), "_new");
    }
    
    @Override
    public void setContext(ProcessStateAction processStateAction,
    		ProcessToolBpmSession bpmSession, Application application,
    		I18NSource messageSource) {
    	super.setContext(processStateAction, bpmSession, application, messageSource);
		PropertyAutoWiring.autowire(this, getAutowiredProperties());
    }
}
