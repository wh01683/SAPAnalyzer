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

    private static Hashtable<Object, String> partPkToName = new Hashtable<Object, String>(10);
    private static Hashtable<Object, String> empPkToName = new Hashtable<Object, String>(10);
    private static Hashtable<String, int[]> tabToColTypes = new Hashtable<String, int[]>(10);
    private static Hashtable<Object, String> suppPkToName = new Hashtable<Object, String>(10);

    private static ArrayList<String> unitOfMeasure = new ArrayList<String>(10);
    private static ArrayList<String> partCategories = new ArrayList<String>(10);
    private static ArrayList<String> matCategories = new ArrayList<String>(10);
    private static ArrayList<String> suppliers = new ArrayList<String>(10);

    private static JTextArea taskOutput = new JTextArea("Loading info from database.");
    private JProgressBar progressBar;
    private static LoadTask task;
    private static int tabCount = 1;
    private static int max;

    class LoadTask extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                DBIO.updateTableNames();
                int progress = 0;
                setProgress(0);
                max = DBIO.getTableNames().size();
                taskOutput.append("Getting units of measure...\n");
                unitOfMeasure = DBIO.getStringResults("select unitcode from units");
                taskOutput.append("Getting part categories...\n");
                partCategories = DBIO.getStringResults("select catname from partcategory");
                taskOutput.append("Getting suppliers...\n");
                suppliers = DBIO.getStringResults("select name from supplier");

                //<editor-fold desc="Load Loop">
                /**
                 * Calls special load methods. In general, methods will fill local array lists and hashtables with information
                 * from the table. This process cycles for all tables.
                 */
                for (String table : DBIO.getTableNames()) {

                    taskOutput.append("\n===========================================================================\n");
                    taskOutput.append("Found table " + table + "\n");
                    taskOutput.append("Processing table: " + table + ". Number " + tabCount + " of " + max + "\n");

                    loadPks(table);
                    loadRefTables(table);
                    loadCols(table);
                    loadFks(table);
                    loadConstraints(table);
                    loadPkVals(table);
                    loadColTypes(table);

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

        //<editor-fold desc="Load Methods">

        /**
         * TODO: Refactor to handle more than one primary key.
         *
         * Loads primary key column name for the given table.
         * @param table Table to obtain primary key of.
         */
        public void loadPks(String table) {

            String pkColName = DBIO.getKeys(table, "P").get(0);
            taskOutput.append("Found primary key column " + pkColName + " for " + table + ".\n");
            tabToPKHash.put(table, pkColName);
        }

        /**
         * Obtains all tables referring to another using foreign keys.
         * @param table Table referred to.
         */
        public void loadRefTables(String table) {
            taskOutput.append("Obtaining tables referring to " + table + ".\n");
            ArrayList<String> tempTabs = DBIO.getReferringTables(table);
            for (String s : tempTabs) {
                taskOutput.append("Found table " + s + " referencing " + table + ".\n");
            }
            tabToRefTabHash.put(table, tempTabs);
        }

        /**
         * Loads all column names for the given table.
         * @param table Table to obtain column names for.
         */
        public void loadCols(String table) {
            taskOutput.append("Obtaining columns belonging to " + table + ".\n");
            ArrayList<String> colNames = DBIO.getColNames("select * from " + table);
            for (String s : colNames) {
                taskOutput.append("Found column " + s + " in " + table + ".\n");
            }
            tabToColNames.put(table, colNames);
        }

        /**
         * Loads all foreign key column names for a given table.
         * @param table Table to load foreign keys for.
         */
        public void loadFks(String table) {
            ArrayList<String> fkNames = DBIO.getKeys(table, "R");
            for (String s : fkNames) {
                taskOutput.append("Found foreign key " + s + " in " + table + ".\n");
            }
            tabToForeignKeyNames.put(table, fkNames);
        }

        /**
         * Load reference constraints for the given table.
         * @param table Owner of the constraints.
         */
        public void loadConstraints(String table) {
            ArrayList<String> refCon = DBIO.getRefConstraints(table);
            for (String s : refCon) {
                taskOutput.append("Found reference constraint " + s + " in " + table + ".\n");
            }
            tabToRefConstraint.put(table, refCon);
        }

        /**
         * Loads all primary key values for a given table. This method also contains some table specific code snippets.
         * @param table Table to load key values for.
         */
        public void loadPkVals(String table) {
            ArrayList<Object> pkVals = DBIO.getPrimaryKeyValues(table);

            //also loads part pk to names

            if (table.equalsIgnoreCase("PART")) {
                ArrayList<ArrayList<Object>> partNames = DBIO.getMultiObResults("select name from part");
                int rowCount = 0;
                for (Object i : pkVals) {
                    if (i != null) {
                        taskOutput.append("Found primary key " + i + " in " + table + ".\n");
                        partPkToName.put(i, partNames.get(rowCount).get(0).toString());
                    }
                    rowCount++;
                }
            } else if (table.equalsIgnoreCase("EMPLOYEES")) {
                ArrayList<ArrayList<Object>> lastNames = DBIO.getMultiObResults("select lastname from employees");
                ArrayList<ArrayList<Object>> firstNames = DBIO.getMultiObResults("select firstname from employees");
                int rowCount = 0;
                for (Object i : pkVals) {
                    if (i != null) {
                        taskOutput.append("Found primary key " + i + " in " + table + ".\n");
                        //puts names in hash table in Lastname, Firstname format.
                        empPkToName.put(i, lastNames.get(rowCount).get(0).toString().concat(",")
                                .concat(firstNames.get(rowCount).get(0).toString()));
                    }
                    rowCount++;
                }
            } else if (table.equalsIgnoreCase("SUPPLIER")) {
                ArrayList<ArrayList<Object>> suppNames = DBIO.getMultiObResults("select name from supplier");
                int rowCount = 0;
                for (Object i : pkVals) {
                    if (i != null) {
                        taskOutput.append("Found primary key " + i + " in " + table + ".\n");
                        suppPkToName.put(i, suppNames.get(rowCount).get(0).toString());
                    }
                    rowCount++;
                }
            }

            for (Object i : pkVals) {
                if (i != null) {
                    taskOutput.append("Found primary key " + i + " in " + table + ".\n");
                }
            }

            tabToPkVals.put(table, pkVals);
        }

        /**
         * Loads SQL integer column types for each column in the table.
         * @param table Table to load column types for.
         */
        public void loadColTypes(String table) {
            int[] colTypes = DBIO.getColumnTypes("select * from " + table);
            int colCount = 0;
            for (int i : colTypes) {
                if (i != 0) {
                    taskOutput.append("Found column type " + i + " for column " + tabToColNames.get(table).get(colCount) + " in " + table + ".\n");
                    colCount++;
                }
            }
            tabToColTypes.put(table, colTypes);
        }
        //</editor-fold>


        /**
         * When done, jump here.
         */
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            taskOutput.append("Done!\n");
            setCursor(null);

        }
    }

    /**
     * Constructor for DBInfo class. On construction, load will commence.
     */
    public DBInfo() {
        super(new BorderLayout());

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


    /**
     * Function for updating progress bar.
     * @param evt PropertyChangeEvent of event being watched.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        }

    }

    /**
     * Method to show loading screen to the user.
     */
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

    //<editor-fold desc="Getters">
    public static Hashtable<String, String> getTabToPKHash() {
        return tabToPKHash;
    }

    public static Hashtable<String, int[]> getTabToColTypes() {
        return tabToColTypes;
    }

    public static Hashtable<Object, String> getPartPkToName() {
        return partPkToName;
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
        return tabToColTypes.get(tableName);
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

    public static Hashtable<Object, String> getSuppPkToName() {
        return suppPkToName;
    }

    public static Hashtable<String, ArrayList<String>> getTabToRefConstraint() {
        return tabToRefConstraint;
    }

    public static Hashtable<Object, String> getEmpPkToName() {
        return empPkToName;
    }

    //</editor-fold>

    /**
     * Deprecated function used to print the entire database to string.
     * @return A single String containing the entire database.
     */
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
