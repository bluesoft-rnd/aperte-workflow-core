package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.dialect.resolver.AbstractDialectResolver;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Fix for https://issues.jboss.org/browse/JBPM-2333 - but still providing auto-detection feature.
 * @author tlipski@bluesoft.net.pl
 */
public class MySQLDialectResolver extends AbstractDialectResolver {

    @Override
    protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
        String databaseName = metaData.getDatabaseProductName();
        int databaseMajorVersion = metaData.getDatabaseMajorVersion();

        if ( "MySQL".equals( databaseName ) ) {
            if (5 == databaseMajorVersion)
                return new MySQL5InnoDBDialect();
            else
                return new MySQLInnoDBDialect();
        }

        return null;
    }
}
