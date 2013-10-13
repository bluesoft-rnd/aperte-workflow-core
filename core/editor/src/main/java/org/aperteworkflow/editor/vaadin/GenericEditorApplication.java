package org.aperteworkflow.editor.vaadin;


import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

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

    @Override
    public void init() {
        current.set(this);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        current.set(this);

        I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));

        // Setting ProcessToolContext was taken from ProcessToolVaadinApplicationPortlet2
        // to preserve functionality used in portlet based Vaadin applications
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            String providerId = "step-editor";
            if (!getRegistry().getBundleRegistry().hasI18NProvider(providerId)) {
                getRegistry().getBundleRegistry().registerI18NProvider(
						new PropertiesBasedI18NProvider(new PropertyLoader() {
							@Override
							public InputStream loadProperty(String path) throws IOException {
								return getClass().getClassLoader().getResourceAsStream(path);
							}
						}, providerId + "-messages"),
						providerId
				);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) 
    {
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
