package pl.net.bluesoft.casemanagement.deployment;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.net.bluesoft.casemanagement.dao.CaseDefinitionDAO;
import pl.net.bluesoft.casemanagement.dao.CaseDefinitionDAOImpl;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;

import javax.naming.NamingException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by pkuciapski on 2014-04-29.
 */
public class CaseDeployerTest {
    private final CaseDeployer deployer = new CaseDeployer();

    private CaseDefinition caseDefinition;

    protected ProcessToolRegistry processToolRegistry = new ProcessToolRegistryImpl();

    private static SessionFactory sessionFactory;
    private Session session;
    private Transaction tx;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        // Create the hibernate session factory
        final Configuration configuration = new Configuration();
        configuration.configure("test.hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
    }

    @Before
    public void before() {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.casemanagement-config.xml");
        assertNotNull(is);
        Object obj = deployer.unmarshallCaseDefinition(is);
        assertNotNull(obj);
        assertTrue(obj instanceof CaseDefinition);
        caseDefinition = (CaseDefinition) obj;
        this.session = sessionFactory.openSession();
        this.tx = session.beginTransaction();
    }

    @After
    public void tearDown() {
        if (tx != null)
            tx.commit();
        if (session != null && session.isOpen())
            session.close();
    }



    @Test
    public void testUnmarshallCaseDefinitionPossibleStates() throws Exception {
        assertNotNull(caseDefinition.getPossibleStates());
        assertEquals(4, caseDefinition.getPossibleStates().size());
        final List<CaseStateDefinition> states = getSortedStates(caseDefinition);
        assertEquals("CLOSED", states.get(0).getName());
        assertEquals("CLOSED_NUMBER", states.get(1).getName());
        assertSame(caseDefinition, states.get(0).getDefinition());
        assertSame(caseDefinition, states.get(1).getDefinition());
    }

    @Test
    public void testUnmarshallCaseDefinitionStateRoles() throws Exception {
        assertNotNull(caseDefinition.getPossibleStates());
        final List<CaseStateDefinition> states = getSortedStates(caseDefinition);
        assertEquals("NEW", states.get(2).getName());
        assertNotNull(states.get(2).getRoles());
        final List<CaseStateRole> roles = getSortedRoles(states.get(2).getRoles());
        assertEquals("EDIT", roles.get(0).getPrivilegeName());
        assertEquals(".*", roles.get(0).getRoleName());
    }

    private List<CaseStateRole> getSortedRoles(Set<CaseStateRole> roles) {
        final List<CaseStateRole> sortedRoles = new ArrayList<CaseStateRole>(roles);
        Collections.sort(sortedRoles, new Comparator<CaseStateRole>() {
            @Override
            public int compare(CaseStateRole role1, CaseStateRole role2) {
                return role1.getRoleName().compareTo(role1.getRoleName());
            }
        });
        return sortedRoles;
    }

    @Test
    public void testUnmarshallCaseDefinitionStateProcesses() throws Exception {
        assertNotNull(caseDefinition.getPossibleStates());
        final List<CaseStateDefinition> states = getSortedStates(caseDefinition);
        assertEquals("NEW", states.get(2).getName());
        assertNotNull(states.get(2).getProcesses());
        final List<CaseStateProcess> processes = getSortedProcesses(states.get(2).getProcesses());
        // assertEquals("Test", processes.get(0).getBpmDefinitionKey());
        assertNotNull(processes.get(0).getBpmDefinitionKey());
    }

    private List<CaseStateProcess> getSortedProcesses(Set<CaseStateProcess> processes) {
        final List<CaseStateProcess> sortedRoles = new ArrayList<CaseStateProcess>(processes);
        Collections.sort(sortedRoles, new Comparator<CaseStateProcess>() {
            @Override
            public int compare(CaseStateProcess csp1, CaseStateProcess csp2) {
                return csp1.getBpmDefinitionKey().compareTo(csp2.getBpmDefinitionKey());
            }
        });
        return sortedRoles;
    }

