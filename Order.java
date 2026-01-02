package model;

import java.sql.Timestamp;

/**
 * Entity class representing a customer order in the GreenGrocer application.
 * 
 * <p>This class extends Entity and represents an order placed by a customer.
 * Orders can contain multiple products and have various statuses throughout
 * their lifecycle (NEW, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED).
 * 
 * <p>Order information includes:
 * <ul>
 *   <li>Customer identification (username)</li>
 *   <li>Product and quantity information</li>
 *   <li>Total cost (including VAT)</li>
 *   <li>Order status and lifecycle timestamps</li>
 *   <li>Delivery information (requested delivery date, actual delivery date, carrier)</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * <p>ENCAPSULATION: Private fields with getters/setters
 * <p>POLYMORPHISM: Overrides getDisplayName()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Order extends Entity {

    /**
     * The username of the customer who placed this order.
     */
    private String username;
    
    /**
     * The unique identifier of the product in this order.
     * Note: For orders with multiple products, use OrderDetail instead.
     */
    private int productId;
    
    /**
     * The quantity of the product ordered (in kilograms).
     */
    private double quantity;
    
    /**
     * The total cost of the order including VAT (in Turkish Lira).
     * Database field: Orders.total (VAT dahil)
     */
    private double total; // DB: Orders.total (VAT dahil)
    
    /**
     * The current status of the order (e.g., "NEW", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "CANCELLED").
     */
    private String status;
    
    /**
     * The timestamp when the customer requested delivery.
     */
    private Timestamp requestedDelivery;
    
    /**
     * The timestamp when the order was actually delivered (null if not yet delivered).
     */
    private Timestamp deliveredAt;
    
    /**
     * The username of the carrier assigned to deliver this order (null if not assigned).
     */
    private String carrierUsername;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Order instance with default values.
     */
    public Order() {
        super();
    }

    /**
     * Constructor with basic order information.
     * 
     * @param orderId The unique identifier for this order
     * @param username The username of the customer
     * @param productId The product ID
     * @param quantity The quantity ordered (in kg)
     * @param total The total cost including VAT (in TL)
     * @param status The order status
     */
    public Order(int orderId, String username, int productId, int quantity, double total, String status) {
        super(orderId);
        this.username = username;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
        this.status = status;
    }

    /**
     * Constructor with order and delivery information.
     * 
     * @param orderId The unique identifier for this order
     * @param username The username of the customer
     * @param status The order status
     * @param requestedDelivery The requested delivery timestamp
     * @param total The total cost including VAT (in TL)
     */
    public Order(int orderId, String username, String status,
            Timestamp requestedDelivery, double total) {
        super(orderId);
        this.username = username;
        this.status = status;
        this.requestedDelivery = requestedDelivery;
        this.total = total;
    }

    /**
     * Constructor with complete order and delivery information.
     * 
     * @param orderId The unique identifier for this order
     * @param username The username of the customer
     * @param status The order status
     * @param requestedDelivery The requested delivery timestamp
     * @param deliveredAt The actual delivery timestamp (null if not delivered)
     * @param carrierUsername The username of the assigned carrier (null if not assigned)
     * @param total The total cost including VAT (in TL)
     */
    public Order(int orderId, String username, String status,
            Timestamp requestedDelivery, Timestamp deliveredAt,
            String carrierUsername, double total) {
        super(orderId);
        this.username = username;
        this.status = status;
        this.requestedDelivery = requestedDelivery;
        this.deliveredAt = deliveredAt;
        this.carrierUsername = carrierUsername;
        this.total = total;
    }

    /**
     * Gets the order ID (backward compatibility method).
     * 
     * <p>This method maps to the inherited getId() method for backward compatibility.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return getId();
    }

    /**
     * Sets the order ID (backward compatibility method).
     * 
     * <p>This method maps to the inherited setId() method for backward compatibility.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        setId(orderId);
    }

    /**
     * Gets the customer username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the customer username.
     * 
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the product ID.
     * 
     * @return The product ID
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     * 
     * @param productId The product ID to set
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Gets the quantity ordered.
     * 
     * @return The quantity in kilograms
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity ordered.
     * 
     * @param quantity The quantity to set (in kg)
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the total cost (alias for getTotal()).
     * 
     * @return The total cost including VAT (in TL)
     */
    public double getTotalCost() {
        return total;
    }

    /**
     * Gets the total cost.
     * 
     * @return The total cost including VAT (in TL)
     */
    public double getTotal() {
        return total;
    }

    /**
     * Sets the total cost.
     * 
     * @param total The total cost to set (in TL, including VAT)
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Gets the order status.
     * 
     * @return The status (e.g., "NEW", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "CANCELLED")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the order status.
     * 
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the requested delivery timestamp.
     * 
     * @return The requested delivery timestamp
     */
    public Timestamp getRequestedDelivery() {
        return requestedDelivery;
    }

    /**
     * Sets the requested delivery timestamp.
     * 
     * @param requestedDelivery The requested delivery timestamp to set
     */
    public void setRequestedDelivery(Timestamp requestedDelivery) {
        this.requestedDelivery = requestedDelivery;
    }

    /**
     * Gets the actual delivery timestamp.
     * 
     * @return The delivery timestamp, or null if not yet delivered
     */
    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    /**
     * Sets the actual delivery timestamp.
     * 
     * @param deliveredAt The delivery timestamp to set (null if not delivered)
     */
    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    /**
     * Gets the carrier username.
     * 
     * @return The carrier username, or null if not assigned
     */
    public String getCarrierUsername() {
        return carrierUsername;
    }

    /**
     * Sets the carrier username.
     * 
     * @param carrierUsername The carrier username to set (null if not assigned)
     */
    public void setCarrierUsername(String carrierUsername) {
        this.carrierUsername = carrierUsername;
    }

    /**
     * Gets a display name for this order.
     * 
     * <p>Returns a formatted string containing the order ID and status.
     * Format: "Order #orderId - status"
     * 
     * @return A formatted display string for this order
     */
    @Override
    public String getDisplayName() {
        return "Order #" + getId() + " - " + status;
    }
}
