package edu.uob;

import org.junit.jupiter.api.AfterEach;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JoinTests {
    private DBServer server;
    private static final String OK_MESSAGE = "[OK]";
    private static final String ERROR_MESSAGE = "[ERROR]";
    private String databaseName;
    private String tableName1;
    private String tableName2;

    public void setup()
    {
        server = new DBServer();
        databaseName = generateRandomName();
        tableName1 = generateRandomName();
        tableName2 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + databaseName + ";");
        sendCommandToServer("USE " + databaseName + ";");
    }

    public void setup1() {

        setup();
        sendCommandToServer("CREATE TABLE " + tableName1 + " (Name, Age, Email, Passed);");
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('Bob', 20, 'bob@bob.co.uk', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('David', 70, 'david@david.co.uk', TRUE);" );
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('Harry', 30, 'harry@google.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('Richard', 44, 'richard@hotmail.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('John', 32, 'johndoe@gmail.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName1 + " VALUES ('Abraham', 61, 'abraham@live.co.uk', TRUE);" );

        sendCommandToServer("CREATE TABLE " + tableName2 + " (Username, Postcode, Foreignkey);");
        sendCommandToServer("INSERT INTO " + tableName2 + " VALUES ('user1', 'AB1', 1);" );
        sendCommandToServer("INSERT INTO " + tableName2 + " VALUES ('user2', 'AB2', 2);" );
        sendCommandToServer("INSERT INTO " + tableName2 + " VALUES ('user3', 'AB3', 3);" );
        sendCommandToServer("INSERT INTO " + tableName2 + " VALUES ('user4', 'AB4', 4);" );
        sendCommandToServer("INSERT INTO " + tableName2 + " VALUES ('user5', 'AB5', 5);" );
    }

    public void setup2()
    {
        setup();
        sendCommandToServer("CREATE TABLE " + tableName1 + ";");
        sendCommandToServer("CREATE TABLE " + tableName2 + ";");
    }

    @AfterEach
    public void destroy()
    {
        sendCommandToServer("DROP DATABASE " + databaseName + ";");
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName()
    {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    //Test basic join
    @Test
    public void testJoin1()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + tableName2 + " ON id AND Foreignkey;");
        assertTrue(response.contains(OK_MESSAGE), "Output string should contain OK");
        assertFalse(response.contains(tableName1 + ".id"), "Table 1's ID is still in joined table");
        assertFalse(response.contains(tableName2 + ".id"), "Table 2's ID is still in joined table");
        assertTrue(response.contains(tableName1 + ".Age"), "Output table should contain TableName1.Age");
        assertTrue(response.contains(tableName1 + ".Email"), "Output table should contain TableName1.Email");
        assertTrue(response.contains(tableName1 + ".Passed"), "Output table should contain TableName1.Passed");
        assertTrue(response.contains(tableName2 + ".Username"), "Output table should contain TableName2.Username");
        assertTrue(response.contains(tableName2 + ".Postcode"), "Output table should contain TableName2.Postcode");
        assertTrue(response.contains("id"), "Output table should contain id");

    }

    //Testing join where attribute names are in format Table.AttributeName

    @Test
    public void testJoin2()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + tableName2 +
                " ON " + tableName1 + ".id AND " + tableName2 + ".Foreignkey;");
        assertTrue(response.contains(OK_MESSAGE), "Output string should contain OK");
        assertFalse(response.contains(tableName1 + ".id"), "Table 1's ID is still in joined table");
        assertFalse(response.contains(tableName2 + ".id"), "Table 2's ID is still in joined table");
        assertTrue(response.contains(tableName1 + ".Age"), "Output table should contain TableName1.Age");
        assertTrue(response.contains(tableName1 + ".Email"), "Output table should contain TableName1.Email");
        assertTrue(response.contains(tableName1 + ".Passed"), "Output table should contain TableName1.Passed");
        assertTrue(response.contains(tableName2 + ".Username"), "Output table should contain TableName2.Username");
        assertTrue(response.contains(tableName2 + ".Postcode"), "Output table should contain TableName2.Postcode");
        assertTrue(response.contains("id"), "Output table should contain id");

    }

    //Cannot join on non-existent tables
    @Test
    public void testJoin3()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + generateRandomName() + " AND " + tableName2 + " ON id AND Foreignkey;");
        assertTrue(response.contains(ERROR_MESSAGE), "Cannot join on non-existent table");

    }
    //Cannot join on non-existent tables
    @Test
    public void testJoin4()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + generateRandomName() + " ON id AND Foreignkey;");
        assertTrue(response.contains(ERROR_MESSAGE), "Cannot join on non-existent table");

    }

    //Cannot join on non-existent attribute
    @Test
    public void testJoin5()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + tableName2 + " ON" + generateRandomName() + " AND Foreignkey;");
        assertTrue(response.contains(ERROR_MESSAGE), "Cannot join on non-existent attribute");

    }

    //Cannot join on non-existent attribute
    @Test
    public void testJoin6()
    {
        setup1();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + tableName2 + " ON id AND " + generateRandomName() + ";");
        assertTrue(response.contains(ERROR_MESSAGE), "Cannot join on non-existent attribute");

    }

    @Test
    public void testJoin7()
    {
        setup2();
        String response = sendCommandToServer("JOIN " + tableName1 + " AND " + tableName2 + " ON id AND id;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to join two empty tables together");
        assertTrue(response.contains("id"), "Output table contains id");
        assertFalse(response.contains(tableName1 + ".id"), "Output table contains Table1 ID");
        assertFalse(response.contains(tableName2 + ".id"), "Output table contains Table2 ID");

    }






}
