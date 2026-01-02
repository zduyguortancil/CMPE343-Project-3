package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Database utility class for managing database connections.
 * 
 * <p>This utility provides a centralized way to obtain database connections
 * to the MySQL database. It encapsulates the database connection parameters
 * (URL, username, password) and provides a single method to get connections.
 * 
 * <p>The connection uses the MySQL JDBC driver and connects to a local
 * MySQL instance on the default port (3306) with the database name "greengrocer".
 * SSL is disabled and public key retrieval is allowed for compatibility.
 * 
 * <p><b>Note:</b> In a production environment, database credentials should
 * be stored securely (e.g., in configuration files or environment variables)
 * rather than hardcoded.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class DBUtil {

    /**
     * JDBC connection URL for the MySQL database.
     * Connects to localhost on port 3306 with database name "greengrocer".
     */
    private static final String URL = "jdbc:mysql://localhost:3306/greengrocer?useSSL=false&allowPublicKeyRetrieval=true";

    /**
     * Database username for authentication.
     */
    private static final String USER = "root";

    /**
     * Database password for authentication.
     */
    private static final String PASS = "1234abcd";

    /**
     * Establishes and returns a connection to the database.
     * 
     * <p>This method creates a new database connection using the configured
     * URL, username, and password. The connection should be closed by the
     * caller when no longer needed, preferably using try-with-resources.
     * 
     * <p>Example usage:
     * <pre>
     * try (Connection con = DBUtil.getConnection()) {
     *     // Use the connection
     * }
     * </pre>
     * 
     * @return A Connection object to the database
     * @throws Exception If a database access error occurs or the connection
     *                   cannot be established (e.g., database server not running,
     *                   invalid credentials, network issues)
     */
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
