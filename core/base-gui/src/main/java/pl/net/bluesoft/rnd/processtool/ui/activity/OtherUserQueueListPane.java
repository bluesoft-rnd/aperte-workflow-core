package pl.net.bluesoft.rnd.processtool.ui.activity;

import org.aperteworkflow.util.liferay.LiferayBridge;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

/**
 * User: POlszewski
 * Date: 2011-09-06
 * Time: 10:44:16
 */
public class OtherUserQueueListPane extends QueueListPane {
    private final UserData userData;
    private ProcessToolBpmSession bmpSession;

    public OtherUserQueueListPane(ActivityMainPane activityMainPane, String title, ProcessQueue q, UserData userData) {
		super(activityMainPane, title, q, false);
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
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            bmpSession = activityMainPane.getBpmSession().createSession(userData, LiferayBridge.getUserRoles(userData), ctx);
        }
        return bmpSession;
    }
}
