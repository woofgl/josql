package org.josql.test;

import java.util.HashMap;
import java.util.Map;

import org.josql.DBHelper;
import org.josql.DBHelperBuilder;
import org.josql.Runner;
import org.josql.test.app.Contact;
import org.josql.test.app.User;
import org.josql.util.MapUtil;

import static org.josql.util.MapUtil.mapIt;
import static org.junit.Assert.*;

import org.junit.Test;

public class CRUDTests extends JSQLizerTestSupport {

    
    @Test
    public void testMapCRUD(){
       
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        
        Runner runner = dbh.newRunner();
        
        Long id;
        
        Map userMap = new HashMap();
        userMap.put("id",1);
        userMap.put("username", "jsmith");
        userMap.put("other", "welcome");
        id = (Long) runner.insert("user",userMap);
        
        
        // create the contact map
        Map contactMap = new HashMap();
        contactMap.put("id",123);
        contactMap.put("name", "miky");
        contactMap.put("firstName", "Mike");
        contactMap.put("other", "welcome");
        
        // insert
        id = (Long) runner.insert("contact",contactMap);
        
        // get it back. 
        Map contactMap2 = runner.get("contact", mapIt("id",id));
        
        // check
        assertEquals("miky",contactMap2.get("name"));
        assertEquals("Mike",contactMap2.get("firstName"));
        
        // update name
        runner.update("contact", id, mapIt("name","supermiky"));
        
        // make sure it did not insert a new one
        int c = runner.executeCount("select count(id) from contact");
        assertEquals(1,c);
        
        // test the delete
        runner.delete("contact",id);
        contactMap = runner.get("contact", mapIt("id",id));
        assertNull(contactMap);
        
        runner.close();
    }
    
    
    @Test
    public void testContactCRUD(){
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        
        Runner runner = dbh.newRunner();
        
        Contact contact = new Contact();
        contact.setId(123L); // for the test sake, contact is not a auto-inc/serial id
        contact.setName("lucky");
        contact.setFirstName("Luc");
        
        // do the insert
        Long id = (Long) runner.insert(contact);
        // check that can get it back
        contact = runner.get(Contact.class, id);
        assertEquals("lucky",contact.getName());
        
        // get it with the map way
        Map<String,Object> contactMap = runner.get("Contact", mapIt("id",id) );
        assertEquals("Luc",contactMap.get("firstName"));
        
        // test the update with ignoreNulls
        contact.setName("luckyluc");
        contact.setFirstName(null);
        contact.setLastName(null);
        runner.update(contact, true); // ignoreNulls flag set to true
        contact = runner.get(Contact.class, id);
        assertEquals("luckyluc",contact.getName());
        assertEquals("Luc",contact.getFirstName());
        
        // get it by name
        contact = runner.get(Contact.class, mapIt("name","luckyluc"));
        assertEquals(id,contact.getId());
        
        // test the delete(object)
        runner.delete(contact);
        contact = runner.get(Contact.class, mapIt("name","luckyluc"));
        assertNull(contact);
        
        // test the delete(class,id)
        contact = new Contact();
        contact.setName("joe");
        id = 111L;
        contact.setId(id);
        id = (Long) runner.insert(contact);
        assertEquals(111L,id);
        contact = runner.get(Contact.class, id);
        runner.delete(Contact.class,id);
        contact = runner.get(Contact.class, id);
        assertNull(contact);
        
        runner.close();
    }
    
    @Test
    public void userSerialIdCRUDTest(){
        
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        
        Runner runner = dbh.newRunner();
        
        User user = new User();
        user.setUsername("jsmith");
        
        Long id = (Long) runner.insert(user);
        
        user = runner.get(User.class, id);
        assertEquals("jsmith",user.getUsername());
    }
    
}
