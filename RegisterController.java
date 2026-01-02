package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the Registration screen in the GreenGrocer application.
 * 
 * <p>This controller handles new user registration with comprehensive validation.
 * It validates username, password, address, and phone number according to business
 * rules and creates new customer accounts in the database.
 * 
 * <p>Validation features:
 * <ul>
 *   <li>Username: 3-20 characters, must contain at least one letter</li>
 *   <li>Password: Minimum 6 characters with letter, digit, and special character</li>
 *   <li>Address: Must contain letters (optional field)</li>
 *   <li>Phone: Turkish phone format validation (optional field)</li>
 *   <li>Duplicate username and phone number checking</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * <p>POLYMORPHISM: Overrides abstract methods from BaseController
 * <p>ENCAPSULATION: Private fields and validation constants
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class RegisterController extends BaseController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField password2Field;

    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;

    @FXML
    private Label infoLabel;

    // ENCAPSULATION: Private validation rules
    private static final int USER_MIN = 3;
    private static final int USER_MAX = 20;
    private static final int PASS_MIN = 6;

    // ========== POLYMORPHISM: Override abstract methods from BaseController
    // ==========

    @Override
    public void setUsername(String username) {
        // Not applicable for registration screen
        this.currentUsername = username;
    }

    @Override
    protected Label getUsernameLabel() {
        return infoLabel; // Use info label as fallback
    }

    @Override
    protected String getScreenTitle() {
        return "Register - GreenGrocer";
    }

    /**
     * Initializes the registration screen.
     * 
     * <p>Clears any error messages when the screen loads.
     */
    @FXML
    public void initialize() {
        setError("");
    }

    /**
     * Handles the register button click.
     * 
     * <p>Performs comprehensive validation on all input fields:
     * <ul>
     *   <li>Validates username format and checks for duplicates</li>
     *   <li>Validates password strength and confirmation match</li>
     *   <li>Validates address format (if provided)</li>
     *   <li>Validates phone number format and checks for duplicates (if provided)</li>
     * </ul>
     * 
     * <p>If validation passes, creates a new customer account and navigates
     * to the login screen. Displays appropriate error messages for validation failures.
     */
    @FXML
    private void handleRegister() {
        setError("");

        String username = safeTrim(usernameField.getText());
        String pass1 = safeTrim(passwordField.getText());
        String pass2 = safeTrim(password2Field.getText());

        String address = safeTrim(addressField.getText());
        String phone = safeTrim(phoneField.getText());

        // ================= USERNAME VALIDATION =================
        if (username.isEmpty()) {
            setError("Username required.");
            return;
        }
        if (username.length() < USER_MIN || username.length() > USER_MAX) {
            setError("Username must be " + USER_MIN + "-" + USER_MAX + " chars.");
            return;
        }
        // Only letters and underscore allowed (no numbers)
        if (!util.ValidationUtil.isValidUsername(username)) {
            setError(
                    "Username must contain at least one letter. Can include numbers but cannot be just numbers (e.g. 'elif12' OK, '123' NOT OK).");
            return;
        }

        // ================= PASSWORD VALIDATION =================
        if (pass1.isEmpty()) {
            setError("Password required.");
            return;
        }

        // Check all password requirements at once
        boolean hasMinLength = pass1.length() >= PASS_MIN;
        boolean hasLetter = pass1.matches(".*[A-Za-z].*");
        boolean hasDigit = pass1.matches(".*\\d.*");
        boolean hasSpecial = pass1.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasMinLength || !hasLetter || !hasDigit || !hasSpecial) {
            setError("Password must be at least " + PASS_MIN
                    + " chars with 1 letter, 1 digit, and 1 special character (!@#$%^&*)");
            return;
        }

        if (!pass1.equals(pass2)) {
            setError("Passwords do not match.");
            return;
        }

        // ================= OPTIONAL FIELDS VALIDATION =================
        String addressDb = emptyToNull(address);
        if (addressDb != null && !util.ValidationUtil.isValidAddress(addressDb)) {
            setError("Address must contain letters, not just numbers (e.g. 'Maltepe 111' OK, '111' NOT OK).");
            return;
        }

        String phoneDb = emptyToNull(phone);
        if (phoneDb != null) {
            // Validate Turkish phone format
            if (!util.ValidationUtil.isValidPhoneNumber(phoneDb)) {
                setError("Invalid phone number! Use format: 0555 555 5555 or 05555555555");
                return;
            }
        }

        // ================= DB =================
        try {
            if (UserDAO.usernameExists(username)) {
                setError("Username already taken.");
                return;
            }

            // Check for duplicate phone number
            if (phoneDb != null && UserDAO.phoneExists(phoneDb)) {
                setError("This phone number is already registered.");
                return;
            }

            boolean ok = UserDAO.registerCustomer(username, pass1, addressDb, phoneDb);
            if (!ok) {
                setError("Registration failed (DB).");
                return;
            }

            // success
            infoLabel.setStyle("-fx-text-fill: green;");
            infoLabel.setText("Account created ✅");

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Registration");
            a.setHeaderText("Success");
            a.setContentText("Account created. You can login now.");
            a.showAndWait();

            goLogin();

        } catch (Exception e) {
            e.printStackTrace();
            setError("Unexpected error occurred.");
        }
    }

    /**
     * Handles the back button click.
     * 
     * <p>Navigates back to the login screen.
     */
    @FXML
    private void handleBack() {
        goLogin();
    }

    /**
     * Navigates to the login screen.
     * 
     * <p>Loads the login FXML file, applies stylesheet, and switches to
     * the login scene while preserving the window's maximized state.
     * Displays an error message if navigation fails.
     */
    private void goLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(loader.load(), 960, 540);
            applyStylesheet(scene);

            stage.setTitle("GreenGrocer Login");
            stage.setScene(scene);
            stage.setMaximized(wasMaximized);
        } catch (Exception e) {
            e.printStackTrace();
            setError("Cannot open login!");
        }
    }

    /**
     * Sets an error message in the info label.
     * 
     * <p>Displays the message in red text. If the message is null,
     * clears the label.
     * 
     * @param msg The error message to display (null to clear)
     */
    private void setError(String msg) {
        infoLabel.setStyle("-fx-text-fill: red;");
        infoLabel.setText(msg == null ? "" : msg);
    }
}
