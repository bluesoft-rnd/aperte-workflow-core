package pl.net.bluesoft.rnd.pt.ext.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import pl.net.bluesoft.rnd.pt.utils.jdbc.JdbcConnector;
import pl.net.bluesoft.rnd.pt.utils.jdbc.JdbcUtils;
import pl.net.bluesoft.rnd.pt.utils.jdbc.builder.SelectBuilder;

import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import static pl.net.bluesoft.util.criteria.Criteria.like;
import static pl.net.bluesoft.util.criteria.Criteria.or;

public class ConnectionTest {
    @Before
    public void initConnection() throws NamingException {
        SimpleNamingContextBuilder ic = new SimpleNamingContextBuilder();

        BasicDataSource ds = new BasicDataSource();
        ds.setUsername("user");
        ds.setPassword("axa");
        ds.setUrl("jdbc:jtds:sqlserver://192.168.1.155:9003/exact;ssl=request;useCursors=true");
        ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
        ic.bind("jdbc/basic", ds);

        ic.activate();
    }

    @Test
    public void testBeanListHandler() throws Exception {
        JdbcConnector connector = JdbcUtils.createSimpleConnector("jdbc/basic");
        List<EsodKonta> results = connector.query(EsodKonta.class, "select * from ESOD_Konta");
        System.out.println(results.size());
    }

    @Test
    public void testConnection() throws Exception {
        JdbcConnector connector = JdbcUtils.createSimpleConnector("jdbc/basic");
        List<Map<String, Object>> results = connector.query("select * from ESOD_Konta");
        System.out.println(results.size());
    }

    @Test
    public void testCamel() throws Exception {
        String sql = new SelectBuilder()
                .forClass(EsodKonta.class)
                .field("numerKonta")
                .field("typKonta")
                .criterion(
                        or(like("numerKonta", "81%"), like("numerKonta", "93%")))
                .build();
        JdbcConnector connector = JdbcUtils.createSimpleConnector("jdbc/basic");
        List<EsodKonta> results = connector.query(EsodKonta.class, sql);
        System.out.println(results.size());
    }

    @Test
    public void testPreference() {
        Preferences myfilePrefs = Preferences.userRoot();
        System.out.println(myfilePrefs.absolutePath());
    }
}
