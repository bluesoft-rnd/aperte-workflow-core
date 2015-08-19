package pl.net.bluesoft.casemanagement.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import pl.net.bluesoft.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.casemanagement.model.CaseStateWidget;

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
        this.caseDAO = new CaseDAOImpl(this.session, this.caseStateDefinitionDAO);
        insertTestData();
    }

    protected void insertTestData() {
        this.testCaseDefinition = this.caseDefinitionDAO.createDefinition("Test-" + System.currentTimeMillis());
        this.testCaseStateDefinition = this.caseStateDefinitionDAO.createStateDefinition("Test-InitialTestState", this.testCaseDefinition.getId());
        insertTestWidgets(this.testCaseStateDefinition);
        this.caseStateDefinitionDAO.createStateDefinition("Test-SomeOtherState", this.testCaseDefinition.getId());
        this.caseDefinitionDAO.setInitialState(this.testCaseDefinition, this.testCaseStateDefinition);
    }

    private void insertTestWidgets(CaseStateDefinition stateDef) {
        final CaseStateWidget widget = new CaseStateWidget();
        //widget.setName("testWidget");
        widget.setClassName("CaseDataWidget");
        widget.setPriority(0);
        widget.setCaseStateDefinition(stateDef);
        stateDef.getWidgets().add(widget);
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
