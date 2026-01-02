package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the GreenGrocer JavaFX application.
 * 
 * <p>This class serves as the entry point for the GreenGrocer application.
 * It initializes the JavaFX application, loads the login screen, and applies
 * the application stylesheet. The application window is maximized by default.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Path to the application stylesheet CSS file.
     */
    private static final String STYLESHEET = "/view/styles.css";

    /**
     * Initializes and displays the JavaFX application window.
     * 
     * <p>This method loads the login FXML file, creates a scene with dimensions
     * 960x540, applies the stylesheet, sets the window title, maximizes the window,
     * and displays it.
     * 
     * @param stage The primary stage for the JavaFX application
     * @throws Exception If the FXML file cannot be loaded or if there are issues
     *                   with resource loading
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));

        Scene scene = new Scene(loader.load(), 960, 540);

        // Apply modern stylesheet
        String css = getClass().getResource(STYLESHEET).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Group30 GreenGrocer - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Main entry point for the application.
     * 
     * <p>Launches the JavaFX application by calling the Application.launch() method.
     * 
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Gets the stylesheet URL for use by controllers.
     * 
     * <p>This method provides a way for controllers to access the application
     * stylesheet URL, which can be used to apply consistent styling across
     * different scenes.
     * 
     * @return The external form URL of the stylesheet resource
     */
    public static String getStylesheet() {
        return Main.class.getResource(STYLESHEET).toExternalForm();
    }
}
