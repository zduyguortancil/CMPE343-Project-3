package model;

/**
 * Abstract base class representing a person (user) in the GreenGrocer system.
 * 
 * <p>This abstract class extends Entity and provides common functionality for
 * all user types in the system (Customer, Carrier, Owner). It defines abstract
 * methods that must be implemented by subclasses to provide role-specific behavior.
 * 
 * <p>Common features for all persons:
 * <ul>
 *   <li>Username and password for authentication</li>
 *   <li>Address and phone number for contact information</li>
 *   <li>Role-specific dashboard view</li>
 *   <li>Display name generation</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity, extended by Customer, Carrier, Owner
 * <p>POLYMORPHISM: Abstract methods for role-specific behavior
 * <p>ENCAPSULATION: Private fields with public getters/setters
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public abstract class Person extends Entity {

    /**
     * The username used for login and identification.
     */
    private String username;
    
    /**
     * The password used for authentication.
     */
    private String password;
    
    /**
     * The address of the person (for delivery or contact purposes).
     */
    private String address;
    
    /**
     * The phone number of the person.
     */
    private String phone;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Person instance with default values.
     */
    public Person() {
        super();
    }

    /**
     * Constructor with ID and contact information.
     * 
     * @param id The unique identifier for this person
     * @param username The username for login
     * @param address The address
     * @param phone The phone number
     */
    public Person(int id, String username, String address, String phone) {
        super(id);
        this.username = username;
        this.address = address;
        this.phone = phone;
    }

    /**
     * Gets the username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the address.
     * 
     * @return The address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * 
     * @param address The address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the phone number.
     * 
     * @return The phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number.
     * 
     * @param phone The phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the password.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * 
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the role of this person.
     * 
     * <p>This is an abstract method that must be implemented by subclasses.
     * Returns the role string (e.g., "customer", "carrier", "owner").
     * 
     * @return The role of this person
     */
    public abstract String getRole();

    /**
     * Gets the FXML file path for the dashboard view.
     * 
     * <p>This is an abstract method that must be implemented by subclasses.
     * Returns the path to the FXML file that defines the role-specific dashboard.
     * 
     * @return The FXML file path for the dashboard view
     */
    public abstract String getDashboardView(); // Returns FXML path

    /**
     * Gets a display name for this person.
     * 
     * <p>Returns a formatted string containing the username and role.
     * Format: "username (role)"
     * 
     * @return A formatted display string for this person
     */
    @Override
    public String getDisplayName() {
        return username + " (" + getRole() + ")";
    }
}
