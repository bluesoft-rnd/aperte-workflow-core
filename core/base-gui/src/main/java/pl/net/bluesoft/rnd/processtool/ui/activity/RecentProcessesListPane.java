package pl.net.bluesoft.rnd.processtool.ui.activity;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Calendar;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class RecentProcessesListPane extends MyProcessesListPane {

	private Calendar minDate;

	public RecentProcessesListPane(ActivityMainPane activityMainPane, String title, Calendar minDate) {
		super(activityMainPane, title);
		this.minDate = minDate;
	}

	@Override
	protected List<ProcessInstance> getProcessInstances(String filterExpression, int offset, int limit) {
        //rather straightforward approach
		ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
		return ctx.getProcessInstanceDAO().getRecentProcesses(getBpmSession().getUser(ctx), minDate, filterExpression,
                offset, limit);
	}
}
