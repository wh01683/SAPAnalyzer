/*
package db;

import org.junit.Assert;
import org.junit.Test;

*/
/**
 * Created by robert on 10/11/2015.
 *//*

public class DBIOTest {

    */
/**
     * @verifies getKeys
     * @see DBIO#getKeys(String, String...)
 *//*

    @Test
    public void getKeys_shouldGetKeys() throws Exception {

        String[] tables = {"EMPLOYEES", "MANAGERS", "COMPANIES", "PLANTS", "UNITS", "PARTCATEGORY", "PART", "STOCKDETAIL", "PART_SUPPLIER"
                , "BOM", "STAFF"};
        String[] knownKeys = {"EMPLOYEEID", "MANAGERID", "COMPANYID", "PLANTID", "UNITCODE", "CATNAME", "PARTID", "PARTID", "PARTID", "PARENTPARTID", "EMPLOYEEID"};
        String[] types = {"P", "F"};
        DBIO.instantiate();
        Assert.assertEquals(DBIO.getKeys("EMPLOYEES", "P").get(0), "EMPLOYEEID");
        Assert.assertEquals(DBIO.getKeys("PART", "P").get(0), "PARTID");
        Assert.assertEquals(DBIO.getKeys("STAFF", "P").get(0), "EMPLOYEEID");

        int count = 0;
        for (String s : tables) {
            Assert.assertEquals(DBIO.getKeys(s, "P").get(0), knownKeys[count]);
            count++;
        }


    }
}
*/
