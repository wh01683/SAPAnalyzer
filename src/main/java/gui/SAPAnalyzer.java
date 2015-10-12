package gui;

import db.DBInfo;
import db.DatabaseIO;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * Created by robert on 9/18/2015.
 */
public class SAPAnalyzer extends JFrame{



    private JPanel pnlMain;
    private JTabbedPane tbDatabaseInformation;
    private JPanel pnlTable;
    private JPanel pnlTableFilter;
    private JComboBox cbSelection;
    private JPanel pnlMainInformation;
    private JButton btnFillInfo;
    private JTable tblShownInformation;
    private JPanel pnlTableSort;
    private JComboBox cbSortCategory;
    private JComboBox cbSortWay;
    private JButton btnSort;
    private JTabbedPane tbSidePanel;
    private JPanel pnlDetail;
    private JPanel pnlInsert;
    private JComboBox cbTableSelect;
    private JPanel pnlCreateBOM;
    private JTextField fldBOMName;
    private JPanel pnlBOMForm;
    private JComboBox cbBOMPlantID;
    private JComboBox cbBOMCompanyID;
    private JButton btnAddComponent;
    private JPanel pnlSummaryView;
    private JTextField fldProdName;
    private JTextField fldProdCost;
    private JTextField fldCompName;
    private JTextField fldCompDesc;
    private JTextField fldProcDesc;
    private JTextField fldProcCost;
    private JTextField fldProcTime;
    private JButton btnAddProcess;
    private JTextArea txtarPreview;
    private JTable tblDetailsTable;
    private JTextField fldProdID;
    private JTextField fldBomId;
    private JTextField fldCompID;
    private JTextField fldProcId;
    private JButton btnAddBomProdToPrev;
    private JTextField fldProcCompId;
    private JTextField fldCompProdId;
    private JTextField fldCompBomId;
    private JButton btnViewInsertQueries;
    private JPanel pnlAddComponents;
    private JPanel pnlAddProcess;
    private JButton btnInsert;
    private static JMenuBar menuBar;

    private BOMPreviewBuilder bomPreviewBuilder;
    DatabaseTableModel databaseTableModel;
    private static JFrame frame;

    private static DatabaseIO dbio;

