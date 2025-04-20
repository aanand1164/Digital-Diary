import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DigitalDiaryUnitTests {
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        connection = DriverManager.getConnection(url, username, password);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (connection != null) connection.close();
    }

    @BeforeEach
    public void cleanDatabase() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM entries");
    }

    @Test
    public void testSaveEntry() throws Exception {
        String query = "INSERT INTO entries (date, title, content) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, "2025-04-19");
        ps.setString(2, "Test Title");
        ps.setString(3, "This is a test entry.");
        int result = ps.executeUpdate();

        assertEquals(1, result);
    }

    @Test
    public void testLoadEntry() throws Exception {
        // First insert
        String insert = "INSERT INTO entries (date, title, content) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setString(1, "2025-04-19");
        ps.setString(2, "LoadTest");
        ps.setString(3, "Load test content");
        ps.executeUpdate();

        // Now load
        String query = "SELECT * FROM entries WHERE title=?";
        PreparedStatement loadPs = connection.prepareStatement(query);
        loadPs.setString(1, "LoadTest");
        ResultSet rs = loadPs.executeQuery();

        assertTrue(rs.next());
        assertEquals("Load test content", rs.getString("content"));
    }

    @Test
    public void testEditEntry() throws Exception {
        // Insert
        String insert = "INSERT INTO entries (date, title, content) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setString(1, "2025-04-19");
        ps.setString(2, "EditTest");
        ps.setString(3, "Old content");
        ps.executeUpdate();

        // Update
        String update = "UPDATE entries SET content=? WHERE title=?";
        PreparedStatement updatePs = connection.prepareStatement(update);
        updatePs.setString(1, "New content");
        updatePs.setString(2, "EditTest");
        int updated = updatePs.executeUpdate();

        assertEquals(1, updated);
    }

    @Test
    public void testDeleteEntry() throws Exception {
        // Insert
        String insert = "INSERT INTO entries (date, title, content) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setString(1, "2025-04-19");
        ps.setString(2, "DeleteTest");
        ps.setString(3, "Some content");
        ps.executeUpdate();

        // Delete
        String delete = "DELETE FROM entries WHERE title=?";
        PreparedStatement delPs = connection.prepareStatement(delete);
        delPs.setString(1, "DeleteTest");
        int deleted = delPs.executeUpdate();

        assertEquals(1, deleted);
    }
}
