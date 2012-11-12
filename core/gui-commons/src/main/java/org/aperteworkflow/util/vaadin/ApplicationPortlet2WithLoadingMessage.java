package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * User: POlszewski
 * Date: 2012-02-02
 * Time: 14:33
 */
public class ApplicationPortlet2WithLoadingMessage extends ApplicationPortlet2 
{
	private Application application;
	

	@Override
	protected void writeAjaxPageHtmlVaadinScripts(RenderRequest request, RenderResponse response, BufferedWriter writer, Application application, String themeName) throws IOException, PortletException {
		response.createResourceURL().setParameter("img", "loader");

		this.application = application;
		
		I18NSource i18NSource = I18NSourceFactory.createI18NSource(request.getLocale());
		writer.write(String.format("<div name='%s'>%s</div>",
				getLoaderTagId(request.getWindowID(), getPortletConfig()),
				i18NSource.getMessage("loader.message")));
		
		super.writeAjaxPageHtmlVaadinScripts(request, response,	writer,	application, themeName);
	}	
	@Override
	protected void handleRequest(PortletRequest request, PortletResponse response) throws PortletException, IOException
	{
		super.handleRequest(request,response);

		if (application != null) {
			for(Window window: application.getWindows()) {
				window.executeJavaScript("hideLoadingMessage('"+getLoaderTagId(request.getWindowID(), getPortletConfig())+"');");
			}
		}
	}

	private static String getLoaderTagId(String portletId, PortletConfig config) {
		return ("vaadinLoader_" + config.getPortletName() + "_" + config.getPortletContext().getPortletContextName() + "_" + portletId.replace("-",""))
				.replaceAll("[^\\w-]","_");
	}
}
