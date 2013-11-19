package org.josql.def;

import java.util.Map;

public class ColumnDef {

    private String name;
    
    public ColumnDef(Map<String,String> columnMap){
        name = columnMap.get("COLUMN_NAME");
    }
    
    public String getName(){
        return name;
    }
    
    public String toString(){
        return name;
    }
    
}
