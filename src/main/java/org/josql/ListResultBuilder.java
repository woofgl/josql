package org.josql;

import java.sql.ResultSet;
import java.util.Map;

public interface ListResultBuilder {

    public void setDBHelper(DBHelper dbHelper);
    
    public void init(ResultSet rs);
    
    public Map each(ResultSet rs);
    
    public void end(ResultSet rs);
    
}
