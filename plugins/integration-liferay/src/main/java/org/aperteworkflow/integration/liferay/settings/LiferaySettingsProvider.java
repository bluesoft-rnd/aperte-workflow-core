package org.aperteworkflow.integration.liferay.settings;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for liferay settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferaySettingsProvider 
{
	public static String getLdapCustomUserAttributes()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		return ctx.getSetting(LiferaySettings.LDAP_CUSTOM_USER_ATTRIBUTES);
	}

}
