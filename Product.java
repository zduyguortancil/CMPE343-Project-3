package model;

/**
 * Entity class representing a product sold in the GreenGrocer store.
 * 
 * <p>This class extends Entity and represents a fruit or vegetable product
 * available for purchase. Products have pricing, stock management, and
 * can have discounts applied. The effective price may increase when stock
 * is low (below threshold) to encourage restocking.
 * 
 * <p>Product features:
 * <ul>
 *   <li>Name, type (fruit/vegetable), and pricing</li>
 *   <li>Stock quantity management (in kilograms)</li>
 *   <li>Low stock threshold for alerts</li>
 *   <li>Discount percentage support</li>
 *   <li>Product image storage</li>
 *   <li>Dynamic pricing based on stock level</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * <p>ENCAPSULATION: Private fields with getters/setters
 * <p>POLYMORPHISM: Overrides getDisplayName()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Product extends Entity {

    /**
     * The name of the product (e.g., "Apple", "Tomato").
     */
    private String name;
    
    /**
     * The base price per kilogram (in Turkish Lira).
     */
    private double price;
    
    /**
     * The current stock quantity (in kilograms).
     */
    private double stock;
    
    /**
     * The type of product ("fruit" or "vegetable").
     */
    private String type;
    
    /**
     * The low stock threshold (in kilograms).
     * When stock falls below this value, alerts may be triggered.
     */
    private int threshold;
    
    /**
     * The product image as a byte array (BLOB).
     */
    private byte[] image;
    
    /**
     * The discount percentage applied to this product (0 if no discount).
     */
    private double discountPercent;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Product instance with default values.
     */
    public Product() {
        super();
    }

    /**
     * Constructor with all parameters including image.
     * 
     * @param productId The unique identifier for this product
     * @param name The product name
     * @param price The base price per kilogram (in TL)
     * @param stock The current stock quantity (in kg)
     * @param type The product type ("fruit" or "vegetable")
     * @param threshold The low stock threshold (in kg)
     * @param image The product image as a byte array (null if no image)
     */
    public Product(int productId,
            String name,
            double price,
            double stock,
            String type,
            int threshold,
            byte[] image) {
        super(productId);
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.type = type;
        this.threshold = threshold;
        this.image = image;
    }

    /**
     * Constructor without image parameter.
     * 
     * <p>Creates a new Product instance without an image. The image is set to null.
     * 
     * @param productId The unique identifier for this product
     * @param name The product name
     * @param price The base price per kilogram (in TL)
     * @param stock The current stock quantity (in kg)
     * @param type The product type ("fruit" or "vegetable")
     * @param threshold The low stock threshold (in kg)
     */
    public Product(int productId,
            String name,
            double price,
            double stock,
            String type,
            int threshold) {
        this(productId, name, price, stock, type, threshold, null);
    }

    /**
     * Gets the product ID (backward compatibility method).
     * 
     * <p>This method maps to the inherited getId() method for backward compatibility.
     * 
     * @return The product ID
     */
    public int getProductId() {
        return getId();
    }

    /**
     * Sets the product ID (backward compatibility method).
     * 
     * <p>This method maps to the inherited setId() method for backward compatibility.
     * 
     * @param productId The product ID to set
     */
    public void setProductId(int productId) {
        setId(productId);
    }

    /**
     * Gets the product name.
     * 
     * @return The product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name.
     * 
     * @param name The product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the base price per kilogram.
     * 
     * @return The base price in Turkish Lira
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the base price per kilogram.
     * 
     * @param price The base price to set (in TL per kg)
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the current stock quantity.
     * 
     * @return The stock quantity in kilograms
     */
    public double getStock() {
        return stock;
    }

    /**
     * Sets the current stock quantity.
     * 
     * @param stock The stock quantity to set (in kg)
     */
    public void setStock(double stock) {
        this.stock = stock;
    }

    /**
     * Gets the product type.
     * 
     * @return The product type ("fruit" or "vegetable")
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the product type.
     * 
     * @param type The product type to set ("fruit" or "vegetable")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the low stock threshold.
     * 
     * @return The threshold in kilograms
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Sets the low stock threshold.
     * 
     * @param threshold The threshold to set (in kg)
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Gets the product image.
     * 
     * @return The product image as a byte array, or null if no image
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the product image.
     * 
     * @param image The product image as a byte array (null if no image)
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Gets the discount percentage.
     * 
     * @return The discount percentage (0 if no discount)
     */
    public double getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Sets the discount percentage.
     * 
     * @param discountPercent The discount percentage to set (0 for no discount)
     */
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    /**
     * Gets the original base price (alias for getPrice()).
     * 
     * @return The original base price in Turkish Lira
     */
    public double getOriginalPrice() {
        return price;
    }

    /**
     * Calculates the effective price per kilogram.
     * 
     * <p>The effective price is calculated as follows:
     * <ol>
     *   <li>Start with the base price</li>
     *   <li>Apply discount percentage if applicable</li>
     *   <li>If stock is at or below threshold, double the price (to encourage restocking)</li>
     * </ol>
     * 
     * @return The effective price per kilogram in Turkish Lira
     */
    public double getEffectivePrice() {
        double discountedPrice = price;
        if (discountPercent > 0) {
            discountedPrice = price * (1 - discountPercent / 100.0);
        }
        return (stock <= threshold) ? discountedPrice * 2.0 : discountedPrice;
    }

    /**
     * Gets a display name for this product.
     * 
     * <p>Returns a formatted string containing the product name and effective price.
     * Format: "ProductName - X.XX TL"
     * 
     * @return A formatted display string for this product
     */
    @Override
    public String getDisplayName() {
        return name + " - " + String.format("%.2f TL", getEffectivePrice());
    }
}
