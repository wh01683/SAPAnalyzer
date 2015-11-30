package gui;

import db.DBIO;
import db.DBInfo;
import gui.createforms.CreateBOM;
import gui.createforms.EditPart;
import gui.createforms.Help;
import gui.custom.DatabaseTableModel;
import gui.custom.PopupMenu;
import gui.custom.TableListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;


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
    private JTable tblShownInformation;
    private JPanel pnlTableSort;
    private JComboBox cbSortCategory;
    private JComboBox cbSortWay;
    private JButton btnSort;
    private JTabbedPane tbSidePanel;
    private JPanel pnlDetail;
    private JComboBox cbTableSelect;
    private JTable tblDetailsTable;
    private JScrollPane scrlTable;
    private static JMenuBar menuBar;

    DatabaseTableModel databaseTableModel;
    private static JFrame frame;

    private SAPAnalyzer(){
        //<editor-fold desc="SAP Constructor">
        DBIO.instantiate();
        for (String s : DBIO.getTableNames()) {
            cbTableSelect.addItem(s);
        }
        DBIO.setCurrentTable(cbTableSelect.getSelectedItem().toString());
        createMenu();
        this.setContentPane(pnlMain);
        this.setVisible(true);
        pnlTable.setVisible(true);
        tblShownInformation.setVisible(true);
        tblShownInformation.setModel(new DatabaseTableModel(getTblShownQuery()));

        pnlDetail.setVisible(true);
        tblDetailsTable.setVisible(true);
        scrlTable.setVisible(true);

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
                    DBIO.setCurrentTable(cbTableSelect.getSelectedItem().toString());
                    setDatabaseTableModel(new DatabaseTableModel("select * from " + cbTableSelect.getSelectedItem().toString()));
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

                    JMenu mnuNew = new JMenu("New");
                    JMenuItem mnuItemBOM = new JMenuItem("BOM");

                    mnuItemBOM.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            CreateBOM createBOM = new CreateBOM((tblShownInformation.getModel().
                                    getValueAt(tblShownInformation.getSelectedRow(),
                                            tblShownInformation.getSelectedColumn())));
                            createBOM.setVisible(true);
                        }
                    });

                    mnuItemShowDetail.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            EditPart editPart = new EditPart((tblShownInformation.getModel().
                                    getValueAt(tblShownInformation.getSelectedRow(),
                                            tblShownInformation.getSelectedColumn())).toString());
                            editPart.setVisible(true);
                        }
                    });
                    mnuView.add(mnuItemShowDetail);
                    mnuNew.add(mnuItemBOM);
                    popup.add(mnuNew);
                    popup.add(mnuView);
                    //popup.add(new JLabel(String.format("Pk: %s, Column Name: %s, Table Name: %s", temp.toString(), colName, tableName)));
                    popup.pack();
                    popup.setSize(300, 100);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        tblShownInformation.getModel().addTableModelListener(listener);
        //</editor-fold>
    }

    public static void main(String[] args) {
        frame = new SAPAnalyzer();
        frame.setContentPane(frame.getContentPane());
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                DBIO.terminate(); // closes connection
                System.exit(0);
            }
        });
        frame.setVisible(true);
        frame.pack();
    }

    /**
     * Updates combo boxes. Used when model changes.
     */
    public void updateCbBoxes(){
        cbSortCategory.removeAllItems();
        cbSelection.removeAllItems();
        cbSelection.addItem("All");

        for(String s : databaseTableModel.getColumnNames()){
            cbSelection.addItem(s);
            cbSortCategory.addItem(s);
        }
    }

    /**
     * Takes care of all related processes associated with changing the table's content.
     * @param databaseTableModel new table model for the table.
     */
    public void setDatabaseTableModel(DatabaseTableModel databaseTableModel) {
        this.databaseTableModel = databaseTableModel;
        DBIO.setCurrentTable((String) cbTableSelect.getSelectedItem());
        tblShownInformation.setModel(databaseTableModel);
        updateCbBoxes();
    }

    /**
     * Method creates the query used to sort the current table's contents.
     * @return Returns the string query.
     */
    private String getSortQuery(){
        String sortQuery = "select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem() + ((cbSortCategory.getSelectedIndex() == 0) ? "" : " order by ") +
                ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortCategory.getSelectedItem() + " ") +
                ((cbSortCategory.getSelectedIndex() == 0) ? "" : cbSortWay.getSelectedItem());

        return sortQuery;
    }

    /**
     * Generates a customized select statement based on which table is selected and
     * if the user only wants to see one column at a time.
     * @return Customized select query.
     */
    private String getTblShownQuery(){
        String shownQuery = "select " + ((cbSelection.getSelectedIndex() == 0) ? "*" : cbSelection.getSelectedItem()) +
                " from " + cbTableSelect.getSelectedItem();
        return shownQuery;
    }

    /**
     * Creates a new table model using the "sorted" query.
     */
    private void sortTableModel(){
        setDatabaseTableModel(new DatabaseTableModel(getSortQuery()));
    }

    /**
     * Used to create the File and Help menus at the top of the form.
     */
    private void createMenu() {


        menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenu mnuHelp = new JMenu("Help");
        JMenuItem mnuItemUnitsNCats = new JMenuItem("Units & Categories");
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

        mnuItemUnitsNCats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Help().setVisible(true);
            }
        });

        mnuItemNew.add(mnuItemBOM);
        mnuItemNew.add(mnuItemPart);
        mnuFile.add(mnuItemNew);
        mnuHelp.add(mnuItemUnitsNCats);
        mnuLoad.add(mnuItemLoadAll);
        mnuFile.add(mnuLoad);
        menuBar.add(mnuFile);
        menuBar.add(mnuHelp);
    }
}


