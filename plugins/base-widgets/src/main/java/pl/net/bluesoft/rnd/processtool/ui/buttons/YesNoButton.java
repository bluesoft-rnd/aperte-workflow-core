package pl.net.bluesoft.rnd.processtool.ui.buttons;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.YesNoDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.Button;

/**
 * @author marcin
 */
@AliasName(name = "YesNoButton")
public class YesNoButton extends StandardValidatingButton {
	@AutoWiredProperty
	String title = "processdata.comments.yesno.title";

	@AutoWiredProperty
	String question = "processdata.comments.yesno.help";
	
    @Override
    protected void showValidationErrorsOrSave(final WidgetContextSupport support, final Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
        if (validationErrors.isEmpty()) {
			YesNoDialog dialog = showDialog(new YesNoDialog(title, question));
			dialog.getYesButton().addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					YesNoButton.super.showValidationErrorsOrSave(support, validationErrors);
				}
			});
        }
        else {
            super.showValidationErrorsOrSave(support, validationErrors);
        }
    }
    
    @Override
    public void setContext(ProcessStateAction processStateAction,
    		ProcessToolBpmSession bpmSession, Application application,
    		I18NSource messageSource) {
    	super.setContext(processStateAction, bpmSession, application, messageSource);
		PropertyAutoWiring.autowire(this, getAutowiredProperties());
    }
}
