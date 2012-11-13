package pl.net.bluesoft.rnd.processtool.plugins;

import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.PluginHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginServlet extends HttpServlet {

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

        initPluginHelper();
        LOGGER.info("initout");
    }

    private synchronized void initPluginHelper() throws ServletException {
        try {
            if (pluginHelper == null) {
                pluginHelper = new PluginHelper();

                ProcessToolRegistry processToolRegistry = (ProcessToolRegistry) getServletContext()
                        .getAttribute(ProcessToolRegistry.class.getName());

                pluginHelper.initialize(
						firstExistingDirectory(getServletConfig().getInitParameter("osgi-plugins-directory"),
								getServletConfig().getServletContext().getRealPath("/WEB-INF/osgi"),
                                ProcessToolContext.Util.getHomePath() + File.separator + "osgi-plugins"),
                        nvl(getServletConfig().getInitParameter("felix-cache-directory"),
                                ProcessToolContext.Util.getHomePath() + File.separator + "felix-cache"),
                        nvl(getServletConfig().getInitParameter("lucene-index-directory"),
                                ProcessToolContext.Util.getHomePath() + File.separator + "lucene-index"),
                        (ProcessToolRegistryImpl) processToolRegistry);
            }
        } catch (Exception e) {
            pluginHelper = null;
            LOGGER.throwing("Exception while osgi init", e.getMessage(), e);
            throw new ServletException(e);
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
        LOGGER.info("destroy");
        super.destroy();
		stopPluginHelper();
	}
}