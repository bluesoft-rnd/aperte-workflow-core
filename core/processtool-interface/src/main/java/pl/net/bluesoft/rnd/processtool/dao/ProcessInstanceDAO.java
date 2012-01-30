package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessInstanceDAO {

	long saveProcessInstance(ProcessInstance processInstance);
	ProcessInstance getProcessInstance(long id);
	ProcessInstance getProcessInstanceByInternalId(String internalId);
	ProcessInstance getProcessInstanceByExternalId(String externalId);
	List<ProcessInstance> findProcessInstancesByKeyword(String key, String processType);
	Map<String,ProcessInstance> getProcessInstanceByInternalIdMap(List<String> internalId);
	void deleteProcessInstance(ProcessInstance instance);
	UserData findOrCreateUser(UserData ud);
	List<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, String filter, int offset, int limit);

    Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning, String[] userRoles, String assignee, String... queues);
}
