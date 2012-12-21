package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.util.lang.Strings;

import static pl.net.bluesoft.util.lang.Strings.withEnding;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 22:06
 */
public class TaskUtil {
	public static String getTaskLink(BpmTask task, ProcessToolContext ctx) {
		String activityPortletUrl = ctx.getSetting(ProcessToolContext.ACTIVITY_PORTLET_URL);
        String url = Strings.hasLength(activityPortletUrl) ? withEnding(activityPortletUrl, "/") : null;
        return url != null ? Strings.withRequestParameter(url, ProcessToolBpmConstants.REQUEST_PARAMETER_TASK_ID, task.getInternalTaskId()) : "";
	}
}
