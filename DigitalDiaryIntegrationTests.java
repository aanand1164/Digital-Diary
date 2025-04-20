import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

public class DigitalDiaryIntegrationTests {

    private static Connection connection;

    @BeforeAll
    public static void setUpDatabaseConnection() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        connection = DriverManager.getConnection(url, username, password);
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testSaveEntryToDatabase() throws SQLException {
        String title = "Mood Today";
        String content = "It was a good day.";
        String date = "2025-04-20";

        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO entries (title, content, date) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setString(1, title);
        stmt.setString(2, content);
        stmt.setString(3, date);

        int affectedRows = stmt.executeUpdate();
        assertTrue(affectedRows > 0, "Entry should be saved");

        // Cleanup
        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM entries WHERE title = ?");
        deleteStmt.setString(1, title);
        deleteStmt.executeUpdate();
    }

    @Test
    public void testSearchEntryByTitle() throws SQLException {
        String title = "SearchTestEntry";
        String content = "Search content";
        String date = "2025-04-20";

        // Insert dummy entry
        PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO entries (title, content, date) VALUES (?, ?, ?)");
        insertStmt.setString(1, title);
        insertStmt.setString(2, content);
        insertStmt.setString(3, date);
        insertStmt.executeUpdate();

        // Test search
        PreparedStatement searchStmt = connection.prepareStatement("SELECT * FROM entries WHERE title = ?");
        searchStmt.setString(1, title);
        ResultSet rs = searchStmt.executeQuery();

        assertTrue(rs.next(), "Entry should be found");
        assertEquals(content, rs.getString("content"));

        // Cleanup
        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM entries WHERE title = ?");
        deleteStmt.setString(1, title);
        deleteStmt.executeUpdate();
    }

    @Test
    public void testDeleteEntry() throws SQLException {
        String title = "DeleteMe";
        String content = "Will be deleted";
        String date = "2025-04-20";

        PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO entries (title, content, date) VALUES (?, ?, ?)");
        insertStmt.setString(1, title);
        insertStmt.setString(2, content);
        insertStmt.setString(3, date);
        insertStmt.executeUpdate();

        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM entries WHERE title = ?");
        deleteStmt.setString(1, title);
        int affectedRows = deleteStmt.executeUpdate();

        assertTrue(affectedRows > 0, "Entry should be deleted");

        // Verify deletion
        PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM entries WHERE title = ?");
        checkStmt.setString(1, title);
        ResultSet rs = checkStmt.executeQuery();

        assertFalse(rs.next(), "Entry should not exist after deletion");
    }

    @Test
    public void testDBConnectionFailureHandling() {
        Exception exception = assertThrows(SQLException.class, () -> {
            Connection badConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wrong_db", "wrong", "wrong");
            badConn.createStatement().executeQuery("SELECT 1");
        });
        assertNotNull(exception.getMessage());
    }
}
