package db;

/**
 * Created by robert on 10/10/2015.
 */
public class TestMain {


    public static void main(String[] args){
        DatabaseIO testio = new DatabaseIO();

        for(String s : testio.getReferringTables("EMPLOYEES")){
            System.out.printf(s);
        }
    }
}
