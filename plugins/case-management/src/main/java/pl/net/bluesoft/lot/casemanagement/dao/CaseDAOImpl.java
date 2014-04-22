package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import java.util.Date;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDAOImpl extends SimpleHibernateBean<Case> implements CaseDAO {
    private CaseDefinitionDAO caseDefinitionDAO;
    private CaseStateDefinitionDAO caseStateDefinitionDAO;

    public CaseDAOImpl(final Session session) {
        super(session);
    }

    public CaseDAOImpl(final Session session, final CaseDefinitionDAO caseDefinitionDAO, final CaseStateDefinitionDAO caseStateDefinitionDAO) {
        this(session);
        this.caseDefinitionDAO = caseDefinitionDAO;
        this.caseStateDefinitionDAO = caseStateDefinitionDAO;
    }

    @Override
    public Case createCase(long caseDefinitionId, String name, String number, long caseStateDefinitionId, Map<String, String> simpleAttributes) {
        final Case newCase = new Case();
        newCase.setName(name);
        newCase.setNumber(number);
        newCase.setCreateDate(new Date());
        newCase.setDefinition(caseDefinitionDAO.getDefinitionById(caseDefinitionId));
        newCase.setState(caseStateDefinitionDAO.getStateDefinitionById(caseStateDefinitionId));
        saveOrUpdate(newCase);
        return newCase;
    }
}
