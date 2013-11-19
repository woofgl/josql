package org.josql.def;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class TableDef {
    
    private String name;
    
    private Map<String,ColumnDef> colDefByName = new HashMap<String, ColumnDef>();
    
    // will be set only if there are more than one id column for this table
    private Set<String> idColumnNames = null;
    
    // will be set only if there is a single id column for this table. 
    private String singleIdColumnName = null;
    
    public TableDef(Map tableMap){
        this.name = (String) tableMap.get("table_name");
        List<Map> columnMapList = (List<Map>) tableMap.get("columns"); 
        for (Map col : columnMapList){
            ColumnDef colDef = new ColumnDef(col);
            colDefByName.put(colDef.getName(), colDef);
        }
        
        List<Map> ids = (List<Map>) tableMap.get("ids");
        if (ids.size() == 1){
            singleIdColumnName = (String) ids.get(0).get("column_name");
        }else{
            String[] idNames = new String[ids.size()];
            for (int i = 0, c = ids.size(); i < c ; i++){
                Map columnMap = ids.get(i);
                String columnName = (String) columnMap.get("column_name");
                idNames[i] = columnName;
            }            
            idColumnNames = ImmutableSet.copyOf(idNames);
        }
    }
    
    public String getName(){
        return name;
    }
    
    
    public String getSingleIdColumnName(){
        return singleIdColumnName;
    }
    
    public boolean isIdColumnName(String colName){
        if (colName != null && singleIdColumnName != null){
            return colName.equals(singleIdColumnName);
        }else{
            return false;
        }
    }
    
    public Set<String> getIdColumnNames(){
        return idColumnNames;
    }
    
    public boolean hasColumnName(String columnName){
        return colDefByName.containsKey(columnName);
    }
    
    public Set<String> getColumnNames(){
        return colDefByName.keySet();
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder(name);
        
        sb.append(" [");
        boolean f = true;
        for (String name : colDefByName.keySet()){
            ColumnDef colDef = colDefByName.get(name);
            if (!f){
                sb.append(",");
            }else{
                f = false;
            }
            sb.append(colDef.getName());
        }
        sb.append("]");
        
        return sb.toString();
    }

}
