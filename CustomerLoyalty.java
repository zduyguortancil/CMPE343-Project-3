package model;

import java.sql.Timestamp;

/**
 * Entity class representing a customer's loyalty program information.
 * 
 * <p>This class extends Entity and manages customer loyalty points, tiers, and discounts.
 * The loyalty program has four tiers (BRONZE, SILVER, GOLD, PLATINUM) with increasing
 * discount percentages based on the customer's total points.
 * 
 * <p>Features:
 * <ul>
 *   <li>Points accumulation (1 point per 10 TL spent)</li>
 *   <li>Automatic tier assignment based on points</li>
 *   <li>Tier-based discount percentages</li>
 *   <li>Total spending tracking</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CustomerLoyalty extends Entity {

    /**
     * Enumeration representing loyalty program tiers.
     * 
     * <p>Each tier has a minimum points requirement and a discount percentage.
     * Tiers are: BRONZE (0 points, 0% discount), SILVER (500 points, 5% discount),
     * GOLD (1000 points, 10% discount), PLATINUM (2500 points, 15% discount).
     */
    public enum Tier {
        /** Bronze tier: 0 points minimum, 0% discount */
        BRONZE(0, 0),
        /** Silver tier: 500 points minimum, 5% discount */
        SILVER(500, 5),
        /** Gold tier: 1000 points minimum, 10% discount */
        GOLD(1000, 10),
        /** Platinum tier: 2500 points minimum, 15% discount */
        PLATINUM(2500, 15);

        /**
         * The minimum points required to reach this tier.
         */
        private final int minPoints;
        
        /**
         * The discount percentage for this tier.
         */
        private final int discountPercent;

        /**
         * Constructor for Tier enum.
         * 
         * @param minPoints The minimum points required for this tier
         * @param discountPercent The discount percentage for this tier
         */
        Tier(int minPoints, int discountPercent) {
            this.minPoints = minPoints;
            this.discountPercent = discountPercent;
        }

        /**
         * Gets the minimum points required for this tier.
         * 
         * @return The minimum points
         */
        public int getMinPoints() {
            return minPoints;
        }

        /**
         * Gets the discount percentage for this tier.
         * 
         * @return The discount percentage
         */
        public int getDiscountPercent() {
            return discountPercent;
        }

        /**
         * Determines the appropriate tier based on the number of points.
         * 
         * <p>Returns the highest tier that the customer qualifies for based on their points.
         * Tiers are checked in descending order (PLATINUM, GOLD, SILVER, BRONZE).
         * 
         * @param points The customer's current points
         * @return The appropriate Tier for the given points
         */
        public static Tier fromPoints(int points) {
            if (points >= PLATINUM.minPoints)
                return PLATINUM;
            if (points >= GOLD.minPoints)
                return GOLD;
            if (points >= SILVER.minPoints)
                return SILVER;
            return BRONZE;
        }
    }

    /**
     * The username of the customer this loyalty record belongs to.
     */
    private String username;
    
    /**
     * The current number of loyalty points the customer has accumulated.
     */
    private int points;
    
    /**
     * The current loyalty tier based on points.
     */
    private Tier tier;
    
    /**
     * The total amount the customer has spent (in Turkish Lira).
     */
    private double totalSpent;
    
    /**
     * The timestamp when this loyalty record was created.
     */
    private Timestamp createdAt;
    
    /**
     * The timestamp when this loyalty record was last updated.
     */
    private Timestamp updatedAt;

    /**
     * Default constructor.
     * 
     * <p>Creates a new CustomerLoyalty instance with default values.
     * The tier is initialized to BRONZE.
     */
    public CustomerLoyalty() {
        super();
        this.tier = Tier.BRONZE;
    }

    /**
     * Constructor with all parameters.
     * 
     * @param loyaltyId The unique identifier for this loyalty record
     * @param username The username of the customer
     * @param points The current loyalty points
     * @param tierStr The tier as a string (will be converted to Tier enum)
     * @param totalSpent The total amount spent by the customer
     */
    public CustomerLoyalty(int loyaltyId, String username, int points,
            String tierStr, double totalSpent) {
        super(loyaltyId);
        this.username = username;
        this.points = points;
        try {
            this.tier = Tier.valueOf(tierStr.toUpperCase());
        } catch (Exception e) {
            this.tier = Tier.BRONZE;
        }
        this.totalSpent = totalSpent;
    }

    /**
     * Gets the username of the customer.
     * 
     * @return The customer username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the customer.
     * 
     * @param username The customer username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the current loyalty points.
     * 
     * @return The current points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Sets the loyalty points and automatically updates the tier.
     * 
     * <p>When points are set, the tier is automatically recalculated based on
     * the new point value using Tier.fromPoints().
     * 
     * @param points The points to set
     */
    public void setPoints(int points) {
        this.points = points;
        updateTier();
    }

    /**
     * Gets the current loyalty tier.
     * 
     * @return The current Tier
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Gets the tier name as a string.
     * 
     * @return The tier name (BRONZE, SILVER, GOLD, or PLATINUM)
     */
    public String getTierName() {
        return tier.name();
    }

    /**
     * Gets the total amount spent by the customer.
     * 
     * @return The total spent amount in Turkish Lira
     */
    public double getTotalSpent() {
        return totalSpent;
    }

    /**
     * Sets the total amount spent by the customer.
     * 
     * @param totalSpent The total spent amount to set (in TL)
     */
    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return The creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     * 
     * @return The update timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     * 
     * @param updatedAt The update timestamp to set
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Adds points to the customer's current point balance and updates the tier.
     * 
     * <p>This method adds the specified number of points to the current balance
     * and automatically recalculates the tier based on the new total.
     * 
     * @param additionalPoints The number of points to add
     */
    public void addPoints(int additionalPoints) {
        this.points += additionalPoints;
        updateTier();
    }

    /**
     * Updates the tier based on the current points.
     * 
     * <p>This private method is called automatically whenever points are changed.
     * It uses Tier.fromPoints() to determine the appropriate tier.
     */
    private void updateTier() {
        this.tier = Tier.fromPoints(this.points);
    }

    /**
     * Gets the discount percentage for the current tier.
     * 
     * <p>Returns the discount percentage associated with the customer's current tier.
     * This can be used to calculate discounts on orders.
     * 
     * @return The discount percentage for the current tier (0-15%)
     */
    public int getDiscountPercent() {
        return tier.getDiscountPercent();
    }

    /**
     * Calculates the number of loyalty points to earn from an order total.
     * 
     * <p>The loyalty program awards 1 point for every 10 Turkish Lira spent.
     * This method calculates the points based on the order total.
     * 
     * @param orderTotal The total amount of the order (in Turkish Lira)
     * @return The number of points to award (orderTotal / 10, rounded down)
     */
    public static int calculatePointsFromOrder(double orderTotal) {
        return (int) (orderTotal / 10);
    }

    /**
     * Gets a display name for this loyalty record.
     * 
     * <p>Returns a formatted string containing the username, tier, and points.
     * Format: "username - TIER (X points)"
     * 
     * @return A formatted display string for this loyalty record
     */
    @Override
    public String getDisplayName() {
        return username + " - " + tier.name() + " (" + points + " points)";
    }
}
