package org.aperteworkflow.webapi.main.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TaskHtmlTemplateLoader implements IHtmlTemplateProvider 
{
	private Configuration configuration;
	
    /** Cache for the */
    private Map<String, String> templateCache = new HashMap<String, String>();
	
	public TaskHtmlTemplateLoader()
	{
		this.configuration = new Configuration();
		this.configuration.setTemplateLoader(this);
	}
	
	public String processTemplate(String templateName, Map<String, Object> templateData) throws ProcessToolTemplateErrorException
	{
        StringWriter sw = new StringWriter();
        try 
        {
            Template template = configuration.getTemplate(templateName);
            template.process(templateData, sw);
        }
        catch (Exception e) {
            throw new ProcessToolTemplateErrorException(e);
        }
        sw.flush();
        return sw.toString();
    }

	@Override
	public void closeTemplateSource(Object templateName) throws IOException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object findTemplateSource(String templateName) throws IOException 
	{
		return templateCache.containsKey(templateName) ? templateCache.get(templateName) : null;
	}

	@Override
	public long getLastModified(Object arg0) 
	{
		return 0;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException
	{
        if (templateSource == null) {
            return null;
        }
        return new StringReader(((String) templateSource));
	}

	@Override
	public void addTemplate(String templateName, String template) 
	{
		templateCache.put(templateName, template);
		
	}

	@Override
	public String getTemplate(String templateName) {
		return templateCache.get(templateName);
	}

	@Override
	public void removeTemplate(String templateName) 
	{
		this.templateCache.remove(templateName);
		
	}

}
