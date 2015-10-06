import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Created by robert on 9/18/2015.
 */
public class SAPAnalyzer {
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


    private static DatabaseIO dbio;

    public static void main(String[] args){

        try {

            for (String s : createColumnHeadings("products")) {
                System.out.println(s);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updateTable(){

    }

    /**
     * Creates an array of column headers using a tableName
     * @param tableName name of tables to fetch from db
     * @return String[] array of column names.
     * @throws SQLException Cannot return array from exception, but does not affect array content.
     * @should should return exact number and content of column names for a give table.
     */

    private static String[] createColumnHeadings(String tableName) throws SQLException{

            dbio = new DatabaseIO();
            ArrayList<String> quer = new ArrayList<String>();
            quer.add("select * from " + tableName);

            ArrayList<ResultSet> resultSets = dbio.executeQuery(quer);

            int c = 0;
            ArrayList<String> temp = new ArrayList<String>();
            for (ResultSet r : resultSets) {
                for (int i = 1; i < r.getMetaData().getColumnCount()+1; i++) {
                    temp.add(r.getMetaData().getColumnName(i));
                    c=i;
                }
            }
            String[] arr = new String[c];
            int count = 0;
            for (String s : temp) {
                arr[count] = s;
                count++;
            }
            return arr;
        }
    }


