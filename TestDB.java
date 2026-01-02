package app;

import util.DBUtil;

/**
 * Simple utility class for testing database connectivity.
 * 
 * <p>This class provides a basic test to verify that the database connection
 * can be established successfully. It attempts to get a connection using
 * DBUtil and prints a success message if the connection is established, or
 * prints an error stack trace if the connection fails.
 * 
 * <p>This is useful for debugging database connection issues during development.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class TestDB {
    
    /**
     * Main method to test database connection.
     * 
     * <p>Attempts to establish a database connection using DBUtil.getConnection().
     * If successful, prints "DB CONNECTION OK" to the console. If an exception
     * occurs, prints the stack trace.
     * 
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        try {
            DBUtil.getConnection();
            System.out.println("DB CONNECTION OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
