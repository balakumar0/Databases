package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class ParserTests {
    private DBServer server;

    private static final String OK_MESSAGE = "[OK]";
    private static final String ERROR_MESSAGE = "[ERROR]";

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

    //Testing that lack of semicolon gives error
    @Test
    public void testInvalidCommand1() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName);

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that lack of semicolon gives error
    @Test
    public void testInvalidCommand2() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("USE " + randomName);

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that lack of semicolon gives error
    @Test
    public void testInvalidCommand3() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("DROP" + randomName);

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that lack of semicolon gives error
    @Test
    public void testInvalidCommand4() {
        String response = sendCommandToServer("DROP DATABASE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that lack of semicolon gives error
    @Test
    public void testInvalidCommand5() {
        String response = sendCommandToServer("DROP TABLE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand6() {
        String response = sendCommandToServer("ALTER TABLE ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand7() {
        String response = sendCommandToServer("ALTER TABLE t ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand8() {
        String response = sendCommandToServer("ALTER TABLE t add ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand9() {
        String response = sendCommandToServer("ALTER TABLE t add attribute1 ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand10() {
        String response = sendCommandToServer("INSERT");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand11() {
        String response = sendCommandToServer("INSERT INTO");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand12() {
        String response = sendCommandToServer("INSERT INTO RANDOM");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand13() {
        String response = sendCommandToServer("INSERT INTO RANDOM VALUES");
        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand14() {
        String response = sendCommandToServer("INSERT INTO RANDOM VALUES (");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand15() {
        String response = sendCommandToServer("INSERT INTO RANDOM VALUES ('F'");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand16() {
        String response = sendCommandToServer("INSERT INTO RANDOM VALUES ('F')");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand17() {
        String response = sendCommandToServer("SELECT");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand18() {
        String response = sendCommandToServer("SELECT *");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand19() {
        String response = sendCommandToServer("SELECT * FROM");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand20() {
        String response = sendCommandToServer("SELECT * FROM t");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand21() {
        String response = sendCommandToServer("SELECT * FROM t");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand22() {
        String response = sendCommandToServer("SELECT * FROM t WHERE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand23() {
        String response = sendCommandToServer("SELECT * FROM t WHERE name ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand24() {
        String response = sendCommandToServer("SELECT * FROM t WHERE name == ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand25() {
        String response = sendCommandToServer("SELECT * FROM t WHERE name == 'Bob'");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand26() {
        String response = sendCommandToServer("SELECT id, name FROM t WHERE name == 'Bob'");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand27() {
        String response = sendCommandToServer("SELECT id, name FROM t WHERE name == 'Bob'");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand28() {
        String response = sendCommandToServer("UPDATE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand29() {
        String response = sendCommandToServer("UPDATE T");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand30() {
        String response = sendCommandToServer("UPDATE T SET");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand31() {
        String response = sendCommandToServer("UPDATE T SET name");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand32() {
        String response = sendCommandToServer("UPDATE T SET name=");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand33() {
        String response = sendCommandToServer("UPDATE T SET name='Bob'");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand34() {
        String response = sendCommandToServer("UPDATE T SET name='Bob' WHERE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand35() {
        String response = sendCommandToServer("UPDATE T SET name='Bob' WHERE age");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand36() {
        String response = sendCommandToServer("UPDATE T SET name='Bob' WHERE age > ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand37() {
        String response = sendCommandToServer("UPDATE T SET name='Bob' WHERE age > 20");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand38() {
        String response = sendCommandToServer("DELETE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand39() {
        String response = sendCommandToServer("DELETE FROM");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand40() {
        String response = sendCommandToServer("DELETE FROM t");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand41() {
        String response = sendCommandToServer("DELETE FROM t WHERE");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand42() {
        String response = sendCommandToServer("DELETE FROM t WHERE name");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand43() {
        String response = sendCommandToServer("DELETE FROM t WHERE id > ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand44() {
        String response = sendCommandToServer("DELETE FROM t WHERE id > 20");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand45() {
        String response = sendCommandToServer("JOIN");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand46() {
        String response = sendCommandToServer("JOIN table1");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand47() {
        String response = sendCommandToServer("JOIN table1 AND");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand48() {
        String response = sendCommandToServer("JOIN table1 AND table2");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand49() {
        String response = sendCommandToServer("JOIN table1 AND table2 ON ");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand50() {
        String response = sendCommandToServer("JOIN table1 AND table2 ON attribute1");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand51() {
        String response = sendCommandToServer("JOIN table1 AND table2 ON attribute1 AND");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    //Testing that commands which end early return error
    @Test
    public void testInvalidCommand52() {
        String response = sendCommandToServer("JOIN table1 AND table2 ON attribute1 AND attribute2");

        assertTrue(response.contains(ERROR_MESSAGE), "An invalid query was made, however an [ERROR] tag was not returned");
    }
}
