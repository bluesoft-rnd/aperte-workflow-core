package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.StepInfo;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessInstanceDAO extends HibernateBean<ProcessInstance> {

    long saveProcessInstance(ProcessInstance processInstance);

    ProcessInstance refreshProcessInstance(ProcessInstance processInstance);

    ProcessInstance getProcessInstance(long id);

    List<ProcessInstance> getProcessInstances(Collection<Long> ids);

    ProcessInstance getProcessInstanceByInternalId(String internalId);

    ProcessInstance getProcessInstanceByExternalId(String externalId);

    Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId);

    void deleteProcessInstance(ProcessInstance instance);
    
    
    Collection<ProcessInstanceLog> getUserHistory(String userLogin, Date startDate, Date endDate);

    Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning, String[] userRoles, String assignee, String... queues);

    Collection<ProcessInstance> getUserProcessesAfterDate(String userLogin, Date minDate);

    ResultsPageWrapper<ProcessInstance> getRecentProcesses(String userLogin, Date minDate, Integer offset, Integer limit);

	Collection<ProcessInstance> getUserProcessesBetweenDates(String userLogin, Date minDate, Date maxDate);

	void saveStepInfos(Collection<StepInfo> stepInfos);

	void removeStopInfos(Collection<String> taskId);
}
