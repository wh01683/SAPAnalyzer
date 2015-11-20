package gui.custom;

import db.DBInfo;

import java.util.ArrayList;

/**
 * Created by robert on 10/8/2015.
 */
public class DBRow {

    private Object[] rowArray;
    private Class[] classList;
    private String tableName;


    /**
     * Creates a new DB Row using a pre-made ArrayList of the row's objects and associated class array.
     *
     * @param row     ArrayList of Objects, the row data retrieved from a table.
     * @param classes Classes associated with the Objects.
     */
    public DBRow(ArrayList<Object> row, Class[] classes) {
        rowArray = new Object[row.size()];
        classList = classes;
        //TODO: set table name for DBRows instantiated using this process.
        for (int i = 0; i < row.size(); i++) {
            try {
                rowArray[i] = classList[i].cast(row.get(i));
            } catch (ClassCastException c) {
                System.out.printf("Could not cast %s to %s.\n", row.get(i).toString(), classList[i]);
            }
        }
    }

    /**
     * Creates a new DBRow object associated with a specific table. Can take in any number of Object values as contents.
     *
     * @param tableName
     * @param content
     */
    public DBRow(String tableName, Object... content){
        if (content.length != DBInfo.getTabToColNames().get(tableName).size()) {

        }
        this.rowArray = content;
        this.tableName = tableName;
        //TODO: Check that size of content array does not exceed table's column count
    }

    /**
     * Gets value at specified column number.
     * @param col Index of column.
     * @return Object value located in the column.
     */
    public Object getValueAtColumn(int col){
        return rowArray[col];
    }

    /**
     * Sets value located at the specified column to a new value.
     *
     * @param col    Column index to update
     * @param newVal Object value to replace old contents.
     */
    public void setValueAtColumn(int col, Object newVal) {

        try {
            rowArray[col] = ((classList[col]).cast(newVal));
        } catch (ClassCastException c) {
            System.out.printf("Class cast exception when setting value at column %d.\nCannot cast %s as %s.\n",
                    col, newVal.toString(), classList[col].toString());
            c.printStackTrace();
        }
    }

    /**
     * Returns the rowArray associated with this DBRow object. Used for populating the table model.
     * @return Object array of the row's values.
     */
    public Object[] getRowArray() {
        return rowArray;
    }

    /**
     * Used to create a pre-formatted insert query to insert the data into the table.
     * @return String SQL statement to insert data to table.
     */
    public String getInsertQuery() {

        StringBuilder queryBuilder = new StringBuilder(new StringBuilder().append("INSERT INTO ").append(tableName).append(" VALUES(").toString());

        for(int i = 0; i < this.getRowArray().length; i++){
            boolean isString = (DBInfo.getColTypes(tableName)[i] == 12 || DBInfo.getColTypes(tableName)[i] == 1);

            if(i == getRowArray().length - 1){
                queryBuilder.append((isString) ? "'" : "").append(getRowArray()[i]).append((isString) ? "'" : "").append(")");
            }else{
                queryBuilder.append((isString) ? "'" : "").append(getRowArray()[i]).append((isString) ? "'" : "").append(", ");
            }
        }
        return queryBuilder.toString();
    }

    /**
     * Gets the table name associated with this row.
     * @return String tablename of the row.
     */
    public String getTableName() {
        return tableName;
    }
}
