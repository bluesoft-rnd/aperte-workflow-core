package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.plugins.DataRegistry;
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

	private static Session getSession()
	{
		return ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
	}



}
