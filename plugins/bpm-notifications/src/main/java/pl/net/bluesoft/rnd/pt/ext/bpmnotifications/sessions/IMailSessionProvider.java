package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;

import javax.mail.Session;

/** 
 * Mail session provider
 * 
 * @author mpawlak
 *
 */
public interface IMailSessionProvider
{
	Session getSession(String profileName);
	
	void refreshConfig();
}
