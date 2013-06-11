package pl.net.bluesoft.rnd.processtool.ui.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import pl.net.bluesoft.rnd.processtool.ui.IWidgetContentProvider;

/**
 * Widget content provider based on file 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class FileWidgetContentProvider implements IWidgetContentProvider {

	private String fileName;
	private URL resource;
	
	public FileWidgetContentProvider(String fileName, URL resource)
	{
		this.fileName = fileName;
		this.resource = resource;
	}

	@Override
	public InputStream getHtmlContent() 
	{
		try
		{
			return resource.openStream();
			//String htmlFileContent = CharStreams.toString(new InputStreamReader(htmlFileStream, "UTF-8"));
			
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
