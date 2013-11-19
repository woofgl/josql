package org.josql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.josql.def.TableDef;

public class Runner {

    static public final Object[] EMPTY_PARAMS = new Object[] {};

    Connection                   con;
    DBHelper                     dbHelper;

    Runner(DBHelper dbHelper, Connection con) {
        this.con = con;
        this.dbHelper = dbHelper;
    }

    // --------- Map CRUD Methods --------- //
    /**
     * Return the first record that matches the map
     * @param tableName
     * @param map Name/Value pairs to match
     * @return
     */
    public Map get(String tableName, Map map) {
        TableDef tableDef = dbHelper.getTableDef(tableName);

        StringBuilder sql = new StringBuilder("select * from ").append('"').append(tableDef.getName()).append('"');
        sql.append(" where ");

        List values = new ArrayList();
        boolean first = true;
        for (Object key : map.keySet()) {
            String propName = key.toString();
            if (tableDef.hasColumnName(propName)) {
                if (!first) {
                    sql.append(" and ");
                } else {
                    first = false;
                }
                Object val = map.get(propName);
                sql.append('"').append(propName).append('"').append(" = ?");
                values.add(val);
            }
        }

        sql.append(" limit 1");
        
        List<Map> results = executeQuery(sql.toString(), values.toArray());
        if (results.size() > 0){
            return results.get(0);
        }else{
            return null;
        }
    }

    
    public int update(Class cls,Object id, Map objectMap){
        TableDef tableDef = dbHelper.getTableDef(cls);
        return update(tableDef.getName(),id,objectMap);
    }
    
    /**
     * Update a record in the database with this objectMap 
     * 
     * @param tableName
     * @param id
     * @param objectMap
     * @return
     */
    public int update(String tableName,Object id, Map objectMap){
        TableDef tableDef = dbHelper.getTableDef(tableName);
        
        //"update contact set role_id = ? where id = ?"
        
        StringBuilder sql = new StringBuilder("update ");
        sql.append('"').append(tableDef.getName()).append('"').append(" set ");
        
        List values = new ArrayList();
        boolean first = true;
        for (Object key : objectMap.keySet()){
            String propName = key.toString();
            if (tableDef.hasColumnName(propName)){
                if (!first){
                    sql.append(" , ");
                }else{
                    first = false;
                }
                sql.append('"').append(propName).append('"');
                sql.append(" = ?");
                values.add(objectMap.get(propName));
            }
        }
        
        sql.append(" where ");
        String singleIdColumnName = tableDef.getSingleIdColumnName(); 
        if (singleIdColumnName != null){
            sql.append('"').append(singleIdColumnName).append("\" = ?");
            values.add(id);
        } else{
            throw new RuntimeException("Runner.update does not support table with multiple ids yet (" + tableDef.getName() + ")");
        }
        
        
        return executeUpdate(sql.toString(),values.toArray());
    }
    
    public Object insert(Class cls, Map<String, Object> objectMap) {
        TableDef tableDef = dbHelper.getTableDef(cls);
        return insert(tableDef.getName(),objectMap);        
    }
    
