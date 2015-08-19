package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.util.lang.Strings;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static pl.net.bluesoft.util.lang.Strings.withEnding;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 22:06
 */
public class TaskUtil {
	public static String getTaskLink(BpmTask task, ProcessToolContext ctx) {
		String activityPortletUrl = ctx.getSetting(BasicSettings.ACTIVITY_PORTLET_URL);
        String url = Strings.hasLength(activityPortletUrl) ? withEnding(activityPortletUrl, "/") : null;
        return url != null ? Strings.withRequestParameter(url, ProcessToolBpmConstants.REQUEST_PARAMETER_TASK_ID, task.getInternalTaskId()) : "";
	}

    public static void saveComment(BpmTask task, UserData user, IUserSource userSource, String comment) {

        UserData actionPerformer = user;
        String taskOwner = task.getAssignee();

        String authorLogin = actionPerformer.getLogin();
        String authorFullName = actionPerformer.getRealName();

        ProcessComment processComment = new ProcessComment();

        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
        processComment.setCreateTime(calendar.getTime());
        processComment.setProcessState(task.getTaskName());
        processComment.setBody(comment);
        processComment.setAuthorLogin(authorLogin);
        processComment.setAuthorFullName(authorFullName);

		     /* Action performed by task owner*/
        if(taskOwner.equals(authorLogin))
        {
            processComment.setAuthorLogin(authorLogin);
            processComment.setAuthorFullName(authorFullName);
        }
		    /* Action performed by substituting user */
        else
        {
            UserData owner = userSource.getUserByLogin(taskOwner);
            processComment.setAuthorLogin(owner.getLogin());
            processComment.setAuthorFullName(owner.getRealName());
            processComment.setSubstituteLogin(authorLogin);
            processComment.setSubstituteFullName(authorFullName);

        }

        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();

        pi.addComment(processComment);
        pi.setSimpleAttribute("commentAdded", "true");
    }
}
