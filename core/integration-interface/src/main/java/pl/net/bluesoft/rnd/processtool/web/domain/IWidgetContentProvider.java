package pl.net.bluesoft.rnd.processtool.web.domain;

import java.io.InputStream;

/**
 * Provider interface for widget content
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetContentProvider 
{
	/** Get HTML content */
	InputStream getHtmlContent();
	
	/** Get file name */
	String getFileName();

}
