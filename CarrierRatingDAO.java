package dao;

import model.CarrierRating;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Carrier Rating operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for carrier
 * ratings. Customers can rate carriers after delivery, and this DAO manages
 * those
 * ratings, calculates averages, and provides rating summaries.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Rating CRUD operations</li>
 * <li>Add ratings for carriers</li>
 * <li>Check if an order has been rated</li>
 * <li>Calculate average ratings for carriers</li>
 * <li>Get all ratings for a carrier</li>
 * <li>Get rating counts</li>
 * <li>Get carrier rating summaries (for owner view)</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;CarrierRating&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CarrierRatingDAO extends AbstractDAO<CarrierRating> {

    /**
     * Singleton instance of CarrierRatingDAO.
     */
    private static final CarrierRatingDAO INSTANCE = new CarrierRatingDAO();

    /**
     * Gets the singleton instance of CarrierRatingDAO.
     * 
     * @return The single instance of CarrierRatingDAO
     */
    public static CarrierRatingDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "CarrierRating";
    }

    @Override
    protected String getIdColumnName() {
        return "rating_id";
    }

    @Override
    protected CarrierRating mapResultSetToEntity(ResultSet rs) throws Exception {
        return new CarrierRating(
                rs.getInt("rating_id"),
                rs.getInt("order_id"),
                rs.getString("carrier_username"),
                rs.getString("customer_username"),
                rs.getInt("rating"),
                rs.getString("comment"),
                rs.getTimestamp("rated_at"));
    }

    /**
     * Adds a rating for a carrier from a customer.
     * 
     * <p>
     * Creates a new rating record for a specific order. Each order can only
     * have one rating.
     * 
     * @param orderId          The ID of the order being rated
     * @param carrierUsername  The username of the carrier being rated
     * @param customerUsername The username of the customer giving the rating
     * @param rating           The rating value (1-5)
     * @param comment          Optional comment from the customer
     * @return true if the rating was added successfully, false otherwise
     */
    public static boolean addRating(int orderId, String carrierUsername,
            String customerUsername, int rating, String comment) {
        String sql = """
                INSERT INTO CarrierRating(order_id, carrier_username, customer_username, rating, comment)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setString(2, carrierUsername);
            ps.setString(3, customerUsername);
            ps.setInt(4, rating);
            ps.setString(5, comment);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if an order has already been rated.
     * 
     * @param orderId The order ID to check
     * @return true if the order has been rated, false otherwise
     */
    public static boolean isOrderRated(int orderId) {
        String sql = "SELECT 1 FROM CarrierRating WHERE order_id = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the average rating for a carrier.
     * 
     * <p>
     * Calculates the average of all ratings given to the specified carrier.
     * 
     * @param carrierUsername The username of the carrier
     * @return The average rating (0.0 if no ratings exist)
     */
    public static double getCarrierAverageRating(String carrierUsername) {
        String sql = "SELECT AVG(rating) as avg_rating FROM CarrierRating WHERE carrier_username = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Gets all ratings for a specific carrier.
     * 
     * <p>
     * Retrieves all rating records for the specified carrier, ordered by
     * most recent first.
     * 
     * @param carrierUsername The username of the carrier
     * @return A list of ratings for the carrier, ordered by rated_at DESC
     */
    public static List<CarrierRating> getRatingsByCarrier(String carrierUsername) {
        List<CarrierRating> list = new ArrayList<>();

        String sql = """
                SELECT * FROM CarrierRating
                WHERE carrier_username = ?
                ORDER BY rated_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(INSTANCE.mapResultSetToEntity(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets the total number of ratings for a carrier.
     * 
     * @param carrierUsername The username of the carrier
     * @return The number of ratings the carrier has received
     */
    public static int getCarrierRatingCount(String carrierUsername) {
        String sql = "SELECT COUNT(*) as cnt FROM CarrierRating WHERE carrier_username = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets a summary of ratings for all carriers.
     * 
     * <p>
     * Retrieves aggregated rating statistics (average rating and count)
     * for each carrier, ordered by average rating (highest first). Used
     * primarily for owner/admin views.
     * 
     * @return A list of CarrierRatingSummary objects, ordered by average rating
     *         DESC
     */
    public static List<CarrierRatingSummary> getAllCarrierRatings() {
        List<CarrierRatingSummary> list = new ArrayList<>();

        String sql = """
                SELECT carrier_username,
                       AVG(rating) as avg_rating,
                       COUNT(*) as rating_count
                FROM CarrierRating
                GROUP BY carrier_username
                ORDER BY avg_rating DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CarrierRatingSummary(
                        rs.getString("carrier_username"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("rating_count")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Summary class for carrier rating statistics.
     * 
     * <p>
     * This inner class holds aggregated rating information for a carrier,
     * including the average rating and total number of ratings received.
     * 
     * @author GreenGrocer Team
     * @version 1.0
     */
    public static class CarrierRatingSummary {
        /**
         * The username of the carrier.
         */
        private final String carrierUsername;

        /**
         * The average rating for this carrier.
         */
        private final double averageRating;

        /**
         * The total number of ratings received by this carrier.
         */
        private final int ratingCount;

        /**
         * Constructor for CarrierRatingSummary.
         * 
         * @param carrierUsername The username of the carrier
         * @param averageRating   The average rating value
         * @param ratingCount     The total number of ratings
         */
        public CarrierRatingSummary(String carrierUsername, double averageRating, int ratingCount) {
            this.carrierUsername = carrierUsername;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }

        /**
         * Gets the carrier username.
         * 
         * @return The carrier username
         */
        public String getCarrierUsername() {
            return carrierUsername;
        }

        /**
         * Gets the average rating.
         * 
         * @return The average rating value
         */
        public double getAverageRating() {
            return averageRating;
        }

        /**
         * Gets the rating count.
         * 
         * @return The total number of ratings
         */
        public int getRatingCount() {
            return ratingCount;
        }
    }
}
