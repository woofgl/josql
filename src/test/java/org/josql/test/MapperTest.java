package org.josql.test;

import java.util.HashMap;
import java.util.Map;

import org.josql.test.app.Contact;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


public class MapperTest {

    @Test
    public void testJacksonMapper() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        
        Contact c = new Contact();
        c.setId(11l);
        c.setName("jen");
        
        System.out.println("c: " + mapper.convertValue(c, Map.class));
        
        
        Map contactMap = new HashMap();

        contactMap.put("id", 12l);
        contactMap.put("name", "Mike");

        Contact contact = mapper.convertValue(contactMap, Contact.class);
        
        System.out.println("contact: id=" + contact.getId() + " name=" + contact.getName());

    }
}
