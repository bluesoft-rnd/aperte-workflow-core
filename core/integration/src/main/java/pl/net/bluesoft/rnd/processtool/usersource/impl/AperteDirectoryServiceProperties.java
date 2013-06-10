package pl.net.bluesoft.rnd.processtool.usersource.impl;

import java.util.Properties;

import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryServicePropertiesProvider;

public class AperteDirectoryServiceProperties implements IDirectoryServicePropertiesProvider {

	@Override
	public Properties getDefaultProperties() 
	{
		return new Properties();
	}

}
