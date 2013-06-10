package pl.net.bluesoft.awf.ext.droolsstep.settings;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for liferay settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DroolsStepSettingsProvider 
{
	public static String getRulesBaseURL()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		return ctx.getSetting(DroolsStepSettings.RULES_BASEURL);
	}

}
