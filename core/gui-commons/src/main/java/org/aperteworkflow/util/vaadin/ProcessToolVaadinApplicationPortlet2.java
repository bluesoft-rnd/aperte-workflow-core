package org.aperteworkflow.util.vaadin;

import com.vaadin.ui.Label;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolVaadinApplicationPortlet2 extends ApplicationPortlet2WithLoadingMessage {

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

    private static ThreadLocal<PortletRequest> CURRENT_REQUEST = new ThreadLocal<PortletRequest>();

    @Override
    protected void handleRequest(final PortletRequest request, final PortletResponse response) throws PortletException, IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        CURRENT_REQUEST.set(request);
        try {
            ProcessToolRegistry registry = (ProcessToolRegistry) getPortletConfig().getPortletContext()
                    .getAttribute(ProcessToolRegistry.class.getName());
            ProcessToolRegistry.ThreadUtil.setThreadRegistry(registry);
            
            if (registry == null) {
				if (getApplication() != null) {
					getApplication().getMainWindow().addComponent(new Label(
							"Aperte Workflow is being installed. Please refresh your page."
					));
				}
				Logger.getLogger(ProcessToolVaadinApplicationPortlet2.class.getSimpleName()).severe(ProcessToolRegistry.class.getName() + " not found in servlet context");
				//throw new ProcessToolException(ProcessToolRegistry.class.getName() + " not found in servlet context");
				return;
            }
            registry.withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    try {
                        try {
                            I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));
                            ProcessToolVaadinApplicationPortlet2.super.handleRequest(request, response);
                        } finally {
                            I18NSource.ThreadUtil.removeThreadI18nSource();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } 
                }
            });
        } finally {
            CURRENT_REQUEST.set(null);
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static PortletRequest getPortletRequest() {
        return CURRENT_REQUEST.get();
    }
}


/*

*/