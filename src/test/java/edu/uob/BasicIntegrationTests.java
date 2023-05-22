package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class BasicIntegrationTests {
    private DBServer server;

    private static final String OK_MESSAGE = "[OK]";
    private static final String ERROR_MESSAGE = "[ERROR]";
    private String databaseName;
    private String tableName;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {

        server = new DBServer();

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
    //Testing examples of commands which return OK
    @Test
    public void testValidCommand1() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");

    }

    //Testing that USE works
    @Test
    public void testValidCommand2() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");


        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");
    }

    @Test
    public void testValidCommand3() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");

    }

    @Test
    public void testValidCommand4() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + "(attrib1, attrib2, attrib3);");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Response should have valid return message");

        assertTrue(response.contains("attrib1"), "Attrib1 should have been created");
        assertTrue(response.contains("attrib2"), "Attrib2 should have been created");
        assertTrue(response.contains("attrib3"), "Attrib3 should have been created");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");

    }

    @Test
    public void testValidCommand5() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + "(attrib1, attrib2, attrib3);");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Response should have valid return message");

        assertTrue(response.contains("attrib1"), "Attrib1 should have been created");
        assertTrue(response.contains("attrib2"), "Attrib2 should have been created");
        assertTrue(response.contains("attrib3"), "Attrib3 should have been created");

        response = sendCommandToServer("INSERT INTO  " + tableName + " VALUES ('a', 123, 1123.34);");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add values");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains("a"), "a should be present");
        assertTrue(response.contains("123"), "The value 123 should be present");
        assertTrue(response.contains("1123.34"), "The value 1123.34 should be present");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");

    }

    //Testing that altering attribute works
    @Test
    public void testValidCommand6() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + "(attrib1, attrib2, attrib3);");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Response should have valid return message");

        assertTrue(response.contains("attrib1"), "Attrib1 should have been created");
        assertTrue(response.contains("attrib2"), "Attrib2 should have been created");
        assertTrue(response.contains("attrib3"), "Attrib3 should have been created");

        response = sendCommandToServer("INSERT INTO  " + tableName + " VALUES ('a', 123, 1123.34);");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add values");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains("a"), "a should be present");
        assertTrue(response.contains("123"), "The value 123 should be present");
        assertTrue(response.contains("1123.34"), "The value 1123.34 should be present");

        response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib4;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add attrib4 when it is not present in table");

        response = sendCommandToServer("SELECT attrib4 FROM " + tableName + ";");

        assertTrue(response.contains("NULL"), "Initialised values for newly created column should be null");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");

    }

    //Testing that altering attribute works
    @Test
    public void testValidCommand7() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + "(attrib1, attrib2, attrib3);");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Response should have valid return message");

        assertTrue(response.contains("attrib1"), "Attrib1 should have been created");
        assertTrue(response.contains("attrib2"), "Attrib2 should have been created");
        assertTrue(response.contains("attrib3"), "Attrib3 should have been created");

        response = sendCommandToServer("INSERT INTO  " + tableName + " VALUES ('a', 123, 1123.34);");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add values");

        response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains("a"), "a should be present");
        assertTrue(response.contains("123"), "The value 123 should be present");
        assertTrue(response.contains("1123.34"), "The value 1123.34 should be present");

        response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib4;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add attrib4 when it is not present in table");

        response = sendCommandToServer("SELECT attrib4 FROM " + tableName + ";");

        assertTrue(response.contains("NULL"), "Initialised values for newly created column should be null");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");
    }

    //Testing that deleting table works, but only once
    @Test
    public void testValidCommand8() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid database should have been created");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "DBServer should be using the newly created database");

        String tableName = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + tableName + "(attrib1, attrib2, attrib3);");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been created");

        response = sendCommandToServer("DROP TABLE " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "A valid table should have been deleted");

        response = sendCommandToServer("DROP TABLE " + tableName + ";");
        assertTrue(response.contains(ERROR_MESSAGE), "A table cannot be deleted twice");

        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Database should have been deleted");
    }


}
