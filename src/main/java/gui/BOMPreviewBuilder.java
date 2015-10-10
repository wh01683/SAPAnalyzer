package gui;

import db.DBInfo;
import db.DatabaseIO;

import javax.swing.*;
import java.util.*;

/**
 * Created by robert on 10/10/2015.
 */
public class BOMPreviewBuilder {

    private static JTextArea text;
    private static Hashtable<String, ArrayList<Integer>> usedPks;
    private static String disableConstraints;
    private Hashtable<String, ArrayList<String>> tabsToRefConstraints = new Hashtable<String, ArrayList<String>>(10);
    private DatabaseIO databaseIO = new DatabaseIO();

    private ArrayList<String> insertBoms = new ArrayList<String>(10);
    private ArrayList<String> insertProds = new ArrayList<String>(10);
    private ArrayList<String> insertComps = new ArrayList<String>(10);
    private ArrayList<String> insertProcs = new ArrayList<String>(10);

    public BOMPreviewBuilder(JTextArea textArea){
        text = textArea;
        usedPks = DBInfo.getTabToPkVals();

        String[] tabs = {"BOM", "PRODUCTS", "COMPONENTS", "PROCESSES"};

        for(String s : tabs){
            tabsToRefConstraints.put(s, databaseIO.getRefConstraintsForTable(s));
        }
    }



    public void addBOMProduct(String[] bom, String[] product){
        if(!usedPks.get("PRODUCTS").contains(product[0]) && !usedPks.get("BOM").contains(bom[0])){

            StringBuilder builder = new StringBuilder();
            Formatter formatter = new Formatter(builder, Locale.US);
            formatter.format("%-20s %-20s %-20s %-20s%n", "BOMID", "Name", "Plant ID", "Company ID");
            formatter.format("%-20s %-20s %-20s %-20s%n", bom[0], bom[1], bom[2], bom[3]);
            formatter.format("%n%-20s %-20s %-20s%n", "Product ID", "Name", "Cost");
            formatter.format("%-20s %-20s %-20s%n", product[0], product[1], product[2]);
            text.append(builder.toString());
            insertProcs.add("insert into BOM (bomid, plantid, cost, item, description, hourest, companyid) VALUES(" + bom[0] + "," + bom[1] + "," +
                    bom[2] + "," + bom[3] + ")");

            insertProcs.add("insert into products (productid, name, price, bomid) VALUES(" + product[0] + "," + product[1]+ "," +
                    product[2]+ "," +product[3] + ")");
        } else {
            JOptionPane.showMessageDialog(null, "Primary key already taken.");
        }
    }

    public void addComponent(String[] component){

        if(!usedPks.get("COMPONENTS").contains(component[0])){
            StringBuilder builder = new StringBuilder();
            Formatter formatter = new Formatter(builder, Locale.US);
            formatter.format("%n%-20s %-20s %-40s%n", "Component ID", "Name", "Description");
            formatter.format("%-20s %-20s %-40s%n", component[0], component[1], component[2]);
            text.append(builder.toString());
            insertProcs.add("insert into COMPONENTS (componentid, productid, bomid, name, descript) VALUES(" + component[0] + "," + component[1]+ "," +
                    component[2]+ "," +component[3] + ")");
        }
    }

    public void addProcess(String[] process){

        if(!usedPks.get("PROCESSES").contains(process[0])){

            StringBuilder builder = new StringBuilder();
            Formatter formatter = new Formatter(builder, Locale.US);
            formatter.format("%n%-20s %-40s %-20s %-20s %n", "Process ID", "Description", "Hourly Cost", "Time Est.");
            formatter.format("%-20s %-40s %-20s %-20s %n", process[0], process[1], process[2], process[3]);
            text.append(builder.toString());
            insertProcs.add("insert into PROCESSES (processid, componentid, description, hourlycost, hourest) VALUES(" + process[0] + "," + process[1]+ "," +
            process[2]+ "," +process[3] + ")");
        }
    }

    public void alterConstraints(String tableName, boolean enable){
        Enumeration enumeration = tabsToRefConstraints.keys();

        while(enumeration.hasMoreElements()){
            String tab = (String)enumeration.nextElement();
            for(int i = 0; i < tabsToRefConstraints.get(tab).size(); i++){
                String query = "alter table " + tab + " " + ((enable)? "enable" : "disable") +" constraint " + tabsToRefConstraints.get(tab).get(i);
                databaseIO.executeQuery(query);
            }
        }
    }





}
