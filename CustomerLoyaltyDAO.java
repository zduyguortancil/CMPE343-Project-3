package dao;

import model.CustomerLoyalty;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Customer Loyalty operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for the
 * customer loyalty program. It manages loyalty points, tiers, and total
 * spending
 * for customers, automatically updating tiers based on points.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Loyalty record CRUD operations</li>
 * <li>Get or create loyalty records</li>
 * <li>Add points from orders</li>
 * <li>Automatic tier calculation and update</li>
 * <li>Total spending tracking</li>
 * <li>Get all loyalty records (for owner view)</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;CustomerLoyalty&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CustomerLoyaltyDAO extends AbstractDAO<CustomerLoyalty> {

    /**
     * Singleton instance of CustomerLoyaltyDAO.
     */
    private static final CustomerLoyaltyDAO INSTANCE = new CustomerLoyaltyDAO();

    /**
     * Gets the singleton instance of CustomerLoyaltyDAO.
     * 
     * @return The single instance of CustomerLoyaltyDAO
     */
    public static CustomerLoyaltyDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "CustomerLoyalty";
    }

    @Override
    protected String getIdColumnName() {
        return "loyalty_id";
    }

    @Override
    protected CustomerLoyalty mapResultSetToEntity(ResultSet rs) throws Exception {
        CustomerLoyalty cl = new CustomerLoyalty(
                rs.getInt("loyalty_id"),
                rs.getString("username"),
                rs.getInt("points"),
                rs.getString("tier"),
                rs.getDouble("total_spent"));
        cl.setCreatedAt(rs.getTimestamp("created_at"));
        cl.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cl;
    }

    /**
     * Gets an existing loyalty record for a customer, or creates a new one if it
     * doesn't exist.
     * 
     * <p>
     * This method ensures that every customer has a loyalty record. If no record
     * exists, a new one is created with default values (0 points, BRONZE tier).
     * 
     * @param username The username of the customer
     * @return The loyalty record (existing or newly created)
     */
    public static CustomerLoyalty getOrCreate(String username) {
        CustomerLoyalty existing = getByUsername(username);
        if (existing != null)
            return existing;

        // Create new record
        String sql = "INSERT INTO CustomerLoyalty(username) VALUES(?)";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getByUsername(username);
    }

    /**
     * Gets the loyalty record for a customer by username.
     * 
     * @param username The username of the customer
     * @return The loyalty record if found, or null if not found
     */
    public static CustomerLoyalty getByUsername(String username) {
        String sql = "SELECT * FROM CustomerLoyalty WHERE username = ?";

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
     * Adds loyalty points from an order and updates the customer's tier.
     * 
     * <p>
     * Calculates points based on order total (1 point per 10 TL spent),
     * adds them to the customer's current points, updates total spending,
     * and automatically recalculates the tier based on the new point total.
     * 
     * @param username   The username of the customer
     * @param orderTotal The total amount of the order (in Turkish Lira)
     * @return true if the update was successful, false otherwise
     */
    public static boolean addPointsFromOrder(String username, double orderTotal) {
        int points = CustomerLoyalty.calculatePointsFromOrder(orderTotal);

        // Get or create record
        CustomerLoyalty cl = getOrCreate(username);
        if (cl == null)
            return false;

        // Update points and total spent
        String sql = """
                UPDATE CustomerLoyalty
                SET points = points + ?,
                    total_spent = total_spent + ?,
                    tier = CASE
                        WHEN points + ? >= 2500 THEN 'PLATINUM'
                        WHEN points + ? >= 1000 THEN 'GOLD'
                        WHEN points + ? >= 500 THEN 'SILVER'
                        ELSE 'BRONZE'
                    END
                WHERE username = ?
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, points);
            ps.setDouble(2, orderTotal);
            ps.setInt(3, points);
            ps.setInt(4, points);
            ps.setInt(5, points);
            ps.setString(6, username);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all loyalty records in the database.
     * 
     * <p>
     * Retrieves all customer loyalty records, ordered by points (highest first).
     * Used primarily for owner/admin views to see customer loyalty statistics.
     * 
     * @return A list of all loyalty records, ordered by points DESC
     */
    public static List<CustomerLoyalty> getAllLoyalty() {
        List<CustomerLoyalty> list = new ArrayList<>();

        String sql = "SELECT * FROM CustomerLoyalty ORDER BY points DESC";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(INSTANCE.mapResultSetToEntity(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
