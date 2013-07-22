package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-07-19
 * Time: 13:48
 */
public interface StartProcessResult {
	ProcessInstance getProcessInstance();
	List<BpmTask> getTasksAssignedToCreator();
}
