package org.josql.test;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.josql.DBHelper;
import org.josql.DBHelperBuilder;
import org.josql.Runner;
import org.junit.Test;

/* Assume SQL
 * create user jsqlizer_test_user with password 'welcome';
 * create database jsqlizer_test_db with owner jsqlizer_test_user;
 */
public class SQLExecuteTests extends JSQLizerTestSupport {

    @Test
    public void testDBHelper() {
        DBHelper dbh = new DBHelperBuilder().newDBHelper(dataSource);
        // List<Map> list = dbh.executeQuery("select id, name from contact");
        Runner runner = dbh.newRunner();

        runner.executeUpdate("insert into contact (id,name,create_date) values (?,?,?)", 1, "mike", new Timestamp(System.currentTimeMillis()));

        runner.executeUpdate("insert into contact (id,name,create_date) values (?,?,?)", new Object[] { 2, "jen",
                new Date() });

        runner.executeUpdate("insert into contact (id,name,email) values (?,?,?)", 3, "angie", "wrong@email.com");
        runner.executeUpdate("update contact set email = ? where id = ?", null, 3);

        runner.executeUpdate("insert into contact (id,name,email) values (?,?,?)", 4, "dan", "dan@gmail.com");
        runner.executeUpdate("insert into contact (id,name,email) values (?,?,?)", 5, "jon", "jon@gmail.com");

        runner.executeUpdate("insert into contact (id,name,email) values (?,?,?)", 6, "loner", "loner@gmail.com");

        // Girls Team
        Integer teamId = (Integer) runner.executeInsert("insert into team (name) values (?)", "girls");
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 2);
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 3);

        // Boys Team
        teamId = (Integer) runner.executeInsert("insert into team (name) values (?)", "boys");
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 1);
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 4);
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 5);

        // Roles
        Long devoloperRoleId = (Long) runner.executeInsert("insert into role (name) values (?)", "developer");
        Long managerRoleId = (Long) runner.executeInsert("insert into role (name) values (?)", "manager");

        // jen is a manager
        runner.executeUpdate("update contact set role_id = ? where id = ?", managerRoleId, 2L);

        // dan and jon are developers
        runner.executeUpdate("update contact set role_id = ? where id = ?", devoloperRoleId, 4L);
        runner.executeUpdate("update contact set role_id = ? where id = ?", devoloperRoleId, 5L);

        // "Jen" is also in the boys team
        runner.executeInsert("insert into team_contact (team_id,contact_id) values (?,?)", teamId, 2);

        List<Map> list = runner.executeQuery("select id, name, email from contact");
        // System.out.println("all  (" + list.size() + "):" + list);
        assertEquals("First user name", "mike", list.get(0).get("name"));
        int c = runner.executeCount("select count(id) from contact");
        assertEquals(c, list.size());

        List<Map> byTeamList = runner.executeQuery("select contact.id, team.id as \"teams[].id\", team.name as \"teams[].name\", contact.name from contact" + " left join team_contact as tc on (contact.id = tc.contact_id)"
                                + " left join team on (tc.team_id = team.id) order by contact.id");
        assertEquals("First user team", "boys", ((List<Map>) byTeamList.get(0).get("teams")).get(0).get("name"));

        List<Map> onlyContactsWithRoles = runner.executeQuery("select contact.id, contact.name, contact.create_date, r.name as \"ro.name\", r.id as \"ro.id\" from contact join role r on (contact.role_id = r.id) order by id");
        assertEquals("First user role", "manager", ((Map) onlyContactsWithRoles.get(0).get("ro")).get("name"));

        runner.close();

    }
}
