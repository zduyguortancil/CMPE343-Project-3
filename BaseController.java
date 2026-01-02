package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Abstract base controller for all JavaFX screen controllers.
 * 
 * <p>This abstract class provides common functionality for all controllers
 * in the GreenGrocer application. It defines abstract methods that must be
 * implemented by subclasses and provides utility methods for common operations
 * like navigation, logout, and displaying messages.
 * 
 * <p>Common features:
 * <ul>
 *   <li>Username management</li>
 *   <li>Screen navigation</li>
 *   <li>Logout functionality</li>
 *   <li>Message display (errors, success, info)</li>
 *   <li>Stylesheet application</li>
 *   <li>String utility methods</li>
 * </ul>
 * 
 * <p>INHERITANCE: All controllers extend this class
 * <p>POLYMORPHISM: Abstract methods must be implemented by subclasses
 * <p>ENCAPSULATION: Protected fields and methods accessible to subclasses
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public abstract class BaseController {

    /**
     * The username of the currently logged-in user.
     * Protected access allows subclasses to access it directly.
     */
    protected String currentUsername;

    // ========== ABSTRACT METHODS (POLYMORPHISM) ==========

    /**
     * Sets the username and initializes the UI for the logged-in user.
     * 
     * <p>This abstract method must be implemented by subclasses to:
     * <ul>
     *   <li>Store the username</li>
     *   <li>Update UI elements with user information</li>
     *   <li>Load user-specific data</li>
     * </ul>
     * 
     * @param username The logged-in username
     */
    public abstract void setUsername(String username);

    /**
     * Returns the username label for this screen.
     * 
     * <p>This abstract method must be implemented by subclasses to return
     * the Label component that displays the username. This label is used
     * by common methods like performLogout() to access the scene.
     * 
     * @return The Label component displaying the username, or null if not available
     */
    protected abstract Label getUsernameLabel();

    /**
     * Returns the screen title for this controller.
     * 
     * <p>This abstract method must be implemented by subclasses to return
     * the title that should be displayed in the window title bar.
     * 
     * @return The screen title string
     */
    protected abstract String getScreenTitle();

    // ========== COMMON METHODS (INHERITANCE) ==========

    /**
     * Performs logout and navigates to the login screen.
     * 
     * <p>This method preserves the window's maximized state, loads the login
     * FXML file, applies the stylesheet, and switches to the login scene.
     * Subclasses can override this method if custom logout behavior is needed.
     * 
     * <p>If an error occurs during logout, an error message is displayed.
     */
    protected void performLogout() {
        try {
            Label label = getUsernameLabel();
            if (label == null || label.getScene() == null)
                return;

            Stage stage = (Stage) label.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(loader.load(), 960, 540);

            applyStylesheet(scene);

            stage.setTitle("GreenGrocer Login");
            stage.setScene(scene);
            stage.setMaximized(wasMaximized);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Logout failed!");
        }
    }

    /**
     * Navigates to a different screen.
     * 
     * <p>This method loads the specified FXML file, applies the stylesheet,
     * and switches to the new scene while preserving the window's maximized state.
     * 
     * <p>If an error occurs during navigation, an error message is displayed.
     * 
     * @param fxmlPath The path to the FXML file to load
     * @param title The title to set for the window
     * @param controller The controller instance (currently unused, reserved for future use)
     */
    protected void navigateToScreen(String fxmlPath, String title, Object controller) {
        try {
            Label label = getUsernameLabel();
            if (label == null || label.getScene() == null)
                return;

            Stage stage = (Stage) label.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 960, 540);
            applyStylesheet(scene);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(wasMaximized);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation failed!");
        }
    }

    /**
     * Applies the modern stylesheet to a scene.
     * 
     * <p>Loads the styles.css file and adds it to the scene's stylesheets.
     * If the CSS file is not found, the method continues without applying
     * styles (no error is thrown).
     * 
     * @param scene The scene to apply the stylesheet to
     */
    protected void applyStylesheet(Scene scene) {
        try {
            String css = getClass().getResource("/view/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            // CSS not found, continue without
        }
    }

    /**
     * Shows an error message in an alert dialog.
     * 
     * <p>Displays an error alert with the specified message. The alert
     * is modal and blocks user interaction until dismissed.
     * 
     * @param message The error message to display
     */
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success or information message in an alert dialog.
     * 
     * <p>Displays an information alert with the specified message. The alert
     * is modal and blocks user interaction until dismissed.
     * 
     * @param message The success/information message to display
     */
    protected void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an info message in a label with appropriate color.
     * 
     * <p>Updates the specified label with the message text and applies
     * color styling: red for errors, green for success/info.
     * 
     * @param label The label to update (ignored if null)
     * @param message The message to display
     * @param isError If true, displays in red; otherwise displays in green
     */
    protected void showInfoLabel(Label label, String message, boolean isError) {
        if (label != null) {
            label.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
            label.setText(message);
        }
    }

    /**
     * Clears the text of an info label.
     * 
     * <p>Sets the label's text to an empty string. Useful for clearing
     * previous messages before showing new ones.
     * 
     * @param label The label to clear (ignored if null)
     */
    protected void clearInfoLabel(Label label) {
        if (label != null) {
            label.setText("");
        }
    }

    // ========== UTILITY METHODS (ENCAPSULATION) ==========

    /**
     * Safely trims a string, returning an empty string if null.
     * 
     * <p>This utility method prevents NullPointerException when trimming strings.
     * If the input is null, returns an empty string; otherwise returns the trimmed string.
     * 
     * @param s The string to trim (can be null)
     * @return The trimmed string, or empty string if input is null
     */
    protected static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    /**
     * Converts an empty or whitespace-only string to null.
     * 
     * <p>This utility method is useful for database operations where empty strings
     * should be stored as NULL. If the input is null or contains only whitespace,
     * returns null; otherwise returns the trimmed string.
     * 
     * @param s The string to convert (can be null)
     * @return null if the string is null or empty/whitespace, otherwise the trimmed string
     */
    protected static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /**
     * Gets the current username.
     * 
     * @return The username of the currently logged-in user
     */
    public String getCurrentUsername() {
        return currentUsername;
    }
}
