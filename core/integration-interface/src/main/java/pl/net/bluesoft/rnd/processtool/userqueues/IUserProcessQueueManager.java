package pl.net.bluesoft.rnd.processtool.userqueues;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;

/**
 * Manager for user process queues, which provides logic to manipulate queues according
 * do process changes
 * 
 * @author mpawlak
 *
 */
public interface IUserProcessQueueManager
{
	/** Actions performed at process assignation change */
	void onTaskAssigne(BpmTask task);
	
	/** Perform action on task finished */
	void onTaskFinished(BpmTask task);
	
	/** Actions performed after process finalization */
	void onProcessFinished(ProcessInstance processInstance, BpmTask bpmTask);

	/** Actions performed after process halt becouse of new subprocess creation */
	void onProcessHalted(ProcessInstance processInstance, BpmTask task);

	/** Actions perform on task assignation to queue */
	void onQueueAssigne(MutableBpmTask mutableTask);
}
