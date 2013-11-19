package org.josql;

import java.sql.SQLException;
import java.sql.Statement;

public class Query {

    Statement stmt;
    protected DBHelper dbHelper;
    protected boolean returnsKey;
    
    Query(DBHelper dbHelper,Statement stmt, boolean returnsKey){
        this.dbHelper = dbHelper;
        this.stmt = stmt;
        this.returnsKey = returnsKey;
    }
    
    public void close(){
        try {
            stmt.close();
        } catch (SQLException e) {
            throw new RSQLException(e);
        }
    }
    
}
