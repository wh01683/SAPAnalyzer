package db;

import java.sql.*;
import java.util.ArrayList;

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
        ArrayList<ResultSet> resultSets = executeQuery(queryStorage.getColNamesQuery(tableName));

        try {
            for (ResultSet rs : resultSets) {
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
                vals.append(values[s] + ")");
            }else{
                vals.append(values[s]+", ");
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
    public ArrayList<Integer> getPksFromTable(String tableName){
        try {
            ArrayList<Integer> pkList = new ArrayList<Integer>(10);
            ArrayList<ResultSet> tempArr = executeQuery("select " + DBInfo.getTabToPKHash().get(tableName) + " from " + tableName);
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++){
                            pkList.add(rs.getInt(i));
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

    /** Gets all constraint names for a given table.
     * @param tableName Table name to query for.
     * @return ArrayList containing all table names referencing the given table's primary key
     * @should refNames
     */
    public ArrayList<String> getRefConstraintsForTable(String tableName){
        try {
            ArrayList<String> fkList = new ArrayList<String>(10);
            ArrayList<ResultSet> tempArr = executeQuery(queryStorage.getFkConstraintsQuery(tableName));
            for (ResultSet rs : tempArr) {
                if (!rs.next()) {
                } else {
                    do {
                        for(int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++){
                            fkList.add(rs.getString(i));
                        }
                    } while (rs.next());
                }
            }
            return fkList;
        }catch (SQLException s) {
            s.printStackTrace();
        }
        return null;
    }
}



