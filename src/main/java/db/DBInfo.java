package db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by robert on 10/10/2015.
 */
public class DBInfo {

    static DatabaseIO dbio = new DatabaseIO();
    private static Hashtable<String, String> tabToPKHash = new Hashtable<String, String>(10);
    private static Hashtable<String, ArrayList<String>> tabToRefTabHash = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToColNames = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<Integer>> tabToPkVals = new Hashtable<String, ArrayList<Integer>>(10);

    public static void start() {
        try {
            dbio.updateTableNames();
            for(String tables : dbio.getTableNames()){
                String pkColName = dbio.getTablePrimaryKey(tables);
                tabToPKHash.put(tables, pkColName);
                ArrayList<String> tempTabs = dbio.getReferringTables(tables);
                tabToRefTabHash.put(tables, tempTabs);
                tabToColNames.put(tables, dbio.getColNames(tables));
                tabToPkVals.put(tables, dbio.getPksFromTable(tables));
            }
        }catch (SQLException s){
            System.out.printf("Error Code: %d",s.getErrorCode());
            s.printStackTrace();
        }
    }

    public static Hashtable<String, String> getTabToPKHash() {
        return tabToPKHash;
    }

    public static Hashtable<String, ArrayList<String>> getTabToRefTabHash() {
        return tabToRefTabHash;
    }

    public static Hashtable<String, ArrayList<String>> getTabToColNames() {
        return tabToColNames;
    }

    public static Hashtable<String, ArrayList<Integer>> getTabToPkVals() {
        return tabToPkVals;
    }
}
