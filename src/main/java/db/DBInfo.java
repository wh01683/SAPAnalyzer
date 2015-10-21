package db;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by robert on 10/10/2015.
 */
public class DBInfo extends JPanel
        implements PropertyChangeListener {

    private static Hashtable<String, String> tabToPKHash = new Hashtable<String, String>(10);
    private static Hashtable<String, ArrayList<String>> tabToRefTabHash = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToColNames = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToForeignKeyNames = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToRefConstraint = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<Object>> tabToPkVals = new Hashtable<String, ArrayList<Object>>(10);
    private static ArrayList<String> unitOfMeasure = new ArrayList<String>(10);
    private static ArrayList<String> partCategories = new ArrayList<String>(10);
    private static ArrayList<String> matCategories = new ArrayList<String>(10);
    private static ArrayList<String> suppliers = new ArrayList<String>(10);

    private static JTextArea taskOutput = new JTextArea("Loading info from database.");
    private JProgressBar progressBar;
    private static LoadTask task;

    class LoadTask extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                DBIO.updateTableNames();
                int progress = 0;
                int tabCount = 1;
                setProgress(0);
                int max = DBIO.getTableNames().size();
                taskOutput.append("Getting units of measure...\n");
                unitOfMeasure = DBIO.getStringResults("select unitcode from units");
                taskOutput.append("Getting part categories...\n");
                partCategories = DBIO.getStringResults("select catname from partcategory");
                taskOutput.append("Getting suppliers...\n");
                suppliers = DBIO.getStringResults("select name from supplier");

                //<editor-fold desc="Load Loop">
                for (String table : DBIO.getTableNames()) {
                    taskOutput.append("\n===========================================================================\n");
                    taskOutput.append("Found table " + table + "\n");
                    taskOutput.append("Processing table: " + table + ". Number " + tabCount + " of " + max + "\n");
                    String pkColName = DBIO.getKeys(table, "P").get(0);
                    taskOutput.append("Found primary key column " + pkColName + " for " + table + ".\n");
                    tabToPKHash.put(table, pkColName);
                    taskOutput.append("Obtaining tables referring to " + table + ".\n");
                    ArrayList<String> tempTabs = DBIO.getReferringTables(table);
                    for (String s : tempTabs) {
                        taskOutput.append("Found table " + s + " referencing " + table + ".\n");
                    }
                    tabToRefTabHash.put(table, tempTabs);
                    taskOutput.append("Obtaining columns belonging to " + table + ".\n");
                    ArrayList<String> colNames = DBIO.getColNames("select * from " + table);
                    for (String s : colNames) {
                        taskOutput.append("Found column " + s + " in " + table + ".\n");
                    }
                    tabToColNames.put(table, colNames);
                    ArrayList<String> fkNames = DBIO.getKeys(table, "R");
                    for (String s : colNames) {
                        taskOutput.append("Found foreign key " + s + " in " + table + ".\n");
                    }
                    tabToForeignKeyNames.put(table, fkNames);

                    ArrayList<String> refCon = DBIO.getRefConstraints(table);
                    for (String s : refCon) {
                        taskOutput.append("Found reference constraint " + s + " in " + table + ".\n");
                    }
                    tabToRefConstraint.put(table, refCon);


                    ArrayList<Object> pkVals = DBIO.getPrimaryKeyValues(table);

                    for (Object i : pkVals) {
                        if (i != null) {
                            taskOutput.append("Found primary key " + i + " in " + table + ".\n");
                        }
                    }

                    tabToPkVals.put(table, DBIO.getPrimaryKeyValues(table));
                    progress += (Math.ceil(100 / (max)));
                    tabCount++;
                    setProgress(Math.min(progress, 100));
                }
                //</editor-fold>

                setProgress(100);
            } catch (SQLException s) {
                System.out.printf("Error Code: %d", s.getErrorCode());
                s.printStackTrace();
            }
            return null;
        }

        public void done() {
            Toolkit.getDefaultToolkit().beep();
            taskOutput.append("Done!\n");
            setCursor(null);

        }
    }

    public DBInfo() {
        super(new BorderLayout());

        //Create the demo's UI.

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(10, 50);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        taskOutput.setLineWrap(false);
        taskOutput.setWrapStyleWord(false);

        JPanel panel = new JPanel();
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        task = new LoadTask();
        task.addPropertyChangeListener(this);
        task.execute();

    }


    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        }

    }

    public static void showLoadScreen() {

        JFrame frame = new JFrame("DB Info Loading");

        //Create and set up the content pane.
        JComponent newContentPane = new DBInfo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

    }

    public static Hashtable<String, String> getTabToPKHash() {
        return tabToPKHash;
    }

    public static Hashtable<String, ArrayList<String>> getTabToRefTabHash() {
        return tabToRefTabHash;
    }

    public static Hashtable<String, ArrayList<String>> getTabToColNames() {
        return tabToColNames;
    }

    public static Hashtable<String, ArrayList<Object>> getTabToPkVals() {
        return tabToPkVals;
    }

    public static Hashtable<String, ArrayList<String>> getTabToForeignKeyNames() {
        return tabToForeignKeyNames;
    }

    public static int[] getColTypes(String tableName) {
        return DBIO.getColumnTypes(tableName);
    }

    public static ArrayList<String> getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public static ArrayList<String> getPartCategories() {
        return partCategories;
    }

    public static ArrayList<String> getMatCategories() {
        return matCategories;
    }

    public static ArrayList<String> getSuppliers() {
        return suppliers;
    }

    public static Hashtable<String, ArrayList<String>> getTabToRefConstraint() {
        return tabToRefConstraint;
    }

    public static String dbToString() {
        StringBuilder db = new StringBuilder("DB\n===================================\n");

        for (String table : DBIO.getTableNames()) {
            db.append("Table: ").append(table).append("\n");
            int colCount = 0;
            Class[] colTypes = DBIO.getColClasses("select * from " + table);
            for (String columnName : DBIO.getColNames("select * from " + table)) {
                db.append("____").append(colCount).append("_").append(columnName).append("_ Type:_").append(colTypes[colCount]).append("\n");
                colCount++;
            }
            db.append("\n");
        }
        return db.toString();
    }
}
