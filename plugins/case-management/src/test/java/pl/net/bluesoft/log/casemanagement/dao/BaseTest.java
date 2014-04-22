package pl.net.bluesoft.log.casemanagement.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import pl.net.bluesoft.lot.casemanagement.dao.*;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;

import javax.naming.NamingException;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public abstract class BaseTest {
    private static SessionFactory sessionFactory;
    protected Session session;
    private Transaction tx;

    protected CaseDAO caseDAO;
    protected CaseDefinitionDAO caseDefinitionDAO;
    protected CaseStateDefinitionDAO caseStateDefinitionDAO;
    protected CaseStageDAO caseStageDAO;

    protected CaseDefinition testCaseDefinition;
    protected CaseStateDefinition testCaseStateDefinition;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        // Create the hibernate session factory
        final Configuration configuration = new Configuration();
        configuration.configure("test.hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
    }

    @Before
    public void setUp() throws Exception {
        this.session = sessionFactory.openSession();
        this.tx = session.beginTransaction();
        this.caseDefinitionDAO = new CaseDefinitionDAOImpl(this.session);
        this.caseStateDefinitionDAO = new CaseStateDefinitionDAOImpl(this.session, this.caseDefinitionDAO);
        this.caseStageDAO = new CaseStageDAOImpl(this.session, this.caseStateDefinitionDAO);
        this.caseDAO = new CaseDAOImpl(this.session, this.caseDefinitionDAO, this.caseStateDefinitionDAO, this.caseStageDAO);
        insertTestData();
    }

    private void insertTestData() {
        this.testCaseDefinition = this.caseDefinitionDAO.createDefinition("Test");
        this.testCaseStateDefinition = this.caseStateDefinitionDAO.createStateDefinition("TestState", this.testCaseDefinition.getId());
    }


    @After
    public void tearDown() {
        if (tx != null)
            //    tx.rollback();
            tx.commit();
        if (session != null && session.isOpen())
            session.close();
    }

}
