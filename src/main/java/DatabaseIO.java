import oracle.jdbc.*;
import oracle.sql.*;
import java.sql.*;

/**
 * Created by robert on 9/19/2015.
 */
public class DatabaseIO {

    private Connection conn = null;

    public void establishConnection(){

        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("");

            Statement sqlStatement = conn.createStatement();
            String selectStuff = "";

            ResultSet rows = sqlStatement.executeQuery(selectStuff);

            while(rows.next()){
                System.out.println(rows.getString("first_name"));
            }

        }catch(SQLException ex){
            System.out.println("SQL Exception : " + ex.getMessage());
            System.out.println("Vendor Error: " + ex.getErrorCode());
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }

    }
}
