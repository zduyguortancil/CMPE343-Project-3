package controller;

import dao.UserDAO;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Login screen in the GreenGrocer application.
 * 
 * <p>This controller handles user authentication and navigation to role-specific
 * dashboards. It provides a login form with username and password fields, and
 * includes premium entrance animations for a polished user experience.
 * 
 * <p>Features:
 * <ul>
 *   <li>User authentication using UserDAO</li>
 *   <li>Role-based navigation (customer, carrier, owner)</li>
 *   <li>Registration screen navigation</li>
 *   <li>Premium entrance animations (fade, slide, scale effects)</li>
 *   <li>Enter key support for quick login</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * <p>POLYMORPHISM: Overrides abstract methods from BaseController
 * <p>ENCAPSULATION: Private fields
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class LoginController extends BaseController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    // Animation elements
    @FXML
    private VBox loginCard;
    @FXML
    private ImageView logoImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox formSection;

    // ========== POLYMORPHISM: Override abstract methods from BaseController
    // ==========

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
    }

    @Override
    protected Label getUsernameLabel() {
        return messageLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "GreenGrocer Login";
    }

    /**
     * Initializes the login screen.
     * 
     * <p>Sets up event handlers for Enter key support and plays the
     * entrance animation when the screen loads.
     */
    @FXML
    public void initialize() {
        messageLabel.setText("");

        // Enter ile login
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        // Play entrance animation
        playEntranceAnimation();
    }

    /**
     * Plays premium entrance animation with fade in and slide up effects.
     * 
     * <p>Creates a sequence of animations:
     * <ul>
     *   <li>Login card: fade in + slide up</li>
     *   <li>Logo: scale + fade (with delay)</li>
     *   <li>Title and subtitle: fade in (staggered)</li>
     *   <li>Form section: fade in + slide up (with delay)</li>
     * </ul>
     * 
     * <p>All animations are played in parallel for a smooth, professional effect.
     */
    private void playEntranceAnimation() {
        // Card fade in + slide up
        loginCard.setTranslateY(30);
        FadeTransition cardFade = new FadeTransition(Duration.millis(600), loginCard);
        cardFade.setFromValue(0);
        cardFade.setToValue(1);

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(600), loginCard);
        cardSlide.setFromY(30);
        cardSlide.setToY(0);
        cardSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition cardAnim = new ParallelTransition(cardFade, cardSlide);

        // Logo scale + fade (starts a bit later)
        logoImage.setScaleX(0.5);
        logoImage.setScaleY(0.5);

        FadeTransition logoFade = new FadeTransition(Duration.millis(500), logoImage);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);
        logoFade.setDelay(Duration.millis(200));

        ScaleTransition logoScale = new ScaleTransition(Duration.millis(500), logoImage);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1);
        logoScale.setToY(1);
        logoScale.setDelay(Duration.millis(200));
        logoScale.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition logoAnim = new ParallelTransition(logoFade, logoScale);

        // Title fade in
        FadeTransition titleFade = new FadeTransition(Duration.millis(400), titleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        titleFade.setDelay(Duration.millis(350));

        // Subtitle fade in
        FadeTransition subtitleFade = new FadeTransition(Duration.millis(400), subtitleLabel);
        subtitleFade.setFromValue(0);
        subtitleFade.setToValue(1);
        subtitleFade.setDelay(Duration.millis(450));

        // Form section fade in
        formSection.setTranslateY(15);
        FadeTransition formFade = new FadeTransition(Duration.millis(500), formSection);
        formFade.setFromValue(0);
        formFade.setToValue(1);
        formFade.setDelay(Duration.millis(500));

        TranslateTransition formSlide = new TranslateTransition(Duration.millis(500), formSection);
        formSlide.setFromY(15);
        formSlide.setToY(0);
        formSlide.setDelay(Duration.millis(500));
        formSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition formAnim = new ParallelTransition(formFade, formSlide);

        // Play all animations
        ParallelTransition allAnimations = new ParallelTransition(
                cardAnim, logoAnim, titleFade, subtitleFade, formAnim);
        allAnimations.play();
    }

    /**
     * Handles the login button click or Enter key press.
     * 
     * <p>Validates input fields, authenticates the user using UserDAO,
     * and navigates to the appropriate dashboard based on the user's role.
     * Displays error messages if authentication fails.
     */
    @FXML
    private void handleLogin() {
        messageLabel.setText("");

        String username = safeTrim(usernameField.getText());
        String password = safeTrim(passwordField.getText());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            return;
        }

        // Use authenticateAndGetPerson to get the user object with role
        model.Person person = UserDAO.authenticateAndGetPerson(username, password);

        System.out.println("  [LoginController] person = " + person);
        if (person == null) {
            System.out.println("  [LoginController] person is NULL - showing error");
            messageLabel.setText("Wrong username or password!");
            return;
        }

        try {
            String role = person.getRole().toLowerCase();
            System.out.println(
                    "  [LoginController] person.getRole() = '" + person.getRole() + "', lowercase = '" + role + "'");
            switch (role) {
                case "customer":
                    openCustomer(username);
                    break;
                case "carrier":
                    openCarrier(username);
                    break;
                case "owner":
                    openOwner(username);
                    break;
                default:
                    messageLabel.setText("Unknown role: " + role);
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error opening dashboard!");
        }
    }

    /**
     * Handles the register button click.
     * 
     * <p>Navigates to the registration screen while preserving the window's
     * maximized state. Displays an error message if navigation fails.
     */
    @FXML
    private void handleOpenRegister() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Scene scene = new Scene(loader.load(), 960, 540);

            applyStylesheet(scene);

            stage.setTitle("GreenGrocer - Register");
            stage.setScene(scene);
            stage.setMaximized(wasMaximized);
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Cannot open register screen!");
        }
    }

    /**
     * Opens the customer dashboard for the specified user.
     * 
     * <p>Loads the customer FXML file, applies stylesheet, sets the controller's
     * username, and switches to the customer scene.
     * 
     * @param username The username of the customer to open the dashboard for
     * @throws Exception If navigation fails (FXML loading, scene creation, etc.)
     */
    private void openCustomer(String username) throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer.fxml"));
        Scene scene = new Scene(loader.load(), 960, 540);

        applyStylesheet(scene);

        CustomerController controller = loader.getController();
        controller.setUsername(username);

        stage.setTitle("Group30 GreenGrocer");
        stage.setScene(scene);
        stage.setMaximized(wasMaximized);
    }

    /**
     * Opens the carrier dashboard for the specified user.
     * 
     * <p>Loads the carrier FXML file, applies stylesheet, sets the controller's
     * username, and switches to the carrier scene.
     * 
     * @param username The username of the carrier to open the dashboard for
     * @throws Exception If navigation fails (FXML loading, scene creation, etc.)
     */
    private void openCarrier(String username) throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/carrier.fxml"));
        Scene scene = new Scene(loader.load(), 960, 540);

        applyStylesheet(scene);

        CarrierController controller = loader.getController();
        controller.setUsername(username);

        stage.setTitle("Group30 GreenGrocer");
        stage.setScene(scene);
        stage.setMaximized(wasMaximized);
    }

    /**
     * Opens the owner dashboard for the specified user.
     * 
     * <p>Loads the owner FXML file, applies stylesheet, sets the controller's
     * username, and switches to the owner scene.
     * 
     * @param username The username of the owner to open the dashboard for
     * @throws Exception If navigation fails (FXML loading, scene creation, etc.)
     */
    private void openOwner(String username) throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/owner.fxml"));
        Scene scene = new Scene(loader.load(), 960, 540);

        applyStylesheet(scene);

        OwnerController controller = loader.getController();
        controller.setUsername(username);

        stage.setTitle("Group30 GreenGrocer");
        stage.setScene(scene);
        stage.setMaximized(wasMaximized);
    }
}
