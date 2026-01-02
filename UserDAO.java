package dao;

import model.*;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User/Person operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for users
 * (Person entities). It uses the Factory Pattern to create appropriate Person
 * subclasses (Customer, Carrier, Owner) based on the role stored in the
 * database.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>User authentication</li>
 * <li>User registration</li>
 * <li>User profile management</li>
 * <li>Username and phone existence checking</li>
 * <li>Factory pattern for role-based Person creation</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Person&gt;
 * <p>
 * POLYMORPHISM: Factory Pattern for Person subtypes
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class UserDAO extends AbstractDAO<Person> {

    /**
     * Singleton instance of UserDAO.
     */
    private static final UserDAO INSTANCE = new UserDAO();

    /**
     * Gets the singleton instance of UserDAO.
     * 
     * @return The single instance of UserDAO
     */
    public static UserDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "UserInfo";
    }

    @Override
    protected String getIdColumnName() {
        return "username";
    }

    // POLYMORPHISM: Factory Pattern - creates correct Person subclass based on role
    @Override
    protected Person mapResultSetToEntity(ResultSet rs) throws Exception {
        String role = rs.getString("role");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String address = rs.getString("address");
        String phone = rs.getString("phone");

        System.out.println("  [mapResultSetToEntity] role='" + role + "', username='" + username + "'");

        Person person = null;
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            person = new Customer(username, password, address, phone);
            System.out.println("  [mapResultSetToEntity] Created Customer object");
        } else if ("CARRIER".equalsIgnoreCase(role)) {
            person = new Carrier(username, password, address, phone);
            System.out.println("  [mapResultSetToEntity] Created Carrier object, getRole()='" + person.getRole() + "'");
        } else if ("OWNER".equalsIgnoreCase(role)) {
            person = new Owner(username, password);
            System.out.println("  [mapResultSetToEntity] Created Owner object");
        } else {
            System.out.println("  [mapResultSetToEntity] Unknown role '" + role + "', returning null");
        }
        return person;
    }

    // ========== STATIC METHODS ==========

    /**
     * Finds a user by username.
     * 
     * <p>
     * Retrieves a Person entity (Customer, Carrier, or Owner) from the database
     * based on the username. Uses the Factory Pattern to create the appropriate
     * subclass based on the role.
     * 
     * @param username The username to search for
     * @return The Person entity if found, or null if not found
     */
    public static Person findByUsername(String username) {
        String sql = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return INSTANCE.mapResultSetToEntity(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticates a user and returns the Person entity.
     * 
     * <p>
     * Verifies the username and password combination, and if valid, returns
     * the corresponding Person entity (Customer, Carrier, or Owner) using the
     * Factory Pattern. Includes debug logging for troubleshooting.
     * 
     * @param username The username to authenticate
     * @param password The password to verify
     * @return The Person entity if authentication succeeds, or null if it fails
     */
    public static Person authenticateAndGetPerson(String username, String password) {
        String sql = "SELECT * FROM UserInfo WHERE username = ? AND password = ?";

        // DEBUG
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Attempting login with:");
        System.out.println("  Username: '" + username + "' (length: " + username.length() + ")");
        System.out.println("  Password: '" + password + "' (length: " + password.length() + ")");

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            System.out.println("  SQL: " + sql);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  ✅ User found in database!");
                    System.out.println("  Role: " + rs.getString("role"));
                    return INSTANCE.mapResultSetToEntity(rs);
                } else {
                    System.out.println("  ❌ No user found with these credentials!");

                    // Check if username exists at all
                    String checkSql = "SELECT username, password, role FROM UserInfo WHERE username = ?";
                    try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                        checkPs.setString(1, username);
                        try (ResultSet checkRs = checkPs.executeQuery()) {
                            if (checkRs.next()) {
                                System.out.println("  Username exists but password mismatch:");
                                System.out.println("    DB Password: '" + checkRs.getString("password") + "'");
                                System.out.println("    Input Password: '" + password + "'");
                            } else {
                                System.out.println("  Username '" + username + "' does not exist in database");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  ❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates a user's profile information.
     * 
     * <p>
     * Updates the address and phone number for a user. Empty or null values
     * are stored as NULL in the database.
     * 
     * @param username The username of the user to update
     * @param address  The new address (can be null or empty)
     * @param phone    The new phone number (can be null or empty)
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateProfile(String username, String address, String phone) {
        String sql = "UPDATE UserInfo SET address=?, phone=? WHERE username=?";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            if (address == null || address.trim().isEmpty()) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, address.trim());
            }

            if (phone == null || phone.trim().isEmpty()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, phone.trim());
            }

            ps.setString(3, username);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== LOGIN/REGISTER ==========

    /**
     * Authenticates a user by username and password.
     * 
     * <p>
     * Checks if the provided username and password combination exists in
     * the database. This is a simple boolean check, unlike authenticateAndGetPerson
     * which also returns the Person entity.
     * 
     * @param username The username to authenticate
     * @param password The password to verify
     * @return true if authentication succeeds, false otherwise
     */
    public static boolean authenticate(String username, String password) {
        String sql = "SELECT COUNT(*) FROM UserInfo WHERE username=? AND password=?";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a username already exists in the database.
     * 
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM UserInfo WHERE username=?";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a phone number already exists in the database.
     * 
     * <p>
     * Returns false if the phone is null or empty. Otherwise checks for
     * duplicate phone numbers.
     * 
     * @param phone The phone number to check
     * @return true if the phone number exists, false if it doesn't or is null/empty
     */
    public static boolean phoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM UserInfo WHERE phone=?";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registers a new customer user.
     * 
     * <p>
     * Creates a new user account with the CUSTOMER role. Address and phone
     * can be null and will be stored as NULL in the database.
     * 
     * @param username The username for the new customer
     * @param password The password for the new customer
     * @param address  The customer's address (can be null)
     * @param phone    The customer's phone number (can be null)
     * @return true if registration succeeds, false otherwise
     */
    public static boolean registerCustomer(String username, String password, String address, String phone) {
        String sql = "INSERT INTO UserInfo(username, password, role, address, phone) VALUES(?,?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, "CUSTOMER");

            if (address == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, address);
            }

            if (phone == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, phone);
            }

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets user information by username (backward compatibility method).
     * 
     * @param username The username to look up
     * @return The Person entity if found, or null if not found
     * @deprecated Use findByUsername() instead
     */
    @Deprecated
    public static Person getUserInfo(String username) {
        return findByUsername(username);
    }

    /**
     * Updates profile by username (backward compatibility method).
     * 
     * @param username The username of the user to update
     * @param address  The new address
     * @param phone    The new phone number
     * @return true if the update was successful, false otherwise
     * @deprecated Use updateProfile() instead
     */
    @Deprecated
    public static boolean updateProfileByUsername(String username, String address, String phone) {
        return updateProfile(username, address, phone);
    }

    // ========== CARRIER OPERATIONS ==========

    /**
     * Gets all carriers in the system.
     * 
     * <p>
     * Retrieves all users with the CARRIER role. Used primarily for
     * owner/admin views.
     * 
     * @return A list of all Carrier entities
     */
    public static List<Carrier> getAllCarriers() {
        List<Carrier> list = new ArrayList<>();
        String sql = "SELECT * FROM UserInfo WHERE role='CARRIER'";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Carrier(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("address"),
                        rs.getString("phone")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Employs (adds) a new carrier to the system.
     * 
     * <p>
     * Creates a new user account with the CARRIER role. The username must
     * be unique. Address and phone can be null.
     * 
     * @param username The username for the new carrier (must be unique)
     * @param password The password for the new carrier
     * @param address  The carrier's address (can be null)
     * @param phone    The carrier's phone number (can be null)
     * @return true if the carrier was added successfully, false if username exists
     *         or operation fails
     */
    public static boolean employCarrier(String username, String password, String address, String phone) {
        if (usernameExists(username)) {
            System.err.println("Cannot employ carrier: username already exists");
            return false;
        }

        String sql = "INSERT INTO UserInfo(username,password,role,address,phone) VALUES(?,?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, "CARRIER");

            if (address == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, address);
            }

            if (phone == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, phone);
            }

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fires (deletes) a carrier from the system.
     * 
     * <p>
     * Deletes a carrier user account, but only if the carrier has no active
     * orders (status NEW or IN_PROGRESS). This prevents data integrity issues.
     * 
     * @param username The username of the carrier to fire
     * @return true if the carrier was fired successfully, false if carrier has
     *         active orders or operation fails
     */
    public static boolean fireCarrier(String username) {
        // Check if carrier has active orders
        String checkSql = "SELECT COUNT(*) FROM Orders WHERE carrier_username=? AND status IN ('NEW','IN_PROGRESS')";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement checkPs = con.prepareStatement(checkSql)) {

            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Cannot fire carrier with active orders");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Delete carrier
        String sql = "DELETE FROM UserInfo WHERE username=? AND role='CARRIER'";
        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all customers in the system.
     * 
     * <p>
     * Retrieves all users with the CUSTOMER role. Used primarily for
     * owner/admin views.
     * 
     * @return A list of all Customer entities
     */
    public static List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM UserInfo WHERE role='CUSTOMER'";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Customer(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("address"),
                        rs.getString("phone")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