    private SAPAnalyzer(){
        dbio = new DatabaseIO();
        for(String s : dbio.getTableNames()){
            cbTableSelect.addItem(s);
        }
        createMenu();
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

        tbDatabaseInformation.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fillBomProdCbBoxes();
            }
        });
        tblShownInformation.setCellSelectionEnabled(true);
        ListSelectionModel cellSelection = tblShownInformation.getSelectionModel();

        cellSelection.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblShownInformation.getSelectedRow();
                int col = tblShownInformation.getSelectedColumn();
                if (row > -1 && col > -1) {
                    fillDetailsTable(dbio.getCurrentTable(), row, col);
                }
            }
        });

        btnAddBomProdToPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBomProd(e);
            }
        });

        btnAddComponent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBomProd(e);
            }
        });

        btnAddProcess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBomProd(e);
            }
        });

        btnViewInsertQueries.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                JTextArea textArea = new JTextArea(bomPreviewBuilder.getPrevQueryText());
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                scrollPane.setPreferredSize(new Dimension(750, 750));
                JOptionPane.showMessageDialog(null, scrollPane, "Queries", JOptionPane.CLOSED_OPTION);
            }
        });

        btnInsert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bomPreviewBuilder.commitInserts();
            }
        });


        TableListener listener = new TableListener();
        tblShownInformation.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = tblShownInformation.rowAtPoint(e.getPoint());
                int c = tblShownInformation.columnAtPoint(e.getPoint());
                if (r >= 0 && r < tblShownInformation.getRowCount()) {
                    tblShownInformation.setRowSelectionInterval(r, r);
                } else {
                    tblShownInformation.clearSelection();
                }

                if (c >= 0 && c < tblShownInformation.getColumnCount()) {
                    tblShownInformation.setColumnSelectionInterval(c, c);
                } else {
                    tblShownInformation.clearSelection();
                }

                int columnindex = tblShownInformation.getSelectedColumn();
                int rowindex = tblShownInformation.getSelectedRow();
                /*Object temp = tblShownInformation.getValueAt(rowindex, columnindex);
                String colName = tblShownInformation.getColumnName(columnindex);
                String tableName = dbio.getCurrentTable();*/

                if (rowindex < 0 || columnindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {

                    JPopupMenu popup = new PopupMenu();
                    JMenu mnuView = new JMenu("View");
                    JMenuItem mnuItemShowDetail = new JMenuItem("Details");
                    mnuItemShowDetail.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            fillDetailsTable(dbio.getCurrentTable(), tblShownInformation.getSelectedRow(), tblShownInformation.getSelectedColumn());
                        }
                    });
                    mnuView.add(mnuItemShowDetail);
                    popup.add(mnuView);
                    //popup.add(new JLabel(String.format("Pk: %s, Column Name: %s, Table Name: %s", temp.toString(), colName, tableName)));
                    popup.pack();
                    popup.setSize(300, 100);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        tblShownInformation.getModel().addTableModelListener(listener);
        txtarPreview.setEditable(false);
        txtarPreview.setVisible(true);
        txtarPreview.setLineWrap(false);
        bomPreviewBuilder = new BOMPreviewBuilder(txtarPreview);


    }

    public static void main(String[] args) {
        frame = new SAPAnalyzer();
        frame.setContentPane(frame.getContentPane());
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //new PopupMenu().setVisible(true);
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

    private void fillBomProdCbBoxes(){
        ArrayList<Integer> companyKeys = DBInfo.getTabToPkVals().get("COMPANIES");
        ArrayList<Integer> plantKeys = DBInfo.getTabToPkVals().get("PLANTS");
        for(Integer i : companyKeys){
            cbBOMCompanyID.addItem(i);
        }
        for(Integer i : plantKeys){
            cbBOMPlantID.addItem(i);
        }
    }
    private String getSortQuery(){

        String sortQuery = "select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem() + ((cbSortCategory.getSelectedIndex() == 0) ? "" : " order by ") +
                ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortCategory.getSelectedItem() + " ") +
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

    private void addBomProd(ActionEvent e){

        JButton source = (JButton)e.getSource();

        int maxColWidth = 0;
        String[] tabs = {"BOM", "PRODUCTS", "COMPONENTS", "PROCESSES"};

        for (int i = 0; i < tabs.length; i++) {
            maxColWidth = (DBInfo.getTabToColNames().get(tabs[i]).size() > maxColWidth)? DBInfo.getTabToColNames().get(tabs[i]).size() : maxColWidth;
        }

        if(source == btnAddBomProdToPrev) {
            String[] bom = new String[maxColWidth];
            String bomName = fldBOMName.getText();
            Integer bomId = Integer.parseInt(fldBomId.getText());
            Integer bomPlantId = (Integer) cbBOMPlantID.getSelectedItem();
            Integer bomCompanyId = (Integer) cbBOMCompanyID.getSelectedItem();
            bom[0] = bomId.toString();
            bom[1] = bomPlantId.toString();
            bom[2] = bomName;
            bom[3] = bomCompanyId.toString();

            String[] prod = new String[maxColWidth];
            Integer prodId = Integer.parseInt(fldProdID.getText());
            Integer prodCost = Integer.parseInt(fldProdCost.getText());
            String prodName = fldProdName.getText();
            prod[0] = prodId.toString();
            prod[1] = prodName;
            prod[2] = prodCost.toString();

            bomPreviewBuilder.addBOMProduct(bom, prod);
            clearAllFields();

        }

        if(source == btnAddComponent) {
            String[] comp = new String[maxColWidth];
            String compName = fldCompName.getText();
            Integer compId = Integer.parseInt(fldCompID.getText());
            String compDesc = fldCompDesc.getText();
            Integer bomId = Integer.parseInt(fldCompBomId.getText());
            Integer prodId = Integer.parseInt(fldCompProdId.getText());
            comp[0] = compId.toString();
            comp[1] = compName;
            comp[2] = compDesc;
            bomPreviewBuilder.addComponent(comp, bomId, prodId);
            clearAllFields();
        }

        if(source == btnAddProcess) {
            String[] proc = new String[maxColWidth];
            String procDesc = fldProcDesc.getText();
            Integer procId = Integer.parseInt(fldProcId.getText());
            Integer procCost = Integer.parseInt(fldProcCost.getText());
            Integer procTime = Integer.parseInt(fldProcTime.getText());
            Integer compId = Integer.parseInt(fldProcCompId.getText());
            proc[0] = procId.toString();
            proc[1] = procDesc;
            proc[2] = procCost.toString();
            proc[3] = procTime.toString();
            bomPreviewBuilder.addProcess(proc, compId);
            clearAllFields();
        }

    }

    private void clearAllFields(){

        fldCompID.setText("");
        fldCompDesc.setText("");
        fldCompName.setText("");

        fldBomId.setText("");
        fldBOMName.setText("");

        fldProdCost.setText("");
        fldProdName.setText("");
        fldProdID.setText("");
        fldCompProdId.setText("");
        fldCompBomId.setText("");

        fldProcId.setText("");
        fldProcCompId.setText("");
        fldProcCost.setText("");
        fldProcDesc.setText("");
        fldProcTime.setText("");
    }

    private void createMenu() {


        menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenuItem mnuLoad = new JMenu("Load");
        JMenuItem mnuItemNew = new JMenu("New");
        JMenuItem mnuItemBOM = new JMenuItem("BOM");
        JMenuItem mnuItemEmp = new JMenuItem("Employee");
        JMenuItem mnuItemProcess = new JMenuItem("Process");
        JMenuItem mnuItemComponent = new JMenuItem("Component");

        JMenuItem mnuItemLoadAll = new JMenuItem("All");
        mnuItemLoadAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DBInfo.showLoadScreen();
                    }
                });
            }
        });


        mnuItemNew.add(mnuItemBOM);
        mnuItemNew.add(mnuItemComponent);
        mnuItemNew.add(mnuItemProcess);
        mnuItemNew.add(mnuItemEmp);
        mnuFile.add(mnuItemNew);

        mnuLoad.add(mnuItemLoadAll);
        mnuFile.add(mnuLoad);
        menuBar.add(mnuFile);

    }


}


