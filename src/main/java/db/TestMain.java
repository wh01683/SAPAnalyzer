package db;

/**
 * Created by robert on 10/10/2015.
 */
public class TestMain {

    /**
     * @param args
     */


    public static void main(String[] args) {


        DBIO.instantiate();

        System.out.printf("%s", DBInfo.dbToString());



    }
}
