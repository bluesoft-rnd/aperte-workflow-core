package pl.net.bluesoft.rnd.processtool.token;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;

/**
 * Token wrapper 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TokenWrapper 
{
	private AccessToken token;
	private UserData user;
	private BpmTask task;

	public TokenWrapper(AccessToken token, UserData user, BpmTask task) 
	{
		this.token = token;
		this.user = user;
		this.task = task;
	}

	public AccessToken getToken() {
		return token;
	}

	public UserData getUser() {
		return user;
	}

	public BpmTask getTask() {
		return task;
	}
	
	public String getTokenAction()
	{
		return token.getActionName();
	}
}
