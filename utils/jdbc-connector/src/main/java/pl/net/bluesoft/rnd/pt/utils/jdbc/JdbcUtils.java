package pl.net.bluesoft.rnd.pt.utils.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import pl.net.bluesoft.rnd.pt.utils.jdbc.exception.DataSourceDiscoveryFailedException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class JdbcUtils {

    public static JdbcConnector createSimpleConnector(String dataSourceBindingName) throws DataSourceDiscoveryFailedException {
        DataSource ds = setupDataSource(dataSourceBindingName);
        return new SimpleQueryJdbcConnector(ds);
    }

    private static DataSource setupDataSource(String name) throws DataSourceDiscoveryFailedException {
        DataSource ds = null;
        try {
            ds = (DataSource) new InitialContext().lookup(name);
        }
        catch (NamingException e) {
            throw new DataSourceDiscoveryFailedException(e);
        }

        if (ds == null) {
            throw new DataSourceDiscoveryFailedException("Unable to find data source by JNDI name: " + name);
        }
        return ds;
    }

//    private static DataSource setupDataSource(String driverClassName, String url, String username, String password) {
//        BasicDataSource ds = new BasicDataSource();
//        ds.setDriverClassName(driverClassName);
//        ds.setUrl(url);
//        ds.setUsername(username);
//        ds.setPassword(password);
//        return ds;
//    }
}
