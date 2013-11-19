package org.josql.test;

import java.util.Properties;

import javax.sql.DataSource;

import org.josql.DBHelper;
import org.josql.DBHelperBuilder;
import org.josql.Runner;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JSQLizerTestSupport {
    static private String URL = "jdbc:postgresql://localhost:5432/josql_test_db"; // ?searchpath=test (not implemented in driver 9.1 yet)
    static private String USER = "josql_test_user";
    static private String PWD = "welcome";
    
    static protected DataSource dataSource; 
    
    @BeforeClass
    static public void initDataSource(){
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); 
        System.setProperties(p);
        
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl(URL);
        cpds.setUser(USER);
        cpds.setPassword(PWD);
        cpds.setUnreturnedConnectionTimeout(0);
        
        dataSource = cpds;
    }
    
    @Before
    public void before(){
        cleanTables();
    }
    
    static protected void cleanTables(){
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        
        // dbh.newRunner().executeUpdate("delete from contact").close().updateResult();
        
        Runner runner = dbh.newRunner();
        runner.executeUpdate("delete from contact");
        runner.executeUpdate("delete from team");
        runner.executeUpdate("delete from team_contact");
        runner.executeUpdate("delete from role");
        runner.executeUpdate("delete from \"user\"");
        runner.close();
    }
        
}
