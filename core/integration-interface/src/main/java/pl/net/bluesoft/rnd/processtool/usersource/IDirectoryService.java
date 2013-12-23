package pl.net.bluesoft.rnd.processtool.usersource;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import pl.net.bluesoft.rnd.processtool.model.UserData;


/**
 * Ldap users source api
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IDirectoryService
{
	Map<String, Properties> getUsersAttributes(Collection<UserData> users);
	
}
