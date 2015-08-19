package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Facade layer for the Notification database access with jdbc driver
 * 
 * @author Maciej Pawlak
 *
 */
public class NotificationsJdbcFacade
{
	/** Get all notifications waiting to be sent */
	public static Collection<BpmNotification> getNotificationsToSend(Connection connection, int interval, int limit)
	{
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int time = 1000*(c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND));

        try
        {
            PreparedStatement statement = connection.prepareStatement("select * from pt_ext_bpm_notification " +
                    "where groupnotifications = false or (groupnotifications = true and sendafterhour >= ? and sendafterhour <= ?) " +
                    "order by recipient asc");

            statement.setInt(1, time - interval);
            statement.setInt(2, time + interval);

            Collection<BpmNotification> notifications = new ArrayList<BpmNotification>();

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next())
            {
                BpmNotification notification = new BpmNotification();

                Long id = resultSet.getLong("ID");
                String attachments = resultSet.getString("ATTACHMENTS");
                String body = resultSet.getString("BODY");
                Boolean groupNotifications = resultSet.getBoolean("GROUPNOTIFICATIONS");
                Boolean sendAsHtml = resultSet.getBoolean("SENDASHTML");
                String profileName = resultSet.getString("PROFILENAME");
                Date notificationCreated = resultSet.getTimestamp("NOTIFICATIONCREATED");
                String recipient = resultSet.getString("RECIPIENT");
                Integer sendAfterHour = resultSet.getInt("SENDAFTERHOUR");
                String sender = resultSet.getString("SENDER");
                String subject = resultSet.getString("SUBJECT");
				String source = resultSet.getString("SOURCE");
				String tag = resultSet.getString("TAG");
				String templateName = resultSet.getString("TEMPLATE_NAME");
                String sentFolderName = resultSet.getString("SENT_FOLDER_NAME");

                notification.setId(id);
                notification.setAttachments(attachments);
                notification.setBody(body);
                notification.setGroupNotifications(groupNotifications);
                notification.setNotificationCreated(notificationCreated);
                notification.setProfileName(profileName);
                notification.setRecipient(recipient);
                notification.setSendAfterHour(sendAfterHour);
                notification.setSendAsHtml(sendAsHtml);
                notification.setSender(sender);
                notification.setSubject(subject);
				notification.setSource(source);
				notification.setTag(tag);
				notification.setTemplateName(templateName);
                notification.setSentFolderName(sentFolderName);

                notifications.add(notification);
            }

            return notifications;
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
	}
	
	public static void removeNotification(Connection connection, BpmNotification notification)
	{
        try
        {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM pt_ext_bpm_notification WHERE id = ?");

            statement.setLong(1, notification.getId());

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
	}
}
