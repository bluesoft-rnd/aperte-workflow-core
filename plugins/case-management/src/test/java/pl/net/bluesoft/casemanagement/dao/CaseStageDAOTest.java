package pl.net.bluesoft.casemanagement.dao;

import org.junit.Test;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseStageDAOTest extends BaseTest {
    private Case testCase;

    @Override
    protected void insertTestData() {
        super.insertTestData();
        this.testCase = this.caseDAO.createCase(this.testCaseDefinition, "CaseStageDAOTest", "no." + System.currentTimeMillis());
    }

    @Test
    public void testCreateCaseStage() throws Exception {
        final CaseStage stage = this.caseStageDAO.createStage(this.testCase, this.testCaseStateDefinition.getId(), "NewStage", null);
        assertNotNull(stage.getId());
        assertEquals("NewStage", stage.getName());
        assertEquals(this.testCase.getId(), stage.getCase().getId());
        assertEquals(this.testCaseStateDefinition, stage.getCaseStateDefinition());
        assertNotNull(stage.getStartDate());
    }

    @Test
    public void testDeleteCaseStage() throws Exception {
        final CaseStage stage = this.caseStageDAO.createStage(this.testCase, this.testCaseStateDefinition.getId(), "StageToDelete", null);
        final CaseStage before = this.caseStageDAO.getStageById(stage.getId());
        assertNotNull(before);
        this.caseStageDAO.deleteStage(stage);
        final CaseStage after = this.caseStageDAO.getStageById(stage.getId());
        assertNull(after);
    }
}
