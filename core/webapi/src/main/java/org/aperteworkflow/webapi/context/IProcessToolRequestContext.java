package org.aperteworkflow.webapi.context;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

/**
 * Process Tool request context 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IProcessToolRequestContext 
{
	/** Is user logged */
	boolean isUserAuthorized();
	
	/** Get user from request */
	UserData getUser();
	
	/** Get registry */
	ProcessToolRegistry getRegistry();
	
	/** Get process tool bpm session */
	ProcessToolBpmSession getBpmSession();

}
