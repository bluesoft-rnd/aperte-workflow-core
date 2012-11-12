package org.aperteworkflow.editor.vaadin;


import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.DefaultI18NSource;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Basic class for editor application which provides integration with rest of
 * the Aperte Workflow infrastructure
 */
public class GenericEditorApplication extends Application implements HttpServletRequestListener {

    private static ThreadLocal<GenericEditorApplication> current = new ThreadLocal<GenericEditorApplication>();

    /**
     * Get current application object associated with this thread of execution
     * @return current application
     */
    public static GenericEditorApplication getCurrent() {
        return current.get();
    }

    /**
     * Get current {@link ProcessToolRegistry}
     * @return current registry
     */
    public static ProcessToolRegistry getRegistry() {
        WebApplicationContext webCtx = (WebApplicationContext) getCurrent().getContext();
        ServletContext sc = webCtx.getHttpSession().getServletContext();
        return (ProcessToolRegistry) sc.getAttribute(ProcessToolRegistry.class.getName());
    }

    @Override
    public void init() {
        current.set(this);
    }

    @Override
    public void onRequestStart(final HttpServletRequest request, HttpServletResponse response) {
        current.set(this);

        I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));

        // Setting ProcessToolContext was taken from ProcessToolVaadinApplicationPortlet2
        // to preserve functionality used in portlet based Vaadin applications
        ServletContext servletContext = request.getSession().getServletContext();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            ProcessToolRegistry registry = (ProcessToolRegistry) servletContext.getAttribute(ProcessToolRegistry.class.getName());

            final String providerId = "step-editor";
            if (!registry.hasI18NProvider(providerId)) {
                registry.registerI18NProvider(
                        new PropertiesBasedI18NProvider(new PropertyLoader() {
                            @Override
                            public InputStream loadProperty(String path) throws IOException {
                                return getClass().getClassLoader().getResourceAsStream(path);
                            }
                        }, providerId + "-messages"),
                        providerId
                );
            }

            registry.withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        ProcessToolContext.Util.removeThreadProcessToolContext();
        I18NSource.ThreadUtil.removeThreadI18nSource();
        current.remove();
    }
    
    protected String getStringParameterByName(String paramterName, Map<String, String[]> paramterMap) {
        String[] value = paramterMap.get(paramterName);
        if (value != null && value.length > 0 && !StringUtils.isEmpty(value[0])) {
            return value[0].trim();
        }
        return null;
    }

}
