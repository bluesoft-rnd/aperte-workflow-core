package pl.net.bluesoft.rnd.processtool.token.callbacks;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;

/**
 * Get the access tokens by given taskId callback class
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GetAccessTokensByTaskIdCallback implements ReturningProcessToolContextCallback<List<AccessToken>>
{
	private Long taskId;
	
	public GetAccessTokensByTaskIdCallback(Long taskId)
	{
		this.taskId = taskId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AccessToken> processWithContext(ProcessToolContext ctx) 
	{
          return (List<AccessToken>)ctx.getHibernateSession().createCriteria(AccessToken.class)
        		  .add(Restrictions.eq(AccessToken._TASK_ID, taskId)).list();
	}
	
}