    @Test
    public void testUnmarshallCaseDefinitionWidgets() throws Exception {
        final List<CaseStateDefinition> states = getSortedStates(caseDefinition);
        final List<CaseStateWidget> widgets = getSortedWidgets(states.get(0).getWidgets());
        assertEquals(1, widgets.size());
        assertSame(states.get(0), widgets.get(0).getCaseStateDefinition());
        assertEquals("TabSheet", widgets.get(0).getClassName());
        assertEquals(Integer.valueOf(0), widgets.get(0).getPriority());
        assertEquals(0, widgets.get(0).getAttributes().size());
        final List<CaseStateWidget> subWidgets = getSortedWidgets(widgets.get(0).getChildren());
        assertEquals(1, subWidgets.size());
        assertNull(subWidgets.get(0).getCaseStateDefinition());
        assertSame(widgets.get(0), subWidgets.get(0).getParent());
        assertEquals("VerticalLayout", subWidgets.get(0).getClassName());
        assertEquals(Integer.valueOf(1), subWidgets.get(0).getPriority());
    }

    @Test
    public void testUnmarshallCaseDefinitionWidgetAttributes() throws Exception {
        final List<CaseStateDefinition> states = getSortedStates(caseDefinition);
        final List<CaseStateWidget> widgets = getSortedWidgets(states.get(0).getWidgets());
        final List<CaseStateWidget> subWidgets = getSortedWidgets(widgets.get(0).getChildren());
        final CaseStateWidget widget = subWidgets.get(0);
        final List<CaseStateWidgetAttribute> attrs = getSortedAttributes(widget);
        assertEquals(1, attrs.size());
        assertSame(widget, attrs.get(0).getCaseStateWidget());
        assertEquals("caption", attrs.get(0).getKey());
        assertEquals("case.main.data.tab.title", attrs.get(0).getValue());
    }

    private List<CaseStateWidgetAttribute> getSortedAttributes(final CaseStateWidget widget) {
        final List<CaseStateWidgetAttribute> attrs = new ArrayList<CaseStateWidgetAttribute>(widget.getAttributes());
        Collections.sort(attrs, new Comparator<CaseStateWidgetAttribute>() {
            @Override
            public int compare(CaseStateWidgetAttribute a1, CaseStateWidgetAttribute a2) {
                return a1.getKey().compareTo(a2.getKey());
            }
        });
        return attrs;
    }

    private List<CaseStateWidget> getSortedWidgets(final Set<CaseStateWidget> widgets) {
        final List<CaseStateWidget> sortedWidgets = new ArrayList<CaseStateWidget>(widgets);
        Collections.sort(sortedWidgets, new Comparator<CaseStateWidget>() {
            @Override
            public int compare(CaseStateWidget caseStateWidget, CaseStateWidget caseStateWidget2) {
                return caseStateWidget.getPriority().compareTo(caseStateWidget2.getPriority());
            }
        });
        return sortedWidgets;
    }

    private List<CaseStateDefinition> getSortedStates(final CaseDefinition definition) {
        final List<CaseStateDefinition> states = new ArrayList<CaseStateDefinition>(definition.getPossibleStates());
        Collections.sort(states, new Comparator<CaseStateDefinition>() {
            @Override
            public int compare(CaseStateDefinition stateDefinition, CaseStateDefinition stateDefinition2) {
                return stateDefinition.getName().compareTo(stateDefinition2.getName());
            }
        });
        return states;
    }

    @Test
    public void testDeployOrUpdateCaseDefinition() throws Exception {
        deployer.deployOrUpdateCaseDefinition(caseDefinition, this.session);
        final CaseDefinitionDAO dao = new CaseDefinitionDAOImpl(this.session);
        final CaseDefinition db = dao.getDefinitionByName(caseDefinition.getName());
        assertEquals(caseDefinition.getName(), db.getName());
        assertEquals(caseDefinition.getId(), db.getId());
    }
}
