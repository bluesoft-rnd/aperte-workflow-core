package pl.net.bluesoft.rnd.processtool.token.callbacks;

import org.hibernate.SQLQuery;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;

/**
 * Delete all access tokens by given taskId
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DeleteAccessTokenByTaskIdCallback extends ProcessToolContextCallback
{
	private Long taskId;
	
	public DeleteAccessTokenByTaskIdCallback(Long taskId)
	{
		this.taskId = taskId;
	}

	@Override
	public void withContext(ProcessToolContext ctx) 
	{
		SQLQuery query = ctx.getHibernateSession().createSQLQuery("delete from pt_access_token where task_id = "+taskId);
		query.executeUpdate();
	}
	
}