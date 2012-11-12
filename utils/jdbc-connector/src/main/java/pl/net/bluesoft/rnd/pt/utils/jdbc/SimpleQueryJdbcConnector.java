package pl.net.bluesoft.rnd.pt.utils.jdbc;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import pl.net.bluesoft.rnd.pt.utils.jdbc.convert.ConvertingBeanProcessor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SimpleQueryJdbcConnector implements JdbcConnector {
    private static final Logger logger = Logger.getLogger(SimpleQueryJdbcConnector.class.getName());

    private QueryRunner run;

    public SimpleQueryJdbcConnector(DataSource dataSource) {
        this.run = new QueryRunner(dataSource);
    }

    @Override
    public <T> List<T> query(Class<T> outputType, String sql, Object... params) throws SQLException {
        logger.warning("Executing SQL: " + sql + " for class: " + outputType.getName() + " with parameters: " + Arrays.toString(params));
        return run.query(sql, new BeanListHandler<T>(outputType, new BasicRowProcessor(new ConvertingBeanProcessor())), params);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        logger.warning("Executing SQL: " + sql + " with parameters: " + Arrays.toString(params));
        return run.query(sql, new MapListHandler(), params);
    }

}
