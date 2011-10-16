package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolRegistryContextLoader implements ServletContextListener {


	//moved back to hibernate.cfg.xml
//	String[] resources = new String[]{
//			"/jbpm.repository.hbm.xml",
//			"/jbpm.execution.hbm.xml",
//			"/jbpm.history.hbm.xml",
//			"/jbpm.task.hbm.xml",
//			"/jbpm.identity.hbm.xml"
//	};

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		try {
			ProcessToolRegistryImpl toolRegistry = new ProcessToolRegistryImpl();
//			for (String resource : resources) {
//				toolRegistry.addHibernateResource(resource, slurp(getClass().getResourceAsStream(resource)));
//
//			}
			toolRegistry.commitModelExtensions();
			toolRegistry.setProcessToolContextFactory(new ProcessToolContextFactoryImpl(toolRegistry));
			sce.getServletContext().setAttribute(ProcessToolRegistry.class.getName(),
			                                     toolRegistry);
		} catch (Exception e) {
			Logger.getLogger(ProcessToolRegistryContextLoader.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		sce.getServletContext().removeAttribute(ProcessToolRegistry.class.getName());
	}

	private byte[] slurp(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c=0;
		while ((c = is.read()) > 0) {
			baos.write(c);
		}
		return baos.toByteArray();
	}
}
