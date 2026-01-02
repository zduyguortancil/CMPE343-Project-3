package dao;

import model.Coupon;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Coupon operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for discount
 * coupons. It handles coupon creation, validation, usage tracking, and
 * retrieval
 * of active coupons.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Coupon CRUD operations</li>
 * <li>Create coupons with discount rules</li>
 * <li>Get coupon by code</li>
 * <li>Coupon usage tracking</li>
 * <li>Coupon activation/deactivation</li>
 * <li>Get active coupons</li>
 * <li>Get all coupons (for owner view)</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Coupon&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CouponDAO extends AbstractDAO<Coupon> {

    /**
     * Singleton instance of CouponDAO.
     */
    private static final CouponDAO INSTANCE = new CouponDAO();

    /**
     * Gets the singleton instance of CouponDAO.
     * 
     * @return The single instance of CouponDAO
     */
    public static CouponDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "Coupon";
    }

    @Override
    protected String getIdColumnName() {
        return "coupon_id";
    }

    @Override
    protected Coupon mapResultSetToEntity(ResultSet rs) throws Exception {
        Coupon c = new Coupon(
                rs.getInt("coupon_id"),
                rs.getString("code"),
                rs.getDouble("discount_percent"),
                rs.getDouble("discount_amount"),
                rs.getDouble("min_order_amount"),
                rs.getTimestamp("valid_from"),
                rs.getTimestamp("valid_until"),
                rs.getInt("max_uses"),
                rs.getInt("used_count"),
                rs.getBoolean("is_active"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }

    /**
     * Creates a new coupon in the database.
     * 
     * <p>
     * The coupon code is automatically converted to uppercase for consistency.
     * 
     * @param code            The coupon code (will be converted to uppercase)
     * @param discountPercent The discount percentage (0 if not applicable)
     * @param discountAmount  The fixed discount amount (0 if not applicable)
     * @param minOrderAmount  The minimum order amount required to use this coupon
     * @param validFrom       The validity start timestamp (null if no start date)
     * @param validUntil      The validity end timestamp (null if no expiration)
     * @param maxUses         The maximum number of times this coupon can be used
     * @return true if the coupon was created successfully, false otherwise
     */
    public static boolean createCoupon(String code, double discountPercent, double discountAmount,
            double minOrderAmount, Timestamp validFrom, Timestamp validUntil,
            int maxUses) {
        String sql = """
                INSERT INTO Coupon(code, discount_percent, discount_amount, min_order_amount,
                                   valid_from, valid_until, max_uses)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.toUpperCase());
            ps.setDouble(2, discountPercent);
            ps.setDouble(3, discountAmount);
            ps.setDouble(4, minOrderAmount);
            ps.setTimestamp(5, validFrom);
            ps.setTimestamp(6, validUntil);
            ps.setInt(7, maxUses);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets a coupon by its code.
     * 
     * <p>
     * The code is automatically converted to uppercase for the search.
     * 
     * @param code The coupon code to search for (case-insensitive)
     * @return The coupon if found, or null if not found
     */
    public static Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM Coupon WHERE code = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.toUpperCase());

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
     * Marks a coupon as used by incrementing its usage count.
     * 
     * <p>
     * Only increments if the coupon is active and has not exceeded
     * the maximum uses. The code is automatically converted to uppercase.
     * 
     * @param code The coupon code to use (case-insensitive)
     * @return true if the coupon was successfully marked as used, false otherwise
     */
    public static boolean useCoupon(String code) {
        String sql = """
                UPDATE Coupon
                SET used_count = used_count + 1
                WHERE code = ? AND is_active = TRUE AND used_count < max_uses
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.toUpperCase());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deactivates a coupon by setting its is_active flag to false.
     * 
     * @param couponId The ID of the coupon to deactivate
     * @return true if the deactivation was successful, false otherwise
     */
    public static boolean deactivateCoupon(int couponId) {
        String sql = "UPDATE Coupon SET is_active = FALSE WHERE coupon_id = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, couponId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all coupons in the database.
     * 
     * <p>
     * Retrieves all coupons regardless of status, ordered by creation date
     * (most recent first). Used primarily for owner/admin views.
     * 
     * @return A list of all coupons, ordered by created_at DESC
     */
    public static List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();

        String sql = "SELECT * FROM Coupon ORDER BY created_at DESC";

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

    /**
     * Gets all active coupons that are currently valid and usable.
     * 
     * <p>
     * A coupon is considered active if:
     * <ul>
     * <li>is_active is true</li>
     * <li>used_count is less than max_uses</li>
     * <li>valid_until is null or in the future</li>
     * </ul>
     * 
     * @return A list of active coupons, ordered by created_at DESC
     */
    public static List<Coupon> getActiveCoupons() {
        List<Coupon> list = new ArrayList<>();

        String sql = """
                SELECT * FROM Coupon
                WHERE is_active = TRUE
                  AND used_count < max_uses
                  AND (valid_until IS NULL OR valid_until > CURRENT_TIMESTAMP)
                ORDER BY created_at DESC
                """;

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
