package db;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by robert on 10/11/2015.
 */
public class DatabaseIOTest {
    /**
     * @verifies refNames
     * @see DatabaseIO#getRefConstraintsForTable(String)
     */
    @Test
    public void getRefConstraintsForTable_shouldRefNames() throws Exception {

        DatabaseIO dbio = new DatabaseIO();
        String bom = "BOM";
        String bomr = "R_4";
        String components = "COMPONENTS";
        String compr = "R_8";
        String employees = "EMPLOYEES";
        String empr = "R_22";
        String mats = "MATERIALS";
        String matr = "R_14";
        String plants = "PLANTS";
        String plantsr1 = "R_28";
        String plantsr2 = "R_23";


        Assert.assertEquals(bomr, dbio.getRefConstraintsForTable(bom).get(0));
        Assert.assertEquals(compr, dbio.getRefConstraintsForTable(components).get(0));
        Assert.assertEquals(empr, dbio.getRefConstraintsForTable(employees).get(0));
        Assert.assertEquals(matr, dbio.getRefConstraintsForTable(mats).get(0));
        Assert.assertEquals(plantsr1, dbio.getRefConstraintsForTable(plants).get(0));
        Assert.assertEquals(plantsr2, dbio.getRefConstraintsForTable(plants).get(1));


    }
}
