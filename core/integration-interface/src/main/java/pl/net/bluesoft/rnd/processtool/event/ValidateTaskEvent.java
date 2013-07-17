package pl.net.bluesoft.rnd.processtool.event;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;


public class ValidateTaskEvent extends AbstractBusEvent 
{
	private BpmTask task;
	private String feedBack;
	
	public ValidateTaskEvent(BpmTask task)
	{
		this.task = task;
	}

	public BpmTask getBpmTask() {
		return task;
	}

	public String getFeedBack() {
		return feedBack;
	}

	public void setFeedBack(String feedBack) {
		this.feedBack = feedBack;
	}
}
