package pl.net.bluesoft.rnd.processtool.ui.activity;

import java.util.Calendar;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;

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
	public List<BpmTask> getBpmTasks() {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        return getBpmSession().findRecentTasks(minDate, offset, limit, ctx);
	}

	@Override
	protected ProcessInstanceFilter getDefaultFilter() {
		ProcessInstanceFilter tfi = new ProcessInstanceFilter();
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		tfi.addOwner(getBpmSession().getUser(ctx));
		tfi.setUpdatedAfter(minDate.getTime());
		tfi.addQueueType(QueueType.ASSIGNED_TO_CURRENT_USER);
		return tfi;
	}
}
