package model;

/**
 * User class for backward compatibility with legacy code.
 * 
 * <p>This class extends Person and is maintained for backward compatibility
 * with existing code that uses a generic User class. New code should use
 * the specific user type classes (Customer, Carrier, Owner) directly.
 * 
 * <p>The class uses a role field to determine the user type and returns
 * the appropriate dashboard view based on the role.
 * 
 * <p>INHERITANCE: Extends Person (which extends Entity)
 * <p>ENCAPSULATION: Uses inherited fields from Person
 * <p>POLYMORPHISM: Inherits polymorphic behavior from Person
 * 
 * <p><b>NOTE:</b> This class is kept for backward compatibility only.
 * New code should use Customer, Carrier, or Owner directly.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class User extends Person {

    /**
     * The role of the user (for backward compatibility).
     * Values: "customer", "carrier", or "owner"
     */
    private String role;

    /**
     * Default constructor.
     * 
     * <p>Creates a new User instance with default values.
     */
    public User() {
        super();
    }

    /**
     * Backward compatible constructor.
     * 
     * @param id The unique identifier for the user
     * @param username The username for login
     * @param role The user role ("customer", "carrier", or "owner")
     * @param address The user's address
     * @param phone The user's phone number
     */
    public User(int id, String username, String role, String address, String phone) {
        super(id, username, address, phone);
        this.role = role;
    }

    /**
     * Gets the role of this user.
     * 
     * @return The role ("customer", "carrier", or "owner")
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of this user.
     * 
     * @param role The role to set ("customer", "carrier", or "owner")
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the FXML file path for the dashboard view based on role.
     * 
     * <p>Returns the appropriate dashboard view path based on the user's role.
     * If role is null, defaults to customer view.
     * 
     * @return The FXML file path for the dashboard view:
     *         "/view/carrier.fxml" for carrier,
     *         "/view/owner.fxml" for owner,
     *         "/view/customer.fxml" for customer or default
     */
    @Override
    public String getDashboardView() {
        if (role == null)
            return "/view/customer.fxml";

        return switch (role.toLowerCase()) {
            case "carrier" -> "/view/carrier.fxml";
            case "owner" -> "/view/owner.fxml";
            default -> "/view/customer.fxml";
        };
    }
}