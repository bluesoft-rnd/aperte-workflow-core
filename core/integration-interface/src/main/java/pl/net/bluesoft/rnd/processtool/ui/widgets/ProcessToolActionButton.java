package pl.net.bluesoft.rnd.processtool.ui.widgets;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolActionButton {
//	void onButtonPress(ProcessInstance processInstance,
//	                     ProcessToolContext ctx,
//	                     Set<ProcessToolDataWidget> dataWidgets,
//	                     Map<ProcessToolDataWidget, Collection<String>> validationErrors,
//	                     ProcessToolActionCallback callback);
    void setContext(ProcessStateAction processStateAction, ProcessToolBpmSession bpmSession,
                    Application application, I18NSource messageSource);
    void setActionCallback(ProcessToolActionCallback callback);

	boolean isVisible(ProcessInstance processInstance);
	boolean isEnabled(ProcessInstance processInstance);
    void setEnabled(boolean enabled);
	void changeButton(Button button);

	String getLabel(ProcessInstance processInstance);
	String getDescription(ProcessInstance processInstance);
	void setLoggedUser(UserData userData);
	boolean isAutoHide();

	void setDefinition(ProcessStateAction psa);

	void saveData(BpmTask pi);

    void loadData(BpmTask task);
}
