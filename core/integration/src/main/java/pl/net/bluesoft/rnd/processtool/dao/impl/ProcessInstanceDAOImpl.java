package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.*;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceDAOImpl extends SimpleHibernateBean<ProcessInstance> implements ProcessInstanceDAO {
	public ProcessInstanceDAOImpl(Session session) {
		super(session);
	}

	public long saveProcessInstance(ProcessInstance processInstance) {
		UserData creator = processInstance.getCreator();
		if (creator != null) {
			if (creator.getId() != null) {
				processInstance.setCreator((UserData) session.get(UserData.class, creator.getId()));
			} else {
				List users = session.createCriteria(UserData.class)
						.add(eq("login", creator.getLogin())).list();
				if (users.isEmpty()) {
					session.saveOrUpdate(creator);
				} else {
					processInstance.setCreator((UserData) users.get(0));
				}
			}
		}
        if (processInstance.getToDelete() != null) {
            for (Object o : processInstance.getToDelete()) {
                session.delete(o);
            }
        }
		session.saveOrUpdate(processInstance);
//		session.flush();
		return processInstance.getId();
	}

	public ProcessInstance getProcessInstance(long id) {
		return (ProcessInstance) session.get(ProcessInstance.class, id);
	}

	@Override
	public ProcessInstance getProcessInstanceByInternalId(String internalIds) {

		List list = session.createCriteria(ProcessInstance.class)
						.add(eq("internalId", internalIds)).list();
		if (list.isEmpty())
			return null;
		else
			return (ProcessInstance) list.get(0);
	}

	@Override
	public ProcessInstance getProcessInstanceByExternalId(String externalId) {
		List list = session.createCriteria(ProcessInstance.class)
						.add(eq("externalKey", externalId)).list();
		if (list.isEmpty())
			return null;
		else
			return (ProcessInstance) list.get(0);
	}

	@Override
	public List<ProcessInstance> findProcessInstancesByKeyword(String keyword, String processType) {
		return session.createCriteria(ProcessInstance.class)
						.add(eq("keyword", keyword))
//						.add(eq("definition.bpmDefinitionKey", processType))
						.addOrder(Order.desc("id"))						
						.list();

	}

	@Override
	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(List<String> internalId) {
		if (internalId.isEmpty()) return new HashMap();
		List<ProcessInstance> list = session.createCriteria(ProcessInstance.class)
						.add(in("internalId", internalId)).list();
		Map<String,ProcessInstance> res = new HashMap();
		for (ProcessInstance pi : list) {
			res.put(pi.getInternalId(), pi);
		}
		return res;
	}

	public List<ProcessInstance> getProcessInstancesByIds(List<Long> ids) {
		if (ids.isEmpty()) return new ArrayList<ProcessInstance>();
		List<ProcessInstance> list = session.createCriteria(ProcessInstance.class)
						.add(in("id", ids)).list();
		return list;
	}

	public void deleteProcessInstance(ProcessInstance instance) {
		session.delete(instance);
	}

	@Override
	public UserData findOrCreateUser(UserData ud) {
		List userList = session.createCriteria(UserData.class)
				.add(Restrictions.eq("login", ud.getLogin())).list();
		if (userList.isEmpty()) {
			//create new user
			session.saveOrUpdate(ud);
			userList = session.createCriteria(UserData.class).add(Restrictions.eq("login", ud.getLogin())).list();
		}
		return (UserData) userList.get(0);


	}

	@Override
	public List<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate) {
		List<Long> list = session.createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.setProjection(Projections.distinct(Projections.property("id")))
				.addOrder(Order.desc("id"))
				.createCriteria("processLogs")
				.add(Restrictions.gt("entryDate", minDate))
				.createAlias("user", "u")
                .createAlias("userSubstitute", "us", CriteriaSpecification.LEFT_JOIN)
				.add(Restrictions.or(
                        Restrictions.eq("u.id", userData.getId()),
                        Restrictions.eq("us.id", userData.getId())))
				.setMaxResults(100).list();

		return getProcessInstancesByIds(list);
	}
}
