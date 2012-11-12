package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.util.lang.Strings;

/**
 * This enumeration represents diffrent queue types
 * 
 * @author Maciej Pawlak
 *
 */
public enum QueueType 
{
	/** User created task, done by others */
    OWN_IN_PROGRESS, 
    /** User created task, assigned to him */
//    OWN_ASSIGNED,
    /** User created task, but it is put in queue */
    OWN_IN_QUEUE,
    /** User created task in finished state */
    OWN_FINISHED, 
    /** Others task, assigned to current user */
//    OTHERS_ASSIGNED,
	/** Tasks assigned to current user regardless who created them */
	ASSIGNED_TO_CURRENT_USER;

    public static QueueType fromString(String name) {
        return Strings.hasText(name) ? valueOf(name.toUpperCase()) : null;
    }

    public static QueueType fromChar(char c) {
        String prefix = ("" + c).toUpperCase();
        QueueType value = null;
        if (Strings.hasText(prefix)) {
            for (QueueType ps : values()) {
                if (ps.name().startsWith(prefix)) {
                    value = ps;
                    break;
                }
            }
        }
        return value;
    }
}
