package pl.net.bluesoft.rnd.processtool.web.domain;

import java.io.InputStream;

/**
 * Provider api for widget content
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IContentProvider
{
	/** Get HTML content */
	InputStream getHtmlContent();
	
	/** Get file name */
	String getFileName();

}
