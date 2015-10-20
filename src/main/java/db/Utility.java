package db;

import java.lang.reflect.Array;

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

}
