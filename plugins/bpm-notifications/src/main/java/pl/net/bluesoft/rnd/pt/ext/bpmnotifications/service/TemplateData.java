package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Template data transfer object
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TemplateData 
{
	/** Locale of the data */
	private Locale locale;
	
	/** Message source */
	private I18NSource messageSource;
	
	/** Data entries map */
	private Map<String, Object> templateData;
	
	private String templateName;
	
	public TemplateData(String templateName, Locale locale, I18NSource messageSource)
	{
		this.locale = locale;
		this.messageSource = messageSource;
		this.templateName = templateName;
		this.templateData = new HashMap<String, Object>();
	}
	
	public void addEntry(String entryName, Object entry)
	{
		this.templateData.put(entryName, entry);
	}
	
	/** Get current template data */
	public Map<String, Object> getData()
	{
		return templateData;
	}
	
	/** Get locale of the template data */
	public Locale getLocale()
	{
		return this.locale;
	}
	
	/** Get message source */
	public I18NSource getMessageSource()
	{
		return this.messageSource;
	}

	public String getTemplateName() {
		return templateName;
	}

}
