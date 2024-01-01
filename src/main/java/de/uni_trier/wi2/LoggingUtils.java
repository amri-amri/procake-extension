package de.uni_trier.wi2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

public class LoggingUtils {

    public static final Logger METHOD_CALL = LoggerFactory.getLogger("method-call");
    public static final Logger DIAGNOSTICS = LoggerFactory.getLogger("diagnostics");

    public static int MAX_LOGGED_STRING_LENGTH = 50000;

    public static String maxSubstring(Object obj){
        if (obj == null) obj = "null";
        return maxSubstring(obj.toString());
    }

    public static String maxSubstring(Object[] objArr){
        return maxSubstring(Arrays.toString(objArr));
    }

    public static String maxSubstring(String str){
        return (str.length() > MAX_LOGGED_STRING_LENGTH ? str.substring(0, MAX_LOGGED_STRING_LENGTH) + "..." : str);
    }

    public static String stringOf(final ResultSet resultSet) throws SQLException {
        if (resultSet == null) return "null";
        if (false) {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            int colCount = rsMetaData.getColumnCount();

            String out = "";
            for (int i = 1; i < colCount; i++) {
                out = out + rsMetaData.getColumnName(i) + " | ";
            }
            out = out + rsMetaData.getColumnName(colCount);

            while (resultSet.next()) {
                String row = "";
                for (int i = 1; i <= colCount; i++) {
                    row += resultSet.getString(i) + " | ";
                }

                out = out + "\n" + row;

            }

            return resultSet.toString();
        }
        return resultSet.toString();
    }

    public static String get2DMatrixString(Object[][] matrix){
        String m = "";
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[i].length ; j++){
                if (matrix[i][j] != null) m = m + matrix[i][j].toString();
                else m = m + "null";
                if (j < matrix[i].length -1) m = m + ", ";
            }
            if (i < matrix.length -1) m = m + "\n";
        }
        return m;
    }

    public static String get2DMatrixString(double[][] matrix){
        String m = "";
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[i].length ; j++){
                m = m + matrix[i][j];
                if (j < matrix[i].length -1) m = m + ", ";
            }
            if (i < matrix.length -1) m = m + "\n";
        }
        return m;
    }
}
