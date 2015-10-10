package gui;

import db.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by robert on 10/8/2015.
 */
public class DBRow {

    private ResultSet rowResultSet;
    private Object[] rowArray;
    private Class[] classList;
    private String tableName;


    public DBRow(ResultSet newContents){

        try {
            rowResultSet = newContents;
            rowArray = new Object[newContents.getMetaData().getColumnCount()];
            classList = new Class[newContents.getMetaData().getColumnCount()];
            for (int c = 1; c < newContents.getMetaData().getColumnCount() + 1; c++) {
                Class<?> dynamicClass = Class.forName(Utility.ConvertType(newContents.getMetaData().getColumnType(c)));
                classList[c-1] = dynamicClass;
                rowArray[c-1] = dynamicClass.cast(newContents.getObject(c));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }catch (ClassNotFoundException c){
            System.out.printf("Class not found when dynamically grabbing classes.");
        }
    }

    public DBRow(String[] namesArr){
        rowArray = namesArr;
        rowResultSet = null;
        classList = new Class[namesArr.length];
        for(int i = 0; i < namesArr.length; i++){
            classList[i] = String.class;
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

    public ResultSet getRowResultSet() {
        return rowResultSet;
    }

    public void setRowResultSet(ResultSet rowResultSet) {
        this.rowResultSet = rowResultSet;
    }

    public Object[] getRowArray() {
        return rowArray;
    }
    public void setRowArray(Object[] rowArray) {
        this.rowArray = rowArray;
    }

    public String getInsertQuery(String tableName){

        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES(");

        for(int i = 0; i < this.getRowArray().length; i++){
            if(i == getRowArray().length - 1){
                queryBuilder.append(getRowArray()[i] + ")");
            }else{
                queryBuilder.append(getRowArray()[i] + ", ");
            }
        }
        return queryBuilder.toString();
    }

    public String getTableName() {
        return tableName;
    }
}
