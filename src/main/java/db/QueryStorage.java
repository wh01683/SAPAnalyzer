package db;

/**
 * Created by robert on 10/9/2015.
 */
public class QueryStorage {

    private final static String USERNAME = "HOWERTONSAP";

    private final static String REFERENCING_TABLES_QUERY = "SELECT DISTINCT ac1.table_name ref_table\n" +
            "FROM all_constraints ac1 JOIN all_constraints ac2\n" +
            "ON ac1.r_constraint_name = ac2.constraint_name\n" +
            "WHERE ac1.constraint_type='R'\n" +
            "AND ac2.constraint_type IN ('P', 'U')\n" +
            "AND ac1.owner = '"+ USERNAME +"'\n" +
            "AND ac2.table_name = ";


    public static String getRefTableQuery(String primaryTableName) {
        String query = REFERENCING_TABLES_QUERY + "'" + primaryTableName + "'";
        return query;
    }


    public static String getPkOrFkNames(String tableName, String type) {
        String query = "SELECT cols.column_name\n" +
                "FROM all_constraints cons, all_cons_columns cols\n" +
                "WHERE cols.table_name = '" + tableName + "'\n" +
                "AND cols.owner = '" + USERNAME + "'\n" +
                "AND cons.constraint_type = '" + type + "'\n" +
                "AND cols.position = 1\n" +
                "AND cons.constraint_name = cols.constraint_name\n" +
                "AND cons.owner = cols.owner\n" +
                "ORDER BY cols.table_name, cols.position";

        return query;
    }

    public static String getPkAndFkNames(String tableName) {
        String query = "SELECT cols.column_name\n" +
                "FROM all_constraints cons, all_cons_columns cols\n" +
                "WHERE cols.table_name = '" + tableName + "'\n" +
                "AND cols.owner = '" + USERNAME + "'\n" +
                "AND cons.constraint_type in ('P', 'R') \n" +
                "AND cols.position = 1\n" +
                "AND cons.constraint_name = cols.constraint_name\n" +
                "AND cons.owner = cols.owner\n" +
                "ORDER BY cols.table_name, cols.position";

        return query;
    }

    public static String getColNamesQuery(String tableName) {
        String query = "select COLUMN_NAME from ALL_TAB_COLUMNS where TABLE_NAME='"+ tableName+"' AND owner = '"+ USERNAME+"'";
        return query;
    }

    public static String getFkConstraintsQuery(String tableName) {
        String query = "select constraint_name \n" +
                "from all_constraints \n" +
                "WHERE constraint_type='R'\n" +
                "AND owner = 'HOWERTONSAP'\n" +
                "and table_name = '"+tableName+"'";
        return query;
    }

    /*
    *
    *
    *
    *
    * SELECT DISTINCT ac1.table_name ref_table
FROM all_constraints ac1 JOIN all_constraints ac2
ON ac1.r_constraint_name = ac2.constraint_name
WHERE ac1.constraint_type='R'
AND ac2.constraint_type IN ('P', 'U')
AND ac1.owner = 'HOWERTONSAP'
AND ac2.table_name = 'PART';

desc all_constraints;

create or replace view howertonref
as select ac1.table_name ref_table, ac1.constraint_name ref_con
from all_constraints ac1 JOIN all_constraints ac2
ON ac1.r_constraint_name = ac2.constraint_name
WHERE ac1.constraint_type = 'R'
AND ac1.owner = 'HOWERTONSAP'
AND ac2.owner = 'HOWERTONSAP'
    *
    *
    *
    *
    *
    * */


}
