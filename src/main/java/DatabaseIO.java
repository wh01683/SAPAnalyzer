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

    public DatabaseIO(){
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
        ArrayList<ResultSet> results = new ArrayList<ResultSet>(queries.size());

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

            for (ResultSet tableRs : temprs) {
                colName = tableRs.getMetaData().getColumnName(col);
                colType = Class.forName(Utility.ConvertType(tableRs.getMetaData().getColumnType(col)));
                pkName = tableRs.getMetaData().getColumnName(0);
            }


            Statement stmt = null;
            StringBuilder vals = new StringBuilder("UPDATE " + currentTable + " SET " + colName +" = " + colType.cast(newData) + " WHERE " + pkName + " = " + pk);

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


    public String getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(String currentTable) {
        this.currentTable = currentTable;
    }
}


