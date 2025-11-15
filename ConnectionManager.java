import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Sets the file path to the SQLite database chosen by the GUI.
 * @param path full path to the .db file
 */
public class ConnectionManager {


    private static String dbPath = null;
    /**
     * Sets the path to Sqlite database file.
     * @param path full file patch to the .db file
     */

    public static void setDatabasePath(String path) { dbPath = path; }
     /**
     * Returns an open SQLite connection using the path provided by the GUI.
     *
     * @return a live JDBC connection to the database
     * @throws SQLException if the path is missing or a connection cannot be created
     */
    public static Connection getConnection() throws SQLException {
        if (dbPath == null || dbPath.isBlank())
            throw new SQLException("Database path not set");
        try {
            Class.forName("org.sqlite.JDBC"); // load driver
        } catch (ClassNotFoundException ignore) { }
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
