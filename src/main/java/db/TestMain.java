package db;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by robert on 10/10/2015.
 */
public class TestMain {

    /**
     * @param args
     */


    public static void main(String[] args) {


        DBIO.instantiate();

//        testInsert("UNITS", "testcode22", "test system", "test description");
//        testInsert("COMPANIES", 1000223, "test company name");

        DBIO.executeWithoutReturn("delete from units where unitcode = 'testcode'");
        DBIO.executeWithoutReturn("delete from companies where companyid = 1000223");



    }


    private static void printAllFromTable(String table) {
        ArrayList<ArrayList<Object>> objects = DBIO.getMultiObResults("select * from " + table);

        for (ArrayList<Object> arrayList : objects) {
            for (Object o : arrayList) {
                System.out.printf("%s\n", o.toString());
            }
        }
    }

    private static void testInsert(String tableName, Object... values) {
        try {
            int[] results = DBIO.insertIntoTable(tableName, values);
            for (int i = 0; i < results.length; i++) {
                System.out.printf("Results[%d] = %d\n", i, results[i]);

            }
        } catch (SQLException s) {
            s.printStackTrace();
        }


    }
}
