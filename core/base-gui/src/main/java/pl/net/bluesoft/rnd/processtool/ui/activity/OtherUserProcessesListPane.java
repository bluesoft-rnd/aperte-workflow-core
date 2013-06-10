package pl.net.bluesoft.rnd.processtool.ui.activity;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;

/**
 * User: POlszewski
 * Date: 2011-09-06
 * Time: 10:44:16
 */
public class OtherUserProcessesListPane extends MyProcessesListPane {
    private UserData userData;
    private ProcessToolBpmSession bpmSession;
    private String baseTitle;

    public OtherUserProcessesListPane(ActivityMainPane activityMainPane, String title) {
        super(activityMainPane, title);
        this.baseTitle = title;
    }

    public void setUserData(UserData userData) {
		if (!pl.net.bluesoft.util.lang.Lang.equals(getLogin(userData), getLogin(this.userData))) {
			bpmSession = null;
		}
        this.userData = userData;
        setTitle(baseTitle + " " + userData.getRealName());
    }

    @Override
    protected void displayProcessData(BpmTask task) {
        activityMainPane.displayProcessData(task, getBpmSession());
    }

    @Override
    protected ProcessToolBpmSession getBpmSession() 
    {
        if (bpmSession == null) 
        {
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            
            IUserSource userSoruce = ObjectFactory.create(IUserSource.class);
            
            userData = userSoruce.getUserByLogin(userData.getLogin(), userData.getCompanyId());
            bpmSession = activityMainPane.getBpmSession().createSession(userData, userData.getRoleNames(), ctx);
        }
        return bpmSession;
    }
}
