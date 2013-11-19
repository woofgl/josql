package org.josql.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.josql.DBHelper;
import org.josql.DBHelperBuilder;
import org.josql.test.app.Contact;
import org.junit.Test;

public class DBHelperMapperTest  extends JSQLizerTestSupport {

    
    @Test
    public void testToMapIgnoreNulls(){
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        
        Contact contact = new Contact();
        contact.setName("luckyluc");
        contact.setFirstName(null);
        contact.setLastName(null);
        
        
        Map map = dbh.toMap(contact,true);
        
        assertEquals("luckyluc",map.get("name"));
        assertEquals(null,map.get("firstName"));
        
    }
}
