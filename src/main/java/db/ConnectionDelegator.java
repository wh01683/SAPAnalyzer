package db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionDelegator implements Connection {
    public void setSchema(String schema) throws SQLException {

    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    public static class LogEntry {
        public String sql;
        public int invocationCount;
        public double avgTime;
        public double maxTime;
    }

    private Connection delegate;

    private Map<String, LogEntry> log;

    public ConnectionDelegator(Connection delegate) {
        this.delegate = delegate;
        this.log = new HashMap<String, LogEntry>();
    }

    public Map<String, LogEntry> getLog() {
        return log;
    }

    public void updateEntry(String sql, long execTime) {
        LogEntry logEntry;
        if (!log.containsKey(sql)) {
            logEntry = new LogEntry();
            logEntry.sql = sql;
            logEntry.invocationCount = 0;
            logEntry.avgTime = 0;
            logEntry.maxTime = 0;
            log.put(sql, logEntry);
        } else {
            logEntry = log.get(sql);
        }

        logEntry.avgTime = (logEntry.invocationCount * logEntry.avgTime + execTime) / (logEntry.invocationCount + 1);
        logEntry.maxTime = Math.max(logEntry.maxTime, execTime);
        logEntry.invocationCount++;
    }

    public void clearWarnings()
            throws SQLException {
        delegate.clearWarnings();
    }

    public void close()
            throws SQLException {
        delegate.close();
    }

    public void commit()
            throws SQLException {
        delegate.commit();
    }

    public Array createArrayOf(String typeName, Object[] elements)
            throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    public Blob createBlob()
            throws SQLException {
        return delegate.createBlob();
    }

    public Clob createClob()
            throws SQLException {
        return delegate.createClob();
    }

    public NClob createNClob()
            throws SQLException {
        return delegate.createNClob();
    }


    public SQLXML createSQLXML()
            throws SQLException {
        return delegate.createSQLXML();
    }


    public Statement createStatement()
            throws SQLException {
        return delegate.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }


    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }


    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }


    public Struct createStruct(String typeName, Object[] attributes)
            throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }


    public boolean getAutoCommit()
            throws SQLException {
        return delegate.getAutoCommit();
    }


    public String getCatalog()
            throws SQLException {
        return delegate.getCatalog();
    }


    public Properties getClientInfo()
            throws SQLException {
        return delegate.getClientInfo();
    }


    public String getClientInfo(String name)
            throws SQLException {
        return delegate.getClientInfo(name);
    }


    public int getHoldability()
            throws SQLException {
        return delegate.getHoldability();
    }


    public DatabaseMetaData getMetaData()
            throws SQLException {
        return delegate.getMetaData();
    }


    public int getTransactionIsolation()
            throws SQLException {
        return delegate.getTransactionIsolation();
    }


    public Map<String, Class<?>> getTypeMap()
            throws SQLException {
        return delegate.getTypeMap();
    }


    public SQLWarning getWarnings()
            throws SQLException {
        return delegate.getWarnings();
    }


    public boolean isClosed()
            throws SQLException {
        return delegate.isClosed();
    }


    public boolean isReadOnly()
            throws SQLException {
        return delegate.isReadOnly();
    }


    public boolean isValid(int timeout)
            throws SQLException {
        return delegate.isValid(timeout);
    }


    public String nativeSQL(String sql)
            throws SQLException {
        return delegate.nativeSQL(sql);
    }


    public CallableStatement prepareCall(String sql)
            throws SQLException {
        return delegate.prepareCall(sql);
    }


    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }


    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return null;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return null;
    }


    public void releaseSavepoint(Savepoint savepoint)
            throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }


    public void rollback()
            throws SQLException {
        delegate.rollback();
    }


    public void rollback(Savepoint savepoint)
            throws SQLException {
        delegate.rollback(savepoint);
    }


    public void setAutoCommit(boolean autoCommit)
            throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }


    public void setCatalog(String catalog)
            throws SQLException {
        delegate.setCatalog(catalog);
    }


    public void setClientInfo(Properties properties)
            throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }


    public void setClientInfo(String name, String value)
            throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }


    public void setHoldability(int holdability)
            throws SQLException {
        delegate.setHoldability(holdability);
    }


    public void setReadOnly(boolean readOnly)
            throws SQLException {
        delegate.setReadOnly(readOnly);
    }


    public Savepoint setSavepoint()
            throws SQLException {
        return delegate.setSavepoint();
    }


    public Savepoint setSavepoint(String name)
            throws SQLException {
        return delegate.setSavepoint(name);
    }


    public void setTransactionIsolation(int level)
            throws SQLException {
        delegate.setTransactionIsolation(level);
    }


    public void setTypeMap(Map<String, Class<?>> map)
            throws SQLException {
        delegate.setTypeMap(map);
    }


    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(delegate);
    }


    public <T> T unwrap(Class<T> iface) {
        return iface.cast(delegate);
    }
}
