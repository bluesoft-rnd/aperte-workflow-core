package pl.net.bluesoft.casemanagement.dao;

import org.junit.Test;
import pl.net.bluesoft.casemanagement.model.Case;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDAOTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(CaseDAOTest.class.getName());

    @Test
    public void testCreateCase() throws Exception {
        final String name = "CaseDAOTest-Name-" + System.currentTimeMillis();
        final String number = "CaseDAOTest-Number-" + System.currentTimeMillis();
        final Case newCase = caseDAO.createCase(this.testCaseDefinition, name, number);
        logger.info(newCase.toString());
        assertEquals(name, newCase.getName());
        assertEquals(number, newCase.getNumber());
        assertEquals(this.testCaseDefinition.getName(), newCase.getDefinition().getName());
        assertEquals("The case state should be set to the initial state from the case definition",
                this.testCaseDefinition.getInitialState().getId(), newCase.getCurrentStage().getCaseStateDefinition().getId());
        assertNotNull(newCase.getCurrentStage().getName());
        assertEquals(this.testCaseStateDefinition.getName(), newCase.getCurrentStage().getName());
    }


    @Test
    public void testGetAllCases() throws Exception {
        final Collection<Case> cases = caseDAO.getAllCases();
        logger.info(cases.toString());
        assertNotNull(cases);
        assertTrue(cases.size() > 0);
    }

    @Test
    public void testGetAllCasesPaged() throws Exception {
        final Collection<Case> cases = caseDAO.getAllCasesPaged("id", true, 1, 0);
        assertNotNull(cases);
        assertEquals(1, cases.size());
    }

    @Test
    public void testGetAllCasesCount() throws Exception {
        final Long count = caseDAO.getAllCasesCount();
        assertTrue(count > 0);
    }

    @Test
    public void testFindCasesByName() throws Exception {
        final String name = "CaseDAOTestFindByName-Name-" + System.currentTimeMillis();
        final String number = "CaseDAOTest-Number-" + System.currentTimeMillis();
        final Case newCase = caseDAO.createCase(this.testCaseDefinition, name, number);
        final List<Case> db = new ArrayList<Case>(caseDAO.findCasesByName(newCase.getName()));
        assertEquals(1, db.size());
        assertEquals(newCase.getName(), db.get(0).getName());
    }

}
