package model;

import java.sql.Timestamp;

/**
 * Entity class representing a discount coupon for the GreenGrocer application.
 * 
 * <p>This class extends Entity and represents a coupon that can be applied to orders
 * to provide discounts. Coupons can offer either a percentage-based discount or a
 * fixed amount discount, or both (in which case the maximum discount is applied).
 * 
 * <p>Coupons have the following features:
 * <ul>
 *   <li>Unique coupon code for identification</li>
 *   <li>Percentage-based or fixed amount discount</li>
 *   <li>Minimum order amount requirement</li>
 *   <li>Validity period (valid from/until timestamps)</li>
 *   <li>Usage limits (maximum uses and current usage count)</li>
 *   <li>Active/inactive status</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Coupon extends Entity {

    /**
     * The unique coupon code that customers use to apply the discount.
     */
    private String code;
    
    /**
     * The discount percentage (0 if not applicable).
     * If both discountPercent and discountAmount are set, the maximum discount is applied.
     */
    private double discountPercent;
    
    /**
     * The fixed discount amount in Turkish Lira (0 if not applicable).
     * If both discountPercent and discountAmount are set, the maximum discount is applied.
     */
    private double discountAmount;
    
    /**
     * The minimum order amount required to use this coupon (in Turkish Lira).
     */
    private double minOrderAmount;
    
    /**
     * The timestamp when the coupon becomes valid (null if no start date).
     */
    private Timestamp validFrom;
    
    /**
     * The timestamp when the coupon expires (null if no expiration date).
     */
    private Timestamp validUntil;
    
    /**
     * The maximum number of times this coupon can be used.
     */
    private int maxUses;
    
    /**
     * The current number of times this coupon has been used.
     */
    private int usedCount;
    
    /**
     * Whether the coupon is currently active and can be used.
     */
    private boolean isActive;
    
    /**
     * The timestamp when this coupon was created.
     */
    private Timestamp createdAt;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Coupon instance with default values.
     */
    public Coupon() {
        super();
    }

    /**
     * Constructor with all parameters.
     * 
     * @param couponId The unique identifier for this coupon
     * @param code The coupon code
     * @param discountPercent The discount percentage (0 if not applicable)
     * @param discountAmount The fixed discount amount (0 if not applicable)
     * @param minOrderAmount The minimum order amount required
     * @param validFrom The validity start timestamp (null if no start date)
     * @param validUntil The validity end timestamp (null if no expiration)
     * @param maxUses The maximum number of uses allowed
     * @param usedCount The current number of uses
     * @param isActive Whether the coupon is active
     */
    public Coupon(int couponId, String code, double discountPercent, double discountAmount,
            double minOrderAmount, Timestamp validFrom, Timestamp validUntil,
            int maxUses, int usedCount, boolean isActive) {
        super(couponId);
        this.code = code;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        this.usedCount = usedCount;
        this.isActive = isActive;
    }

    /**
     * Gets the coupon code.
     * 
     * @return The coupon code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the coupon code.
     * 
     * @param code The coupon code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the discount percentage.
     * 
     * @return The discount percentage (0 if not applicable)
     */
    public double getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Sets the discount percentage.
     * 
     * @param discountPercent The discount percentage to set
     */
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    /**
     * Gets the fixed discount amount.
     * 
     * @return The discount amount in Turkish Lira (0 if not applicable)
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the fixed discount amount.
     * 
     * @param discountAmount The discount amount to set (in TL)
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Gets the minimum order amount required.
     * 
     * @return The minimum order amount in Turkish Lira
     */
    public double getMinOrderAmount() {
        return minOrderAmount;
    }

    /**
     * Sets the minimum order amount required.
     * 
     * @param minOrderAmount The minimum order amount to set (in TL)
     */
    public void setMinOrderAmount(double minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    /**
     * Gets the validity start timestamp.
     * 
     * @return The validity start timestamp, or null if no start date
     */
    public Timestamp getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the validity start timestamp.
     * 
     * @param validFrom The validity start timestamp (null if no start date)
     */
    public void setValidFrom(Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Gets the validity end timestamp.
     * 
     * @return The validity end timestamp, or null if no expiration date
     */
    public Timestamp getValidUntil() {
        return validUntil;
    }

    /**
     * Sets the validity end timestamp.
     * 
     * @param validUntil The validity end timestamp (null if no expiration)
     */
    public void setValidUntil(Timestamp validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Gets the maximum number of uses allowed.
     * 
     * @return The maximum number of uses
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * Sets the maximum number of uses allowed.
     * 
     * @param maxUses The maximum number of uses to set
     */
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    /**
     * Gets the current number of times this coupon has been used.
     * 
     * @return The current usage count
     */
    public int getUsedCount() {
        return usedCount;
    }

    /**
     * Sets the current usage count.
     * 
     * @param usedCount The usage count to set
     */
    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    /**
     * Checks if the coupon is currently active.
     * 
     * @return true if the coupon is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of the coupon.
     * 
     * @param active true to activate the coupon, false to deactivate it
     */
    public void setActive(boolean active) {
        isActive = active;
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
     * Checks if the coupon is valid for use at the current time.
     * 
     * <p>A coupon is valid if:
     * <ul>
     *   <li>It is active</li>
     *   <li>It has not exceeded the maximum number of uses</li>
     *   <li>The current time is after the validFrom timestamp (if set)</li>
     *   <li>The current time is before the validUntil timestamp (if set)</li>
     * </ul>
     * 
     * @return true if the coupon is valid for use, false otherwise
     */
    public boolean isValid() {
        if (!isActive)
            return false;
        if (usedCount >= maxUses)
            return false;

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (validFrom != null && now.before(validFrom))
            return false;
        if (validUntil != null && now.after(validUntil))
            return false;

        return true;
    }

    /**
     * Calculates the discount amount for a given order total.
     * 
     * <p>This method first checks if the coupon is valid and if the order total
     * meets the minimum order amount requirement. If both conditions are met,
     * it calculates the discount as follows:
     * <ul>
     *   <li>If discountPercent is set, calculates percentage-based discount</li>
     *   <li>If discountAmount is set, uses the fixed amount</li>
     *   <li>If both are set, applies the maximum of the two</li>
     *   <li>The discount never exceeds the order total</li>
     * </ul>
     * 
     * @param orderTotal The total amount of the order (in Turkish Lira)
     * @return The discount amount to apply (in Turkish Lira), or 0 if coupon is invalid
     *         or order total is below minimum requirement
     */
    public double calculateDiscount(double orderTotal) {
        if (!isValid())
            return 0;
        if (orderTotal < minOrderAmount)
            return 0;

        double discount = 0;
        if (discountPercent > 0) {
            discount = orderTotal * (discountPercent / 100.0);
        }
        if (discountAmount > 0) {
            discount = Math.max(discount, discountAmount);
        }

        return Math.min(discount, orderTotal); // Don't exceed order total
    }

    /**
     * Gets a display name for this coupon.
     * 
     * <p>Returns a formatted string containing the coupon code and discount information.
     * If a percentage discount is set, displays "CODE - X% off".
     * Otherwise, displays "CODE - X TL off".
     * 
     * @return A formatted display string for this coupon
     */
    @Override
    public String getDisplayName() {
        if (discountPercent > 0) {
            return code + " - " + discountPercent + "% off";
        }
        return code + " - " + discountAmount + " TL off";
    }
}
