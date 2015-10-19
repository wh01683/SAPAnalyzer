package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by robert on 10/10/2015.
 */
public class TestMain {

    /**
     * @param args
     */


    public static void main(String[] args){

        DatabaseIO dbio = new DatabaseIO();
        ArrayList<ResultSet> resultSets = dbio.executeQuery("select * from part");
        try {
            for (ResultSet r : resultSets) {
                do {
                    for (int i = 1; i < r.getMetaData().getColumnCount() + 1; i++) {
                        System.out.printf("Column Type: %d, Column Name: %s\n",
                                r.getMetaData().getColumnType(i), r.getMetaData().getColumnName(i));
                    }
                } while (r.next());
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }
}
