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

    static DatabaseIO dbio = new DatabaseIO();
    private static Hashtable<String, String> tabToPKHash = new Hashtable<String, String>(10);
    private static Hashtable<String, ArrayList<String>> tabToRefTabHash = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToColNames = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<String>> tabToForeignKeyNames = new Hashtable<String, ArrayList<String>>(10);
    private static Hashtable<String, ArrayList<Integer>> tabToPkVals = new Hashtable<String, ArrayList<Integer>>(10);
    private static JTextArea taskOutput = new JTextArea("Loading info from database.");
    private JProgressBar progressBar;
    private static LoadTask task;

    class LoadTask extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dbio.updateTableNames();
                int count = 0;
                setProgress(0);
                int max = dbio.getTableNames().size();
                for (String tables : dbio.getTableNames()) {

                    System.out.printf("Count :%d ", count);
                    taskOutput.append("Found table " + tables + "\n");
                    taskOutput.append("Processing table: " + tables + ". Number " + count + " of " + max + "\n");
                    String pkColName = dbio.getTablePrimaryKey(tables);
                    taskOutput.append("Found primary key column " + pkColName + " for " + tables + ".\n");
                    tabToPKHash.put(tables, pkColName);
                    taskOutput.append("Obtaining tables referring to " + tables + ".\n");
                    ArrayList<String> tempTabs = dbio.getReferringTables(tables);
                    for (String s : tempTabs) {
                        taskOutput.append("Found table " + s + " referencing " + tables + ".\n");
                    }
                    tabToRefTabHash.put(tables, tempTabs);
                    taskOutput.append("Obtaining columns belonging to " + tables + ".\n");
                    ArrayList<String> colNames = dbio.getColNames(tables);
                    for (String s : colNames) {
                        taskOutput.append("Found column " + s + " in " + tables + ".\n");
                    }
                    tabToColNames.put(tables, colNames);
                    ArrayList<String> fkNames = dbio.getTableForeignKey(tables);
                    for (String s : colNames) {
                        taskOutput.append("Found foreign key " + s + " in " + tables + ".\n");
                    }
                    tabToForeignKeyNames.put(tables, fkNames);

                    ArrayList<Integer> pkVals = dbio.getPksFromTable(tables);
                    for (Integer i : pkVals) {
                        taskOutput.append("Found primary key " + i + " in " + tables + ".\n");
                    }
                    tabToPkVals.put(tables, dbio.getPksFromTable(tables));
                    count += (Math.ceil(100 / (max)));
                    setProgress(Math.min(count, 100));
                }
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
                    "Completed %d of task.\n", task.getProgress()));
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

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.

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

    public static Hashtable<String, ArrayList<Integer>> getTabToPkVals() {
        return tabToPkVals;
    }

    public static Hashtable<String, ArrayList<String>> getTabToForeignKeyNames() {
        return tabToForeignKeyNames;
    }

    public static int[] getColTypes(String tableName) {
        return dbio.getColumnTypes(tableName);
    }
}
