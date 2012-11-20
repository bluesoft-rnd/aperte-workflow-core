package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade;

import java.util.Collection;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;

/**
 * Facade layer for the Notification database access
 * 
 * @author Maciej Pawlak
 *
 */
public class NotificationsFacade 
{
	/** Get all notifications waiting to be sent */
	public static Collection<BpmNotification> getNotificationsToSend()
	{
		return (List<BpmNotification>)getSession()
				.createCriteria(BpmNotification.class)
				.setLockMode(LockMode.UPGRADE_NOWAIT)
				.list();
	}
	/** Get all notifications properties */
	public static Collection<BpmNotificationMailProperties> getNotificationMailProperties()
	{
		Session session = getSession();
		
		return session.createCriteria(BpmNotificationMailProperties.class).list();
	}
	
	/** Saves given notifications to database */
	public static void addNotificationToBeSent(BpmNotification notification)
	{
		Session session = getSession();
		
		session.saveOrUpdate(notification);
	}
	
	public static void removeNotification(BpmNotification notification) 
	{
		Session session = getSession();
		
		session.delete(notification);
		
	}
	
	private static Session getSession()
	{
		return ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
	}



}
