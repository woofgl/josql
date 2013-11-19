package org.josql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.josql.def.TableDef;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DBHelper {

    private Class<? extends ListResultBuilder> defaultResultListBuilderClass = SimpleListResultBuilder.class;

    private DataSource                         dataSource;

    private Map<Class, ToDbConverter>          javaToDbConverterByJavaType   = null;

    private Map<Class, ToJavaConverter>        dbToJavaConverterByJavaType   = null;

    private Map<String, TableDef>              tableDefByLowerCaseName       = new HashMap<String, TableDef>();

    private ObjectMapper                       mapper                        = new ObjectMapper();

    DBHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --------- Builder setters --------- //
    void setToDbConverter(Map<Class, ToDbConverter> javaToDbConverterByJavaType) {
        this.javaToDbConverterByJavaType = javaToDbConverterByJavaType;
    }

    void setToJavaConverter(Map<Class, ToJavaConverter> dbToJavaConverterByJavaType) {
        this.dbToJavaConverterByJavaType = dbToJavaConverterByJavaType;
    }

    // --------- /Builder setters --------- //

    // --------- init --------- //
    public void init() {
        dbScan();
    }

    private void dbScan() {
        try {
            Connection con = dataSource.getConnection();
            DatabaseMetaData dmd = con.getMetaData();

            ResultSet rs = dmd.getTables(null, null, null, new String[] { "TABLE" });
            List<Map> results = buildResults(rs);

            for (Map tableMap : results) {
                String tableName = (String) tableMap.get("table_name");
                String lcTableName = tableName.toLowerCase();
                TableDef tableDef = tableDefByLowerCaseName.get(lcTableName);
                if (tableDef == null) {
                    List<Map> ids = buildResults(dmd.getIndexInfo(null, null, tableName, true, false));
                    List<Map> cols = buildResults(dmd.getColumns(null, null, tableName, null));
                    tableMap.put("columns", cols);
                    tableMap.put("ids", ids);
                    tableDef = new TableDef(tableMap);
                    tableDefByLowerCaseName.put(lcTableName, tableDef);
                } else {
                    continue;
                }
            }
            con.close();
        } catch (SQLException e) {
            new RSQLException(e);
        }
    }

    // --------- /init --------- //

    // --------- Mapper --------- //
    /**
     * Transform a Map to a specific class using the bean pattern. Note: for now, we are using Jackson mapper.
     * 
     * @param map
     *            the name/value map (if null, return null)
     * @param cls
     * @return
     */
    public <T> T toObj(Map map, Class<T> cls) {
        if (map == null) {
            return null;
        } else {
            return mapper.convertValue(map, cls);
        }
    }

    /**
     * Transform a object to a Map using the bean pattern. Note: for now, we are using Jackson mapper.
     * 
     * @param obj
     * @return
     */
    public Map toMap(Object obj) {
        return toMap(obj, false);
    }

    public Map toMap(Object obj, boolean ignoreNulls) {
        if (obj == null) {
            return null;
        } else {
            Map map = mapper.convertValue(obj, Map.class);
            if (ignoreNulls) {
                Iterator<Map.Entry> iter = map.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry entry = iter.next();
                    Object val = entry.getValue();
                    if (val == null){
                        iter.remove();
                    }
                }
            }
            return map;
        }
    }

    // --------- /Mapper --------- //

    // --------- Def Methods --------- //
    public TableDef getTableDef(String tableName) {
        if (tableName != null) {
            return tableDefByLowerCaseName.get(tableName.toLowerCase());
        }
        return null;
    }

    public TableDef getTableDef(Class beanClass) {
        // for now, just use the Class name as the table name
        String tableName = beanClass.getSimpleName();
        if (tableName != null) {
            return tableDefByLowerCaseName.get(tableName.toLowerCase());
        }
        return null;
    }

    // --------- /Def Methods --------- //

    public Runner newRunner() {
        return new Runner(this, getConnection());
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    // --------- val --------- //
    public Object getJavaVal(Object val) {
        if (dbToJavaConverterByJavaType != null) {
            ToJavaConverter conv = dbToJavaConverterByJavaType.get(val.getClass());
            if (conv != null) {
                val = conv.toJava(val);
            }
        }
        return val;
    }

    public Object getDbVal(Object val) {
        if (javaToDbConverterByJavaType != null) {
            ToDbConverter conv = javaToDbConverterByJavaType.get(val.getClass());
            if (conv != null) {
                val = conv.toDb(val);
            }
        }
        return val;
    }

    // --------- /val --------- //

    // --------- Query Related Methods --------- //
    PreparedStatement setValues(PreparedStatement pStmt, Object[] vals) {

        try {
            pStmt.clearParameters();
            // return now if no vals
            if (vals == null) {
                return pStmt;
            }
            for (int i = 0; i < vals.length; i++) {
                int cidx = i + 1;
                Object val = vals[i];

                // null seems to work for update/insert, but not for select in the where close (has to use is null)
                if (val == null) {
                    pStmt.setObject(cidx, null);
                } else {
                    val = getDbVal(val);
                    pStmt.setObject(cidx, val);
                }
            }
            return pStmt;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    List<Map> buildResults(ResultSet rs) {
        List<Map> results = new ArrayList<Map>();

        ListResultBuilder rb = null;
        try {
            rb = defaultResultListBuilderClass.newInstance();
            rb.setDBHelper(this);
        } catch (Exception e1) {
            throw new RSQLException(RSQLException.Error.CANNOT_INSTANCIATE_LIST_RESOURCE_BUILDER, e1);
        }

        try {
            rb.init(rs);

            while (rs.next()) {
                Map m = rb.each(rs);
                if (m != null) {
                    results.add(m);
                }
            }

            rb.end(rs);

            // Note: not sure if we should close here.
            // Perhaps, it should responsibility of caller.
            rs.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    // --------- /Query Related Methods --------- //
}