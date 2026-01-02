package app;

import util.DBUtil;
import java.io.*;
import java.sql.*;
import java.nio.file.*;

/**
 * Utility class to add product images to the database.
 * This class provides functionality to read image files from a specified
 * directory
 * and update the corresponding product records in the database with image data.
 * 
 * <p>
 * Run this class to add sample images to products in the database.
 * The images are matched to products by name and stored as binary data.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class AddProductImages {

    /**
     * Main method to execute the product image addition process.
     * 
     * <p>
     * This method:
     * <ul>
     * <li>Connects to the database using DBUtil</li>
     * <li>Iterates through a predefined list of product names and their
     * corresponding image prefixes</li>
     * <li>Finds matching image files in the specified directory</li>
     * <li>Reads the image files as binary data</li>
     * <li>Updates the Product table in the database with the image data</li>
     * </ul>
     * 
     * <p>
     * The method uses a case-insensitive search to match product names.
     * If an image is found and the product exists, the product's image field is
     * updated.
     * 
     * 
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        System.out.println("=== Adding Product Images ===\n");

        // Image directory (artifacts folder)
        String imageDir = "C:/Users/ahmet/.gemini/antigravity/brain/d7d446bb-c104-401f-b9bd-ba3cc68514a3";

        // Map product names to image files
        String[][] products = {
                { "Apple", "apple_product" },
                { "Tomato", "tomato_product" },
                // Add more as images are generated
        };

        try (Connection con = DBUtil.getConnection()) {
            System.out.println("Connected to database.\n");

            for (String[] product : products) {
                String productName = product[0];
                String imagePrefix = product[1];

                // Find the image file
                File dir = new File(imageDir);
                File[] matches = dir.listFiles((d, name) -> name.startsWith(imagePrefix) && name.endsWith(".png"));

                if (matches != null && matches.length > 0) {
                    File imageFile = matches[0];
                    System.out.println("Found image: " + imageFile.getName());

                    // Read image bytes
                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    System.out.println("  Size: " + imageBytes.length + " bytes");

                    // Update product in database
                    String sql = "UPDATE Product SET image = ? WHERE LOWER(name) LIKE ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setBytes(1, imageBytes);
                        ps.setString(2, "%" + productName.toLowerCase() + "%");

                        int updated = ps.executeUpdate();
                        if (updated > 0) {
                            System.out.println("  ✅ Updated " + updated + " product(s) for: " + productName);
                        } else {
                            System.out.println("  ⚠️ No product found matching: " + productName);
                        }
                    }
                } else {
                    System.out.println("❌ Image not found for: " + productName);
                }
            }

            System.out.println("\n=== Done ===");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
