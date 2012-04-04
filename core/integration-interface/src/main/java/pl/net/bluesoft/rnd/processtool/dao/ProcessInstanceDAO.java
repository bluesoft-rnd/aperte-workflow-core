package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.*;

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

    List<ProcessInstance> findProcessInstancesByKeyword(String key, String processType);

    Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId);

    void deleteProcessInstance(ProcessInstance instance);

    Collection<ProcessInstanceLog> getUserHistory(UserData user, Date startDate, Date endDate);

    UserData findOrCreateUser(UserData ud);
//	List<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, String filter, int offset, int limit);

    Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning, String[] userRoles, String assignee, String... queues);

    Collection<ProcessInstance> getUserProcessesAfterDate(UserData userData, Calendar minDate);

    ResultsPageWrapper<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, Integer offset, Integer limit);

    ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(Collection<String> internalIds, ProcessInstanceFilter filter,
                                                                                    Integer offset, Integer limit);

}
