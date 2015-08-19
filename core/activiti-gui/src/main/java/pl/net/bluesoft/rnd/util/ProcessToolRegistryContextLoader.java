package pl.net.bluesoft.rnd.util;

import org.aperteworkflow.ext.activiti.ActivitiContextFactoryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.RegistryHolder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolRegistryContextLoader implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {

		try {
			ProcessToolRegistryImpl toolRegistry = new ProcessToolRegistryImpl();
			toolRegistry.commitModelExtensions();
            toolRegistry.setBpmDefinitionLanguage("bpmn20");
			toolRegistry.setProcessToolContextFactory(new ActivitiContextFactoryImpl(toolRegistry));
			sce.getServletContext().setAttribute(ProcessToolRegistry.class.getName(), toolRegistry);
            RegistryHolder.setRegistry(toolRegistry);

		} catch (Exception e) {
			Logger.getLogger(ProcessToolRegistryContextLoader.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		sce.getServletContext().removeAttribute(ProcessToolRegistry.class.getName());
	}
}
