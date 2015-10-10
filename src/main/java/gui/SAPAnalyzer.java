package gui;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import db.DBInfo;
import db.DatabaseIO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * Created by robert on 9/18/2015.
 */
public class SAPAnalyzer extends JFrame{



    private JPanel pnlMain;
    private JTabbedPane tbDatabaseInformation;
    private JPanel pnlTable;
    private JPanel pnlTree;
    private JTree treeSAPHierarchy;
    private JPanel pnlTableFilter;
    private JComboBox cbSelection;
    private JPanel pnlMainInformation;
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
    private JTabbedPane tbEditTable;
    private JPanel pnlInsertEmployee;
    private JPanel pnlInsertBOM;
    private JPanel pnlInsertProduct;
    private JPanel pnlInsertComponent;
    private JPanel pnlInsertProcess;
    private JPanel pnlCreateBOM;
    private JTextField fldBOMName;
    private JPanel pnlBOMForm;
    private JComboBox cbPlantID;
    private JComboBox cbCompanyID;
    private JButton btnAddComponent;
    private JPanel pnlAddComponents;
    private JPanel pnlSummaryView;
    private JTextField fldProdName;
    private JTextField fldProdCost;
    private JTextField fldCompName;
    private JTextField fldCompDesc;
    private JPanel pnlAddProcess;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JButton btnAddProcess;
    private JTextArea txtarPreview;
    private JTable tblDetailsTable;
    private JButton btnClear;

    DatabaseTableModel databaseTableModel;
    private static JFrame frame;

    private static DatabaseIO dbio;

    private SAPAnalyzer(){
        DBInfo.start();
        dbio = new DatabaseIO();
        for(String s : dbio.getTableNames()){
            cbTableSelect.addItem(s);
        }

        this.setContentPane(pnlMain);
        this.setVisible(true);
        pnlTable.setVisible(true);
        tblShownInformation.setVisible(true);
        tblShownInformation.setModel(new DatabaseTableModel(getTblShownQuery()));

        this.btnFillInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillTableModel();
            }
        });

        pnlDetail.setVisible(true);
        tblDetailsTable.setVisible(true);

        fillTableModel();

        cbTableSelect.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    cbSortCategory.removeAllItems();
                    cbSelection.removeAllItems();
                    cbSelection.addItem("All");
                }
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tblShownInformation.clearSelection();
                    tblShownInformation.setCellSelectionEnabled(false);
                    fillTableModel();
                    updateCbBoxes();
                    tblShownInformation.setCellSelectionEnabled(true);
                }
            }
        });

        btnSort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortTableModel();
            }
        });

        tblShownInformation.setCellSelectionEnabled(true);
        ListSelectionModel cellSelection = tblShownInformation.getSelectionModel();

        cellSelection.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblShownInformation.getSelectedRow();
                int col = tblShownInformation.getSelectedColumn();
                if(row > -1 && col > -1){
                    fillDetailsTable(dbio.getCurrentTable(), row, col);
                }
            }
        });

        TableListener listener = new TableListener();
        tblShownInformation.getModel().addTableModelListener(listener);

    }

    public static void main(String[] args) {
        frame = new SAPAnalyzer();
        frame.setContentPane(new SAPAnalyzer().getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();

    }

    public void updateCbBoxes(){
        cbSortCategory.removeAllItems();
        cbSelection.removeAllItems();
        cbSelection.addItem("All");

        for(String s : databaseTableModel.getColumnNames()){
            cbSelection.addItem(s);
            cbSortCategory.addItem(s);
        }
    }

    public void setDatabaseTableModel(DatabaseTableModel databaseTableModel) {
        databaseTableModel.addTableModelListener(new TableListener());
        this.databaseTableModel = databaseTableModel;
        dbio.setCurrentTable((String) cbTableSelect.getSelectedItem());
        tblShownInformation.setModel(databaseTableModel);
        updateCbBoxes();
    }

    private void fillTableModel(){
        setDatabaseTableModel(new DatabaseTableModel(getTblShownQuery()));
    }

    private String getSortQuery(){

        String sortQuery = "select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem() + ((cbSortCategory.getSelectedIndex() == 0) ? "" : " order by ") + ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortCategory.getSelectedItem() + " ") +
                ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortWay.getSelectedItem());

        return sortQuery;
    }

    private String getTblShownQuery(){
        String shownQuery = "select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem();
        return shownQuery;
    }

    public static DatabaseIO getDbio(){
        return dbio;
    }

    private void sortTableModel(){
        setDatabaseTableModel(new DatabaseTableModel(getSortQuery()));
    }

    public void fillDetailsTable(String tableName, int row, int col){
        DatabaseTableModel temp = new DatabaseTableModel(tableName, tblShownInformation.getValueAt(row, col));
        tblDetailsTable.setCellSelectionEnabled(false);
        tblDetailsTable.setModel(temp);
    }



}


