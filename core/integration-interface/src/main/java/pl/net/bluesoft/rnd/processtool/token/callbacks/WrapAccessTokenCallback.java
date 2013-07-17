package pl.net.bluesoft.rnd.processtool.token.callbacks;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSessionHelper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.processtool.token.exception.NoBpmTaskFoundForTokenException;
import pl.net.bluesoft.rnd.processtool.token.exception.NoUserFoundForTokenException;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Wrap {@link AccessToken} to {@link TokenWrapper}. Look for {@link UserData} with specified login in token and 
 * create {@link BpmTask}
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WrapAccessTokenCallback implements ReturningProcessToolContextCallback<TokenWrapper>
{
	private AccessToken accessToken;
	
	public WrapAccessTokenCallback(AccessToken accessToken)
	{
		this.accessToken = accessToken;
	}

	@Override
	public TokenWrapper processWithContext(ProcessToolContext ctx) 
	{
		/* Get user associated with current token */
		UserData user = ctx.getUserDataDAO().loadUserByLogin(accessToken.getUser());
		
		if(user == null)
			throw new NoUserFoundForTokenException("No user was found in system [userLogin="+accessToken.getUser()+"]");
		
		/* Get task associated with current token */
		ProcessToolBpmSession autoSession = getRegistry().getProcessToolSessionFactory().createAutoSession(ctx);
		BpmTask task = ProcessToolBpmSessionHelper.getTaskData(autoSession, ctx, accessToken.getTaskId().toString());
		
		if(task == null)
			throw new NoBpmTaskFoundForTokenException("No task was found in system [taskId="+accessToken.getTaskId()+"]." +
					" \n There is possibility that someone already has changed the process state");
				
		/* Create token wrapper */
		TokenWrapper tokenWrapper = new TokenWrapper(accessToken, user, task);
		
		return tokenWrapper;
	}
	
}