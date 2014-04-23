package pl.net.bluesoft.lot.casemanagement.dao;

import org.junit.Test;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseSimpleAttribute;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDAOTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(CaseDAOTest.class.getName());

    @Test
    public void testCreateCase() throws Exception {
        final String name = "name-" + System.currentTimeMillis();
        final String number = "number-" + System.currentTimeMillis();
        final Case newCase = caseDAO.createCase(this.testCaseDefinition, name, number, new HashMap<String, String>());
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
    public void testCreateCaseWithAttributes() throws Exception {
        final String name = "name-" + System.currentTimeMillis();
        final String number = "number-" + System.currentTimeMillis();
        final Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("key1", "value1");
        attrs.put("key2", "value2");
        final Case newCase = caseDAO.createCase(this.testCaseDefinition, name, number, attrs);
        final Case dbCase = caseDAO.getCaseById(newCase.getId());
        logger.info(dbCase.getSimpleAttributes().toString());
        assertNotNull(dbCase.getSimpleAttributes());
        assertEquals(2, dbCase.getSimpleAttributes().size());
        List<CaseSimpleAttribute> attrList = new ArrayList(dbCase.getSimpleAttributes());
        Collections.sort(attrList);
        assertEquals("key1", attrList.get(0).getKey());
        assertEquals("key2", attrList.get(1).getKey());
        assertEquals("value1", attrList.get(0).getValue());
        assertEquals("value2", attrList.get(1).getValue());
    }

}
