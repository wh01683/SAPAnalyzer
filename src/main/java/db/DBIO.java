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
    public static void alterConstraints(boolean enable, String... tables) {

        ArrayList<String> queries = new ArrayList<String>(10);

        for (String table : tables) {
            ArrayList<String> constraints = DBInfo.getTabToRefConstraint().get(table);
            for (String constraint : constraints) {
                queries.add("alter table " + table + " " + ((enable) ? "enable" : "disable") + " constraint " + constraint);
            }
            queries.add("alter table " + table + " " + ((enable) ? "enable" : "disable") + " all triggers");
        }
        DBIO.executeQuery(queries);
    }

    /**
     * Executes an arraylist of String queries and returns an ArrayList of the associated result sets.
     *
     * @param queries ArrayList of queries to execute.
     * @return ArrayList of ResultSet objects.
     */
    private static ArrayList<ResultSet> executeQuery(ArrayList<String> queries) {

        Statement stmt = null;
        ArrayList<ResultSet> results = new ArrayList<ResultSet>(10);

        try {

            for (String query : queries) {
                stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery(query);
                results.add(rs);
            }

        }catch (SQLException e){
            for (String s : queries) {
                System.out.printf(s);
            }
            e.printStackTrace();
            return null;
        }

        return results;
    }

    /**
     * Overrides executeQuery by taking an array of queries instead of an arraylist. Can accept 0, 1, inf
     *
     * @param queries Queries to execute
     * @return returns arraylist of associated ResultSets
     */
    private static ArrayList<ResultSet> executeQuery(String... queries) {
        ArrayList<String> quers = new ArrayList<String>(queries.length);

        for(String query : queries){
            quers.add(query);
        }

        return executeQuery(quers);
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
        ArrayList<ResultSet> resultSet = executeQuery(query);

        try {
            for (ResultSet rs : resultSet) {
                if (!rs.next()) {
                } else {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        cols.add(rs.getMetaData().getColumnName(i));
                    }
                    rs.close();
                }
            }
        }catch (SQLException s ){
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
    public static int[] insertIntoTable(String table, Object... values) throws SQLException {

        Statement stmt = null;
        StringBuilder vals = new StringBuilder("INSERT INTO " + table + " VALUES ("+ values[0] + (values.length > 1? ", " : " "));
        for(int s = 1; s < values.length; s++){
            if(s == values.length - 1){
                vals.append(values[s]).append(")");
            }else{
                vals.append(values[s]).append(", ");
            }
        }

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(vals.toString());
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());
            int[] updateCount = stmt.executeBatch();

            con.commit();
            return updateCount;

        }catch (SQLException e){
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
    }

    public static ArrayList<String> getTableNames() {
        return tableNames;
    }


    public static int[] updateTable(int col, Object pk, Object newData) {
        try {
            ArrayList<ResultSet> temprs = executeQuery("select * from " + currentTable);
            String colName = "";
            Class<?> colType = null;
            String pkName = "";
            boolean isStringType = false;

            for (ResultSet tableRs : temprs) {
                colName = tableRs.getMetaData().getColumnName(col + 1);
                colType = Utility.ConvertType(tableRs.getMetaData().getColumnType(col + 1));
                isStringType = (tableRs.getMetaData().getColumnType(col + 1) == 12 || tableRs.getMetaData().getColumnType(col + 1) == 1);
                pkName = tableRs.getMetaData().getColumnName(1);
            }


            Statement stmt = null;
            StringBuilder vals = new StringBuilder("UPDATE " + currentTable + " SET " + colName +" = " + (isStringType? "'" : "")
                    + colType.cast(newData) + (isStringType? "'" : "") + " WHERE " + pkName + " = " + pk);

            DBIO.con.setAutoCommit(false);
            stmt = DBIO.con.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());

            int[] updateCount = stmt.executeBatch();

            DBIO.con.commit();

            stmt.close();
            return updateCount;
        }catch (SQLException s) {
            s.printStackTrace();
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
            ArrayList<ResultSet> tempArr = executeQuery(QueryStorage.getRefTableQuery(tableName));
            ArrayList<String> refTableNames = new ArrayList<String>(10);
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                            refTableNames.add(rs.getString(i));
                        }
                    } while (rs.next());
                }
            }
            return refTableNames;
        }catch (SQLException s){
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
     * @param tableName Table name to query.
     * @return ArrayList of primary keys.
     */
    public static ArrayList<Object> getPksFromTable(String tableName) {
        try {
            ArrayList<Object> pkList = new ArrayList<Object>(10);
            ArrayList<ResultSet> tempArr = executeQuery("select " + DBInfo.getTabToPKHash().get(tableName) + " from " + tableName);
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++){
                            pkList.add(rs.getObject(i));
                        }
                    } while (rs.next());
                }
            }
            return pkList;
        }catch (SQLException s) {
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
                ArrayList<ResultSet> tempArr = executeQuery(QueryStorage.getPkAndFkNames(tableName));
                for (ResultSet rs : tempArr) {
                    if (!rs.next()) {
                    } else {
                        do {
                            results.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
                return results;
            } catch (SQLException s) {
                s.printStackTrace();
            }
            return null;
        } else {
            try {
                ArrayList<ResultSet> tempArr = executeQuery(QueryStorage.getPkOrFkNames(tableName, types[0]));
                for (ResultSet rs : tempArr) {
                    if (!rs.next()) {
                    } else {
                        do {
                            results.add(rs.getString(1));
                        } while (rs.next());
                    }
                }
                return results;
            } catch (SQLException s) {
                s.printStackTrace();
            }
            return null;
        }

    }

    public static ArrayList<String> getRefConstraints(String tableName) {

        ArrayList<String> refList = new ArrayList<String>(10);
        ArrayList<ResultSet> tempArr = executeQuery(QueryStorage.getFkConstraintsQuery(tableName));
        try {
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                            refList.add(rs.getString(i));
                        }
                    } while (rs.next());
                }
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
                ArrayList<ResultSet> tempArr = executeQuery(s);
                for (ResultSet rs : tempArr) {
                    if (!rs.next()) {
                    } else {
                        do {
                            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                                resultList.add(rs.getString(i));
                            }
                        } while (rs.next());
                    }
                }
            }
            return resultList;
        }catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ArrayList<Object>> getMultiObResults(String... queries) {
        try {
            ArrayList<ArrayList<Object>> resultList = new ArrayList<ArrayList<Object>>(10);
            for (String s : queries) {
                ArrayList<ResultSet> tempArr = executeQuery(s);
                for (ResultSet rs : tempArr) {
                    if (!rs.next()) {
                    } else {
                        do {
                            ArrayList<Object> results = new ArrayList<Object>(5);
                            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                                results.add(rs.getObject(i));
                            }
                            resultList.add(results);
                        } while (rs.next());
                    }
                }
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
            ArrayList<ResultSet> tempArr = executeQuery(query);
            int[] colTypes = new int[tempArr.get(0).getMetaData().getColumnCount()];
            for (ResultSet rs : tempArr) {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        colTypes[i - 1] = rs.getMetaData().getColumnType(i);
                    }
                } while (rs.next());
            }
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



