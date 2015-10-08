import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by robert on 9/19/2015.
 */

import oracle.jdbc.*;

import javax.sql.DataSource;
import javax.xml.transform.Result;


public class DatabaseIO {


    private final String DATABASE_NAME = "sap";
    private final String USER_NAME = "HOWERTONSAP";
    private final String PASSWORD = "database015";
    private Connection connection = null;
    private ArrayList<String> tableNames;

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
}