    /**
     * Does a SQL insert given a tableName with the property map. Note that only the properties
     * from the map that have a corresponding column will be taken in account (others will be silently ignored). <br />
     * Also, if the value of an ID property is null, it will be ignored (this is to make the common case of inserting an Object
     * to a row with a serial pk column does not complain)
     *
     * @param tableName
     * @param objectMap
     * @return the ID of the inserted row. (for now, supports only single id)
     */
    public Object insert(String tableName, Map objectMap) {

        TableDef tableDef = dbHelper.getTableDef(tableName);

        StringBuilder sql = new StringBuilder("insert into ");
        sql.append('"').append(tableDef.getName()).append('"').append(" (");
        List values = new ArrayList();

        // insert into team_contact (team_id,contact_id) values (?,?)

        boolean first = true;
        for (Object key : objectMap.keySet()) {
            String propName = key.toString();
            if (tableDef.hasColumnName(propName)) {
                Object value = objectMap.get(propName);
                if (!(value == null && tableDef.isIdColumnName(propName))){
                    if (!first) {
                        sql.append(",");
                    } else {
                        first = !first;
                    }
                    sql.append('"').append(propName).append('"');
                    
                    values.add(value);
                }
            }
        }
        sql.append(") values (");
        for (int i = 0, c = values.size(); i < c; i++) {
            if (i > 0){
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        Object id = executeInsert(sql.toString(), values.toArray());
        return id;
    }

    // --------- /Map CRUD Methods --------- //
    
    // --------- Bean CRUD Methods --------- //
    public <T> T get(Class<T> cls, Map map){
        TableDef tableDef = dbHelper.getTableDef(cls);
        Map resultMap = get(tableDef.getName(),map);
        return dbHelper.toObj(resultMap, cls);
    }
    
    
    public <T> T get(Class<T> cls, Object id){
        TableDef tableDef = dbHelper.getTableDef(cls);
        
        String idName = tableDef.getSingleIdColumnName();
        Map values = new HashMap();
        values.put(idName,id);
        
        Map resultMap = get(tableDef.getName(),values);
        
        return dbHelper.toObj(resultMap, cls);
    }
    
    public int update(Object obj, boolean ignoreNulls){
        TableDef tableDef = dbHelper.getTableDef(obj.getClass());
        Map map = dbHelper.toMap(obj, ignoreNulls);
        String idName = tableDef.getSingleIdColumnName();
        Object id = map.get(idName);
        return update(tableDef.getName(),id,map);
    }    
    
    /**
     * Insert a Java object to the database. Note that for now, the class name must match the target 
     * table name (we will add annotation later). 
     * 
     * @param obj
     * @return the ID of the inserted row.
     */
    public Object insert(Object obj){
        TableDef tableDef = dbHelper.getTableDef(obj.getClass());
        Map map = dbHelper.toMap(obj);
        return insert(tableDef.getName(),map);
    }
    // --------- /Bean CRUD Methods --------- //
    
    // --------- SQL Execute Methods --------- //
    public int executeCount(String sql, Object... values){
        PQuery query = null;
        int r;
        try {
            query = newPQuery(sql);
            r = query.executeCount(values);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return r;        
    }
    
    public Object executeInsert(String sql, Object... values) {
        PQuery query = null;
        Object r;
        try {
            query = newPQuery(sql, true);
            r = query.executeInsert(values);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return r;
    }

    public int executeUpdate(String sql, Object... values) {
        PQuery query = null;
        int r;
        try {
            query = newPQuery(sql);
            r = query.executeUpdate(values);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return r;
    }

    public List<Map> executeQuery(String sql, Object... values) {
        PQuery query = null;
        List<Map> r;
        try {
            query = newPQuery(sql);
            r = query.executeQuery(values);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return r;
    }

    // --------- /SQL Execute Methods --------- //

    // --------- Query Factory --------- //
    public Query newQuery(String sql) {
        return null;
    }

    public PQuery newPQuery(String sql) {
        return newPQuery(sql, false);
    }

    public PQuery newPQuery(String sql, boolean returnKey) {
        try {
            PreparedStatement pstmt;
            if (returnKey) {
                pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                pstmt = con.prepareStatement(sql);
            }

            return new PQuery(dbHelper, pstmt);
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    // --------- /Query Factory --------- //

    public Runner startTransaction() {
        try {
            con.setAutoCommit(false);
            return this;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    public Runner endTransaction() {
        try {
            con.setAutoCommit(true);
            return this;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    public Runner roolback() {
        try {
            con.rollback();
            return this;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    public Runner commit() {
        try {
            con.commit();
            return this;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

    /**
     * Close the runner (i.e. the connection)
     * 
     * @return this runner
     */
    public Runner close() {
        try {
            this.con.close();
            return this;
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }

}
