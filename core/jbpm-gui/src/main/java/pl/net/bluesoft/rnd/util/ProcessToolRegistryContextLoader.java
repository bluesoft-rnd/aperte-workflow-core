package pl.net.bluesoft.rnd.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolJbpmSessionFactory;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolRegistryContextLoader implements ServletContextListener 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	@Override
	public void contextInitialized(ServletContextEvent sce) 
	{
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, sce.getServletContext());

		try {
			ProcessToolRegistryImpl toolRegistry = (ProcessToolRegistryImpl)processToolRegistry;
			toolRegistry.commitModelExtensions();
            toolRegistry.setBpmDefinitionLanguage("bpmn20");
			toolRegistry.setProcessToolContextFactory(new ProcessToolContextFactoryImpl(toolRegistry));
			toolRegistry.setProcessToolSessionFactory(new ProcessToolJbpmSessionFactory());
			sce.getServletContext().setAttribute(ProcessToolRegistry.class.getName(), toolRegistry);

			ProcessToolRegistry.Util.setInstance(toolRegistry);
			ProcessToolRegistry.Util.setAwfClassLoader(Thread.currentThread().getContextClassLoader());
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
