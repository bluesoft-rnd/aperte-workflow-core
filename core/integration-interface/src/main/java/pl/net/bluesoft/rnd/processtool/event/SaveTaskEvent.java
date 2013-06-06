package pl.net.bluesoft.rnd.processtool.event;


public class SaveTaskEvent extends AbstractBusEvent 
{
	private String taskId;
	private String feedBack;
	
	public SaveTaskEvent(String taskId)
	{
		this.taskId = taskId;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getFeedBack() {
		return feedBack;
	}

	public void setFeedBack(String feedBack) {
		this.feedBack = feedBack;
	}
}
