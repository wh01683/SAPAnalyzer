package db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by robert on 9/19/2015.
 */


public class DBIO {


    private final static String DATABASE_NAME = "sap";
    private final static String USER_NAME = "HOWERTONSAP";
    private final static String PASSWORD = "database015";
    private static ConnectionDelegator con = null;
    private static ArrayList<String> tableNames;
    private static String currentTable;

    public DBIO() {
        MakeConnection();
    }

    public static void instantiate() {
        MakeConnection();
    }

    public static void MakeConnection() {
        try {
            con = new ConnectionDelegator(DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:" + DATABASE_NAME, USER_NAME, PASSWORD));
            DBIO.updateTableNames();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Alters constraints on a set of given tables by enabling (pass true) or disabling (pass false) each foreign key
     * and each trigger associated with each table.
     *
     * @param enable True: enable all triggers and reference constraints. False: Disable all.
     * @param tables Array of table names to alter.
     */
    public static int[] alterConstraints(boolean enable, String... tables) {


        int[] results = new int[0];
        try {
            Statement stmt = con.createStatement();
            for (String table : tables) {
                ArrayList<String> constraints = DBInfo.getTabToRefConstraint().get(table);
                for (String constraint : constraints) {
                    stmt.addBatch("alter table " + table + " " + ((enable) ? "enable" : "disable") + " constraint " + constraint);
                }
                stmt.addBatch("alter table " + table + " " + ((enable) ? "enable" : "disable") + " all triggers");
            }

            results = stmt.executeBatch();

            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Executes an arraylist of String query and returns an ArrayList of the associated result sets.
     *
     * @param query ArrayList of query to execute.
     * @return ArrayList of ResultSet objects.
     */
    private static ResultSet executeQuery(String query) throws SQLException {

        ArrayList<ResultSet> results = new ArrayList<ResultSet>(10);

        Statement stmt = con.prepareStatement(query);

        try {

            ResultSet rs = stmt.executeQuery(query);
            results.add(rs);

            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Overrides executeQuery by taking an array of queries instead of an arraylist. Can accept 0, 1, inf
     *
     * @param queries Queries to execute
     * @return returns arraylist of associated ResultSets
     */
    private static ArrayList<ResultSet> executeQuery(String... queries) {

        ArrayList<ResultSet> resultSets = new ArrayList<ResultSet>(10);

        try {
            for (String query : queries) {
                resultSets.add(executeQuery(query));
            }

        } catch (SQLException s) {
            s.printStackTrace();
        }

        return resultSets;
    }

    /**
     * Retrieves all column names associated with a given query's result set.
     * Uses select * from.. to get names of all columnnames
     *
     * @param query Query to execute
     * @return returns an ArrayList of associated column names.
     */
    public static ArrayList<String> getColNames(String query) {
        ArrayList<String> cols = new ArrayList<String>(10);
        try {
            ResultSet rs = executeQuery(query);

            if (!rs.next()) {
            } else {
                for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                    cols.add(rs.getMetaData().getColumnName(i));
                }
            }
            rs.close();

        } catch (SQLException s) {
            s.printStackTrace();
            return null;
        }
        return cols;
    }


    /**
     * Inserts any number of values (Object []) into a given table. Must insert a value for all columns.
     *
     * @param table  Table to insert to.
     * @param values Objects to insert.
     * @return returns number of rows updated.
     * @throws SQLException
     */
    static int[] insertIntoTable(String table, Object... values) throws SQLException {

        Statement stmt = null;
        StringBuilder vals = new StringBuilder("INSERT INTO " + table + " VALUES (" + values[0] + (values.length > 1 ? ", " : " "));
        for (int s = 1; s < values.length; s++) {
            if (s == values.length - 1) {
                vals.append(values[s]).append(")");
            } else {
                vals.append(values[s]).append(", ");
            }
        }

        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());
            int[] updateCount = stmt.executeBatch();

            con.commit();
            return updateCount;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            con.setAutoCommit(true);
        }
    }


    public static void updateTableNames() throws SQLException {

        tableNames = new ArrayList<String>(10);
        ResultSet rs = con.getMetaData().getTables(null, USER_NAME, "%", null);
        while (rs.next()) {
            tableNames.add(rs.getString(3));
        }
        rs.close();
    }

    public static ArrayList<String> getTableNames() {
        return tableNames;
    }


    /**
     * Updates a column in a given table with a new value.
     *
     * @param col     Column to update
     * @param pk      Primary key value to match with the row to be updated
     * @param newData Object containing new PK value.
     * @return returns int array of updates
     */
    public static int[] updateTable(int col, Object pk, Object newData) throws SQLException {

        Statement stmt = null;

        try {

            Class<?> colType = getColClasses("select * from " + currentTable)[col];
            boolean isStringType = (colType == String.class || colType == Character.class);

            StringBuilder vals = new StringBuilder("UPDATE " + currentTable + " SET " + DBInfo.getTabToColNames().get(currentTable).get(col) + " = " + (isStringType ? "'" : "")
                    + colType.cast(newData) + (isStringType ? "'" : "") + " WHERE " + DBInfo.getTabToPKHash().get(currentTable) + " = " + pk);

            con.setAutoCommit(false);

            stmt = con.createStatement();
            stmt.addBatch(vals.toString());

            int[] updateCount = stmt.executeBatch();

            con.commit();
            stmt.close();

            return updateCount;
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            con.setAutoCommit(true);
        }
        return null;
    }

    /**
     * Gets Array List of all table names referring a given table using foreign keys.
     *
     * @param tableName Table name to query.
     * @return ArrayList containing Strings of all table table names referencing the given table.
     */
    public static ArrayList<String> getReferringTables(String tableName) {
        try {
            ResultSet rs = executeQuery(QueryStorage.getRefTableQuery(tableName));
            ArrayList<String> refTableNames = new ArrayList<String>(10);
            if (!rs.next()) {
            } else {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        refTableNames.add(rs.getString(i));
                    }
                } while (rs.next());
            }

            rs.close();
            return refTableNames;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    public static String getCurrentTable() {
        return currentTable;
    }

    public static void setCurrentTable(String currentTable) {
        DBIO.currentTable = currentTable;
    }


    /**
     * Gets all primary keys associated with the given table's primary key column.
     *
     * @param tableName Table name to query.
     * @return ArrayList of primary keys.
     */
    public static ArrayList<Object> getPksFromTable(String tableName) {
        try {
            ArrayList<Object> pkList = new ArrayList<Object>(10);

            ResultSet rs = executeQuery("select " + DBInfo.getTabToPKHash().get(tableName) + " from " + tableName);
            if (!rs.next()) {
            } else {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        pkList.add(rs.getObject(i));
                    }
                } while (rs.next());
            }
            rs.close();

            return pkList;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    /**
     * @param tableName Table name to obtain keys for.
     * @param types
     * @return
     * @should getKeys
     */
    public static ArrayList<String> getKeys(String tableName, String... types) {
        ArrayList<String> results = new ArrayList<String>(10);
        if (types.length > 1) {
            try {
                ResultSet rs = executeQuery(QueryStorage.getPkAndFkNames(tableName));
                if (!rs.next()) {
                } else {
                    do {
                        results.add(rs.getString(1));
                    } while (rs.next());
                }
                rs.close();
                return results;
            } catch (SQLException s) {
                s.printStackTrace();
            }
            return null;
        } else {
            try {
                ResultSet rs = executeQuery(QueryStorage.getPkOrFkNames(tableName, types[0]));
                if (!rs.next()) {
                } else {
                    do {
                        results.add(rs.getString(1));
                    } while (rs.next());
                }
                rs.close();
                return results;
            } catch (SQLException s) {
                s.printStackTrace();
            }
            return null;
        }

    }

    public static ArrayList<String> getRefConstraints(String tableName) {

        ArrayList<String> refList = new ArrayList<String>(10);

        try {
            ResultSet rs = executeQuery(QueryStorage.getFkConstraintsQuery(tableName));
            if (!rs.next()) {
            } else {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        refList.add(rs.getString(i));
                    }
                } while (rs.next());
                rs.close();
            }

            return refList;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> getStringResults(String... queries) {
        try {
            ArrayList<String> resultList = new ArrayList<String>(10);
            for (String s : queries) {
                ResultSet rs = executeQuery(s);
                if (!rs.next()) {
                } else {
                    do {
                        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                            resultList.add(rs.getString(i));
                        }
                    } while (rs.next());
                    rs.close();
                }
            }
            return resultList;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ArrayList<Object>> getMultiObResults(String... queries) {
        try {
            ArrayList<ArrayList<Object>> resultList = new ArrayList<ArrayList<Object>>(10);
            for (String s : queries) {
                ResultSet rs = executeQuery(s);
                if (!rs.next()) {
                } else {
                    do {
                        ArrayList<Object> results = new ArrayList<Object>(5);
                        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                            Object temp = rs.getObject(i);
                            results.add((temp == null) ? "" : temp);
                        }
                        resultList.add(results);
                    } while (rs.next());
                }
                rs.close();

            }
            return resultList;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    /**
     * Returns integer array of column types. Index will start at 0
     *
     * @param query query to get column types for.
     * @return int[] of SQL types.
     */

    public static int[] getColumnTypes(String query) {
        try {

            ResultSet rs = executeQuery(query);
            int[] colTypes = new int[rs.getMetaData().getColumnCount()];
            if (!rs.next()) {
            } else {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        colTypes[i - 1] = rs.getMetaData().getColumnType(i);
                    }
                } while (rs.next());
            }
            rs.close();
            return colTypes;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    /**
     * Gets array of classes to associate with a given query's resultant columns
     *
     * @param query query to execute
     * @return returns array of Classes.
     */
    public static Class[] getColClasses(String query) {

        int[] colTypes = getColumnTypes(query);
        Class[] classes = new Class[colTypes.length];

        for (int i = 0; i < colTypes.length; i++) {
            classes[i] = Utility.ConvertType(colTypes[i]);
        }
        return classes;
    }

}



