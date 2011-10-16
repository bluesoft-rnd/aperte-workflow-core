package pl.net.bluesoft.rnd.processtool.ui.activity;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;
import pl.net.bluesoft.rnd.util.liferay.LiferayBridge;

import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

/**
 * User: POlszewski
 * Date: 2011-09-06
 * Time: 10:44:16
 */
public class OtherUserProcessesListPane extends MyProcessesListPane {
    private final UserData userData;
    private ProcessToolBpmSession bmpSession;

    public OtherUserProcessesListPane(ActivityMainPane activityMainPane, String title, UserData userData) {
		super(activityMainPane, title, false);
		this.userData = userData;
        init(title);
	}

    @Override
    protected void displayProcessData(ProcessInstance processInstance) {
        activityMainPane.displayProcessData(processInstance, getBpmSession());
    }

    @Override
    protected ProcessToolBpmSession getBpmSession() {
        if (bmpSession == null) {
            ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
            bmpSession = activityMainPane.getBpmSession().createSession(userData, LiferayBridge.getUserRoles(userData), ctx);
        }
        return bmpSession;
    }
}
