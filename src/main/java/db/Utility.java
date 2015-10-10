package db;

/**
 * Created by robert on 10/6/2015.
 */
public class Utility {

    public static String ConvertType(int sqlType) {
        switch (sqlType) {
            case -8:
                return "java.lang.Integer";
            case 4:
                return "java.lang.Integer";
            case 0:
                return "java.lang.ref.Null";
            case 2003:
                return "java.lang.reflect.Array";
            case 16:
                return "java.lang.Boolean";
            case 91:
                return "java.lang.String";
            case 12:
                return "java.lang.String";
            case 2:
                return "java.lang.Number";
            case 3:
                return "java.lang.Float";
            case 8:
                return "java.lang.Double";
            default:
                return "java.lang.Object";
        }
    }
}
