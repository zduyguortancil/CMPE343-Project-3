package controller;

import dao.UserDAO;
import model.Person;
import util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the Profile editing screen in the GreenGrocer application.
 * 
 * <p>This controller allows users to view and edit their profile information,
 * including address and phone number. It provides validation for the edited
 * fields and updates the database when changes are saved.
 * 
 * <p>Features:
 * <ul>
 *   <li>Loads current profile data from database</li>
 *   <li>Address validation (must contain letters)</li>
 *   <li>Phone number validation (Turkish format)</li>
 *   <li>Duplicate phone number checking</li>
 *   <li>Profile update functionality</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * <p>POLYMORPHISM: Overrides abstract methods from BaseController
 * <p>ENCAPSULATION: Private fields with controlled access
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class ProfileController extends BaseController {

    @FXML
    private Label usernameDisplayLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label infoLabel;

    // ========== POLYMORPHISM: Override abstract methods from BaseController
    // ==========

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameDisplayLabel.setText(username);
        loadCurrentProfile();
    }

    @Override
    protected Label getUsernameLabel() {
        return usernameDisplayLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Edit Profile";
    }

    /**
     * Initializes the profile screen.
     * 
     * <p>Clears any info messages when the screen loads.
     */
    @FXML
    public void initialize() {
        clearInfoLabel(infoLabel);
    }

    /**
     * Loads the current user's profile data from the database.
     * 
     * <p>Retrieves the user's information using UserDAO and populates
     * the address and phone fields with the current values.
     */
    private void loadCurrentProfile() {
        Person user = UserDAO.getUserInfo(currentUsername);
        if (user != null) {
            addressField.setText(user.getAddress() != null ? user.getAddress() : "");
            phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        }
    }

    /**
     * Handles the save button click.
     * 
     * <p>Validates the address and phone number inputs, checks for duplicate
     * phone numbers (excluding the current user), and updates the profile in
     * the database if validation passes. Displays success or error messages
     * accordingly.
     */
    @FXML
    private void handleSave() {
        clearInfoLabel(infoLabel);

        String address = emptyToNull(addressField.getText());
        String phone = emptyToNull(phoneField.getText());

        // Address validation
        if (address != null && !ValidationUtil.isValidAddress(address)) {
            showInfoLabel(infoLabel,
                    "Address must contain at least one letter (e.g., 'Maltepe 111' is valid, '111' is not).", true);
            return;
        }

        // Phone validation using ValidationUtil (Turkish phone format)
        if (phone != null && !ValidationUtil.isValidPhoneNumber(phone)) {
            showInfoLabel(infoLabel, "Invalid phone format. Use: 05XXXXXXXXX, +905XXXXXXXXX, or 5XXXXXXXXX", true);
            return;
        }

        // Check if phone already exists (excluding current user)
        if (phone != null) {
            // Get current user's phone to compare
            Person currentUser = UserDAO.getUserInfo(currentUsername);
            String currentPhone = currentUser != null ? currentUser.getPhone() : null;

            // Only check if phone changed
            if (!phone.equals(currentPhone) && UserDAO.phoneExists(phone)) {
                showInfoLabel(infoLabel, "This phone number is already registered to another account.", true);
                return;
            }
        }

        // Update in database
        boolean success = UserDAO.updateProfileByUsername(currentUsername, address, phone);

        if (success) {
            showInfoLabel(infoLabel, "Profile updated ✅", false);
        } else {
            showInfoLabel(infoLabel, "Update failed!", true);
        }
    }

    /**
     * Handles the cancel button click.
     * 
     * <p>Closes the profile editing window without saving any changes.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) addressField.getScene().getWindow();
        stage.close();
    }
}
