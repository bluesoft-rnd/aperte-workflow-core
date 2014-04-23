package pl.net.bluesoft.lot.casemanagement.dao;

import org.junit.Before;
import org.junit.Test;
import pl.net.bluesoft.lot.casemanagement.dao.*;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseStage;

import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseStageDAOTest extends BaseTest {
    private Case testCase;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        insertTestData();
    }

    private void insertTestData() {
        this.testCase = this.caseDAO.createCase(this.testCaseDefinition, "test", "no." + System.currentTimeMillis(), null);
    }

    @Test
    public void testCreateCaseStage() throws Exception {
        final CaseStage stage = this.caseStageDAO.createStage(this.testCase.getId(), this.testCaseStateDefinition.getId(), "NewStage");
        assertNotNull(stage.getId());
        assertEquals("NewStage", stage.getName());
        assertEquals(this.testCase.getId(), stage.getCase().getId());
        assertEquals(this.testCaseStateDefinition, stage.getCaseStateDefinition());
        assertNotNull(stage.getStartDate());
    }

    @Test
    public void testDeleteCaseStage() throws Exception {
        final CaseStage stage = this.caseStageDAO.createStage(this.testCase.getId(), this.testCaseStateDefinition.getId(), "StageToDelete");
        final CaseStage before = this.caseStageDAO.getStageById(stage.getId());
        assertNotNull(before);
        this.caseStageDAO.deleteStage(stage);
        final CaseStage after = this.caseStageDAO.getStageById(stage.getId());
        assertNull(after);
    }
}
