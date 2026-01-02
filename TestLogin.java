package app;

import dao.UserDAO;
import util.DBUtil;
import java.sql.*;

/**
 * Utility class for testing database connection and user authentication.
 * 
 * <p>This class provides comprehensive testing functionality for:
 * <ul>
 *   <li>Database connection verification</li>
 *   <li>User existence checking in the database</li>
 *   <li>User creation if the test user doesn't exist</li>
 *   <li>Authentication testing using UserDAO</li>
 * </ul>
 * 
 * <p>This is useful for debugging authentication issues and verifying that
 * the database and user authentication system are working correctly.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class TestLogin {
    
    /**
     * Main method to test database connection and user authentication.
     * 
     * <p>This method performs the following tests:
     * <ol>
     *   <li>Tests database connection</li>
     *   <li>Checks if the 'cust' user exists in the UserInfo table</li>
     *   <li>If the user doesn't exist, creates it with username 'cust',
     *       password 'cust', and role 'customer'</li>
     *   <li>Tests authentication using UserDAO.authenticateAndGetPerson()</li>
     *   <li>Prints the authentication result and user role</li>
     * </ol>
     * 
     * <p>All operations print informative messages to the console, and any
     * exceptions are caught and printed with stack traces.
     * 
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        System.out.println("=== Testing DB Connection ===");
        try (Connection con = DBUtil.getConnection()) {
            System.out.println("DB CONNECTION: OK");

            // Check if cust user exists
            System.out.println("\n=== Checking 'cust' user ===");
            String sql = "SELECT * FROM UserInfo WHERE username = 'cust'";
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User found!");
                    System.out.println("  username: " + rs.getString("username"));
                    System.out.println("  password: " + rs.getString("password"));
                    System.out.println("  role: " + rs.getString("role"));
                } else {
                    System.out.println("User 'cust' NOT FOUND!");
                    System.out.println("Creating user...");

                    String insert = "INSERT INTO UserInfo (username, password, role) VALUES ('cust', 'cust', 'customer')";
                    try (PreparedStatement insertPs = con.prepareStatement(insert)) {
                        insertPs.executeUpdate();
                        System.out.println("User 'cust' created successfully!");
                    }
                }
            }

            // Test authenticate
            System.out.println("\n=== Testing UserDAO.authenticate ===");
            model.Person person = UserDAO.authenticateAndGetPerson("cust", "cust");
            System.out.println("authenticateAndGetPerson('cust', 'cust') returned: " + person);

            if (person == null) {
                System.out.println("AUTH FAILED - Check password in database!");
            } else {
                System.out.println("AUTH SUCCESS - Role: " + person.getRole());
            }

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
