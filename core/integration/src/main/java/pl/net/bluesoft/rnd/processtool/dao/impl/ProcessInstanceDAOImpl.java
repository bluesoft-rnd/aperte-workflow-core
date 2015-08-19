package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.transform.NestedAliasToBeanResultTransformer;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.StepInfo;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttachmentAttribute;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttribute;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Transformer;

import java.util.*;

import static org.hibernate.criterion.Restrictions.*;
import static pl.net.bluesoft.rnd.processtool.model.ProcessInstance.*;
import static pl.net.bluesoft.util.lang.DateUtil.addDays;
import static pl.net.bluesoft.util.lang.DateUtil.truncHours;


/**
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
public class ProcessInstanceDAOImpl extends SimpleHibernateBean<ProcessInstance> implements ProcessInstanceDAO {
	public ProcessInstanceDAOImpl(Session session) {
		super(session);
	}

	@Override
	public long saveProcessInstance(ProcessInstance processInstance) {
		if (processInstance.getToDelete() != null) {
			for (Object o : processInstance.getToDelete()) {
				session.delete(o);
			}
		}
		Set<ProcessInstanceAttribute> procAttrib = processInstance.getProcessAttributes();
		for (ProcessInstanceAttribute attrib:procAttrib){
			if (attrib instanceof ProcessInstanceAttachmentAttribute && attrib.getId()!=null){
				session.evict(session.get(attrib.getClass(), attrib.getId()));
			}
		}
		session.saveOrUpdate(processInstance);
		session.flush();

		return processInstance.getId();
	}

	@Override
	public ProcessInstance getProcessInstance(long id) {
		return (ProcessInstance) session.get(ProcessInstance.class, id);
	}

	@Override
	public List<ProcessInstance> getProcessInstances(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		return getSession().createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.add(Restrictions.in(_ID, ids)).list();
	}

	@Override
	public ProcessInstance refreshProcessInstance(ProcessInstance processInstance) {
		return (ProcessInstance) getSession().merge(processInstance);
	}

	@Override
	public ProcessInstance getProcessInstanceByInternalId(String internalIds) {
		ProcessInstance pi = (ProcessInstance) getSession().createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.add(eq(_INTERNAL_ID, internalIds))
				.uniqueResult();
		return pi;
	}

	@Override
	public ProcessInstance getProcessInstanceByExternalId(String externalId) {
		List list = session.createCriteria(ProcessInstance.class)
				.add(eq(_EXTERNAL_KEY, externalId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		if (list.isEmpty()) {
			return null;
		}
		else {
			return (ProcessInstance)list.get(0);
		}
	}

	@Override
	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId) {
		if (internalId.isEmpty()) {
			return new HashMap<String, ProcessInstance>();
		}
		List<ProcessInstance> list = getSession().createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.add(in(_INTERNAL_ID, internalId))
				.list();
		return Collections.transform(list, new Transformer<ProcessInstance, String>() {
			@Override
			public String transform(ProcessInstance obj) {
				return obj.getInternalId();
			}
		});
	}

	@Override
	public void deleteProcessInstance(ProcessInstance instance) {
		long start = System.currentTimeMillis();
		session.delete(instance);
		long duration = System.currentTimeMillis() - start;
		logger.severe("deleteProcessInstanceByInternalId: " +  duration);
	}

	@Override
	public Collection<ProcessInstanceLog> getUserHistory(String userLogin, Date startDate, Date endDate) {

		long start = System.currentTimeMillis();
		Criteria criteria = session.createCriteria(ProcessInstanceLog.class)
				.addOrder(Order.desc(ProcessInstanceLog._ENTRY_DATE));

		if (userLogin != null) {
			criteria.add(or(eq(ProcessInstanceLog._USER_LOGIN, userLogin), eq(ProcessInstanceLog._USER_SUBSTITUTE_LOGIN, userLogin)));
		}

		if (startDate != null) {
			criteria.add(ge(ProcessInstanceLog._ENTRY_DATE, truncHours(startDate)));
		}
		if (endDate != null) {
			criteria.add(le(ProcessInstanceLog._ENTRY_DATE, truncHours(addDays(endDate, 1))));
		}

		criteria.createAlias(ProcessInstanceLog._STATE, "s", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias(ProcessInstanceLog._PROCESS_INSTANCE, "pi");
		criteria.createAlias("pi.definition", "def");
		criteria.createAlias(ProcessInstanceLog._OWN_PROCESS_INSTANCE, "ownPi");

		ProjectionList pl = Projections.projectionList()
				.add(Projections.id(), _ID)
				.add(Projections.property(ProcessInstanceLog._ENTRY_DATE), ProcessInstanceLog._ENTRY_DATE)
				.add(Projections.property(ProcessInstanceLog._EVENT_I18N_KEY), ProcessInstanceLog._EVENT_I18N_KEY)
				.add(Projections.property(ProcessInstanceLog._EXECUTION_ID), ProcessInstanceLog._EXECUTION_ID)
				.add(Projections.property(ProcessInstanceLog._ADDITIONAL_INFO), ProcessInstanceLog._ADDITIONAL_INFO)
				.add(Projections.property(ProcessInstanceLog._USER_LOGIN), ProcessInstanceLog._USER_LOGIN)
				.add(Projections.property("s.id"), "state.id")
				.add(Projections.property("s.name"), "state.name")
				.add(Projections.property("s.description"), "state.description")
				.add(Projections.property(ProcessInstanceLog._USER_SUBSTITUTE_LOGIN), ProcessInstanceLog._USER_SUBSTITUTE_LOGIN)
				.add(Projections.property("pi.id"), "processInstance.id")
				.add(Projections.property("pi.internalId"), "processInstance.internalId")
				.add(Projections.property("pi.externalKey"), "processInstance.externalKey")
				.add(Projections.property("pi.status"), "processInstance.status")
				.add(Projections.property("pi.createDate"), "processInstance.createDate")
				.add(Projections.property("def.id"), "processInstance.definition.id")
				.add(Projections.property("def.description"), "processInstance.definition.description")
				.add(Projections.property("def.comment"), "processInstance.definition.comment")
				.add(Projections.property("ownPi.id"), "ownProcessInstance.id");

		criteria.setProjection(pl);

		criteria.setResultTransformer(new NestedAliasToBeanResultTransformer(ProcessInstanceLog.class));

		long duration = System.currentTimeMillis() - start;
		logger.severe("getUserHistory: " +  duration);

		return criteria.list();
	}


	@Override
	public Collection<ProcessInstance> getUserProcessesAfterDate(String userLogin, Date minDate) {
		return getUserProcessesBetweenDates(userLogin, minDate, null);
	}


	@Override
	public Collection<ProcessInstance> getUserProcessesBetweenDates(String userLogin, Date minDate, Date maxDate) {

		long start = System.currentTimeMillis();
		Session session = getSession();

		ProjectionList properties = Projections.projectionList();
		properties.add(Projections.property(_ID));
		properties.add(Projections.property(_INTERNAL_ID));
		Criteria criteria = session.createCriteria(ProcessInstance.class)
				.setProjection(Projections.distinct(properties))
				.createCriteria(_PROCESS_LOGS);

		if(minDate!=null){
			criteria.add(gt(ProcessInstanceLog._ENTRY_DATE, minDate));
		}

		if(maxDate!=null){
			criteria.add(le(ProcessInstanceLog._ENTRY_DATE, maxDate));
		}

		criteria.add(eq(ProcessInstanceLog._USER_LOGIN, userLogin));

		List<Object[]> list = criteria.list();
		Collection<ProcessInstance> collect = Collections.collect(list, new Transformer<Object[], ProcessInstance>() {
			@Override
			public ProcessInstance transform(Object[] row) {
				ProcessInstance pi = new ProcessInstance();
				pi.setId((Long) row[0]);
				pi.setInternalId((String) row[1]);
				return pi;
			}
		});

		long duration = System.currentTimeMillis() - start;
		logger.severe("getUserProcessesBetweenDates: " +  duration);
		return collect;

	}

	@Override
	public void saveStepInfos(Collection<StepInfo> stepInfos) {
		for (StepInfo stepInfo : stepInfos) {
			getSession().saveOrUpdate(stepInfo);
		}
	}

	@Override
	public void removeStopInfos(Collection<String> taskIds) {
		if (taskIds.isEmpty()) {
			return;
		}

		List<Long> taskIdsLong = new ArrayList<Long>();

		for (String taskId : taskIds) {
			taskIdsLong.add(Long.parseLong(taskId));
		}

		getSession().createQuery("delete from StepInfo si where si.taskId in (:taskIds)")
				.setParameterList("taskIds", taskIdsLong)
				.executeUpdate();
	}

	@Override
	public ResultsPageWrapper<ProcessInstance> getRecentProcesses(String userLogin, Date minDate, Integer offset, Integer limit) {
		Session session = getSession();
		List<ProcessInstance> instances = null;
		if (offset != null && limit != null) {
			List<Long> list = session.createCriteria(ProcessInstance.class)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.distinct(Projections.property(_ID)))
					.addOrder(Order.desc(_ID))
					.setFirstResult(offset)
					.setMaxResults(limit)
					.createCriteria(_PROCESS_LOGS)
					.add(Restrictions.gt(ProcessInstanceLog._ENTRY_DATE, minDate))
					.add(Restrictions.eq(ProcessInstanceLog._USER_LOGIN, userLogin))
					.list();
			instances = getProcessInstances(list);
		}

		Number total = (Number) session.createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.setProjection(Projections.countDistinct(_ID))
				.createCriteria(_PROCESS_LOGS)
				.add(Restrictions.gt(ProcessInstanceLog._ENTRY_DATE, minDate))
				.add(Restrictions.eq(ProcessInstanceLog._USER_LOGIN, userLogin)).uniqueResult();

		ResultsPageWrapper<ProcessInstance> resultsPageWrapper = new ResultsPageWrapper<ProcessInstance>(instances != null ? instances : new ArrayList<ProcessInstance>(),
				total == null ? 0 : total.intValue());

		return resultsPageWrapper;
	}

	@Override
	public Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit,
													   boolean onlyRunning, String[] userRoles,
													   String assignee, String... queues) {
		long start = System.currentTimeMillis();

		List<ProcessInstance> list = (List<ProcessInstance>)session.createCriteria(ProcessInstance.class)
				.add(or(like(_EXTERNAL_KEY, "%"+filter+"%"), like(_INTERNAL_ID, "%"+filter+"%")))
				.setMaxResults(limit)
				.setFirstResult(offset)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc(_CREATE_DATE))
				.list();
		long duration = System.currentTimeMillis() - start;
		logger.severe("searchProcesses: " +  duration);
		return list;
	}
}
