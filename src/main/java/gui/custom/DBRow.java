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


    public DBRow(ArrayList<Object> row, Class[] classes) {
        rowArray = new Object[row.size()];
        classList = classes;

        for (int i = 0; i < row.size(); i++) {
            try {
                rowArray[i] = classList[i].cast(row.get(i));
            } catch (ClassCastException c) {
                System.out.printf("Could not cast %s to %s.\n", row.get(i).toString(), classList[i]);
            }
            }

    }

    public DBRow(String tableName, Object... content){
        this.rowArray = content;
        this.tableName = tableName;
    }

    public Object getValueAtColumn(int col){
        return rowArray[col];
    }

    public void setValueAtColumn(int col, Object newVal){
        rowArray[col] = newVal;
    }

    public Object[] getRowArray() {
        return rowArray;
    }
    public void setRowArray(Object[] rowArray) {
        this.rowArray = rowArray;
    }

    public String getInsertQuery() {

        StringBuilder queryBuilder = new StringBuilder(new StringBuilder().append("INSERT INTO ").append(tableName).append(" VALUES(").toString());

        for(int i = 0; i < this.getRowArray().length; i++){
            boolean isString = (DBInfo.getColTypes(tableName)[i + 1] == 12 || DBInfo.getColTypes(tableName)[i + 1] == 1);

            if(i == getRowArray().length - 1){

                queryBuilder.append((isString) ? "'" : "").append(getRowArray()[i]).append((isString) ? "'" : "").append(")");
            }else{
                queryBuilder.append((isString) ? "'" : "").append(getRowArray()[i]).append((isString) ? "'" : "").append(", ");
            }
        }
        return queryBuilder.toString();
    }

    public String getTableName() {
        return tableName;
    }
}
