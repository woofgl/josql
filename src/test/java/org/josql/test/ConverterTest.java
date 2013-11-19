package org.josql.test;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.josql.DBHelper;
import org.josql.DBHelperBuilder;
import org.josql.Runner;
import org.josql.ToJavaConverter;
import org.junit.Test;


/* Assume SQL
 * create user jsqlizer_test_user with password 'welcome';
 * create database jsqlizer_test_db with owner jsqlizer_test_user;
 */
public class ConverterTest extends JSQLizerTestSupport {

    @Test
    public void testDBHelper() {
        DBHelper dbh = new DBHelperBuilder().addToJavaConverter(java.sql.Timestamp.class, new ToJavaConverter<java.sql.Timestamp>() {
            @Override
            public Object toJava(Timestamp dbObj) {
                return new Date(dbObj.getTime());
            }
        }).newDBHelper(dataSource);

        // List<Map> list = dbh.executeQuery("select id, name from contact");
        Runner runner = dbh.newRunner();

        runner.executeUpdate("insert into contact (id,name,create_date) values (?,?,?)", 1, "mike", new Timestamp(System.currentTimeMillis()));

        List<Map> contacts = runner.executeQuery("select contact.id, contact.name, contact.create_date from contact");

        Object val = contacts.get(0).get("create_date");
        assertEquals("Java type for DB Timestamp type",java.util.Date.class,val.getClass());
        runner.close();

    }
}
