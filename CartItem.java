package model;

/**
 * Entity class representing an item in a customer's shopping cart.
 * 
 * <p>This class extends Entity and represents a single product item that a customer
 * has added to their shopping cart. Each cart item contains information about the
 * product, its price, and the quantity (in kilograms) the customer wants to purchase.
 * 
 * <p>The class provides methods to:
 * <ul>
 *   <li>Store product information (ID, name, unit price, quantity)</li>
 *   <li>Calculate the subtotal for the item (unit price × quantity)</li>
 *   <li>Display a formatted name for the item</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * <p>ENCAPSULATION: Private fields with getters/setters
 * <p>POLYMORPHISM: Overrides getDisplayName()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CartItem extends Entity {

    /**
     * The unique identifier of the product this cart item represents.
     */
    private int productId;
    
    /**
     * The name of the product.
     */
    private String name;
    
    /**
     * The unit price of the product (price per kilogram in Turkish Lira).
     */
    private double unitPrice;
    
    /**
     * The quantity of the product in the cart (in kilograms).
     */
    private double kg;

    /**
     * Constructor to create a new CartItem.
     * 
     * @param productId The unique identifier of the product
     * @param name The name of the product
     * @param unitPrice The unit price per kilogram (in TL)
     * @param kg The quantity in kilograms
     */
    public CartItem(int productId, String name, double unitPrice, double kg) {
        super(productId);
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.kg = kg;
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
     * Gets the product name.
     * 
     * @return The product name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unit price per kilogram.
     * 
     * @return The unit price in Turkish Lira
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Gets the quantity in kilograms.
     * 
     * @return The quantity in kg
     */
    public double getKg() {
        return kg;
    }

    /**
     * Sets the quantity in kilograms.
     * 
     * @param kg The quantity to set (in kg)
     */
    public void setKg(double kg) {
        this.kg = kg;
    }

    /**
     * Calculates the subtotal for this cart item.
     * 
     * <p>Multiplies the unit price by the quantity (kg) to get the total
     * price for this item.
     * 
     * @return The subtotal (unit price × quantity) in Turkish Lira
     */
    public double getSubTotal() {
        return unitPrice * kg;
    }

    /**
     * Gets a display name for this cart item.
     * 
     * <p>Returns a formatted string containing the product name and quantity.
     * Format: "ProductName (X.XX kg)"
     * 
     * @return A formatted display string for this cart item
     */
    @Override
    public String getDisplayName() {
        return name + " (" + kg + " kg)";
    }
}
