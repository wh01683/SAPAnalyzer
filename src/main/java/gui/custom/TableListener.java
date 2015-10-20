package gui.custom;

import db.DBIO;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.sql.SQLException;

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
            try {
                DBIO.updateTable(column, pkdata, data);
            } catch (SQLException s) {
                s.printStackTrace();
            }
        }
    }





}
