package org.aperteworkflow.ui.view.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import pl.net.bluesoft.rnd.processtool.plugins.IWidgetVersionProvider;

/**
 * Version provider using git version control system. 
 * Use it with git-commit-id-plugin maven plugin, which generates
 * git.properties file inside every bundle 
 *  
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GitWidgetVersionProvider implements IWidgetVersionProvider 
{
	public String getWidgetVersionInfo(Class<?> widgetClass)
	{
    	try 
    	{
        	Properties gitProperties;
        	        	
        	Bundle bundle = FrameworkUtil.getBundle(widgetClass);
        	
        	/* Class is part of osgi bundle, use class loader from bundle context */
        	if(bundle != null)
        		gitProperties = loadPropertiesFromOsgi(bundle);
        	else
        		gitProperties = loadPropertiesFromDefaultClassLoader(widgetClass);

        	if(gitProperties == null)
        		return null; 
			
			StringBuilder widgetVersionBuilder = new StringBuilder();
			
			widgetVersionBuilder.append(gitProperties.getProperty("git.commit.id.describe"));
			widgetVersionBuilder.append(" [");
			widgetVersionBuilder.append(gitProperties.getProperty("git.commit.time"));
			widgetVersionBuilder.append("]");
			
	    	return widgetVersionBuilder.toString();
		} 
    	catch (IOException e) 
    	{
    		return "[file format error]";
		} 

    }
	
	/** Load properties files using osgi context 
	 * @throws IOException */
	private Properties loadPropertiesFromOsgi(Bundle bundle) throws IOException
	{
		Properties gitProperties = new Properties();
		
    	
    	BundleContext bundleContext = bundle.getBundleContext();

    	URL propertiesURL = bundleContext.getBundle().getResource("git.properties");
    	
    	if(propertiesURL == null)
    		return null; 
    	
    	InputStream in = propertiesURL.openStream();
    	
		gitProperties.load(in);

		in.close();
		
		return gitProperties;
	}
	
	/** Load properties file using default class loader 
	 * @throws IOException */
	private Properties loadPropertiesFromDefaultClassLoader(Class<?> widgetClass) throws IOException
	{
		Properties gitProperties = new Properties();
		
    	InputStream in = this.getClass().getClassLoader().getResourceAsStream("git.properties");
    	
    	if(in == null)
    		return null;
    	
		gitProperties.load(in);
		in.close();
		
		return gitProperties;
	}

}
