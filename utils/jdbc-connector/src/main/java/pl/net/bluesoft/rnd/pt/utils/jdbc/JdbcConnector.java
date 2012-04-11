package pl.net.bluesoft.rnd.pt.utils.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface JdbcConnector {
    <T> List<T> query(Class<T> outputType, String sql, Object... params) throws SQLException;

    List<Map<String, Object>> query(String sql, Object... params) throws SQLException;
}
