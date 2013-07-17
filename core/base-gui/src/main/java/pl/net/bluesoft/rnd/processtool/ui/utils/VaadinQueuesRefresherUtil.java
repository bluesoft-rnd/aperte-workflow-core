package pl.net.bluesoft.rnd.processtool.ui.utils;

import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import com.vaadin.ui.Window;

public class VaadinQueuesRefresherUtil 
{
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
		String refreshInterval = ProcessToolContext.Util.getThreadProcessToolContext().getSetting(BasicSettings.REFRESHER_INTERVAL_SETTINGS_KEY);
		if(refreshInterval == null || refreshInterval.isEmpty())
			return;
		
		Integer interval = Integer.parseInt(refreshInterval);
		VaadinQueuesRefresherUtil.changeRefreshInterval(mainWindow, interval);
		
	}
}
