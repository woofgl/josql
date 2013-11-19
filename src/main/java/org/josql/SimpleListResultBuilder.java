package org.josql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.postgresql.jdbc4.Jdbc4ResultSetMetaData;

public class SimpleListResultBuilder implements ListResultBuilder {

    private ColumnDef[] columnDefs;
    private int         columnCount;
    private boolean     hasSub = false;
    
    private Map<Object,Map> entityById = new HashMap<Object, Map>();
    
    private DBHelper dbHelper; 
    
    
    public void setDBHelper(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }
    
    public void init(ResultSet rs) {
        try {
            Jdbc4ResultSetMetaData rsmd = (Jdbc4ResultSetMetaData) rs.getMetaData();
            columnCount = rsmd.getColumnCount();
            columnDefs = new ColumnDef[columnCount];
            for (int i = 0; i < columnCount; i++) {
                int cidx = i + 1;
                columnDefs[i] = new ColumnDef(rsmd, cidx);
                hasSub = hasSub || columnDefs[i].isSub;
            }
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    public Map each(ResultSet rs) {
        Map r = null;

        try {
            r = new HashMap<String, Object>();
            Map m = null;
            Object idVal = null;
            
            Map<String,Map> itemArrayBySubName = null;
            Map<String,Map> itemBySubName = null;
            
            for (int i = 0; i < columnCount; i++) {
                int cidx = i + 1;
                Object val = rs.getObject(cidx);
                if (val == null){
                    continue;
                }
                val = dbHelper.getJavaVal(val);
                ColumnDef cDef = columnDefs[i];
                String name = cDef.name;
                
                if (hasSub && i == 0){
                    idVal = val;
                    m = entityById.get(idVal);
                    if (m != null){
                        continue;
                    }
                }

                if (cDef.isSub){
                    Map<String,Map> bySubName; 
                    if (cDef.isArray){
                        bySubName = itemArrayBySubName = (itemArrayBySubName == null)?new HashMap<String, Map>():itemArrayBySubName;
                    }else{
                        bySubName = itemBySubName = (itemBySubName == null)?new HashMap<String, Map>():itemBySubName;
                    }
                    
                    Map item = bySubName.get(cDef.subName);
                    if (item == null){
                        item = new HashMap();
                        bySubName.put(cDef.subName, item);
                    }
                    item.put(cDef.name, val);
                }
                // if it is a array Column
                else{
                    // make sure the map was not already populated
                    if (m == null) {
                        r.put(name, val);
                    }
                }

            } // for

            // If we have at least one array, we need to put the result in the entityById for
            // future resulset. 
            if (hasSub){
                Map entity = (m != null)?m:r;
                // add the eventual arrayItem
                if (itemArrayBySubName != null){
                    
                    
                    for (String arrayName : itemArrayBySubName.keySet()){
                        Map item = itemArrayBySubName.get(arrayName);
                        List<Map> items = (List<Map>) entity.get(arrayName);
                        if (items == null){
                            items = new ArrayList<Map>();
                            entity.put(arrayName, items);
                        }
                        items.add(item);
                    }
                }
                
                if (itemBySubName != null){
                    for (String subName : itemBySubName.keySet()){
                        Map item = itemBySubName.get(subName);
                        entity.put(subName,item);
                    }
                }
                
                if (m == null){
                    entityById.put(idVal, r);
                }else{
                    // if we have a m, this mean we do not need to add anything.
                    return null;
                }
            }
            return r;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }

    }

    public void end(ResultSet rs) {

    }
}

class ColumnDef {
    int     cidx;

    String  columnName;
    String  baseColumnName;
    String  baseTableName;

    String  name;
    String  subName;
    boolean isSub = false;
    boolean isArray = false;

    ColumnDef(Jdbc4ResultSetMetaData rsmd, int cidx) {

        try {
            this.cidx = cidx;
            name = columnName = rsmd.getColumnName(cidx);
            baseColumnName = rsmd.getBaseColumnName(cidx);
            baseTableName = rsmd.getBaseTableName(cidx);

            int arrayStrIdx = columnName.indexOf("[].");
            if (arrayStrIdx != -1) {
                isSub = true;
                isArray = true;
                subName = columnName.substring(0, arrayStrIdx);
                name = columnName.substring(arrayStrIdx + 3);
            }else{
                int subIdx = columnName.indexOf(".");
                if (subIdx != -1){
                    isSub = true;
                    subName = columnName.substring(0,subIdx);
                    name = columnName.substring(subIdx + 1);
                }
            }
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    public String toString() {
        return "cidx: " + cidx + " name: " + columnName + "\t\t baseTableName: " + baseTableName +  "\t\t baseColumndName: " + baseColumnName;
    }
}
