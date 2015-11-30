package db;

/**
 * Created by robert on 10/9/2015.
 *
 * This class is used to store SQL query snippets which may be used across the application but may
 * only vary by a key or by a table name.
 */
public class QueryStorage {

    // Used when filtering results based on user.
    private final static String USERNAME = "HOWERTONSAP";

    private final static String REFERENCING_TABLES_QUERY = "select ac1.table_name ref_table \n" +
            "from user_constraints ac1 JOIN user_constraints ac2\n" +
            "ON ac1.r_constraint_name = ac2.constraint_name\n" +
            "WHERE ac1.constraint_type = 'R'\n" +
            "and ac1.owner = '" + USERNAME + "'\n" +
            "and ac2.table_name = ";


    /**
     * Obtains query used to obtain all tables referencing the given table.
     * @param primaryTableName Table being referred to.
     * @return Returns the completed query.
     */
    public static String getRefTableQuery(String primaryTableName) {
        String query = REFERENCING_TABLES_QUERY + "'" + primaryTableName + "'";
        return query;
    }


    /**
     * Query used to get key column names (depending on type) for a given table.
     * @param tableName Table to get key column names for.
     * @param type Type of key. For foreign keys, use type = "R". For primary keys, use type = "P"
     * @return Returns the completed query.
     */
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

    /**
     * Uses a recursive query to obtain all parts with associated quantities used to produce a given part.
     * @param partID Part ID primary key of the part to check.
     * @return Returns the completed query.
     */
    public static String getPartList(Object partID) {
        String query = "\n" +
                "  WITH RPL (ParentPartID, ChildPartID, QTY) AS\n" +
                "   (\n" +
                "      SELECT ROOT.ParentPartID, ROOT.ChildPartID, ROOT.QTY\n" +
                "       FROM BOM ROOT\n" +
                "       WHERE ROOT.ParentPartID = " + partID.toString() + "\n" +
                "    UNION ALL\n" +
                "      SELECT PARENT.ParentPartID, CHILD.ChildPartID, PARENT.QTY*CHILD.QTY\n" +
                "       FROM RPL PARENT, BOM CHILD\n" +
                "       WHERE PARENT.ChildPartID = CHILD.ParentPartID\n" +
                "   )\n" +
                "SELECT temp.ParentPartID AS \"Parent Part ID\", p.NAME AS \"Child Part Name\", temp.ChildPartID AS \"Child Part ID\", SUM(temp.QTY) AS \"Total QTY Used\"\n" +
                " FROM RPL temp JOIN PART p ON temp.ChildPartID = p.partid\n" +
                "  GROUP BY temp.ParentPartID, p.NAME, temp.ChildPartID\n" +
                "  ORDER BY temp.ParentPartID, p.name, temp.ChildPartID";

        return query;
    }

    /**
     * Query used to obtain both primary key columns and foreign key columns for a given table.
     * @param tableName Table to obtain column names for.
     * @return Returns the completed query.
     */
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

    /**
     * Used to obtain all column names for a given table.
     * @param tableName Table name to get column names for.
     * @return Returns the completed query.
     */
    public static String getColNamesQuery(String tableName) {
        String query = "select COLUMN_NAME from ALL_TAB_COLUMNS where TABLE_NAME='"+ tableName+"' AND owner = '"+ USERNAME+"'";
        return query;
    }

    /**
     * Used to obtain all referrential constraints for a given table.
     * @param tableName Table with the constraints.
     * @return Returns the completed query.
     */
    public static String getFkConstraintsQuery(String tableName) {
        String query = "select constraint_name \n" +
                "from all_constraints \n" +
                "WHERE constraint_type='R'\n" +
                "AND owner = 'HOWERTONSAP'\n" +
                "and table_name = '"+tableName+"'";
        return query;
    }



}
