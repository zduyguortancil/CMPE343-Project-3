package model;

/**
 * Entity class representing an owner user in the GreenGrocer application.
 * 
 * <p>This class extends Person and represents the store owner who manages
 * products, views orders, and oversees the business operations. Owners have
 * full administrative access to the system.
 * 
 * <p>Owner capabilities:
 * <ul>
 *   <li>Product management (add, edit, delete products)</li>
 *   <li>Order management and oversight</li>
 *   <li>System administration</li>
 *   <li>Access to owner dashboard</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Person
 * <p>POLYMORPHISM: Overrides abstract methods getRole() and getDashboardView()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Owner extends Person {

    /**
     * Default constructor.
     * 
     * <p>Creates a new Owner instance with default values.
     */
    public Owner() {
        super();
    }

    /**
     * Constructor with ID and contact information.
     * 
     * @param id The unique identifier for the owner
     * @param username The username for login
     * @param address The owner's address
     * @param phone The owner's phone number
     */
    public Owner(int id, String username, String address, String phone) {
        super(id, username, address, phone);
    }

    /**
     * Constructor with username and password.
     * 
     * <p>Creates a new owner with the specified credentials.
     * This constructor is typically used when registering a new owner.
     * 
     * @param username The username for login
     * @param password The password for authentication
     */
    public Owner(String username, String password) {
        super();
        setUsername(username);
        setPassword(password);
    }

    /**
     * Gets the role of this user.
     * 
     * <p>Returns "owner" to identify this user as an owner type.
     * This is used for role-based access control throughout the application.
     * 
     * @return The string "owner"
     */
    @Override
    public String getRole() {
        return "owner";
    }

    /**
     * Gets the FXML file path for the owner dashboard view.
     * 
     * <p>Returns the path to the FXML file that defines the owner interface.
     * This view provides administrative functionality including product management
     * and order oversight.
     * 
     * @return The path "/view/owner.fxml"
     */
    @Override
    public String getDashboardView() {
        return "/view/owner.fxml";
    }
}
