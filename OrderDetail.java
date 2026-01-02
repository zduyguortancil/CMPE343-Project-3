package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing detailed order information with customer and product details.
 * 
 * <p>This class extends Entity and provides comprehensive order information including
 * customer details, delivery information, and a list of all products in the order.
 * This is used primarily for displaying orders to carriers in the available deliveries view.
 * 
 * <p>The OrderDetail includes:
 * <ul>
 *   <li>Customer information (username, address, phone)</li>
 *   <li>Order status and lifecycle timestamps</li>
 *   <li>Delivery information (requested date, actual delivery, carrier)</li>
 *   <li>Total cost including VAT</li>
 *   <li>List of all order items with product details</li>
 *   <li>Cancellation information if applicable</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class OrderDetail extends Entity {

    /**
     * The unique identifier of the order.
     */
    private int orderId;
    
    /**
     * The username of the customer who placed this order.
     */
    private String customerUsername;
    
    /**
     * The delivery address of the customer.
     */
    private String customerAddress;
    
    /**
     * The phone number of the customer.
     */
    private String customerPhone;
    
    /**
     * The current status of the order (e.g., "NEW", "ASSIGNED", "DELIVERED", "CANCELLED").
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
     * The total cost of the order including VAT (in Turkish Lira).
     */
    private double totalVatIncluded;
    
    /**
     * The timestamp when the order was created.
     */
    private Timestamp createdAt;
    
    /**
     * The timestamp when the order was cancelled (null if not cancelled).
     */
    private Timestamp cancelledAt;
    
    /**
     * The reason for cancellation (null if not cancelled).
     */
    private String cancelReason;

    /**
     * The list of product items in this order.
     */
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Default constructor.
     * 
     * <p>Creates a new OrderDetail instance with default values.
     */
    public OrderDetail() {
        super();
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
     * Sets the order ID and also updates the inherited ID.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
        setId(orderId);
    }

    /**
     * Gets the customer username.
     * 
     * @return The customer username
     */
    public String getCustomerUsername() {
        return customerUsername;
    }

    /**
     * Sets the customer username.
     * 
     * @param customerUsername The customer username to set
     */
    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    /**
     * Gets the customer address.
     * 
     * @return The customer address
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * Sets the customer address.
     * 
     * @param customerAddress The customer address to set
     */
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    /**
     * Gets the customer phone number.
     * 
     * @return The customer phone number
     */
    public String getCustomerPhone() {
        return customerPhone;
    }

    /**
     * Sets the customer phone number.
     * 
     * @param customerPhone The customer phone number to set
     */
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    /**
     * Gets the order status.
     * 
     * @return The status
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
     * Gets the total cost including VAT.
     * 
     * @return The total cost in Turkish Lira
     */
    public double getTotalVatIncluded() {
        return totalVatIncluded;
    }

    /**
     * Sets the total cost including VAT.
     * 
     * @param totalVatIncluded The total cost to set (in TL)
     */
    public void setTotalVatIncluded(double totalVatIncluded) {
        this.totalVatIncluded = totalVatIncluded;
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
     * Gets the cancellation timestamp.
     * 
     * @return The cancellation timestamp, or null if not cancelled
     */
    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    /**
     * Sets the cancellation timestamp.
     * 
     * @param cancelledAt The cancellation timestamp to set (null if not cancelled)
     */
    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    /**
     * Gets the cancellation reason.
     * 
     * @return The cancellation reason, or null if not cancelled
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * Sets the cancellation reason.
     * 
     * @param cancelReason The cancellation reason to set (null if not cancelled)
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * Gets the list of order items.
     * 
     * @return The list of OrderItem objects
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Sets the list of order items.
     * 
     * @param items The list of OrderItem objects to set
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * Adds an order item to the list.
     * 
     * @param item The OrderItem to add
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Gets a formatted string representation of all products in this order.
     * 
     * <p>Returns a comma-separated list of product names with their quantities.
     * Format: "Product1 (X.XX kg), Product2 (Y.YY kg), ..."
     * Returns "No items" if the order has no items.
     * 
     * @return A formatted string listing all products and quantities
     */
    public String getProductListString() {
        if (items == null || items.isEmpty())
            return "No items";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (i > 0)
                sb.append(", ");
            sb.append(item.getProductName())
                    .append(" (").append(item.getKg()).append(" kg)");
        }
        return sb.toString();
    }

    /**
     * Checks if the order can be cancelled.
     * 
     * <p>An order can be cancelled if:
     * <ul>
     *   <li>The order status is "NEW"</li>
     *   <li>The order was created less than 1 hour ago</li>
     * </ul>
     * 
     * @return true if the order can be cancelled, false otherwise
     */
    public boolean canCancel() {
        if (!"NEW".equalsIgnoreCase(status))
            return false;
        if (createdAt == null)
            return false;

        long oneHourMs = 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        return (now - createdAt.getTime()) <= oneHourMs;
    }

    /**
     * Gets a display name for this order detail.
     * 
     * <p>Returns a formatted string containing the order ID, customer username, and status.
     * Format: "Order #orderId - customerUsername - status"
     * 
     * @return A formatted display string for this order detail
     */
    @Override
    public String getDisplayName() {
        return "Order #" + orderId + " - " + customerUsername + " - " + status;
    }

    /**
     * Inner class representing a single product item within an order.
     * 
     * <p>This class stores information about a product that was ordered, including
     * the product ID, name, quantity, and the price at the time of order (which
     * may differ from the current price).
     * 
     * @author GreenGrocer Team
     * @version 1.0
     */
    public static class OrderItem {
        /**
         * The unique identifier of the product.
         */
        private int productId;
        
        /**
         * The name of the product.
         */
        private String productName;
        
        /**
         * The quantity of the product ordered (in kilograms).
         */
        private double kg;
        
        /**
         * The price per kilogram at the time the order was placed.
         * This is the effective price (getEffectivePrice) at order time,
         * which may include discounts or special pricing.
         */
        private double priceAtTime; // getEffectivePrice at order time

        /**
         * Default constructor.
         * 
         * <p>Creates a new OrderItem instance with default values.
         */
        public OrderItem() {
        }

        /**
         * Constructor with all parameters.
         * 
         * @param productId The unique identifier of the product
         * @param productName The name of the product
         * @param kg The quantity ordered (in kilograms)
         * @param priceAtTime The price per kilogram at the time of order (in TL)
         */
        public OrderItem(int productId, String productName, double kg, double priceAtTime) {
            this.productId = productId;
            this.productName = productName;
            this.kg = kg;
            this.priceAtTime = priceAtTime;
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
         * Gets the product name.
         * 
         * @return The product name
         */
        public String getProductName() {
            return productName;
        }

        /**
         * Sets the product name.
         * 
         * @param productName The product name to set
         */
        public void setProductName(String productName) {
            this.productName = productName;
        }

        /**
         * Gets the quantity ordered.
         * 
         * @return The quantity in kilograms
         */
        public double getKg() {
            return kg;
        }

        /**
         * Sets the quantity ordered.
         * 
         * @param kg The quantity to set (in kg)
         */
        public void setKg(double kg) {
            this.kg = kg;
        }

        /**
         * Gets the price per kilogram at the time of order.
         * 
         * @return The price per kilogram in Turkish Lira
         */
        public double getPriceAtTime() {
            return priceAtTime;
        }

        /**
         * Sets the price per kilogram at the time of order.
         * 
         * @param priceAtTime The price to set (in TL per kg)
         */
        public void setPriceAtTime(double priceAtTime) {
            this.priceAtTime = priceAtTime;
        }

        /**
         * Calculates the line total for this order item.
         * 
         * <p>Multiplies the quantity (kg) by the price per kilogram to get
         * the total cost for this item.
         * 
         * @return The line total (quantity × price) in Turkish Lira
         */
        public double getLineTotal() {
            return kg * priceAtTime;
        }
    }
}
