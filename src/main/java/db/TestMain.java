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

//        DBIO.executeWithoutReturn("delete from units where unitcode = 'testcode'");
//        DBIO.executeWithoutReturn("delete from companies where companyid = 1000223");

        //col num = 12


//        testUpdate("PART", "WASTAGE", "PARTID",1000, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1001, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1002, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1003, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1004, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1005, randDub(7, 15));
//        testUpdate("PART", "WASTAGE", "PARTID",1006, randDub(7, 15));


        //test("PART", 100000000);

    }

    private static void test(String table, int iterations) {
        for (int i = 0; i < iterations; i++) {
            printAllFromTable(table, i);
        }
    }

    private static void printAllFromTable(String table, int testIteration) {
        ArrayList<ArrayList<Object>> objects = DBIO.getMultiObResults("select * from " + table);

        System.out.printf("Iteration: %d\n", testIteration);
        int objectCount = 0;
        for (ArrayList<Object> arrayList : objects) {
            for (Object o : arrayList) {
                System.out.printf("Item: %d - Contents: %s\n", objectCount, o.toString());
                objectCount++;
            }
        }
    }

    private static void testInsert(String tableName, Object... values) {
        try {
            int[] results = DBIO.insertIntoTable(tableName, values);
            printResults(results);
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static double randDub(int min, int max) {

        double randWaste = (Math.random() * min) + (max - min);
        return Math.round(randWaste * 100d) / 100d;
    }

    private static void testUpdate(String tableName, String columnName, String constrainCol, Object primaryKey, Object newData) {

        try {
            int[] results = DBIO.updateTable(tableName, columnName, constrainCol, primaryKey, newData);
            printResults(results);
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static void printResults(int[] results) {
        for (int i = 0; i < results.length; i++) {
            System.out.printf("Results[%d] = %d\n", i, results[i]);
        }
    }
}
