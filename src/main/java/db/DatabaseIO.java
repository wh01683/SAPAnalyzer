package db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by robert on 9/19/2015.
 */


public class DatabaseIO {


    private final static String DATABASE_NAME = "sap";
    private final static String USER_NAME = "HOWERTONSAP";
    private final static String PASSWORD = "database015";
    private static ConnectionDelegator con = null;
    private static ArrayList<String> tableNames;
    private static String currentTable;
    private static QueryStorage queryStorage;

    public DatabaseIO(){
        queryStorage = new QueryStorage();
        MakeConnection();
    }

    public static void MakeConnection() {
        try {
            con = new ConnectionDelegator(DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:" + DATABASE_NAME, USER_NAME, PASSWORD));
            DatabaseIO.updateTableNames();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void alterConstraints(boolean enable, String... tables) {

        ArrayList<String> queries = new ArrayList<String>(10);

        for (String table : tables) {
            ArrayList<String> constraints = DBInfo.getTabToRefConstraint().get(table);
            for (String constraint : constraints) {
                queries.add("alter table " + table + " " + ((enable) ? "enable" : "disable") + " constraint " + constraint);
            }
            queries.add("alter table " + table + " " + ((enable) ? "enable" : "disable") + " all triggers");
            }
        DatabaseIO.executeQuery(queries);
    }

    public static ArrayList<ResultSet> executeQuery(ArrayList<String> queries) {

        Statement stmt = null;
        ArrayList<ResultSet> results = new ArrayList<ResultSet>(10);

        try {
            stmt = con.createStatement();

            for (String query : queries){
                ResultSet rs = stmt.executeQuery(query);
                results.add(rs);
                stmt = con.createStatement();
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

    public static ArrayList<ResultSet> executeQuery(String... queries) {
        ArrayList<String> quers = new ArrayList<String>(queries.length);

        for(String query : queries){
            quers.add(query);
        }

        return executeQuery(quers);
    }

    public static ArrayList<String> getColNames(String tableName) {
        ArrayList<String> cols = new ArrayList<String>(10);
        ArrayList<ResultSet> resultSet = executeQuery(queryStorage.getColNamesQuery(tableName));

        try {
            for (ResultSet rs : resultSet) {
                if (!rs.next()) {
                } else {
                    do {
                        for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++){
                            cols.add(rs.getString(i));
                        }
                    } while (rs.next());
                }
            }
        }catch (SQLException s ){
            s.printStackTrace();
            return null;
        }
        return cols;
    }

    public static int[] insertIntoTable(DatabaseIO databaseIO, String table, String... values) throws SQLException {

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
            databaseIO.con.setAutoCommit(false);
            stmt = databaseIO.con.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());
            int[] updateCount = stmt.executeBatch();

            databaseIO.con.commit();
            return updateCount;

        }catch (SQLException e){
            e.printStackTrace();
            return null;
        } finally {
            if (stmt != null) { stmt.close(); }
            databaseIO.con.setAutoCommit(true);
        }
    }

    public static void updateTableNames() throws SQLException {

        tableNames = new ArrayList<String>(10);
        ResultSet rs = con.getMetaData().getTables(null, USER_NAME, "%", null);
        while (rs.next()) {
            tableNames.add(rs.getString(3));
        }
    }

    public ArrayList<String> getTableNames() {
        return tableNames;
    }


    public static int[] updateTable(DatabaseIO databaseIO, int col, Object pk, Object newData) {
        try {
            ArrayList<ResultSet> temprs = executeQuery("select * from " + currentTable);
            String colName = "";
            Class<?> colType = null;
            String pkName = "";
            boolean isStringType = false;

            for (ResultSet tableRs : temprs) {
                colName = tableRs.getMetaData().getColumnName(col + 1);
                colType = Class.forName(Utility.ConvertType(tableRs.getMetaData().getColumnType(col + 1)));
                isStringType = (tableRs.getMetaData().getColumnType(col + 1) == 12 || tableRs.getMetaData().getColumnType(col + 1) == 1);
                pkName = tableRs.getMetaData().getColumnName(1);
            }

            Statement stmt = null;
            StringBuilder vals = new StringBuilder("UPDATE " + currentTable + " SET " + colName +" = " + (isStringType? "'" : "")
                    + colType.cast(newData) + (isStringType? "'" : "") + " WHERE " + pkName + " = " + pk);

            databaseIO.con.setAutoCommit(false);
            stmt = databaseIO.con.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());

            int[] updateCount = stmt.executeBatch();

            databaseIO.con.commit();

            return updateCount;
        }catch (SQLException s){
            s.printStackTrace();
        }catch (ClassNotFoundException c){
            c.printStackTrace();
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
            ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getRefTableQuery(tableName));
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

    public String getCurrentTable() {
        return currentTable;
    }

    public static void setCurrentTable(DatabaseIO databaseIO, String currentTable) {
        databaseIO.currentTable = currentTable;
    }

    /**
     * Gets column name of primary key column associated with the given table name.
     * @param tableName Table name to query.
     * @return Primary Key column name.
     */
    public String getTablePrimaryKey(String tableName){
            try {
                String pkColName = "";
                ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getPkOrFkNames(tableName, "P"));
                for (ResultSet rs : tempArr) {
                    if (!rs.next()) {
                    } else {
                        do {
                            pkColName = rs.getString(1);
                        } while (rs.next());
                    }
                }
                return pkColName;
            }catch (SQLException s){
                s.printStackTrace();
            }
            return null;
    }


    /**
     * Gets column name of primary key column associated with the given table name.
     *
     * @param tableName Table name to query.
     * @return Primary Key column name.
     */
    public static ArrayList<String> getTableForeignKey(String tableName) {
        try {
            ArrayList<String> fks = new ArrayList<String>(10);
            ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getPkAndFkNames(tableName));
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        fks.add(rs.getString(1));
                    } while (rs.next());
                }
            }
            return fks;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
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


    public static ArrayList<String> getRefConstraints(String tableName) {

        ArrayList<String> refList = new ArrayList<String>(10);
        ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getFkConstraintsQuery(tableName));
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
                            ArrayList<Object> results = new ArrayList<Object>(10);
                            results.add(null);
                            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                                results.add((rs.getObject(i) == null) ? "" : rs.getObject(i));
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
     * Returns integer array of column types WITH SAME COLUMN NUMBER AS IN TABLE. Index will start at 1
     *
     * @param tableName table name to obtain column types for
     * @return int[] of SQL types.
     */
    public static int[] getColumnTypes(String tableName) {
        try {
            ArrayList<ResultSet> tempArr = executeQuery("select * from " + tableName);
            int[] colTypes = new int[tempArr.get(0).getMetaData().getColumnCount() + 1];
            for (ResultSet rs : tempArr) {
                do {
                    for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                        colTypes[i] = rs.getMetaData().getColumnType(i);
                    }
                } while (rs.next());
            }
            return colTypes;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

}



