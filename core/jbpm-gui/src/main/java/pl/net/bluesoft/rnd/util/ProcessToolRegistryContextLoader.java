package pl.net.bluesoft.rnd.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolRegistryContextLoader implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ProcessToolRegistryImpl toolRegistry = new ProcessToolRegistryImpl();
			toolRegistry.commitModelExtensions();
            toolRegistry.setBpmDefinitionLanguage("bpmn");
			toolRegistry.setProcessToolContextFactory(new ProcessToolContextFactoryImpl(toolRegistry));
			sce.getServletContext().setAttribute(ProcessToolRegistry.class.getName(), toolRegistry);
			
			ProcessToolRegistry.ThreadUtil.setThreadRegistry(toolRegistry);
		}
		catch (Exception e) {
			Logger.getLogger(ProcessToolRegistryContextLoader.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		sce.getServletContext().removeAttribute(ProcessToolRegistry.class.getName());
		try {
			JbpmService.getInstance().destroy();
		}
		catch (Exception e) {
			Logger.getLogger(ProcessToolRegistryContextLoader.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
