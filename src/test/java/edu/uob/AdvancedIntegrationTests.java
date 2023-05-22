package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class AdvancedIntegrationTests {
    private DBServer server;
    private static final String OK_MESSAGE = "[OK]";
    private static final String ERROR_MESSAGE = "[ERROR]";
    private String databaseName;
    private String tableName;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {

        server = new DBServer();
        databaseName = generateRandomName();
        tableName = "marks";
        sendCommandToServer("CREATE DATABASE " + databaseName + ";");
        sendCommandToServer("USE " + databaseName + ";");
        sendCommandToServer("CREATE TABLE " + tableName + " (Name, Age, Email, Passed);");
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('Bob', 20, 'bob@bob.co.uk', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('David', 70, 'david@david.co.uk', TRUE);" );
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('Harry', 30, 'harry@google.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('Richard', 44, 'richard@hotmail.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('John', 32, 'johndoe@gmail.com', FALSE);" );
        sendCommandToServer("INSERT INTO " + tableName + " VALUES ('Abraham', 61, 'abraham@live.co.uk', TRUE);" );
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

    //Testing that altering attribute works

    @Test
    public void testAlter1() {
        String response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add attrib when it is not present in table");

        response = sendCommandToServer("SELECT attrib FROM " + tableName + ";");

        assertTrue(response.contains("NULL"), "Initialised values for newly created column should be null");

    }

    @Test
    public void testAlter2() {

        String response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add attrib when it is not present in table");

        response = sendCommandToServer("ALTER TABLE " + tableName + " DROP attrib;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to drop attrib when it is present in table");

    }

    @Test
    public void testAlter3() {

        String response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to add attrib when it is not present in table");

        response = sendCommandToServer("ALTER TABLE " + tableName + " ADD attrib;");
        assertTrue(response.contains(ERROR_MESSAGE), "Should NOT be able to add attrib when it is present in table");

        response = sendCommandToServer("ALTER TABLE " + tableName + " DROP attrib;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to drop attrib when it is present in table");

    }

    @Test
    public void testAlter4() {
        String response = sendCommandToServer("ALTER TABLE " + tableName + " DROP attrib;");
        assertTrue(response.contains(ERROR_MESSAGE), "Should NOT be able to drop attrib when it is NOT present in table");

    }

    // Trying to remove ID from table
    @Test
    public void testAlter5() {
        String response = sendCommandToServer("ALTER TABLE " + tableName + " DROP id;");
        assertTrue(response.contains(ERROR_MESSAGE), "Should NOT be able to delete ID");
    }

    // Trying to add ID to table
    @Test
    public void testAlter6() {
        String response = sendCommandToServer("ALTER TABLE " + tableName + " ADD id;");
        assertTrue(response.contains(ERROR_MESSAGE), "Should NOT be able to add ID");
    }


    //Update with basic if condition - should change all values
    @Test
    public void testUpdate1() {
        sendCommandToServer("UPDATE " + tableName + " SET " +  "age = 0 WHERE id >= 1;");

        String response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 1;");

        assertTrue(response.contains(OK_MESSAGE), "Response should be OK");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 2;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 3;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 4;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 5;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 6;");
        assertTrue(response.contains("0"), "Response should contain 0");

    }

    //Testing update where attributeName is in format tableName.attributeName
    @Test
    public void testUpdate2() {
        sendCommandToServer("UPDATE " + tableName + " SET " + tableName + ".age = 0 WHERE id >= 1;");
        String response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Response should be OK");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 2;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 3;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 4;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 5;");
        assertTrue(response.contains("0"), "Response should contain 0");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 6;");
        assertTrue(response.contains("0"), "Response should contain 0");
    }

    //Testing update with complex nested condition
    @Test
    public void testUpdate3() {
        sendCommandToServer("UPDATE " + tableName + " SET " +  "age = 0 WHERE " +
                "((name == 'Bob' OR name == 'Harry') AND (((id <= 10)) or id >= -1000));");

        String response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 1;");
        assertTrue(response.contains("0"), "Response should contain 0 as age has been updated");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 2;");
        assertTrue(response.contains("70"), "Response should contain 70 as age has NOT been updated");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 3;");
        assertTrue(response.contains("0"), "Response should contain 0 as age has been updated");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 4;");
        assertTrue(response.contains("44"), "Response should contain 44 as age has NOT been updated");
    }


    //Testing update with condition referencing wrong table - should return error
    @Test
    public void testUpdate4() {
        String response = sendCommandToServer("UPDATE " + tableName + " SET " +  "age = 0 WHERE " +
                "((name == 'Bob' OR " + tableName + "s.name == 'Harry') OR (((id <= 10)) or id >= -1000));");
        assertTrue(response.contains(ERROR_MESSAGE), "Response should give error as query is semantically invalid");

    }

    //Testing update with multiple columns being updated
    @Test
    public void testUpdate5() {
        String response = sendCommandToServer("UPDATE " + tableName + " SET " + tableName + ".age = 322323, name = 'Test' WHERE id == 3;");
        assertTrue(response.contains(OK_MESSAGE), "Response should give error as query is semantically invalid");
        response = sendCommandToServer("SELECT * FROM " + tableName + " WHERE id == 3;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to select from table");
        assertTrue(response.contains("Test"), "Name should now be Test");
        assertTrue(response.contains("322323"), "Age should now be 322323");
        assertFalse(response.contains("Harry"), "Harry should now be removed from table");
        assertFalse(response.contains("30"), "The age should now have changed");


    }


    //Testing delete with simple query
    @Test
    public void testDelete1() {
        String response = sendCommandToServer("DELETE FROM " + tableName + " WHERE id == 1;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to delete from table");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 1;");
        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("1"), "ID 1 should be removed");

    }

    //Testing that delete can delete all rows in table
    @Test
    public void testDelete2() {
        String response = sendCommandToServer("DELETE FROM " + tableName + " WHERE id >= 1;");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to delete from table");

        response = sendCommandToServer("SELECT age FROM " + tableName + " ;");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("1"), "ID 1 should be removed");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("2"), "ID 2 should be removed");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("3"), "ID 3 should be removed");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("4"), "ID 4 should be removed");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("5"), "ID 5 should be removed");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("6"), "ID 6 should be removed");

    }

    //Testing that delete can delete rows where string comparison is used
    @Test
    public void testDelete3() {
        String response = sendCommandToServer("DELETE FROM " + tableName + " WHERE name LIKE 'ob';");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to delete from table");

        response = sendCommandToServer("SELECT age FROM " + tableName + " WHERE id == 1;");

        assertFalse(response.contains(ERROR_MESSAGE), "Response should not give error");
        assertFalse(response.contains("1"), "ID 1 should be removed");

    }

   //Testing select with multiple columns, in different order to how it's stored in table and in different casing
    @Test
    public void testSelect1() {
        String response = sendCommandToServer("SELECT email, pAsSed FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to select from table");

        assertTrue(response.contains("bob@bob.co.uk"), "Should contain bob's email");
        assertTrue(response.contains("david@david.co.uk"), "Should contain david's email");
        assertTrue(response.contains("harry@google.com"), "Should contain harry's email");
        assertTrue(response.contains("richard@hotmail.com"), "Should contain richard's email");
        assertTrue(response.contains("johndoe@gmail.com"), "Should contain john's email");
        assertTrue(response.contains("abraham@live.co.uk"), "Should contain abraham's email");

        assertTrue(response.contains("Passed"), "Should contain column title of passed");
        assertTrue(response.contains("TRUE"), "Should contain TRUE value");
        assertTrue(response.contains("FALSE"), "Should contain FALSE value");

    }

    //Selecting all columns
    @Test
    public void testSelect2() {
        String response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to select from table");

        assertTrue(response.contains("Name"), "Output string should contain Name");
        assertTrue(response.contains("Age"), "Output string should contain Age");
        assertTrue(response.contains("Email"), "Output string should contain Email");
        assertTrue(response.contains("Passed"), "Output string should contain Passed");

    }

    //Selecting all columns
    @Test
    public void testSelect3() {
        String response = sendCommandToServer("SELECT * FROM " + tableName + ";");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to select from table");

        assertTrue(response.contains("Name"), "Output string should contain Name");
        assertTrue(response.contains("Age"), "Output string should contain Age");
        assertTrue(response.contains("Email"), "Output string should contain Email");
        assertTrue(response.contains("Passed"), "Output string should contain Passed");

    }

    //Selecting columns with complex, nested conditions
    @Test
    public void testSelect4() {
        String response = sendCommandToServer("SELECT * FROM " + tableName + " WHERE" +
                "(name == 'Bob' OR name == 'Tim' OR name == 'Test') OR ((passed == FALSE));");
        assertTrue(response.contains(OK_MESSAGE), "Should be able to select from table");

        assertTrue(response.contains("Bob"), "Output string should contain Bob");
        assertTrue(response.contains("Harry"), "Output string should contain Harry");
        assertTrue(response.contains("Richard"), "Output string should contain Richard");
        assertTrue(response.contains("John"), "Output string should contain John");

        assertFalse(response.contains("David"), "Output string should NOT contain David");
        assertFalse(response.contains("Abraham"), "Output string should NOT contain Abraham");

    }

    //Selecting where one column does not exist
    @Test
    public void testSelect5() {
        String response = sendCommandToServer("SELECT * FROM " + tableName + " WHERE" +
                "(name == 'Bob' OR name == 'Tim' OR name == 'Test') OR ((gfkjfgjk == FALSE));");
        assertTrue(response.contains(ERROR_MESSAGE), "Should not be able to select from table");

    }

    //Selecting where column is attached to wrong table
    @Test
    public void testSelect6() {
        String response = sendCommandToServer("SELECT * FROM " + tableName + " WHERE" +
                "(name == 'Bob' OR " + tableName + "ss.name == 'Tim' OR name == 'Test') OR ((passed == FALSE));");
        assertTrue(response.contains(ERROR_MESSAGE), "Should not be able to select from table");

    }

    //Selecting non-existent attribute from table gives error
    @Test
    public void testSelect7() {
        String response = sendCommandToServer("SELECT nonexistentattribute FROM " + tableName + ";");
        assertTrue(response.contains(ERROR_MESSAGE), "Should not be able to select non-existent attribute from table");

    }


}
