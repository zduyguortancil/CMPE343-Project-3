package util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validation utility class for business rules and data validation.
 * 
 * <p>This utility provides comprehensive validation methods for various
 * data types used in the GreenGrocer application, including:
 * <ul>
 *   <li>Username validation (alphanumeric with at least one letter)</li>
 *   <li>Product name validation (against known fruits and vegetables)</li>
 *   <li>Stock quantity validation (within reasonable limits)</li>
 *   <li>Price validation (positive values within limits)</li>
 *   <li>Turkish phone number validation (multiple formats supported)</li>
 *   <li>Address validation (must contain letters)</li>
 * </ul>
 * 
 * <p>The class maintains sets of valid fruit and vegetable names (100+ each)
 * for product name validation, and provides user-friendly error messages
 * for invalid inputs.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class ValidationUtil {

    /**
     * Set of valid fruit names (100+ common fruits in English).
     * Used for product name validation when type is "fruit".
     */
    private static final Set<String> VALID_FRUITS = new HashSet<>(Arrays.asList(
            "apple", "red apple", "green apple", "banana", "orange", "mandarin", "tangerine",
            "grape", "strawberry", "cherry", "peach", "apricot", "plum", "fig", "pomegranate",
            "watermelon", "melon", "kiwi", "pineapple", "avocado", "lemon", "lime", "blueberry",
            "raspberry", "blackberry", "pear", "grapefruit", "quince", "mango", "coconut",
            "papaya", "guava", "passion fruit", "dragon fruit", "lychee", "persimmon", "nectarine",
            "cantaloupe", "honeydew", "date", "cranberry", "gooseberry", "mulberry", "elderberry",
            "currant", "clementine", "kumquat", "star fruit", "kiwano", "rambutan", "longan",
            "ackee", "durian", "jackfruit", "mangosteen", "soursop", "tamarind", "breadfruit",
            "plantain", "boysenberry", "cloudberry", "loganberry", "salmonberry", "thimbleberry",
            "dewberry", "chokeberry", "acai berry", "goji berry", "jabuticaba", "miracle fruit",
            "pitaya", "sapodilla", "cherimoya", "sugar apple", "custard apple", "feijoa", "jujube",
            "medlar", "rowan", "sea buckthorn", "service berry", "sloe", "wild strawberry",
            "wood apple", "african cherry", "bilberry", "crowberry", "hackberry", "hawthorn",
            "huckleberry", "juniper berry", "lingonberry", "mayapple", "nannyberry", "pawpaw",
            "salal", "serviceberry", "snowberry", "wayfaring tree", "barberry"));

    /**
     * Set of valid vegetable names (100+ common vegetables in English).
     * Used for product name validation when type is "vegetable".
     */
    private static final Set<String> VALID_VEGETABLES = new HashSet<>(Arrays.asList(
            "tomato", "cherry tomato", "cucumber", "green pepper", "red pepper", "bell pepper",
            "yellow pepper", "orange pepper", "eggplant", "zucchini", "potato", "sweet potato",
            "onion", "red onion", "white onion", "spring onion", "garlic", "carrot", "spinach",
            "lettuce", "iceberg lettuce", "romaine lettuce", "parsley", "dill", "arugula", "basil",
            "mint", "thyme", "oregano", "rosemary", "cilantro", "sage", "broccoli", "cauliflower",
            "leek", "cabbage", "red cabbage", "mushroom", "corn", "peas", "green beans", "asparagus",
            "celery", "beetroot", "radish", "turnip", "parsnip", "rutabaga", "kohlrabi", "fennel",
            "artichoke", "brussels sprouts", "kale", "swiss chard", "collard greens", "bok choy",
            "napa cabbage", "endive", "radicchio", "watercress", "mustard greens", "beet greens",
            "dandelion greens", "turnip greens", "sorrel", "chicory", "escarole", "frisee", "mache",
            "okra", "squash", "butternut squash", "acorn squash", "pumpkin", "spaghetti squash",
            "chayote", "jicama", "taro", "yam", "cassava", "daikon", "horseradish", "ginger",
            "turmeric", "jerusalem artichoke", "salsify", "water chestnut", "bamboo shoots",
            "bean sprouts", "alfalfa sprouts", "snow peas", "snap peas", "lima beans", "fava beans",
            "edamame", "shallot", "scallion", "chives"));

    /**
     * Minimum allowed stock quantity (in kg).
     */
    public static final double MIN_STOCK = 0.0;
    
    /**
     * Maximum allowed stock quantity (in kg).
     */
    public static final double MAX_STOCK = 10000.0;

    /**
     * Minimum allowed price (in Turkish Lira).
     */
    public static final double MIN_PRICE = 0.01;
    
    /**
     * Maximum allowed price (in Turkish Lira).
     * Set to a reasonable limit for fruits and vegetables.
     */
    public static final double MAX_PRICE = 5000.0;

    /**
     * Validates a username according to business rules.
     * 
     * <p>Username validation rules:
     * <ul>
     *   <li>Must contain only letters, numbers, and underscores</li>
     *   <li>Must contain at least one letter (cannot be just numbers/underscores)</li>
     *   <li>Cannot be null or empty</li>
     * </ul>
     * 
     * <p>Examples:
     * <ul>
     *   <li>Valid: "elif12", "12elif", "user_name"</li>
     *   <li>Invalid: "123", "_____", "", null</li>
     * </ul>
     * 
     * @param username The username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // Allow letters, numbers, and underscore
        if (!username.matches("[A-Za-z0-9_]+")) {
            return false;
        }

        // MUST contain at least one letter (cannot be just numbers/underscores)
        if (!username.matches(".*[A-Za-z].*")) {
            return false;
        }

        return true;
    }

    /**
     * Validates a product name against known fruits and vegetables.
     * 
     * <p>The product name must be in the list of valid fruits or vegetables
     * depending on the specified type. The comparison is case-insensitive
     * and trimmed. The class maintains sets of 100+ valid fruit and vegetable
     * names for validation.
     * 
     * @param name The product name to validate (case-insensitive)
     * @param type The product type ("fruit" or "vegetable")
     * @return true if the product name is valid for the given type, false otherwise
     */
    public static boolean isValidProductName(String name, String type) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String normalized = name.trim().toLowerCase();

        if ("fruit".equalsIgnoreCase(type)) {
            return VALID_FRUITS.contains(normalized);
        } else if ("vegetable".equalsIgnoreCase(type)) {
            return VALID_VEGETABLES.contains(normalized);
        }

        return false;
    }

    /**
     * Validates a stock quantity value.
     * 
     * <p>The stock must be within the defined limits (MIN_STOCK to MAX_STOCK).
     * Stock is measured in kilograms (kg).
     * 
     * @param stock The stock quantity to validate (in kg)
     * @return true if stock is between MIN_STOCK (0.0) and MAX_STOCK (10000.0), false otherwise
     */
    public static boolean isValidStock(double stock) {
        return stock >= MIN_STOCK && stock <= MAX_STOCK;
    }

    /**
     * Validates a price value.
     * 
     * <p>The price must be positive and within the defined limits (MIN_PRICE to MAX_PRICE).
     * Price is measured in Turkish Lira (TL).
     * 
     * @param price The price to validate (in TL)
     * @return true if price is between MIN_PRICE (0.01) and MAX_PRICE (5000.0), false otherwise
     */
    public static boolean isValidPrice(double price) {
        return price >= MIN_PRICE && price <= MAX_PRICE;
    }

    /**
     * Validate Turkish phone number.
     * Accepts formats (with or without spaces/dashes):
     * - 0555 555 5555 or 05555555555 (11 digits starting with 05)
     * - +90 555 555 5555 or +905555555555 (country code)
     * - 555 555 5555 or 5555555555 (10 digits starting with 5)
     * 
     * @return true if valid Turkish mobile format
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        phone = phone.trim();

        // Remove spaces and dashes for validation
        String cleaned = phone.replaceAll("[\\s-]", "");

        // Must be all digits (except optional + at start)
        if (!cleaned.matches("\\+?[0-9]+")) {
            return false;
        }

        // Must have minimum length (reject "123", "000" etc)
        if (cleaned.length() < 10) {
            return false;
        }

        // Pattern 1: 05XXXXXXXXX (exactly 11 digits)
        if (cleaned.matches("05[0-9]{9}")) {
            return true;
        }

        // Pattern 2: +905XXXXXXXXX (exactly 13 chars with +90)
        if (cleaned.matches("\\+905[0-9]{9}")) {
            return true;
        }

        // Pattern 3: 5XXXXXXXXX (exactly 10 digits)
        if (cleaned.matches("5[0-9]{9}")) {
            return true;
        }

        return false;
    }

    /**
     * Validates an address string.
     * 
     * <p>The address must contain at least one letter (not just numbers or symbols).
     * This ensures that the address is meaningful and not just a numeric code.
     * 
     * @param address The address string to validate
     * @return true if the address contains at least one letter, false otherwise
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        // Must contain at least one letter (not just numbers/symbols)
        return address.matches(".*[A-Za-z].*");
    }

    /**
     * Gets a user-friendly error message for an invalid product name.
     * 
     * <p>Returns a descriptive error message based on the product type,
     * helping users understand what went wrong and providing examples
     * of valid product names.
     * 
     * @param type The product type ("fruit" or "vegetable")
     * @return A user-friendly error message string
     */
    public static String getProductNameError(String type) {
        if ("fruit".equalsIgnoreCase(type)) {
            return "Invalid fruit name. Please enter a valid fruit (e.g., Apple, Banana, Orange).";
        } else if ("vegetable".equalsIgnoreCase(type)) {
            return "Invalid vegetable name. Please enter a valid vegetable (e.g., Tomato, Carrot, Potato).";
        }
        return "Invalid product name.";
    }

    /**
     * Gets example valid product names for a given product type.
     * 
     * <p>Returns a string containing examples of valid product names
     * for the specified type. This is useful for displaying hints to
     * users when they need to enter a product name.
     * 
     * @param type The product type ("fruit" or "vegetable")
     * @return A string containing example product names, or empty string if type is invalid
     */
    public static String getValidProductExamples(String type) {
        if ("fruit".equalsIgnoreCase(type)) {
            return "Examples: Apple, Banana, Orange, Grape, Strawberry, Kiwi";
        } else if ("vegetable".equalsIgnoreCase(type)) {
            return "Examples: Tomato, Cucumber, Carrot, Potato, Onion, Lettuce";
        }
        return "";
    }
}
