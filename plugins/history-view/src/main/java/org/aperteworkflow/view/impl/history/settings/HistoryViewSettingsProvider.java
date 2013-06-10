package org.aperteworkflow.view.impl.history.settings;

import java.util.ArrayList;
import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for liferay settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class HistoryViewSettingsProvider 
{
	public static Collection<String> getSuperUserRoles()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		String rolesAsString = ctx.getSetting(HistoryViewSettings.SUPERUSER_ROLES);
		
		Collection<String> roles = new ArrayList<String>();
		
		if(rolesAsString == null)
			return roles;
		

        for (String roleName : rolesAsString.split(",")) 
            roles.add(roleName);
        
        
        return roles;

	}

}
