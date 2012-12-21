package pl.net.bluesoft.rnd.processtool.ui.activity;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * User: POlszewski
 * Date: 2011-09-06
 * Time: 10:44:16
 */
public class OtherUserQueueListPane extends QueueListPane {
    private UserData userData;
    private ProcessToolBpmSession bpmSession;

    public OtherUserQueueListPane(ActivityMainPane activityMainPane) {
        super(activityMainPane);
    }

    public void setUserData(UserData userData) {
		if (!pl.net.bluesoft.util.lang.Lang.equals(getLogin(userData), getLogin(this.userData))) {
			bpmSession = null;
		}
        this.userData = userData;
    }

    @Override
    protected void displayProcessData(BpmTask task) {
        activityMainPane.displayProcessData(task, getBpmSession());
    }

    @Override
    protected ProcessToolBpmSession getBpmSession() {
        if (bpmSession == null) {
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            bpmSession = activityMainPane.getBpmSession().createSession(userData, userData.getRoleNames(), ctx);
        }
        return bpmSession;
    }
}
