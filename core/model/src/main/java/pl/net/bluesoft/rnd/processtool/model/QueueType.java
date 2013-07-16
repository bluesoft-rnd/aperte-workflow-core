package pl.net.bluesoft.rnd.processtool.model;

/**
 * This enumeration represents diffrent queue types
 * 
 * @author Maciej Pawlak
 *
 */
public enum QueueType {
	/** User created task, done by others */
    OWN_IN_PROGRESS, 
    /** User created task, assigned to him */
    OWN_ASSIGNED, 
    /** User created task, but it is put in queue */
    OWN_IN_QUEUE,
    /** User created task in finished state */
    OWN_FINISHED, 
    /** Others task, assigned to current user */
    ASSIGNED_TO_CURRENT_USER
}
