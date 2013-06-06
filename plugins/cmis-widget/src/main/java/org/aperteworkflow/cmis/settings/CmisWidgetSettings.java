package org.aperteworkflow.cmis.settings;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;

/**
 * Trip Widget Settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum CmisWidgetSettings implements IProcessToolSettings 
{
	ATOM_REPOSITORY_URL("atom.repository.url"),
	ATOM_REPOSITORY_USERNAME("atom.repository.username"),
	ATOM_REPOSITORY_PASSWORD("atom.repository.password"),
	ATOM_REPOSTIORY_MAINFOLDER("atom.repository.mainfolder");
	
	private String key;
	private CmisWidgetSettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}

}
