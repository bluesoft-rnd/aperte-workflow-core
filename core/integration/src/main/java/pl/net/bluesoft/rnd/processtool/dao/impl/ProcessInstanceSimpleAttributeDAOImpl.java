package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceSimpleAttributeDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleAttribute;

import java.util.*;

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import static pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleAttribute.*;

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
				.setProjection(projectionList()
						.add(property(_KEY))
						.add(property(_VALUE)))
				.add(eq(_PROCESS_INSTANCE_ID, processId))
				.list();

		return toMap(list);
	}

	@Override
	public void setSimpleAttribute(Long processId, String key, String newValue) {
		ProcessInstanceSimpleAttribute attribute = getSimpleAttribute(processId, key);

		if (attribute == null) {
			attribute = new ProcessInstanceSimpleAttribute(key, newValue);
			attribute.setProcessInstance(new ProcessInstance());
			attribute.getProcessInstance().setId(processId);
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

	@Override
	public Map<String, String> getSimpleAttributeValues(Long processId, Collection<String> keys) {
		if (keys.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Object[]> list = session.createCriteria(ProcessInstanceSimpleAttribute.class)
				.setProjection(projectionList().add(property(_KEY)).add(property(_VALUE)))
				.add(eq(_PROCESS_INSTANCE_ID, processId))
				.add(in(_KEY, keys))
				.list();

		Map<String, String> result = new HashMap<String, String>();

		for (Object[] row : list) {
			result.put((String)row[0], (String)row[1]);
		}
		return result;
	}
}
