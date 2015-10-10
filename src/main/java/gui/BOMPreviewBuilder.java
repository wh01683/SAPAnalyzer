package gui;

import db.DBInfo;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * Created by robert on 10/10/2015.
 */
public class BOMPreviewBuilder {

    private static JTextArea text;
    private static Hashtable<String, ArrayList<Integer>> usedPks;

    public BOMPreviewBuilder(JTextArea textArea){
        text = textArea;
        usedPks = DBInfo.getTabToPkVals();

    }

    public void addBOM(String[] bom){
        if(!usedPks.get("BOM").contains(bom[0])){

        }
    }

    public void addProduct(String[] product){
        if(!usedPks.get("PRODUCTS").contains(product[0])){

        }
    }

    public void addComponent(String[] component){

        if(!usedPks.get("COMPONENTS").contains(component[0])){

        }
    }

    public void addProcess(String[] process){

        if(!usedPks.get("PROCESSES").contains(process[0])){

        }
    }

}
