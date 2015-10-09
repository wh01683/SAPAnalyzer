/**
 * Created by robert on 10/9/2015.
 */
public class QueryStorage {

    private String userName = "";

    private final String REFERENCING_TABLES_QUERY = "SELECT DISTINCT ac1.table_name ref_table\n" +
            "FROM all_constraints ac1 JOIN all_constraints ac2\n" +
            "ON ac1.r_constraint_name = ac2.constraint_name\n" +
            "WHERE ac1.constraint_type='R'\n" +
            "AND ac2.constraint_type IN ('P', 'U')\n" +
            "AND ac1.owner = '"+ userName +"'\n" +
            "AND ac2.table_name = ";


    public String getRefTableQuery(String primaryTableName){
        String query = REFERENCING_TABLES_QUERY + "'" + primaryTableName + "'";
        return query;
    }

    public QueryStorage(String newUserName){
        userName = newUserName;
    }



}
