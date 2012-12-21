package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceFilterDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.List;

public class ProcessInstanceFilterDAOImpl extends SimpleHibernateBean<ProcessInstanceFilter> implements ProcessInstanceFilterDAO {
	public ProcessInstanceFilterDAOImpl(Session session) {
		super(session);
	}

	@Override
	public List<ProcessInstanceFilter> findAllByUserData(UserData userData) {
        Session session = getSession();
		return session.createCriteria(ProcessInstanceFilter.class)
				       .add(Restrictions.eq("filterOwner", userData))
				       .addOrder(Order.asc("name"))
				       .list();
	}

	@Override
	public ProcessInstanceFilter fullLoadById(Long id) {
        Session session = getSession();
		Criteria criteria = session.createCriteria(ProcessInstanceFilter.class);
		for (String relation : ProcessInstanceFilter.LAZY_RELATIONS) {
			criteria.setFetchMode(relation, FetchMode.JOIN);
		}
		return (ProcessInstanceFilter) criteria.add(Restrictions.eq("id", id)).uniqueResult();
	}

	public long saveProcessInstanceFilter(ProcessInstanceFilter processInstance) {
        Session session = getSession();
		session.saveOrUpdate(processInstance);
//		session.flush();
		return processInstance.getId();
	}

	@Override
	public void deleteFilter(ProcessInstanceFilter filter) {
        Session session = getSession();
		session.delete(filter);
	}
}
