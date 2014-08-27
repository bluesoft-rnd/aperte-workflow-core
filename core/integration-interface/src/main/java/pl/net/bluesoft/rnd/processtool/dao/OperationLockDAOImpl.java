package pl.net.bluesoft.rnd.processtool.dao;

import org.hibernate.*;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;

import java.sql.*;
import java.util.Date;
import java.util.List;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author mpawlak@bluesoft.net.pl
 */
public class OperationLockDAOImpl implements OperationLockDAO

{
    Connection connection;
    public OperationLockDAOImpl(Connection connection) {
       this.connection = connection;
    }

    private static Timestamp getSqlDate(Date date) {
        return new Timestamp(date.getTime());
    }

    public void createLock(OperationLock lock)
    {

        try
        {


            PreparedStatement statement = connection.prepareStatement("INSERT INTO pt_lock_operation (ID, LOCK_DATE, LOCK_MODE, LOCK_NAME, LOCK_RELEASE_DATE) " +
                    " VALUES(nextval('db_seq_id'), ?, ?, ?, ?)");

            statement.setTimestamp(1, getSqlDate(lock.getLockDate()));
            statement.setString(2, lock.getLockMode().toString());
            statement.setString(3, lock.getLockName());
            statement.setTimestamp(4, getSqlDate(lock.getLockReleaseDate()));

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }

    }

    @Override
    public OperationLock getLock(String operationName)
    {
        try {
            PreparedStatement getLockStatement = connection.prepareStatement("SELECT lock.* FROM pt_lock_operation lock WHERE lock.lock_name = ?");
            getLockStatement.setString(1, operationName);


            ResultSet resultSet = getLockStatement.executeQuery();
            if(resultSet.next())
            {
                Long id = resultSet.getLong("ID");
                Date date = resultSet.getTimestamp("LOCK_DATE");
                String lockName = resultSet.getString("LOCK_NAME");
                String lockMode = resultSet.getString("LOCK_MODE");
                Date releaseDate = resultSet.getTimestamp("LOCK_RELEASE_DATE");

                OperationLock operationLock = new OperationLock();
                operationLock.setId(id);
                operationLock.setLockMode(OperationLockMode.valueOf(lockMode));
                operationLock.setLockDate(date);
                operationLock.setLockReleaseDate(releaseDate);
                operationLock.setLockName(lockName);

                return operationLock;
            }

            return null;
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
    }

    @Override
    public void removeLock(OperationLock operationLock)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM pt_lock_operation WHERE LOCK_NAME = ?");

            statement.setString(1, operationLock.getLockName());

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Probem with sql", e);
        }
    }
}
