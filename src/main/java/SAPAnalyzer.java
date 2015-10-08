import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Created by robert on 9/18/2015.
 */
public class SAPAnalyzer extends JFrame implements TableModelListener{



    private JPanel pnlMain;
    private JTabbedPane tbDatabaseInformation;
    private JPanel pnlTable;
    private JPanel pnlTree;
    private JTree treeSAPHierarchy;
    private JPanel pnlDropboxFilter;
    private JPanel pnlTableFilter;
    private JComboBox cbSelection;
    private JCheckBox chkCompanies;
    private JCheckBox chkPlants;
    private JCheckBox chkBOM;
    private JCheckBox chkMaterials;
    private JCheckBox chkProcesses;
    private JCheckBox chkLogistics;
    private JCheckBox chkProducts;
    private JPanel pnlMainInformation;
    private JPanel pnlCompare;
    private JComboBox cbComparisonMethod;
    private JButton btnFillInfo;
    private JTable tblShownInformation;
    private JPanel tblTableSort;
    private JComboBox cbSortCategory;
    private JComboBox cbSortWay;
    private JButton btnSort;
    private JTabbedPane tbSidePanel;
    private JPanel pnlDetail;
    private JPanel pnlEdit;
    private JPanel pnlInsert;
    private JComboBox cbTableSelect;

    DatabaseTableModel databaseTableModel;
    private String[] columnHeadings;
    private static JFrame frame;

    private static DatabaseIO dbio;

    private SAPAnalyzer(){
        dbio = new DatabaseIO();
        for(String s : dbio.getTableNames()){
            cbTableSelect.addItem(s);
        }

        this.setContentPane(pnlMain);
        this.setVisible(true);
        pnlTable.setVisible(true);
        tblShownInformation.setVisible(true);
        fillTableModel();
        this.btnFillInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillTableModel();
            }
        });

        cbTableSelect.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.DESELECTED){
                    cbSortCategory.removeAllItems();
                    cbSortCategory.addItem("All");
                    cbSelection.removeAllItems();
                    cbSelection.addItem("All");
                }
                if(e.getStateChange() == ItemEvent.SELECTED){
                    fillTableModel();
                    updateCbBoxes();
                }
            }

        });
    }

    public static void main(String[] args){
        frame = new SAPAnalyzer();
        frame.setContentPane(new SAPAnalyzer().getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();

    }


    public void tableChanged(TableModelEvent e) {
        System.out.println("TableModelEvent triggered!");
        int row = e.getFirstRow();
        int column = e.getColumn();
        Object test = tblShownInformation.getModel().getValueAt(row, column);
        System.out.println("row: " + row + " column: " + column);
        System.out.println(test.toString());
    }

    public void updateCbBoxes(){
        cbSortCategory.removeAllItems();
        cbSortCategory.addItem("All");
        cbSelection.removeAllItems();
        cbSelection.addItem("All");

        for(String s : databaseTableModel.getColumnNames()){
            cbSelection.addItem(s);
            cbSortCategory.addItem(s);
        }
    }

    public void setDatabaseTableModel(DatabaseTableModel databaseTableModel) {
        this.databaseTableModel = databaseTableModel;
        tblShownInformation.setModel(databaseTableModel);
        updateCbBoxes();
    }

    private void fillTableModel(){
        databaseTableModel = new DatabaseTableModel("select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem());
        setDatabaseTableModel(databaseTableModel);
    }
}


