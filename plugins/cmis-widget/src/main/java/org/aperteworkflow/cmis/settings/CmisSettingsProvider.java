package org.aperteworkflow.cmis.settings;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for cmis settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class CmisSettingsProvider 
{
	/** Get url of the repository service */
	public static String getAtomRepostioryUrl()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_URL);
	}
	
	/** Get username for atom repository serivce */
	public static String getAtomRepostioryUsername()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_USERNAME);
	}
	
	/** Get password for atom repository serivce */
	public static String getAtomRepostioryPassword()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_PASSWORD);
	}
	
	/** Get main folder name for atom repository serivce */
	public static String getAtomRepostioryMainFolderName()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(CmisWidgetSettings.ATOM_REPOSTIORY_MAINFOLDER);
	}

}
