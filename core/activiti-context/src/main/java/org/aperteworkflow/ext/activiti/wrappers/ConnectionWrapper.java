package org.aperteworkflow.ext.activiti.wrappers;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ConnectionWrapper implements Connection {
    
    private Connection c;

    public ConnectionWrapper(Connection c) {
        this.c = c;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return c.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return c.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return c.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return c.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        c.setAutoCommit(autoCommit);  
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return c.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        //nothing, since we manage transactions externally
    }

    @Override
    public void rollback() throws SQLException {
        //nothing, since we manage transactions externally        
    }

    @Override
    public void close() throws SQLException {
        //nothing, since we manage transactions externally        
    }

    @Override
    public boolean isClosed() throws SQLException {
        return c.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return c.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        //nothing, since we manage transactions externally      
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return c.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        c.setCatalog(catalog);  
    }

    @Override
    public String getCatalog() throws SQLException {
        return c.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        //nothing, since we manage transactions externally      
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return c.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return c.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        c.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return c.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return c.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return c.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return c.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        c.setTypeMap(map); 
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        c.setHoldability(holdability);  
    }

    @Override
    public int getHoldability() throws SQLException {
        return c.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return c.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return c.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        c.rollback(savepoint);  
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        c.releaseSavepoint(savepoint);  
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return c.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return c.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return c.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return c.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return c.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return c.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return c.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return c.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return c.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return c.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return c.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        c.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        c.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return c.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return c.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return c.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return c.createStruct(typeName, attributes);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return c.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return isWrapperFor(iface);
    }
}
