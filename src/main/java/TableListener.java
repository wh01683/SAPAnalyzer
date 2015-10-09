import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Created by robert on 10/8/2015.
 */
public class TableListener implements TableModelListener{


    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            DatabaseTableModel model = (DatabaseTableModel) e.getSource();
            Object data = model.getValueAt(row, column);
            Object pkdata = model.getValueAt(row, 0);
            System.out.println("row: " + row + " column: " + column);
            System.out.println(data.toString());
            SAPAnalyzer.getDbio().updateTable(column, pkdata, data);
        }
    }


}
