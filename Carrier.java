package model;

/**
 * Entity class representing a carrier user in the GreenGrocer application.
 * 
 * <p>This class extends Person and represents a delivery carrier who delivers
 * orders to customers. Carriers can view available orders, accept delivery assignments,
 * and mark orders as delivered.
 * 
 * <p>Carrier capabilities:
 * <ul>
 *   <li>View available orders for delivery</li>
 *   <li>Accept delivery assignments</li>
 *   <li>Mark orders as delivered</li>
 *   <li>View delivery history</li>
 *   <li>Access carrier dashboard</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Person
 * <p>POLYMORPHISM: Overrides abstract methods getRole() and getDashboardView()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Carrier extends Person {

    /**
     * Default constructor.
     * 
     * <p>Creates a new Carrier instance with default values.
     */
    public Carrier() {
        super();
    }

    /**
     * Constructor with ID and contact information.
     * 
     * @param id The unique identifier for the carrier
     * @param username The username for login
     * @param address The carrier's address
     * @param phone The carrier's phone number
     */
    public Carrier(int id, String username, String address, String phone) {
        super(id, username, address, phone);
    }

    /**
     * Constructor with username, password, and contact information.
     * 
     * <p>Creates a new carrier with the specified credentials and contact details.
     * This constructor is typically used when registering a new carrier.
     * 
     * @param username The username for login
     * @param password The password for authentication
     * @param address The carrier's address
     * @param phone The carrier's phone number
     */
    public Carrier(String username, String password, String address, String phone) {
        super();
        setUsername(username);
        setPassword(password);
        setAddress(address);
        setPhone(phone);
    }

    /**
     * Gets the role of this user.
     * 
     * <p>Returns "carrier" to identify this user as a carrier type.
     * This is used for role-based access control throughout the application.
     * 
     * @return The string "carrier"
     */
    @Override
    public String getRole() {
        return "carrier";
    }

    /**
     * Gets the FXML file path for the carrier dashboard view.
     * 
     * <p>Returns the path to the FXML file that defines the carrier interface.
     * This view provides delivery functionality including viewing available orders
     * and managing deliveries.
     * 
     * @return The path "/view/carrier.fxml"
     */
    @Override
    public String getDashboardView() {
        return "/view/carrier.fxml";
    }
}
