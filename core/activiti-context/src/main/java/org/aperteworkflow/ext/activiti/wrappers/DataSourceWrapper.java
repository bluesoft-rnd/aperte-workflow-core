package org.aperteworkflow.ext.activiti.wrappers;

import org.hibernate.Session;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DataSourceWrapper implements DataSource {
    private PrintWriter logWriter = new PrintWriter(System.out);
    private int loginTimeout = 60;

    private Session session;

    public DataSourceWrapper(Session session) {
        this.session = session;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ConnectionWrapper(session.connection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }


    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new java.sql.SQLFeatureNotSupportedException("getParentLogger not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.newInstance();
        } catch (InstantiationException e) {
            throw new SQLException(e);
        } catch (IllegalAccessException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
