package pl.net.bluesoft.rnd.processtool.ui.utils;

import org.apache.commons.lang3.StringUtils;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import com.vaadin.ui.Window;


/**
 * Util witch provides tools to support javascript queues refresher
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class QueuesPanelRefresherUtil 
{
	/** Refresh interval in seconds */
	private static final String REFRESHER_INTERVAL_SETTINGS_KEY = "refresher.interval";
	
	public static String getQueueTaskId(String taskName)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "user-task-name-"+fixedTaskName;
	}

	public static String getQueueProcessQueueId(String queueId)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "user-queue-name-"+fixedQueueId;
	}

	public static String getSubstitutedQueueTaskId(String taskName, String userLogin)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-task-name-"+fixedTaskName;
	}
	
	public static String getSubstitutedRootNodeId(String userLogin)
	{	
		return "substituted-"+userLogin+"-user-root-node";
	}

	public static String getSubstitutedQueueProcessQueueId(String queueId, String userLogin)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-queue-name-"+fixedQueueId;
	}
	
	/** Register button with given button id */
	public static void registerUser(Window mainWindow, String userLogin)
	{
		mainWindow.executeJavaScript("setCurrentUser('"+userLogin+"');");
	}
	
	/** Change refresh interval in seconds */
	public static void changeRefreshInterval(Window mainWindow, int seconds)
	{
		mainWindow.executeJavaScript("setRefreshInterval("+seconds*1000+");");
	}

	public static void unregisterUser(Window mainWindow, String login) 
	{
		mainWindow.executeJavaScript("clearRefreshCurrentUser();");
	}
	
	public static void changeRefresherInterval(Window mainWindow)
	{
		String refreshInterval = ProcessToolContext.Util.getThreadProcessToolContext().getSetting(REFRESHER_INTERVAL_SETTINGS_KEY);
		if(refreshInterval == null || refreshInterval.isEmpty())
			return;
		
		Integer interval = Integer.parseInt(refreshInterval);
		QueuesPanelRefresherUtil.changeRefreshInterval(mainWindow, interval);
		
	}
}
