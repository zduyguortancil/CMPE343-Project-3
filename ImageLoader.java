package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import util.DBUtil;

/**
 * Utility class for loading product images and setting up products in the database.
 * 
 * <p>This class provides functionality to:
 * <ul>
 *   <li>Clear existing products from the database</li>
 *   <li>Insert 25 predefined fruits with prices</li>
 *   <li>Insert 25 predefined vegetables with prices</li>
 *   <li>Load product images from the images_market folder into the database</li>
 * </ul>
 * 
 * <p>The class reads image files (JPG format) from the images_market folder
 * and associates them with products by matching product names. Products are
 * created with default stock (100.0 kg) and threshold (10 kg).
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class ImageLoader {

    /**
     * The folder name where product images are stored.
     * Default: "images_market"
     */
    private static final String IMAGE_FOLDER = "images_market";

    // 25 Fruits with prices
    private static final String[][] FRUITS = {
            { "Red Apple", "15.00" },
            { "Green Apple", "16.00" },
            { "Banana", "35.00" },
            { "Orange", "12.00" },
            { "Mandarin", "14.00" },
            { "Grape", "25.00" },
            { "Strawberry", "45.00" },
            { "Cherry", "55.00" },
            { "Peach", "30.00" },
            { "Apricot", "35.00" },
            { "Plum", "28.00" },
            { "Fig", "40.00" },
            { "Pomegranate", "22.00" },
            { "Watermelon", "8.00" },
            { "Melon", "10.00" },
            { "Kiwi", "38.00" },
            { "Pineapple", "50.00" },
            { "Avocado", "45.00" },
            { "Lemon", "10.00" },
            { "Lime", "12.00" },
            { "Blueberry", "70.00" },
            { "Raspberry", "75.00" },
            { "Pear", "18.00" },
            { "Grapefruit", "16.00" },
            { "Quince", "20.00" }
    };

    // 25 Vegetables with prices
    private static final String[][] VEGETABLES = {
            { "Tomato", "18.00" },
            { "Cherry Tomato", "25.00" },
            { "Cucumber", "12.00" },
            { "Green Pepper", "15.00" },
            { "Red Pepper", "20.00" },
            { "Bell Pepper", "22.00" },
            { "Eggplant", "15.00" },
            { "Zucchini", "14.00" },
            { "Potato", "8.00" },
            { "Onion", "10.00" },
            { "Red Onion", "12.00" },
            { "Garlic", "40.00" },
            { "Carrot", "12.00" },
            { "Spinach", "18.00" },
            { "Lettuce", "15.00" },
            { "Iceberg Lettuce", "18.00" },
            { "Parsley", "8.00" },
            { "Dill", "8.00" },
            { "Arugula", "20.00" },
            { "Mint", "10.00" },
            { "Broccoli", "25.00" },
            { "Cauliflower", "22.00" },
            { "Leek", "16.00" },
            { "Cabbage", "10.00" },
            { "Mushroom", "30.00" }
    };

    /**
     * Main method to execute the product and image setup process.
     * 
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Clears existing products and related order data</li>
     *   <li>Inserts 25 fruits with predefined names and prices</li>
     *   <li>Inserts 25 vegetables with predefined names and prices</li>
     *   <li>Loads images from the images_market folder and associates them with products</li>
     * </ol>
     * 
     * <p>All operations are performed within a database transaction. If any
     * error occurs, the stack trace is printed.
     * 
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        System.out.println("=== GreenGrocer Product & Image Setup ===\n");

        try (Connection conn = DBUtil.getConnection()) {

            // Step 1: Clear existing products
            System.out.println("1. Clearing existing products...");
            clearProducts(conn);

            // Step 2: Insert fruits
            System.out.println("2. Adding 25 fruits...");
            insertProducts(conn, FRUITS, "fruit");

            // Step 3: Insert vegetables
            System.out.println("3. Adding 25 vegetables...");
            insertProducts(conn, VEGETABLES, "vegetable");

            // Step 4: Load images
            System.out.println("4. Loading images from " + IMAGE_FOLDER + "...\n");
            loadImages(conn);

            System.out.println("\n=== Setup Complete! ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all existing products and related order data from the database.
     * 
     * <p>This method temporarily disables foreign key checks to allow deletion
     * of products that may be referenced by orders. It deletes all records
     * from OrderItems, Orders, and Product tables, then re-enables foreign
     * key checks.
     * 
     * @param conn The database connection to use
     * @throws SQLException If a database access error occurs
     */
    private static void clearProducts(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Disable foreign key checks to allow clearing tables
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            stmt.executeUpdate("DELETE FROM OrderItems");
            stmt.executeUpdate("DELETE FROM Orders"); // Added this to clear parent orders
            stmt.executeUpdate("DELETE FROM Product");

            // Re-enable foreign key checks
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("   Products cleared.");
        }
    }

    /**
     * Inserts products into the database.
     * 
     * <p>Inserts the provided products with the specified type. Each product
     * is created with:
     * <ul>
     *   <li>Name from the products array</li>
     *   <li>Type (fruit or vegetable)</li>
     *   <li>Price from the products array</li>
     *   <li>Default stock: 100.0 kg</li>
     *   <li>Default threshold: 10 kg</li>
     * </ul>
     * 
     * @param conn The database connection to use
     * @param products A 2D array where each row contains [name, price]
     * @param type The product type ("fruit" or "vegetable")
     * @throws SQLException If a database access error occurs
     */
    private static void insertProducts(Connection conn, String[][] products, String type) throws SQLException {
        String sql = "INSERT INTO Product (name, type, price, stock, threshold) VALUES (?, ?, ?, 100.0, 10)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] product : products) {
                ps.setString(1, product[0]);
                ps.setString(2, type);
                ps.setDouble(3, Double.parseDouble(product[1]));
                ps.executeUpdate();
            }
            System.out.println("   Added " + products.length + " " + type + "s.");
        }
    }

    /**
     * Loads product images from the images_market folder into the database.
     * 
     * <p>This method attempts to load images for all fruits and vegetables
     * defined in the FRUITS and VEGETABLES arrays. Images are expected to be
     * in JPG format with the filename matching the product name (e.g., "Apple.jpg").
     * 
     * <p>The method counts how many images were successfully loaded and how many
     * were not found, and prints a summary.
     * 
     * @param conn The database connection to use
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs while reading image files
     */
    private static void loadImages(Connection conn) throws SQLException, IOException {
        String sql = "UPDATE Product SET image = ? WHERE name = ?";
        int loaded = 0;
        int notFound = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Load fruit images
            for (String[] fruit : FRUITS) {
                if (loadImage(ps, fruit[0]))
                    loaded++;
                else
                    notFound++;
            }

            // Load vegetable images
            for (String[] veg : VEGETABLES) {
                if (loadImage(ps, veg[0]))
                    loaded++;
                else
                    notFound++;
            }
        }

        System.out.println("   ✅ " + loaded + " images loaded");
        if (notFound > 0) {
            System.out.println("   ⚠️ " + notFound + " images not found");
        }
    }

    /**
     * Loads a single product image from the images_market folder.
     * 
     * <p>Attempts to find and load an image file with the format "ProductName.jpg"
     * from the images_market folder. If found, the image is read and stored in
     * the database for the matching product.
     * 
     * @param ps The prepared statement for updating product images
     * @param productName The name of the product to load the image for
     * @return true if the image was successfully loaded, false if the file was not found
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs while reading the image file
     */
    private static boolean loadImage(PreparedStatement ps, String productName) throws SQLException, IOException {
        File imageFile = new File(IMAGE_FOLDER + File.separator + productName + ".jpg");

        if (!imageFile.exists()) {
            System.out.println("   ❌ Not found: " + productName + ".jpg");
            return false;
        }

        try (FileInputStream fis = new FileInputStream(imageFile)) {
            ps.setBinaryStream(1, fis, (int) imageFile.length());
            ps.setString(2, productName);
            ps.executeUpdate();
            System.out.println("   ✓ " + productName);
            return true;
        }
    }
}