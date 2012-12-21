package pl.net.bluesoft.rnd.processtool.event;

import java.io.Serializable;

/** 
 * Marker for system bus events
 * 
 * @author Maciej Pawlak
 *
 */
public interface IEvent extends Serializable 
{
	/** Get type of this event */
	TypeMarker getEventType();

	/** Marker for the enumeration with event type */
	public interface TypeMarker {}
}
