package pl.net.bluesoft.casemanagement.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDefinitionDAOImpl extends SimpleHibernateBean<CaseDefinition> implements CaseDefinitionDAO {

    public CaseDefinitionDAOImpl(final Session session) {
        super(session);
    }

    @Override
    public CaseDefinition getDefinitionById(final long caseDefinitionId) {
        return (CaseDefinition) this.session.get(CaseDefinition.class, caseDefinitionId);
    }

    @Override
    public CaseDefinition createDefinition(final String name) {
        final CaseDefinition def = new CaseDefinition();
        def.setName(name);
        saveOrUpdate(def);
        return def;
    }

    @Override
    public void setInitialState(final CaseDefinition caseDefinition, final CaseStateDefinition initialState) {
        caseDefinition.setInitialState(initialState);
        this.session.update(caseDefinition);
    }

    private void addCaseStateDefinition(final CaseDefinition caseDefinition, final long initialCaseStateDefinitionId) {
        final CaseStateDefinition csd = new CaseStateDefinition();
        csd.setId(initialCaseStateDefinitionId);
        caseDefinition.setInitialState(csd);
    }

    @Override
    public CaseDefinition createDefinition(final String name, final long initialCaseStateDefinitionId) {
        final CaseDefinition def = new CaseDefinition();
        def.setName(name);
        addCaseStateDefinition(def, initialCaseStateDefinitionId);
        saveOrUpdate(def);
        return def;
    }

    @Override
    public CaseDefinition getDefinitionByName(final String name) {
        return (CaseDefinition) session.createCriteria(CaseDefinition.class)
                .add(eq(CaseDefinition.NAME, name))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
    }

    @Override
    public CaseDefinition createOrUpdateDefinition(final CaseDefinition caseDefinition) {
        final CaseDefinition existing = getDefinitionByName(caseDefinition.getName());
        CaseDefinition merged = null;
        if (existing != null) {
			if (!caseDefinition.isSimilar(existing)) {
				caseDefinition.setId(existing.getId());
				mergeStates(caseDefinition, existing);
				merged = (CaseDefinition)getSession().merge(caseDefinition);
			}
        } else {
            saveOrUpdate(caseDefinition);
            merged = caseDefinition;
        }
        return merged;
    }

	private void mergeStates(CaseDefinition newDefinition, CaseDefinition oldDefinition) {
		for (CaseStateDefinition newState : newDefinition.getPossibleStates()) {
			CaseStateDefinition oldState = oldDefinition.getState(newState.getName());

			if (oldState != null) {
				newState.setId(oldState.getId());

				for (CaseStateRole role : oldState.getRoles()) {
					role.setStateDefinition(null);
				}
				oldState.getRoles().clear();

				for (CaseStateWidget stateWidget : oldState.getWidgets()) {
					stateWidget.setCaseStateDefinition(null);
				}
				oldState.getWidgets().clear();

				for (CaseStateProcess process : oldState.getProcesses()) {
					process.setStateDefinition(null);
				}
				oldState.getProcesses().clear();
			}
		}

		String initialStateName = newDefinition.getInitialStateName();

		if (initialStateName == null) {
			if (newDefinition.getInitialState() != null) {
				initialStateName = newDefinition.getInitialState().getName();
			}
		}

		if (initialStateName != null) {
			newDefinition.setInitialState(newDefinition.getState(initialStateName));
		}
	}
}
