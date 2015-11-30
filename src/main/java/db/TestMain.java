package db;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by robert on 10/10/2015.
 *
 * This class was created to test certain parts of the application when the GUI was not working properly.
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

    /**
     * Runs a test of specified iterations by simply querying a table over and over again and printing the results.
     * Used for stress testing to ensure connections were closing correctly.
     * @param table Table to query.
     * @param iterations Number of times to query the table.
     */
    private static void test(String table, int iterations) {
        for (int i = 0; i < iterations; i++) {
            printAllFromTable(table, i);
        }
    }

    /**
     * Used to print all contents of a table. This method was primarily used to text the DBIO.getMultiObResults() method.
     * @param table Table to query.
     * @param testIteration Number of the test iteration. Used to track bulk executions.
     */
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

    /**
     * Used to text insertions into a given table.
     * @param tableName Table inserting into.
     * @param values Objects to insert.
     */
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


    private static void printResults(int[] results) {
        for (int i = 0; i < results.length; i++) {
            System.out.printf("Results[%d] = %d\n", i, results[i]);
        }
    }
}
