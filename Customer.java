package model;

/**
 * Entity class representing a customer user in the GreenGrocer application.
 * 
 * <p>This class extends Person and represents a customer who can browse products,
 * add items to their cart, place orders, and manage their account. Customers have
 * access to the customer dashboard view which provides shopping functionality.
 * 
 * <p>The Customer class provides:
 * <ul>
 *   <li>User authentication and identification</li>
 *   <li>Personal information (address, phone)</li>
 *   <li>Role-based access control (role: "customer")</li>
 *   <li>Dashboard view path for the customer interface</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Person
 * <p>POLYMORPHISM: Overrides abstract methods getRole() and getDashboardView()
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Customer extends Person {

    /**
     * Default constructor.
     * 
     * <p>Creates a new Customer instance with default values.
     */
    public Customer() {
        super();
    }

    /**
     * Constructor with ID and basic information.
     * 
     * @param id The unique identifier for the customer
     * @param username The username for login
     * @param address The customer's address
     * @param phone The customer's phone number
     */
    public Customer(int id, String username, String address, String phone) {
        super(id, username, address, phone);
    }

    /**
     * Constructor with username, password, and contact information.
     * 
     * <p>Creates a new customer with the specified credentials and contact details.
     * This constructor is typically used when registering a new customer.
     * 
     * @param username The username for login
     * @param password The password for authentication
     * @param address The customer's address
     * @param phone The customer's phone number
     */
    public Customer(String username, String password, String address, String phone) {
        super();
        setUsername(username);
        setPassword(password);
        setAddress(address);
        setPhone(phone);
    }

    /**
     * Gets the role of this user.
     * 
     * <p>Returns "customer" to identify this user as a customer type.
     * This is used for role-based access control throughout the application.
     * 
     * @return The string "customer"
     */
    @Override
    public String getRole() {
        return "customer";
    }

    /**
     * Gets the FXML file path for the customer dashboard view.
     * 
     * <p>Returns the path to the FXML file that defines the customer interface.
     * This view provides shopping functionality including product browsing,
     * cart management, and order placement.
     * 
     * @return The path "/view/customer.fxml"
     */
    @Override
    public String getDashboardView() {
        return "/view/customer.fxml";
    }
}
