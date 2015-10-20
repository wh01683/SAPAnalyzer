package db;

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
        ArrayList<ArrayList<Object>> objects = DBIO.getMultiObResults("select * from part");

        for (ArrayList<Object> arrayList : objects) {
            for (Object o : arrayList) {
                System.out.printf("%s\n", o.toString());
            }
        }
    }
}
