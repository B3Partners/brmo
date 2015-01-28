package nl.b3p.brmo.loader.util;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.commons.dbutils.BeanProcessor;

/**
 * Custom Bean Processor used to map staging columns. Some columns contain 
 * underscores which BasicRowProcessor does not map.
 * 
 * @author Boy de Wit
 */
public class StagingBeanProcessor extends BeanProcessor {
    
    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd,
            PropertyDescriptor[] props) throws SQLException {
        
        int[] arr = new int[props.length];
        arr[0] = -1; // meaningless
        
        int idx = 1;
        for (PropertyDescriptor descr : props) {            
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String name = rsmd.getColumnName(i);
                String testName = name.toLowerCase().replaceAll("_", "");                
                
                if (descr.getName().toLowerCase().equals(testName)) {
                    arr[idx] = i;
                    idx++;
                    
                    break;
                }
            }
        }
        
        return arr;
    }    
}
