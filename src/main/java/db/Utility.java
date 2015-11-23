package db;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by robert on 10/6/2015.
 */
public class Utility {

    public static Class<?> ConvertType(int sqlType) {
        switch (sqlType) {
            case -8:
                return Integer.class;
            case 4:
                return Integer.class;
            case 0:
                return Object.class;
            case 2003:
                return Array.class;
            case 16:
                return Boolean.class;
            case 91:
                return String.class;
            case 12:
                return String.class;
            case 2:
                return Number.class;
            case 3:
                return Float.class;
            case 8:
                return Double.class;
            case 1:
                return String.class;
            default:
                return Object.class;
        }
    }

    /**
     * Flips a hashtable mapping Objects (primary keys) to Strings (names or otherwise) and returns a resultant hashtable
     * mapping the Strings to Objects.
     *
     * @param pkToName Hashtable to flip (Object, String)
     * @return Flipped hashtable (String, Object)
     */
    public static Hashtable<String, Object> flipPkHash(Hashtable<Object, String> pkToName) {
        Enumeration e = pkToName.keys();
        Hashtable<String, Object> resultHash = new Hashtable<String, Object>(pkToName.size());
        while (e.hasMoreElements()) {
            Object next = e.nextElement();
            resultHash.put(pkToName.get(next), next);
        }
        return resultHash;
    }

}
