import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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

    DatabaseTableModel databaseTableModel;
    private static JFrame frame;
    private String currentDatabase;

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
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    cbSortCategory.removeAllItems();
                    cbSelection.removeAllItems();
                    cbSelection.addItem("All");
                }
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fillTableModel();
                    updateCbBoxes();
                }
            }
        });

        btnSort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortTableModel();
            }
        });
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
        dbio.setCurrentTable((String) cbTableSelect.getSelectedItem());
        this.databaseTableModel = databaseTableModel;
        tblShownInformation.setModel(databaseTableModel);
        updateCbBoxes();
    }

    private void fillTableModel(){
        dbio.setCurrentTable((String)cbTableSelect.getSelectedItem());
        databaseTableModel = new DatabaseTableModel("select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem());
        this.databaseTableModel.addTableModelListener(new TableListener());
        setDatabaseTableModel(databaseTableModel);
    }

    public static DatabaseIO getDbio(){
        return dbio;
    }

    private void sortTableModel(){
        dbio.setCurrentTable((String)cbTableSelect.getSelectedItem());
        databaseTableModel = new DatabaseTableModel("select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem() + ((cbSortCategory.getSelectedIndex() == 0) ? "" : " order by ") + ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortCategory.getSelectedItem() + " ") +
                ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortWay.getSelectedItem()));
        this.databaseTableModel.addTableModelListener(new TableListener());
        setDatabaseTableModel(databaseTableModel);
    }



}


