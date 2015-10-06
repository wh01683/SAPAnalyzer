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


    }

    public void updateTable(){

    }

    private String[] createColumnHeadings(String tableName) throws SQLException{

            dbio = new DatabaseIO();
            ArrayList<String> quer = new ArrayList<String>();
            quer.add("select * from " + tableName);

            ArrayList<ResultSet> resultSets = dbio.executeQuery(quer);

            int c = 0;
            ArrayList<String> temp = new ArrayList<String>();
            for (ResultSet r : resultSets) {

                do {
                    c++;
                    temp.add(r.getMetaData().getColumnName(c));
                } while (r.next());
                c = 0;
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


