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
    private Connection connection = null;

    public void DatabaseIO(){

        MakeConnection();
    }

    public void MakeConnection() {


        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:sap", "system", "database015");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<ResultSet> executeQuery(ArrayList<String> queries){

        MakeConnection();
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


}


