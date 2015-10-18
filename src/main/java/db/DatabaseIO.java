package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by robert on 9/19/2015.
 */


public class DatabaseIO {


    private final String DATABASE_NAME = "sap";
    private final String USER_NAME = "HOWERTONSAP";
    private final String PASSWORD = "database015";
    private Connection connection = null;
    private ArrayList<String> tableNames;
    private String currentTable;
    private QueryStorage queryStorage;
    private Hashtable<String, ArrayList<String>> tabsToRefConstraints = new Hashtable<String, ArrayList<String>>(10);

    public DatabaseIO(){
        this.queryStorage = new QueryStorage();
        MakeConnection();
    }

    public void MakeConnection() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:" + DATABASE_NAME, USER_NAME, PASSWORD);
            updateTableNames();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void alterConstraints(String table, boolean enable) {
        if (tabsToRefConstraints == null) {
            tabsToRefConstraints = DBInfo.getTabToRefConstraint();
        } else {
            for (int i = 0; i < tabsToRefConstraints.get(table).size(); i++) {
                String constraintQuery = "alter table " + table + " " + ((enable) ? "enable" : "disable") + " constraint " +
                        tabsToRefConstraints.get(table).get(i);
                String triggerQuery = "alter table " + table + " " + ((enable) ? "enable" : "disable") + " all triggers";
                executeQuery(triggerQuery, constraintQuery);
            }
        }
    }

    public ArrayList<ResultSet> executeQuery(ArrayList<String> queries){

        Statement stmt = null;
        ArrayList<ResultSet> results = new ArrayList<ResultSet>(10);

        try {
            stmt = connection.createStatement();

            for (String query : queries){
                ResultSet rs = stmt.executeQuery(query);
                results.add(rs);
                stmt = connection.createStatement();
            }
            return results;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<ResultSet> executeQuery(String... queries){
        ArrayList<String> quers = new ArrayList<String>(queries.length);

        for(String query : queries){
            quers.add(query);
        }

        return executeQuery(quers);
    }

    public ArrayList<String> getColNames(String tableName){
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
            return cols;
        }catch (SQLException s ){
            s.printStackTrace();
        }
        return null;
    }

    public int[] insertIntoTable(String table, String...values) throws SQLException{

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
            this.connection.setAutoCommit(false);
            stmt = this.connection.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());
            int[] updateCount = stmt.executeBatch();

            this.connection.commit();
            return updateCount;

        }catch (SQLException e){
            e.printStackTrace();
            return null;
        } finally {
            if (stmt != null) { stmt.close(); }
            this.connection.setAutoCommit(true);
        }
    }

    public void updateTableNames()throws SQLException{

        tableNames = new ArrayList<String>(10);
        ResultSet rs = connection.getMetaData().getTables(null, USER_NAME, "%", null);
        while (rs.next()) {
            tableNames.add(rs.getString(3));
        }
    }

    public ArrayList<String> getTableNames() {
        return tableNames;
    }


    public int[] updateTable(int col, Object pk, Object newData){
        try {
            ArrayList<String> temp = new ArrayList<String>(1);
            temp.add("select * from " + currentTable);
            ArrayList<ResultSet> temprs = executeQuery(temp);
            String colName = "";
            Class<?> colType = null;
            String pkName = "";
            boolean isStringType = false;

            for (ResultSet tableRs : temprs) {
                colName = tableRs.getMetaData().getColumnName(col + 1);
                colType = Class.forName(Utility.ConvertType(tableRs.getMetaData().getColumnType(col + 1)));
                isStringType = (tableRs.getMetaData().getColumnType(col + 1) == 12);
                pkName = tableRs.getMetaData().getColumnName(1);
            }

            Statement stmt = null;
            StringBuilder vals = new StringBuilder("UPDATE " + currentTable + " SET " + colName +" = " + (isStringType? "'" : "")
                    + colType.cast(newData) + (isStringType? "'" : "") + " WHERE " + pkName + " = " + pk);

            this.connection.setAutoCommit(false);
            stmt = this.connection.createStatement();
            stmt.addBatch(vals.toString());

            System.out.printf(vals.toString());
            int[] updateCount = stmt.executeBatch();

            this.connection.commit();

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
    public ArrayList<String> getReferringTables(String tableName){
        try {
            ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getRefTableQuery(tableName));
            ArrayList<String> refTableNames = new ArrayList<String>(10);
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                            refTableNames.add(rs.getString(i + 1));
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

    public void setCurrentTable(String currentTable) {
        this.currentTable = currentTable;
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
    public ArrayList<String> getTableForeignKey(String tableName) {
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
    public ArrayList<Object> getPksFromTable(String tableName) {
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


    public ArrayList<String> getRefConstraints(String tableName) {

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

    public ArrayList<String> getStringResults(String... queries) {
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

    public ArrayList<ArrayList<Object>> getMultiObResults(String... queries) {
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
     * Returns integer array of column types WITH SAME COLUMN NUMBER AS IN TABLE. Index will start at 1
     *
     * @param tableName table name to obtain column types for
     * @return int[] of SQL types.
     */
    public int[] getColumnTypes(String tableName) {
        try {
            ArrayList<ResultSet> tempArr = executeQuery("select * from " + tableName);
            int[] colTypes = new int[tempArr.get(0).getMetaData().getColumnCount() + 1];
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                            colTypes[i] = rs.getMetaData().getColumnType(i);
                        }
                    } while (rs.next());
                }
            }
            return colTypes;
        } catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }

}



