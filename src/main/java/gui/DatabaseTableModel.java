package gui;

import db.DBInfo;
import db.DatabaseIO;
import db.QueryStorage;

import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by robert on 10/8/2015.
 */
public class DatabaseTableModel extends AbstractTableModel  {

    private Object[][] tableData = new Object[0][0];
    private String[] columnNames = new String[0];
    private ArrayList<DBRow> rowList = new ArrayList<DBRow>(10);
    private DatabaseIO dbio = new DatabaseIO();
    private QueryStorage queryStorage = new QueryStorage();


    /**
     * @param tableName
     * @param pk
     */
    public DatabaseTableModel(String tableName, Object pk){

        ArrayList<String> tabNames = DBInfo.getTabToRefTabHash().get(tableName);
        if(!(tabNames == null)) {
            String[] quers = new String[tabNames.size()];
            int count = 0;
            for (String s : tabNames) {
                quers[count] = "select * from " + s + " where " + DBInfo.getTabToPKHash().get(tableName) +
                        " = " + pk.toString();
            }
            processQueries(quers);
        }
    }

    /**
     * Uses an array of Strings to populate the table data. Can be 1 to inf.
     * @param queries
     */
    public DatabaseTableModel(String... queries){
        repopulateData(queries);
    }


    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        if(firstRow != 0 && lastRow != 0) {
            super.fireTableRowsUpdated(firstRow, lastRow);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return super.getColumnClass(columnIndex);
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

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
     * Creates a String array of column names given a particular query. Not all columns are present in a query, so this method
     * will get the unique column name set on an individual query basis.
     * @param query Query to process.
     * @return A String array of column names associated with the query.
     */
    private String[] createColumnHeadings(String query) {
        try {
            ArrayList<ResultSet> resultSets = dbio.executeQuery(query);
            int c = 0;
            ArrayList<String> temp = new ArrayList<String>();
            for (ResultSet r : resultSets) {
                for (int i = 1; i < r.getMetaData().getColumnCount() + 1; i++) {
                    temp.add(r.getMetaData().getColumnName(i));
                    c = i;
                }
            }
            String[] arr = new String[c];
            int count = 0;
            for (String s : temp) {
                arr[count] = s;
                count++;
            }
            return arr;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getColumnNames() {
        try {
            return columnNames;
        }catch (NullPointerException n){
            n.printStackTrace();
            return null;
        }
    }

    /**
     * Processes an array of String queries and will generate a two dimensional Object array for use in the table model.
     * @param queries Queries to execute.
     * @return Two dimensional array containing query results.
     */
    private Object[][] processQueries(String... queries){

        int maxColLength = 0;
        int rowCount = 0;

        ArrayList<DBRow> tempRowList = new ArrayList<DBRow>(10);

        for (String s : queries) {
            String[] cols = createColumnHeadings(s);
            rowCount++;
            tempRowList.add(new DBRow(cols));
            ArrayList<ResultSet> tempQuerySet = dbio.executeQuery(queries);
            for (ResultSet rs : tempQuerySet) {
                try {
                    if (!rs.next()) {
                    } else {
                        do {
                            DBRow temp = new DBRow(rs);
                            tempRowList.add(temp);
                            rowCount++;
                        } while (rs.next());
                    }
                }catch (SQLException sql){
                    System.out.println("SQL exception caught in processQueries method in DBModelClass.");
                    sql.printStackTrace();
                }
            }
            maxColLength = (cols.length > maxColLength) ? cols.length : maxColLength;
            this.columnNames = (this.columnNames.length < cols.length)? cols : this.columnNames;
        }

        Object[][] tempTableData = new Object[tempRowList.size()][maxColLength];
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

}