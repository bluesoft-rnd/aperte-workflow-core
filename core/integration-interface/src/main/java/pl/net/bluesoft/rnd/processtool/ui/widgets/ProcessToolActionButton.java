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
    void setContext(ProcessStateAction processStateAction,
                    ProcessToolBpmSession bpmSession,
                    Application application,
                    I18NSource messageSource);

    boolean isEnabled();

    void setActionCallback(ProcessToolActionCallback callback);

	boolean isVisible(BpmTask task);
	boolean isEnabled(BpmTask task);
    void setEnabled(boolean enabled);
	void changeButton(Button button);

	String getLabel(BpmTask task);
	String getDescription(BpmTask task);
	void setLoggedUser(UserData userData);
	boolean isAutoHide();

	void setDefinition(ProcessStateAction psa);

	void saveData(BpmTask task);

    void loadData(BpmTask task);
}
