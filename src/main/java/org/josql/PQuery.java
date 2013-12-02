package org.josql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PQuery extends Query {

    private PreparedStatement pstmt;

    PQuery(DBHelper dbHelper, PreparedStatement pstmt) {
        super(dbHelper, pstmt,false);
        this.pstmt = pstmt;
    }
    
    PQuery(DBHelper dbHelper, PreparedStatement pstmt,boolean returnsKey) {
        super(dbHelper,pstmt,returnsKey);
        this.pstmt = pstmt;
    }    

    public PQuery setValues(Object... values){
        dbHelper.setValues(pstmt, values);
        return this;
    }
    
    public int executeCount(Object... values){
        int r = 0;
        ResultSet rs;
        setValues(values);
        
        try {
            rs = pstmt.executeQuery();
            if (rs.next()){
                r = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RSQLException(e);
        }        
        
        return r;
        
    }
    
    public int executeUpdate(Object... values) {
        int r;
        setValues(values);
        try {
            r = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
        return r;
    }
    
    public Object executeInsert(Object... values){
        Object r;
        setValues(values);
        try {
            r = pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                r = generatedKeys.getObject(1);
            } 
            return r;
        } catch (SQLException e){
            throw new RSQLException(e);
        }
    }
    
    
    public List<Map> executeQuery(Object... values) {
        ResultSet rs;
        setValues(values);
        try {
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
        
        List<Map> result = dbHelper.buildResults(rs);
        return result;
    }

}
