package pl.net.bluesoft.rnd.processtool.token;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;

/**
 * Access Token factory interface
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IAccessTokenFactory 
{
	AccessToken create(BpmTask userTask, String actionName);

}
