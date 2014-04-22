package pl.net.bluesoft.lot.casemanagement.dao;

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
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Set;
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
        final Case newCase = caseDAO.createCase(this.testCaseDefinition.getId(), name, number, new HashMap<String, String>());
        logger.info(newCase.toString());
        assertEquals(name, newCase.getName());
        assertEquals(number, newCase.getNumber());
        assertEquals(this.testCaseDefinition.getName(), newCase.getDefinition().getName());
        assertEquals("The case state should be set to the initial state from the case definition",
                this.testCaseDefinition.getInitialState().getId(), newCase.getCurrentStage().getCaseStateDefinition().getId());
        assertNotNull(newCase.getCurrentStage().getName());
        assertEquals(this.testCaseStateDefinition.getName(), newCase.getCurrentStage().getName());
    }

}
