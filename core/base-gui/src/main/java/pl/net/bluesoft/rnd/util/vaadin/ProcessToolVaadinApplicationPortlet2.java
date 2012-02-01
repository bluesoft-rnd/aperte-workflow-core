package pl.net.bluesoft.rnd.util.vaadin;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.i18n.DefaultI18NSource;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolVaadinApplicationPortlet2 extends ApplicationPortlet2 {

	@Override
	protected void handleRequest(final PortletRequest request, final PortletResponse response) throws PortletException, IOException {


		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {

			ProcessToolRegistry registry = (ProcessToolRegistry) getPortletConfig()
					.getPortletContext().getAttribute(ProcessToolRegistry.class.getName());
			registry.withProcessToolContext(new ProcessToolContextCallback() {
				@Override
				public void withContext(ProcessToolContext ctx) {
					ProcessToolContext.Util.setProcessToolContextForThread(ctx);
					try {
//						request.setAttribute(ProcessToolContextImpl.class.getName(), ctx);
                        try {
                            I18NSource.ThreadUtil.setThreadI18nSource(new DefaultI18NSource(request.getLocale()));
						    ProcessToolVaadinApplicationPortlet2.super.handleRequest(request, response);
                        }
                        finally {
                            I18NSource.ThreadUtil.setThreadI18nSource(null);
                        }
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						ProcessToolContext.Util.removeProcessToolContextForThread(ctx);
					}
				}
			});
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}


	}
}
