package app;

import util.DBUtil;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility class for applying database schema updates and fixes.
 * 
 * <p>This class provides functionality to update the database schema by:
 * <ul>
 *   <li>Adding missing columns to existing tables (Orders, Product)</li>
 *   <li>Creating new tables if they don't exist (CarrierRating, Invoice,
 *       OrderStatusHistory, Messages, Coupon, CustomerLoyalty)</li>
 * </ul>
 * 
 * <p>The class handles expected errors gracefully (e.g., columns that already exist)
 * and provides console output for each operation. This is useful for database
 * migration and ensuring the schema is up-to-date.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class SchemaFixer {
    
    /**
     * Main method to execute schema updates.
     * 
     * <p>This method applies a series of SQL statements to update the database schema.
     * It executes each statement and handles errors gracefully, printing informative
     * messages to the console. Expected errors (such as columns that already exist)
     * are noted but do not stop the execution.
     * 
     * <p>The updates include:
     * <ul>
     *   <li>Adding timestamp columns to Orders table</li>
     *   <li>Adding threshold column to Product table</li>
     *   <li>Creating CarrierRating, Invoice, OrderStatusHistory, Messages,
     *       Coupon, and CustomerLoyalty tables if they don't exist</li>
     * </ul>
     * 
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        System.out.println("Applying schema updates...");

        String[] updates = {
                // Add missing columns to Orders
                "ALTER TABLE Orders ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                "ALTER TABLE Orders ADD COLUMN cancelled_at TIMESTAMP NULL",
                "ALTER TABLE Orders ADD COLUMN cancel_reason TEXT",

                // Add missing columns to Product if needed (just in case)
                "ALTER TABLE Product ADD COLUMN threshold INT DEFAULT 10",

                // Create tables if not exist (from schema_updates.sql)
                // CarrierRating
                """
                        CREATE TABLE IF NOT EXISTS CarrierRating (
                            rating_id INT AUTO_INCREMENT PRIMARY KEY,
                            order_id INT NOT NULL UNIQUE,
                            carrier_username VARCHAR(50) NOT NULL,
                            customer_username VARCHAR(50) NOT NULL,
                            rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                            comment TEXT,
                            rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
                        )
                        """,
                // Invoice
                """
                        CREATE TABLE IF NOT EXISTS Invoice (
                            invoice_id INT AUTO_INCREMENT PRIMARY KEY,
                            order_id INT NOT NULL,
                            invoice_pdf LONGBLOB,
                            invoice_content LONGTEXT,
                            transaction_log LONGTEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
                        )
                        """,
                // OrderStatusHistory
                """
                        CREATE TABLE IF NOT EXISTS OrderStatusHistory (
                            history_id INT AUTO_INCREMENT PRIMARY KEY,
                            order_id INT NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            changed_by VARCHAR(50),
                            notes TEXT,
                            FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
                        )
                        """,
                // Messages
                """
                        CREATE TABLE IF NOT EXISTS Messages (
                            message_id INT AUTO_INCREMENT PRIMARY KEY,
                            sender_username VARCHAR(50) NOT NULL,
                            receiver_username VARCHAR(50) NOT NULL,
                            subject VARCHAR(200),
                            content TEXT NOT NULL,
                            is_read BOOLEAN DEFAULT FALSE,
                            parent_message_id INT,
                            sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (parent_message_id) REFERENCES Messages(message_id) ON DELETE SET NULL
                        )
                        """,
                // Coupon
                """
                         CREATE TABLE IF NOT EXISTS Coupon (
                            coupon_id INT AUTO_INCREMENT PRIMARY KEY,
                            code VARCHAR(50) UNIQUE NOT NULL,
                            discount_percent DECIMAL(5,2) DEFAULT 0,
                            discount_amount DECIMAL(10,2) DEFAULT 0,
                            min_order_amount DECIMAL(10,2) DEFAULT 0,
                            valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            valid_until TIMESTAMP,
                            max_uses INT DEFAULT 1,
                            used_count INT DEFAULT 0,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                        """,
                // CustomerLoyalty
                """
                        CREATE TABLE IF NOT EXISTS CustomerLoyalty (
                            loyalty_id INT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) UNIQUE NOT NULL,
                            points INT DEFAULT 0,
                            tier VARCHAR(20) DEFAULT 'BRONZE',
                            total_spent DECIMAL(12,2) DEFAULT 0,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                        )
                        """
        };

        try (Connection con = DBUtil.getConnection();
                Statement stmt = con.createStatement()) {

            for (String sql : updates) {
                try {
                    System.out.println("Executing: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                    stmt.executeUpdate(sql);
                    System.out.println("  ✅ Success");
                } catch (Exception e) {
                    // Ignore expected errors (e.g. column already exists)
                    System.out.println("  ℹ️ Note: " + e.getMessage());
                }
            }
            System.out.println("Schema updates completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
