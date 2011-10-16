package pl.net.bluesoft.rnd.util.vaadin;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolVaadinApplicationPortlet2 extends ApplicationPortlet2 {

	private static class ExceptionCarrier extends RuntimeException {
		private Exception realException;

		private ExceptionCarrier(Exception realException) {
			this.realException = realException;
		}

		public Exception getRealException() {
			return realException;
		}

		public void setRealException(Exception realException) {
			this.realException = realException;
		}
	}


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
						ProcessToolVaadinApplicationPortlet2.super.handleRequest(request, response);
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
