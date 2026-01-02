package model;

import java.sql.Timestamp;

/**
 * Entity class representing a history record of order status changes.
 * 
 * <p>This class extends Entity and tracks all status changes for orders,
 * providing an audit trail of order lifecycle events. Each time an order
 * status changes, a new history record is created.
 * 
 * <p>History information includes:
 * <ul>
 *   <li>The order ID that was changed</li>
 *   <li>The new status value</li>
 *   <li>When the change occurred</li>
 *   <li>Who made the change (username)</li>
 *   <li>Optional notes about the change</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class OrderStatusHistory extends Entity {

    /**
     * The unique identifier of the order whose status was changed.
     */
    private int orderId;
    
    /**
     * The new status value (e.g., "NEW", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "CANCELLED").
     */
    private String status;
    
    /**
     * The timestamp when the status change occurred.
     */
    private Timestamp changedAt;
    
    /**
     * The username of the user who made the status change.
     */
    private String changedBy;
    
    /**
     * Optional notes about the status change.
     */
    private String notes;

    /**
     * Default constructor.
     * 
     * <p>Creates a new OrderStatusHistory instance with default values.
     */
    public OrderStatusHistory() {
        super();
    }

    /**
     * Constructor with all parameters.
     * 
     * @param historyId The unique identifier for this history record
     * @param orderId The order ID whose status was changed
     * @param status The new status value
     * @param changedAt The timestamp when the change occurred
     * @param changedBy The username of the user who made the change
     * @param notes Optional notes about the change
     */
    public OrderStatusHistory(int historyId, int orderId, String status,
            Timestamp changedAt, String changedBy, String notes) {
        super(historyId);
        this.orderId = orderId;
        this.status = status;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.notes = notes;
    }

    /**
     * Gets the order ID.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the status value.
     * 
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status value.
     * 
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when the change occurred.
     * 
     * @return The change timestamp
     */
    public Timestamp getChangedAt() {
        return changedAt;
    }

    /**
     * Sets the timestamp when the change occurred.
     * 
     * @param changedAt The change timestamp to set
     */
    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    /**
     * Gets the username of the user who made the change.
     * 
     * @return The username
     */
    public String getChangedBy() {
        return changedBy;
    }

    /**
     * Sets the username of the user who made the change.
     * 
     * @param changedBy The username to set
     */
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    /**
     * Gets the optional notes about the change.
     * 
     * @return The notes, or null if no notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the optional notes about the change.
     * 
     * @param notes The notes to set (null if no notes)
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets a display name for this history record.
     * 
     * <p>Returns a formatted string containing the order ID, status, and change timestamp.
     * Format: "Order #orderId -> status at timestamp"
     * 
     * @return A formatted display string for this history record
     */
    @Override
    public String getDisplayName() {
        return "Order #" + orderId + " -> " + status + " at " + changedAt;
    }
}
