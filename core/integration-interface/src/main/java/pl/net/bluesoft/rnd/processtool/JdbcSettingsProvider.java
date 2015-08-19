package pl.net.bluesoft.rnd.processtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.ExpiringCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 *
 * Aperte simple jdbc settings provider
 *
 * @author: mpawlak@bluesoft.net.pl
 */
public class JdbcSettingsProvider
{

    public static String getSetting(Connection connection, String key)
    {
        if(key == null || key.isEmpty())
            return null;

        try {
            PreparedStatement getLockStatement = connection.prepareStatement("SELECT value_ as VALUE from pt_setting where key_ = ?");
            getLockStatement.setString(1, key);


            ResultSet resultSet = getLockStatement.executeQuery();
            if(resultSet.next())
            {
                return resultSet.getString("VALUE");
            }

            return null;
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
    }

    public static void setSetting(Connection connection, String key, String value)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("UPDATE pt_setting set value_=? where key_ = ?");

            statement.setString(1, value);
            statement.setString(2, key);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
    }

}
