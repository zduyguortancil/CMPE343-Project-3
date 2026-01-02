package dao;

import model.Product;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Product entity operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for products,
 * including CRUD operations and specialized queries for fruits, vegetables, and
 * stock management. It uses the Singleton pattern to ensure a single instance.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Product CRUD operations</li>
 * <li>Get fruits and vegetables separately</li>
 * <li>Stock management (update, low stock detection)</li>
 * <li>Discount management</li>
 * <li>Product existence checking</li>
 * <li>Image management</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Product&gt;
 * <p>
 * POLYMORPHISM: Implements abstract methods from AbstractDAO
 * <p>
 * ENCAPSULATION: Private helper methods
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class ProductDAO extends AbstractDAO<Product> {

    /**
     * Singleton instance of ProductDAO.
     */
    private static final ProductDAO INSTANCE = new ProductDAO();

    /**
     * Gets the singleton instance of ProductDAO.
     * 
     * @return The single instance of ProductDAO
     */
    public static ProductDAO getInstance() {
        return INSTANCE;
    }

    // POLYMORPHISM: Implement abstract methods from AbstractDAO
    @Override
    protected String getTableName() {
        return "Product";
    }

    @Override
    protected String getIdColumnName() {
        return "product_id";
    }

    @Override
    protected Product mapResultSetToEntity(ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getDouble("stock"));
        p.setType(rs.getString("type"));
        p.setThreshold(rs.getInt("threshold"));
        p.setImage(rs.getBytes("image"));
        try {
            double discount = rs.getDouble("discount_percent");
            p.setDiscountPercent(discount);
        } catch (Exception e) {
            System.err.println("Error reading discount_percent: " + e.getMessage());
            p.setDiscountPercent(0);
        }
        return p;
    }

    // ========== STATIC METHODS ==========

    /**
     * Checks if a product with the same name and type already exists.
     * 
     * <p>
     * The comparison is case-insensitive for the product name.
     * 
     * @param name The product name to check (case-insensitive)
     * @param type The product type ("fruit" or "vegetable")
     * @return true if a product with the same name and type exists, false otherwise
     */
    public static boolean productExists(String name, String type) {
        String sql = "SELECT COUNT(*) FROM Product WHERE LOWER(name) = LOWER(?) AND type = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Adds a new product to the database without an image.
     * 
     * <p>
     * This is a convenience method that calls addProduct() with a null image.
     * 
     * @param name      The product name
     * @param price     The base price per kilogram (in TL)
     * @param stock     The initial stock quantity (in kg)
     * @param type      The product type ("fruit" or "vegetable")
     * @param threshold The low stock threshold (in kg, minimum 1)
     * @return true if the product was added successfully, false otherwise
     */
    public static boolean addProduct(String name,
            double price,
            double stock,
            String type,
            int threshold) {
        return addProduct(name, price, stock, type, threshold, null);
    }

    /**
     * Adds a new product to the database.
     * 
     * <p>
     * Creates a new product record with the specified information. The threshold
     * is automatically adjusted to a minimum of 1 if a lower value is provided.
     * 
     * @param name      The product name
     * @param price     The base price per kilogram (in TL)
     * @param stock     The initial stock quantity (in kg)
     * @param type      The product type ("fruit" or "vegetable")
     * @param threshold The low stock threshold (in kg, will be set to minimum 1)
     * @param image     The product image as a byte array (null if no image)
     * @return true if the product was added successfully, false otherwise
     */
    public static boolean addProduct(String name,
            double price,
            double stock,
            String type,
            int threshold,
            byte[] image) {

        // Threshold validation: minimum 1
        if (threshold < 1)
            threshold = 1;

        String sql = """
                INSERT INTO Product(name, price, stock, type, threshold, image)
                VALUES(?,?,?,?,?,?)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setDouble(3, stock);
            ps.setString(4, type);
            ps.setInt(5, threshold);

            if (image == null) {
                ps.setNull(6, java.sql.Types.BLOB);
            } else {
                ps.setBytes(6, image);
            }

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all products in the database.
     * 
     * <p>
     * Retrieves all products regardless of stock level, ordered alphabetically
     * by name. Used primarily for owner/admin views.
     * 
     * @return A list of all products, ordered by name ASC
     */
    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        String sql = """
                SELECT product_id, name, price, stock, type, threshold, image
                FROM Product
                ORDER BY LOWER(name) ASC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getString("type"),
                        rs.getInt("threshold"),
                        rs.getBytes("image")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets products of a specific type with stock >= 0.
     * 
     * <p>
     * Retrieves products filtered by type and ordered alphabetically by name.
     * Includes products with zero stock. Used for customer browsing.
     * 
     * @param type The product type ("fruit" or "vegetable")
     * @return A list of products of the specified type, ordered by name ASC
     */
    public static List<Product> getProductsByType(String type) {
        List<Product> list = new ArrayList<>();

        String sql = """
                SELECT product_id, name, price, stock, type, threshold, image, discount_percent
                FROM Product
                WHERE type = ? AND stock >= 0
                ORDER BY LOWER(name) ASC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getDouble("stock"),
                            rs.getString("type"),
                            rs.getInt("threshold"),
                            rs.getBytes("image"));
                    try {
                        p.setDiscountPercent(rs.getDouble("discount_percent"));
                    } catch (Exception e) {
                        p.setDiscountPercent(0);
                    }
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets all fruits with stock >= 0.
     * 
     * <p>
     * Convenience method that retrieves all fruit products. Used for
     * customer browsing.
     * 
     * @return A list of fruit products, ordered by name ASC
     */
    public static List<Product> getFruits() {
        return getProductsByType("fruit");
    }

    /**
     * Gets all vegetables with stock >= 0.
     * 
     * <p>
     * Convenience method that retrieves all vegetable products. Used for
     * customer browsing.
     * 
     * @return A list of vegetable products, ordered by name ASC
     */
    public static List<Product> getVegetables() {
        return getProductsByType("vegetable");
    }

    /**
     * Gets all available products with stock >= 0.
     * 
     * <p>
     * Retrieves all products regardless of type, ordered alphabetically
     * by name. Used for customer browsing.
     * 
     * @return A list of all available products, ordered by name ASC
     */
    public static List<Product> getAvailableProducts() {
        List<Product> list = new ArrayList<>();

        String sql = """
                SELECT product_id, name, price, stock, type, threshold, image, discount_percent
                FROM Product
                WHERE stock >= 0
                ORDER BY LOWER(name) ASC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getString("type"),
                        rs.getInt("threshold"),
                        rs.getBytes("image"));
                try {
                    p.setDiscountPercent(rs.getDouble("discount_percent"));
                } catch (Exception e) {
                    p.setDiscountPercent(0);
                }
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets all products with low stock (stock &lt;= threshold).
     * 
     * <p>
     * Retrieves products where the current stock has fallen to or below
     * the threshold value. Used for generating low stock alerts.
     * 
     * @return A list of products with low stock, ordered by stock (lowest first)
     */
    public static List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();

        String sql = """
                SELECT product_id, name, price, stock, type, threshold, image, discount_percent
                FROM Product
                WHERE stock <= threshold
                ORDER BY stock
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getString("type"),
                        rs.getInt("threshold"),
                        rs.getBytes("image"));
                try {
                    p.setDiscountPercent(rs.getDouble("discount_percent"));
                } catch (Exception e) {
                    p.setDiscountPercent(0);
                }
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Updates the price of a product.
     * 
     * @param productId The ID of the product to update
     * @param newPrice  The new price per kilogram (in TL)
     * @return true if the update was successful, false otherwise
     */
    public static boolean updatePrice(int productId, double newPrice) {
        String sql = "UPDATE Product SET price=? WHERE product_id=?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, newPrice);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the low stock threshold for a product.
     * 
     * <p>
     * The threshold must be at least 1. If a value less than 1 is provided,
     * the update is rejected.
     * 
     * @param productId    The ID of the product to update
     * @param newThreshold The new threshold value (in kg, must be >= 1)
     * @return true if the update was successful, false if threshold is invalid or
     *         update fails
     */
    public static boolean updateThreshold(int productId, int newThreshold) {
        // Threshold validation: minimum 1
        if (newThreshold < 1) {
            return false; // Reject invalid threshold
        }

        String sql = "UPDATE Product SET threshold=? WHERE product_id=?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newThreshold);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds stock to a product.
     * 
     * <p>
     * Increases the product's stock by the specified amount. The amount
     * must be positive.
     * 
     * @param productId The ID of the product
     * @param addKg     The amount of stock to add (in kg, must be > 0)
     * @return true if the stock was added successfully, false if amount is invalid
     *         or update fails
     */
    public static boolean addStock(int productId, double addKg) {
        if (addKg <= 0)
            return false;

        String sql = "UPDATE Product SET stock = stock + ? WHERE product_id=?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, addKg);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decreases stock for a product.
     * 
     * <p>
     * Decreases the product's stock by the specified amount. The decrease
     * only succeeds if the product has sufficient stock. The amount must be
     * positive.
     * 
     * @param productId  The ID of the product
     * @param decreaseKg The amount of stock to decrease (in kg, must be > 0)
     * @return true if the stock was decreased successfully, false if amount is
     *         invalid,
     *         insufficient stock, or update fails
     */
    public static boolean decreaseStock(int productId, double decreaseKg) {
        if (decreaseKg <= 0)
            return false;

        String sql = "UPDATE Product SET stock = stock - ? WHERE product_id=? AND stock >= ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, decreaseKg);
            ps.setInt(2, productId);
            ps.setDouble(3, decreaseKg);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the discount percentage for a product.
     * 
     * <p>
     * The discount percentage must be between 0 and 100. If an invalid
     * value is provided, the update is rejected.
     * 
     * @param productId       The ID of the product to update
     * @param discountPercent The new discount percentage (0-100)
     * @return true if the update was successful, false if percentage is invalid or
     *         update fails
     */
    public static boolean updateDiscount(int productId, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100)
            return false;

        String sql = "UPDATE Product SET discount_percent=? WHERE product_id=?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, discountPercent);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decreases stock for a product within a transaction.
     * 
     * <p>
     * This method is used when decreasing stock as part of a larger transaction
     * (e.g., when creating an order). It uses the provided connection instead of
     * creating a new one, allowing it to participate in the transaction.
     * 
     * <p>
     * The decrease only succeeds if the product has sufficient stock.
     * 
     * @param con       The database connection to use (must be part of an active
     *                  transaction)
     * @param productId The ID of the product
     * @param kg        The amount of stock to decrease (in kg, must be > 0)
     * @return true if the stock was decreased successfully, false if amount is
     *         invalid
     *         or insufficient stock
     * @throws Exception If a database error occurs
     */
    public static boolean decreaseStockKg(Connection con,
            int productId,
            double kg) throws Exception {

        if (kg <= 0)
            return false;

        String sql = """
                UPDATE Product
                SET stock = stock - ?
                WHERE product_id = ?
                  AND stock >= ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, kg);
            ps.setInt(2, productId);
            ps.setDouble(3, kg);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Deletes a product and all related data.
     * 
     * <p>
     * This method performs a cascading delete:
     * <ol>
     * <li>Deletes related system message alerts</li>
     * <li>Deletes related order items</li>
     * <li>Deletes related orders (legacy support)</li>
     * <li>Deletes the product itself</li>
     * </ol>
     * 
     * @param productId The ID of the product to delete
     * @return true if the deletion was successful, false otherwise
     */
    public static boolean deleteProduct(int productId) {
        try (Connection con = DBUtil.getConnection()) {
            // 1. Delete related system messages (alerts)
            String deleteAlertsSQL = "DELETE FROM SystemMessage WHERE related_product_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteAlertsSQL)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // 2. Delete related order items
            String deleteOrderItemsSQL = "DELETE FROM OrderItems WHERE product_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteOrderItemsSQL)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // 3. Delete related orders (handling legacy product_id column in Orders table)
            String deleteOrdersSQL = "DELETE FROM Orders WHERE product_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteOrdersSQL)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // 4. Finally delete the product
            String sql = "DELETE FROM Product WHERE product_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, productId);
                return ps.executeUpdate() == 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the name of a product.
     * 
     * @param productId The ID of the product to update
     * @param newName   The new product name
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateName(int productId, String newName) {
        String sql = "UPDATE Product SET name=? WHERE product_id=?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets a product by its ID.
     * 
     * @param productId The ID of the product to retrieve
     * @return The product if found, or null if not found
     */
    public static Product getProductById(int productId) {
        return INSTANCE.findById(productId);
    }
}
