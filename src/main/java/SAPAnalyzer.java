import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Created by robert on 9/18/2015.
 */
public class SAPAnalyzer extends JFrame {
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

    private String[] columnHeadings;
    private static JFrame frame;


    private static DatabaseIO dbio = new DatabaseIO();

    private SAPAnalyzer(){
        this.setContentPane(pnlMain);
        this.setVisible(true);
        pnlTable.setVisible(true);
        tblShownInformation.setVisible(true);

        this.btnFillInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTable("employees");
            }
        });

    }

    public static void main(String[] args){
        frame = new JFrame("SAP Analyzer");
        frame.setContentPane(new SAPAnalyzer().getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
    }


    public void updateTable(String tableName){

        Object[] columnNames = createColumnHeadings(tableName);
        ArrayList<ArrayList<Object>> rows = new ArrayList<ArrayList<Object>>(50);
        Object[][] data;


        ArrayList<String> q = new ArrayList<String>();
        q.add("select * from " + tableName);
        ArrayList<ResultSet> rsset = dbio.executeQuery(q);

        try {
            for (ResultSet rs : rsset) {

                int r = 0;
                if (!rs.next()) {

                } else {
                    do {
                        rows.add(new ArrayList<Object>(10));
                        for(int c = 1; c<rs.getMetaData().getColumnCount()+1; c++) {
                        Class<?> dynamicClass = Class.forName(Utility.ConvertType(rs.getMetaData().getColumnType(c)));
                        Object obj = dynamicClass.cast(rs.getObject(c));
                        rows.get(r).add(obj);
                        }
                        r++;
                    } while (rs.next());
                }
            }
            data = new Object[rows.size()+1][columnNames.length];
            data[0] = columnNames;
            for(int r = 1; r< rows.size()+1; r++){
                for (int c = 0; c < columnNames.length; c++){
                    data[r][c] = rows.get(r-1).get(c);
                }
            }
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
            tblShownInformation.setModel(tableModel);
        }catch (SQLException s){
            s.printStackTrace();
        }catch(ClassNotFoundException c){
            System.out.println("Error encountered when dynamically casting, class not found.");
            c.printStackTrace();
        }
    }

     /**
     * Creates an array of column headers using a tableName
     * @param tableName name of tables to fetch from db
     * @return String[] array of column names.
     * @throws SQLException Cannot return array from exception, but does not affect array content.
     * @should names of table columns
     */

    private static String[] createColumnHeadings(String tableName) {

        try {
            ArrayList<String> quer = new ArrayList<String>();
            quer.add("select * from " + tableName);

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

    }


