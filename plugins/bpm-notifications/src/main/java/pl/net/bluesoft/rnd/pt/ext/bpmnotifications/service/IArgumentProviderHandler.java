package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.Collection;

/**
 * Handler for template argument providers
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IArgumentProviderHandler 
{
	public void registerTemplateArgumentProvider(TemplateArgumentProvider provider);

	public void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider);

	public Collection<TemplateArgumentProvider> getTemplateArgumentProviders();
	
	
}
