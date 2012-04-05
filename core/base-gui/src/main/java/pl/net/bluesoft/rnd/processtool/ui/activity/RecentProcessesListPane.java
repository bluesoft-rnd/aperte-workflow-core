package pl.net.bluesoft.rnd.processtool.ui.activity;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.TaskState;

import java.util.Calendar;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class RecentProcessesListPane extends MyProcessesListPane {

	private Calendar minDate;

	public RecentProcessesListPane(ActivityMainPane activityMainPane, String title) {
		super(activityMainPane, title);
	}

    public void setMinDate(Calendar minDate) {
        this.minDate = minDate;
    }

    @Override
	protected ResultsPageWrapper<BpmTask> getBpmTasks() {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        return getBpmSession().findRecentTasks(minDate, offset, limit, ctx);
	}

	@Override
	protected ProcessInstanceFilter getDefaultFilter() {
		ProcessInstanceFilter tfi = new ProcessInstanceFilter();
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		tfi.addOwner(getBpmSession().getUser(ctx));
		tfi.setUpdatedAfter(minDate.getTime());
		tfi.addState(TaskState.OPEN);
		return tfi;
	}
}
