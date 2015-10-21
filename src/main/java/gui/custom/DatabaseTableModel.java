package gui.custom;

import db.DBIO;
import db.DBInfo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Created by robert on 10/8/2015.
 */
public class DatabaseTableModel extends AbstractTableModel  {

    private Object[][] tableData;
    private String[] columnNames;
    private ArrayList<DBRow> rowList;


    /** Used for populating the details panel table. Finds all information in all tables where the PRIMARY KEY from the GIVEN TABLE
     * is reference by those tables.
     * @param tableName Table name of the referenced table.
     * @param pk Primary key of referenced table.
     */
    public DatabaseTableModel(String tableName, Object pk){

        ArrayList<String> tabNames = DBInfo.getTabToRefTabHash().get(tableName);
        if(!(tabNames == null)) {
            ArrayList<String> quers = new ArrayList<String>(10);
            for (String s : tabNames) {
                ArrayList<String> fkNames = DBInfo.getTabToForeignKeyNames().get(s);

                for (String fk : fkNames) {
                    quers.add("select * from " + s + " where " + fk +
                            " = " + pk.toString());
                }
            }
            this.tableData = processQueries(quers.toArray(new String[quers.size()]));
        }
    }

    /**
     * Uses an array of Strings to populate the table data. Can be 1 to inf.
     * @param queries Queries used to populate the table model.
     */
    public DatabaseTableModel(String... queries){
        repopulateData(queries);
    }


    /**
     * Used for updating table via JTable alteration
     *
     * @param firstRow Highest (lowest index) row in selection.
     * @param lastRow  Lowest (highest index) row in seleciton.
     */
    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        super.fireTableRowsUpdated(firstRow, lastRow);
    }

    /**
     * Gets the class of the specified column.
     *
     * @param columnIndex Index of specified column.
     * @return Returns class of the specified column.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return super.getColumnClass(columnIndex);
    }

    /**
     * Number of columns in Database model.
     * @return Number of columns.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets the column name using the column's index.
     * @param column Index of column.
     * @return Name of column.
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Number of rows in the Database model.
     * @return returns size of the rowList
     */
    public int getRowCount() {
        return rowList.size();
    }

    /**
     * Clears model by resetting tableData.
     */
    public void clearModel() {
        tableData = new Object[0][0];
        rowList = new ArrayList<DBRow>(0);
        columnNames = new String[0];
        fireTableDataChanged();
    }

    /**
     * Checks whether cell is editable. Default is true for this table.
     * @param rowIndex Row number to check.
     * @param columnIndex Column number to check.
     * @return True if editable, false otherwise.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * Gets the value located at a given row and column.
     * @param rowIndex Row of desired value.
     * @param colIndex Column of desired value.
     * @return Object located at given location.
     */
    public Object getValueAt(int rowIndex, int colIndex) {
        try {
            return rowList.get(rowIndex).getRowArray()[colIndex];

        }catch (NullPointerException n) {
            System.out.println("Null pointer caught in DBModel getValAt");
            n.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException a) {
            System.out.printf("Array index out of bounds. Row: %d, Col: %d\n", rowIndex, colIndex);
            for (DBRow row : rowList) {
                System.out.printf("\n_ROW_Size: %s\n", row.getRowArray().length);
                for (Object o : row.getRowArray()) {
                    System.out.printf("_%s_", o.toString());
                }
            }
            return null;
        }
    }

    /**
     * Sets value at a given location to a new Object value.
     * @param newVal Value to replace old value.
     * @param rowIndex Row of value to be replaced.
     * @param colIndex Column of value to be replaced.
     */
    @Override
    public void setValueAt(Object newVal, int rowIndex, int colIndex) {
        try {
            DBRow row = rowList.get(rowIndex);
            row.setValueAtColumn(colIndex, newVal);
            tableData[rowIndex][colIndex] = newVal;
            fireTableCellUpdated(rowIndex, colIndex);
        }catch (NullPointerException n){
            n.printStackTrace();
        }
    }


    /**
     * Processes an array of String queries and will generate a two dimensional Object array for use in the table model.
     * @param queries Queries to execute.
     * @return Two dimensional array containing query results.
     */
    private Object[][] processQueries(String... queries){
        ArrayList<DBRow> tempRowList = new ArrayList<DBRow>(10);
        for (String s : queries) {
            columnNames = DBIO.getColNames(s).toArray(new String[DBIO.getColNames(s).size()]);
            ArrayList<ArrayList<Object>> tempQuerySet = DBIO.getMultiObResults(s);
            for (ArrayList<Object> row : tempQuerySet) {
                DBRow temp = new DBRow(row, DBIO.getColClasses(s));
                tempRowList.add(temp);
            }
        }
        Object[][] tempTableData = new Object[tempRowList.size()][columnNames.length];
        int countRows = 0;
        for(DBRow rows : tempRowList) {
            tempTableData[countRows] = rows.getRowArray();
            countRows++;
        }
        this.rowList = tempRowList;
        return tempTableData;
    }

    /**
     * Repopulates the tableData array using an Object array produced by the new queries.
     * @param queries Queries to repopulate the table with.
     */
    public void repopulateData(String... queries){
        this.tableData = processQueries(queries);
    }

    public void populateTable(){
        for (int row = 0; row < tableData.length; row++) {
            for (int col = 0; col < tableData[row].length; col++) {
                setValueAt(tableData[row][col], row, col);
            }
        }
    }

    public String[] getColumnNames() {
        return columnNames;
    }
}