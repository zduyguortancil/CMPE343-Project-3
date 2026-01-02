package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class for automatically loading product images from the images_market folder.
 * 
 * <p>This utility provides functionality to search for and load product images
 * based on product names. It supports multiple image formats (PNG, JPG, JPEG)
 * and performs case-insensitive matching with normalization (handles spaces
 * and different naming conventions).
 * 
 * <p>The utility searches for images in the "images_market" folder and matches
 * them to products using flexible matching criteria:
 * <ul>
 *   <li>Exact case-insensitive match (e.g., "arugula" matches "Arugula.jpg")</li>
 *   <li>Normalized match without spaces (e.g., "red apple" matches "RedApple.jpg")</li>
 * </ul>
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class ImageUtil {

    /**
     * The folder name where product images are stored.
     * Default: "images_market"
     */
    private static final String IMAGE_FOLDER = "images_market";

    /**
     * Load image for a product by name.
     * Searches for PNG, JPG, or JPEG files in images_market folder.
     * 
     * @param productName The product name (case-insensitive)
     * @return byte array of image data, or null if not found
     */
    public static byte[] loadProductImage(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return null;
        }

        String targetName = productName.trim();
        System.out.println("🔍 Searching for image: '" + targetName + "'");

        File folder = new File(IMAGE_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("❌ Image folder not found: " + IMAGE_FOLDER);
            return null;
        }

        File[] files = folder.listFiles();
        if (files == null)
            return null;

        // Try to find a match case-insensitively
        for (File file : files) {
            if (!file.isFile())
                continue;

            String fileName = file.getName();
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot == -1)
                continue;

            String nameWithoutExt = fileName.substring(0, lastDot);
            String ext = fileName.substring(lastDot).toLowerCase();

            // Check if extension is supported
            if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
                continue;
            }

            // Match criteria:
            // 1. Exact case-insensitive match (e.g., "arugula" matches "Arugula.jpg")
            // 2. Normalized match (no spaces, e.g., "red apple" matches "RedApple.jpg")
            String normalizedTarget = targetName.toLowerCase().replaceAll("\\s+", "");
            String normalizedFile = nameWithoutExt.toLowerCase().replaceAll("\\s+", "");

            if (nameWithoutExt.equalsIgnoreCase(targetName) || normalizedFile.equals(normalizedTarget)) {
                System.out.println("  ✅ Match found: " + fileName);
                return readImageFile(file);
            }
        }

        System.out.println("⚠ No image found for product: " + productName);
        return null;
    }

    /**
     * Reads an image file and converts it to a byte array.
     * 
     * <p>This method reads the entire contents of the specified image file
     * into a byte array. The file size is determined first, then the file
     * is read in a single operation.
     * 
     * @param file The image file to read
     * @return Byte array containing the image data, or null if an error occurs
     */
    private static byte[] readImageFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] imageData = new byte[(int) file.length()];
            fis.read(imageData);
            System.out.println("✅ Loaded image: " + file.getName());
            return imageData;
        } catch (IOException e) {
            System.err.println("Error reading image file: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an image exists for a given product name.
     * 
     * <p>This is a convenience method that uses loadProductImage() to determine
     * if an image file exists for the specified product. It returns true if
     * an image can be found and loaded, false otherwise.
     * 
     * @param productName The product name to check (case-insensitive)
     * @return true if an image exists for the product, false otherwise
     */
    public static boolean imageExists(String productName) {
        return loadProductImage(productName) != null;
    }
}
