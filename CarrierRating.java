package model;

import java.sql.Timestamp;

/**
 * Entity class representing a customer's rating of a carrier for a specific order.
 * 
 * <p>This class extends Entity and represents a rating given by a customer to a carrier
 * after order delivery. The rating is on a scale of 1 to 5 stars, and customers can
 * optionally provide a comment along with their rating.
 * 
 * <p>Each rating is associated with:
 * <ul>
 *   <li>A specific order (orderId)</li>
 *   <li>The carrier being rated (carrierUsername)</li>
 *   <li>The customer giving the rating (customerUsername)</li>
 *   <li>A numeric rating (1-5)</li>
 *   <li>An optional comment</li>
 *   <li>A timestamp of when the rating was given</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CarrierRating extends Entity {

    /**
     * The unique identifier of the order this rating is for.
     */
    private int orderId;
    
    /**
     * The username of the carrier being rated.
     */
    private String carrierUsername;
    
    /**
     * The username of the customer giving the rating.
     */
    private String customerUsername;
    
    /**
     * The rating value on a scale of 1 to 5.
     */
    private int rating; // 1-5
    
    /**
     * Optional comment provided by the customer along with the rating.
     */
    private String comment;
    
    /**
     * Timestamp indicating when the rating was given.
     */
    private Timestamp ratedAt;

    /**
     * Default constructor.
     * 
     * <p>Creates a new CarrierRating instance with default values.
     */
    public CarrierRating() {
        super();
    }

    /**
     * Constructor with all parameters.
     * 
     * <p>Creates a new CarrierRating instance with the specified values.
     * 
     * @param ratingId The unique identifier for this rating
     * @param orderId The unique identifier of the order this rating is for
     * @param carrierUsername The username of the carrier being rated
     * @param customerUsername The username of the customer giving the rating
     * @param rating The rating value (1-5)
     * @param comment Optional comment provided by the customer
     * @param ratedAt Timestamp indicating when the rating was given
     */
    public CarrierRating(int ratingId, int orderId, String carrierUsername,
            String customerUsername, int rating, String comment, Timestamp ratedAt) {
        super(ratingId);
        this.orderId = orderId;
        this.carrierUsername = carrierUsername;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.comment = comment;
        this.ratedAt = ratedAt;
    }

    /**
     * Gets the order ID associated with this rating.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID associated with this rating.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the username of the carrier being rated.
     * 
     * @return The carrier username
     */
    public String getCarrierUsername() {
        return carrierUsername;
    }

    /**
     * Sets the username of the carrier being rated.
     * 
     * @param carrierUsername The carrier username to set
     */
    public void setCarrierUsername(String carrierUsername) {
        this.carrierUsername = carrierUsername;
    }

    /**
     * Gets the username of the customer giving the rating.
     * 
     * @return The customer username
     */
    public String getCustomerUsername() {
        return customerUsername;
    }

    /**
     * Sets the username of the customer giving the rating.
     * 
     * @param customerUsername The customer username to set
     */
    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    /**
     * Gets the rating value (1-5).
     * 
     * @return The rating value
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating value.
     * 
     * <p>The rating is automatically clamped to the valid range of 1-5.
     * If a value less than 1 is provided, it is set to 1.
     * If a value greater than 5 is provided, it is set to 5.
     * 
     * @param rating The rating value to set (will be clamped to 1-5)
     */
    public void setRating(int rating) {
        if (rating < 1)
            rating = 1;
        if (rating > 5)
            rating = 5;
        this.rating = rating;
    }

    /**
     * Gets the optional comment provided with the rating.
     * 
     * @return The comment, or null if no comment was provided
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the optional comment for this rating.
     * 
     * @param comment The comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the timestamp when the rating was given.
     * 
     * @return The rating timestamp
     */
    public Timestamp getRatedAt() {
        return ratedAt;
    }

    /**
     * Sets the timestamp when the rating was given.
     * 
     * @param ratedAt The rating timestamp to set
     */
    public void setRatedAt(Timestamp ratedAt) {
        this.ratedAt = ratedAt;
    }

    /**
     * Gets a display name for this rating.
     * 
     * <p>Returns a formatted string containing the carrier username and rating value.
     * Format: "carrierUsername - rating stars"
     * 
     * @return A formatted display string for this rating
     */
    @Override
    public String getDisplayName() {
        return carrierUsername + " - " + rating + " stars";
    }
}
