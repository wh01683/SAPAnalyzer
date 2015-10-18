package gui;

import db.DBInfo;
import db.DatabaseIO;
import gui.createforms.CreateBOM;
import gui.createforms.EditPart;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;


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
    private JComboBox cbTableSelect;
    private JTable tblDetailsTable;
    private static JMenuBar menuBar;

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

                if (rowindex < 0 || columnindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {

                    JPopupMenu popup = new PopupMenu();
                    JMenu mnuView = new JMenu("View");
                    JMenuItem mnuItemShowDetail = new JMenuItem("Details");
                    mnuItemShowDetail.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            new EditPart(true).setVisible(true);
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
    }

    public static void main(String[] args) {
        frame = new SAPAnalyzer();
        frame.setContentPane(frame.getContentPane());
        frame.setJMenuBar(menuBar);
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


    private void createMenu() {


        menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenuItem mnuLoad = new JMenu("Load");
        JMenuItem mnuItemNew = new JMenu("New");
        JMenuItem mnuItemBOM = new JMenuItem("BOM");
        JMenuItem mnuItemPart = new JMenuItem("Part");

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

        mnuItemPart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new EditPart(false).setVisible(true);

            }
        });

        mnuItemBOM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CreateBOM().setVisible(true);
            }
        });
        mnuItemNew.add(mnuItemBOM);
        mnuItemNew.add(mnuItemPart);
        mnuFile.add(mnuItemNew);

        mnuLoad.add(mnuItemLoadAll);
        mnuFile.add(mnuLoad);
        menuBar.add(mnuFile);
    }

    private void addChildMenus(JMenuItem parent, Hashtable<String, ActionListener> namesToActionListeners) {
        Enumeration enumer = namesToActionListeners.keys();
        while (enumer.hasMoreElements()) {
            String label = (String) enumer.nextElement();
            JMenuItem temp = new JMenuItem(label);
            temp.addActionListener(namesToActionListeners.get(label));
            parent.add(temp);
        }
    }


}


