package pl.net.bluesoft.rnd.processtool.plugins;

import org.osgi.framework.BundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.di.DefaultDependencyInjectionInitializer;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.PluginHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginServlet extends HttpServlet implements ServletContextListener
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;

    private ServletContext servletContext;

    static PluginHelper pluginHelper;

    private static Logger LOGGER = Logger.getLogger(PluginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        stopPluginHelper();
		initPluginHelper();
    }

	@Override
    public void init() throws ServletException {
        LOGGER.info("init");

    }

    private synchronized void initPluginHelper()
    {
        if(pluginHelper != null)
            return;

        try
        {
                pluginHelper = new PluginHelper();

                LOGGER.log(Level.INFO, "[CONFIG] Aperte home path: "+ProcessToolContext.Util.getHomePath());

                pluginHelper.initialize(
						firstExistingDirectory(servletContext.getInitParameter("osgi-plugins-directory"),
                                servletContext.getRealPath("/WEB-INF/osgi"),
                                System.getProperty("aperte.osgi.dir")
                                ,
                                ProcessToolContext.Util.getHomePath() + File.separator + "osgi-plugins"),
                        nvl(servletContext.getInitParameter("felix-cache-directory"),
                                ProcessToolContext.Util.getHomePath() + File.separator + "felix-cache"),
						(ProcessToolRegistryImpl) processToolRegistry);
        }
        catch (Exception e) {
            pluginHelper = null;
            LOGGER.throwing("Exception while osgi init", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

	private synchronized void stopPluginHelper() {
		if (pluginHelper != null) {
			try {
				pluginHelper.stopPluginSystem();
				pluginHelper = null;
			} catch (BundleException e) {
				LOGGER.throwing("Exception while osgi stop", e.getMessage(), e);
			}
		}
	}

	private static String firstExistingDirectory(String... dirs) {
		for (String dir : dirs) {
			if (dir != null && new File(dir).exists()) {
				return dir;
			}
		}
		return null;
	}

    @Override
    public void destroy() {
        LOGGER.info("Stop OSGi plugins...");
        super.destroy();
		stopPluginHelper();
	}

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        this.servletContext = servletContextEvent.getServletContext();

        /* Initialize dependencies */
        DefaultDependencyInjectionInitializer.injectDependencies();

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        if(processToolRegistry == null)
        {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(servletContextEvent.getServletContext());
            if(processToolRegistry == null)
            {
                LOGGER.log(Level.SEVERE, "No process tool registry! ");
                return;
            }
        }

        initPluginHelper();
        LOGGER.info("initout");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.info("Stop OSGi plugins...");
        stopPluginHelper();
    }
}