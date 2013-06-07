package pl.net.bluesoft.rnd.processtool.application.activity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.terminal.gwt.server.SessionExpiredException;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;

/**
 * To use this class for standalone application, add 	 
 * {@code <servlet>
 * 	<servlet-class>pl.net.bluesoft.rnd.processtool.application.activity.ResponsiveUIApplicationServlet</servlet-class>
 * </servlet>
 * }
 * 
 * This class provide logic for scalability and correct width handling for 
 * mobile devices (default {@link ApplicationServlet} has problems with that)
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ResponsiveUIApplicationServlet extends ApplicationServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri, HttpServletRequest request) throws IOException 
	{
		super.writeAjaxPageHtmlHeader(page, title, themeUri, request);
		
		/* Get the browser info */
		WebApplicationContext context = getApplicationContext(request.getSession());
		WebBrowser browser = context.getBrowser();

		/* There is bug with scaling on Safari */
		if(browser.isSafari())
		{
	        page.append("<meta name=\"apple-touch-fullscreen\" content=\"yes\" />");
	        page.append("<meta name=\"apple-mobile-web-app-capable\" "
	                + "content=\"yes\" />");
	        page.append("<meta name=\"apple-mobile-web-app-status-bar-style\" "
	                + "content=\"black\" />");
			
			page.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1, user-scalable=yes\"/>");
		}
		else
		{
			page.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=yes\"/>");
		}
	}
	
	@Override
	protected Application getExistingApplication(HttpServletRequest request,boolean allowSessionCreation) throws MalformedURLException, SessionExpiredException 
	{
		/* Fix for NullPointer in Mozzila */
		try
		{
			Application application = super.getExistingApplication(request, allowSessionCreation);
			return application;
		}
		catch(Throwable ex)
		{
			return null;
		}
	}
}
