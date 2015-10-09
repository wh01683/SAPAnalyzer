import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by robert on 10/8/2015.
 */
public class DatabaseTableModel extends AbstractTableModel{

    private Object[][] tableData;
    private String[] columnNames;
    private ArrayList<DBRow> rowList = new ArrayList<DBRow>(10);
    private DatabaseIO dbio = new DatabaseIO();

    public DatabaseTableModel(String query){


        if(query != null){
            repopulate(query);
            columnNames = createColumnHeadings(query);
        }

    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        super.fireTableRowsUpdated(firstRow, lastRow);
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
        return tableData.length;
    }

    public void clearModel() {
        tableData = new Object[0][0];
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int colIndex) {
        /*if (rowIndex < 0 || rowIndex >= getRowCount()) {
            // throw exception
        }
        if (colIndex < 0 || colIndex >= getColumnCount()) {
            // throw exception
        }*/
        DBRow dbRow = rowList.get(rowIndex);

        return dbRow.getValueAtColumn(colIndex);
    }

    @Override
    public void setValueAt(Object newVal, int rowIndex, int colIndex) {
        /*if (rowIndex < 0 || rowIndex >= getRowCount()) {
            // throw exception
        }
        if (colIndex < 0 || colIndex >= getColumnCount()) {
            // throw exception
        }*/

        DBRow row = rowList.get(rowIndex);
        row.setValueAtColumn(colIndex, newVal);
        fireTableCellUpdated(rowIndex, colIndex);

    }

    public void repopulate(String query) {

        columnNames = createColumnHeadings(query);
        rowList.add(new DBRow(columnNames));

        ArrayList<String> q = new ArrayList<String>();
        q.add(query);

        ArrayList<ResultSet> rsset = dbio.executeQuery(q);

        try {
            for (ResultSet rs : rsset) {
                if (!rs.next()) {
                } else {
                    do {
                        DBRow temp = new DBRow(rs);
                        rowList.add(temp);
                    } while (rs.next());
                }
            }
            tableData = new Object[rowList.size()][columnNames.length];

            int r = 0;
            for(DBRow row : rowList){
                tableData[r] = row.getRowArray();
                r++;
            }

            for(int row = 0; row < tableData.length; row ++){
                for(int col = 0; col < tableData[row].length; col++){
                    setValueAt(tableData[row][col], row, col);
                }
            }

        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private String[] createColumnHeadings(String query) {

        try {
            ArrayList<String> quer = new ArrayList<String>();

            quer.add(query);

            ArrayList<ResultSet> resultSets = dbio.executeQuery(quer);

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
        return columnNames;
    }

    }

