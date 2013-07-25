package pl.net.bluesoft.rnd.pt.ext.user.service;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;

/**
 * Mock for LDAP operations
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AperteDirectoryService implements IDirectoryService {
	@Override
	public Map<String, Properties> getUsersAttributes(Collection<UserData> users) {
		// TODO Auto-generated method stub
		return null;
	}
}
