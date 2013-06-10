package pl.net.bluesoft.rnd.processtool.token.callbacks;

import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;

/**
 * Get the access token by tokenId callback class
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GetAccessTokenCallback implements ReturningProcessToolContextCallback<AccessToken>
{
	private String token;
	
	public GetAccessTokenCallback(String token)
	{
		this.token = token;
	}

	@Override
	public AccessToken processWithContext(ProcessToolContext ctx) 
	{
          return (AccessToken)ctx.getHibernateSession().createCriteria(AccessToken.class)
        		  .add(Restrictions.eq(AccessToken._TOKEN, token)).uniqueResult();
	}
	
}