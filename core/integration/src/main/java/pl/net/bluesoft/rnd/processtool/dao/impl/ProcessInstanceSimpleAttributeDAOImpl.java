package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceSimpleAttributeDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;

import java.util.List;
import java.util.Map;

import static org.hibernate.criterion.Restrictions.eq;
import static pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute.*;

/**
 * @author kkolodziej@bluesoft.net.pl
 */
public class ProcessInstanceSimpleAttributeDAOImpl extends SimpleHibernateBean<ProcessInstanceSimpleAttribute> implements ProcessInstanceSimpleAttributeDAO {
	public ProcessInstanceSimpleAttributeDAOImpl(Session hibernateSession) {
		super(hibernateSession);
	}

	@Override
	public String getSimpleAttributeValue(Long processId, String key) {
		ProcessInstanceSimpleAttribute attribute = getSimpleAttribute(processId, key);
		return attribute != null ? attribute.getValue() : null;
	}

	@Override
	public Map<String, String> getSimpleAttributesList(Long processId) {
		List<Object[]> list = session.createCriteria(ProcessInstanceSimpleAttribute.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.setProjection(Projections.projectionList()
						.add(Projections.property(_KEY))
						.add(Projections.property(_VALUE)))
				.add(eq(_PROCESS_INSTANCE_ID, processId))
				.list();

		return toMap(list);
	}

	@Override
	public void setSimpleAttribute(Long processId, String key, String newValue) {
		ProcessInstanceSimpleAttribute attribute = getSimpleAttribute(processId, key);

		if (attribute == null) {
			attribute = new ProcessInstanceSimpleAttribute(key, newValue);
			session.saveOrUpdate(attribute);
		}
		else {
			attribute.setValue(newValue);
			session.update(attribute);
		}
	}

	private ProcessInstanceSimpleAttribute getSimpleAttribute(Long processId, String key) {
		return (ProcessInstanceSimpleAttribute)session.createCriteria(ProcessInstanceSimpleAttribute.class)
				.add(eq(_PROCESS_INSTANCE_ID, processId))
				.add(eq(_KEY, key))
				.uniqueResult();
	}
}
