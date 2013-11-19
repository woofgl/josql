package org.josql;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public class DBHelperBuilder {

    private Map<Class,ToDbConverter> toDbConverterByJavaType = null; 
    
    private Map<Class,ToJavaConverter> toJavaConverterByJavaType = null; 
    
    public DBHelperBuilder(){
        // adding the Date converter (right now, convert to java.sql.Timestamp
        addToDbConverter(Date.class, new ToDbConverter<Date>() {
            @Override
            public Object toDb(Date javaVal) {
                java.sql.Timestamp dbVal = new java.sql.Timestamp(((Date)javaVal).getTime());
                return dbVal;
            }
        });
    }
    
    public DBHelper newDBHelper(DataSource dataSource){
        DBHelper dbHelper = new DBHelper(dataSource);
        
        dbHelper.setToDbConverter(toDbConverterByJavaType);
        dbHelper.setToJavaConverter(toJavaConverterByJavaType);
        dbHelper.init();
        return dbHelper;
    }
    
    
    public <T> DBHelperBuilder addToJavaConverter(Class<T> javaType,ToJavaConverter<T> dbToJavaConverter){
        if (toJavaConverterByJavaType == null){
            toJavaConverterByJavaType = new HashMap<Class, ToJavaConverter>();
        }
        toJavaConverterByJavaType.put(javaType,dbToJavaConverter);
        return this;
    }    
    
    public <T> DBHelperBuilder addToDbConverter(Class<T> javaType,ToDbConverter<T> javaToDbConverter){
        if (toDbConverterByJavaType == null){
            toDbConverterByJavaType = new HashMap<Class, ToDbConverter>();
        }
        toDbConverterByJavaType.put(javaType,javaToDbConverter);
        return this;
    }
}
