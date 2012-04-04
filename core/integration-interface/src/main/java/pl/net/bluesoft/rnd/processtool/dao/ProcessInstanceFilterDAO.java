package pl.net.bluesoft.rnd.processtool.dao;


import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.List;

public interface ProcessInstanceFilterDAO extends HibernateBean<ProcessInstanceFilter> {
	List<ProcessInstanceFilter> findAllByUserData(UserData userData);

	ProcessInstanceFilter fullLoadById(Long id);

	long saveProcessInstanceFilter(ProcessInstanceFilter processInstance);

	void deleteFilter(ProcessInstanceFilter filter);
}
