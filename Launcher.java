package app;

/**
 * Launcher class for the GreenGrocer application.
 * 
 * <p>This class serves as the entry point that performs necessary setup
 * operations before starting the main application. It runs schema updates
 * to ensure the database is properly configured, then launches the JavaFX
 * application.
 * 
 * <p>Execution flow:
 * <ol>
 *   <li>Runs SchemaFixer to apply database schema updates</li>
 *   <li>Launches the main JavaFX application (Main)</li>
 * </ol>
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Launcher {
    
    /**
     * Main entry point for the application launcher.
     * 
     * <p>This method first runs schema updates to fix any missing database
     * columns or tables, then starts the main JavaFX application.
     * 
     * @param args Command-line arguments passed to both SchemaFixer and Main
     */
    public static void main(String[] args) {
        // Run schema updates to fix missing columns
        SchemaFixer.main(args);

        // Start app
        Main.main(args);
    }
}
