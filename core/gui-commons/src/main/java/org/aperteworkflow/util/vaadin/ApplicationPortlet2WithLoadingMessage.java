package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * User: POlszewski
 * Date: 2012-02-02
 * Time: 14:33
 */
public class ApplicationPortlet2WithLoadingMessage extends ApplicationPortlet2 {
	@Override
	protected void writeAjaxPageHtmlVaadinScripts(RenderRequest request, RenderResponse response, BufferedWriter writer, Application application, String themeName) throws IOException, PortletException {
		response.createResourceURL().setParameter("img", "loader");
        //TODO - i18n
		writer.write(String.format("<div id='%s'>Please wait, loading...</div>", getLoaderTagId(getPortletConfig())));
		super.writeAjaxPageHtmlVaadinScripts(request, response,	writer,	application, themeName);
	}	

	public static void hideLoadingMessage(Window window, PortletApplicationContext2 context) {
		String js = "if (document.getElementById) {" +
				"	var el = document.getElementById('%s');" +
				"	if (el) {" +
				"		el.style.display = 'none';" +
				"	}" +
				"}";
		window.executeJavaScript(String.format(js, getLoaderTagId(context.getPortletConfig())));
	}

	private static String getLoaderTagId(PortletConfig config) {
		return ("vaadinLoader_" + config.getPortletName() + "_" + config.getPortletContext().getPortletContextName())
				.replaceAll("[^\\w-]","_");
	}
}
