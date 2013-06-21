package pl.net.bluesoft.rnd.processtool.web.widgets.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;

/**
 * Widget JavaScript provider based on file 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class FileWidgetJavaScriptProvider implements IWidgetScriptProvider
{
	private String fileName;
	private URL resource;
	
	public FileWidgetJavaScriptProvider(String fileName, URL resource)
	{
		this.fileName = fileName;
		this.resource = resource;
	}

	@Override
	public InputStream getJavaScriptContent() 
	{
		try
		{
			return resource.openStream();
			
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Exception during html file opening: "+fileName, ex);
		}
	}

	@Override
	public String getFileName()
	{
		return this.fileName;
	}



}
