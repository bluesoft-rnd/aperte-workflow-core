package pl.net.bluesoft.rnd.processtool;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;

public class DaoTest {

    ProcessDefinitionDAO processDefinitionDao;

	ProcessToolContext ctx;// = new ProcessToolContext();
    @Before
    public void setup() {
		//processDefinitionDao = ctx.getProcessDefinitionDAO();
    }

    @Test
    public void testProcessDao() {
        ProcessToolRegistry registry = new ProcessToolRegistryImpl();
        Session sess = registry.getSessionFactory().openSession();
        sess.close();

    }

}
