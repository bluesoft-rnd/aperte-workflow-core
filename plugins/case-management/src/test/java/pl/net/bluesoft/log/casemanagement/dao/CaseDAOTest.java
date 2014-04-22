package pl.net.bluesoft.log.casemanagement.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.net.bluesoft.lot.casemanagement.dao.*;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDAOTest {
    private static final Logger logger = Logger.getLogger(CaseDAOTest.class.getName());

    private static SessionFactory sessionFactory;
    private Session session;
    private Transaction tx;

    private CaseDAO dao;
    private CaseDefinitionDAO caseDefinitionDAO;
    private CaseStateDefinitionDAO caseStateDefinitionDAO;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        // Create the hibernate session factory
        final Configuration configuration = new Configuration();
        configuration.configure("test.hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
    }

    @Before
    public void setUp() throws NamingException {
        this.session = sessionFactory.openSession();
        this.tx = session.beginTransaction();
        this.caseDefinitionDAO = new CaseDefinitionDAOImpl(this.session);
        this.caseStateDefinitionDAO = new CaseStateDefinitionDAOImpl(this.session);
        this.dao = new CaseDAOImpl(this.session, this.caseDefinitionDAO, this.caseStateDefinitionDAO);
    }

    @After
    public void tearDown() {
        if (tx != null)
            //tx.rollback();
            tx.commit();
        if (session != null && session.isOpen())
            session.close();
    }

    @Test
    public void testCreateCase() throws Exception {
        Case newCase = dao.createCase(1, "test", "1/2014", 1, new HashMap<String, String>());
        logger.info(newCase.toString());
    }
}
