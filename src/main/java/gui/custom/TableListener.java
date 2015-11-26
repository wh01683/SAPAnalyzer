package gui.custom;

import db.DBIO;

import javax.swing.*;
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

            System.out.println("row: " + row + " column: " + column);
            System.out.println(data.toString());

            try {
                if (DBIO.getCurrentTable().equalsIgnoreCase("BOM")) {
                    DBIO.updateTable(DBIO.getCurrentTable(), model.getColumnName(column), data,
                            new String[]{model.getColumnName(2), model.getColumnName(3)}, new Object[]{model.getValueAt(row, 2), model.getValueAt(row, 3)});
                } else {
                    DBIO.updateTable(DBIO.getCurrentTable(), model.getColumnName(column), data,
                            new String[]{model.getColumnName(0)}, new Object[]{model.getValueAt(row, 0)});
                }
            } catch (SQLException s) {
                JOptionPane.showMessageDialog(null, "Could not update " + DBIO.getCurrentTable() +
                        ". Column: " + model.getColumnName(column) + ". Row: " + row + ". Attempted to insert " + data.toString());
                s.printStackTrace();
            }
        }

    }
}
