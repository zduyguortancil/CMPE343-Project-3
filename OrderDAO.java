package dao;

import model.CartItem;
import model.Order;
import model.OrderDetail;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) for Order operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides comprehensive database operations
 * for orders, including order creation, status management, carrier assignment,
 * and detailed order retrieval. It handles complex operations like creating
 * orders
 * from cart items and managing order status transitions.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Order CRUD operations</li>
 * <li>Create orders from shopping cart</li>
 * <li>Order status management</li>
 * <li>Carrier assignment and delivery tracking</li>
 * <li>Order detail retrieval with items</li>
 * <li>Order cancellation</li>
 * <li>Customer order history</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Order&gt;
 * <p>
 * POLYMORPHISM: Implements abstract methods from AbstractDAO
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class OrderDAO extends AbstractDAO<Order> {

    /**
     * Singleton instance of OrderDAO.
     */
    private static final OrderDAO INSTANCE = new OrderDAO();

    /**
     * Gets the singleton instance of OrderDAO.
     * 
     * @return The single instance of OrderDAO
     */
    public static OrderDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "Orders";
    }

    @Override
    protected String getIdColumnName() {
        return "order_id";
    }

    @Override
    protected Order mapResultSetToEntity(ResultSet rs) throws Exception {
        Order o = new Order();
        o.setId(rs.getInt("order_id"));
        o.setUsername(rs.getString("username"));
        o.setStatus(rs.getString("status"));
        o.setRequestedDelivery(rs.getTimestamp("requested_delivery"));

        try {
            o.setTotal(rs.getDouble("total_cost"));
        } catch (SQLException e) {
            o.setTotal(0.0);
        }

        return o;
    }

    // ===================== CUSTOMER CHECKOUT =====================

    /**
     * Creates an order from a shopping cart.
     * 
     * <p>
     * This method performs a transactional operation that:
     * <ol>
     * <li>Creates a new order record</li>
     * <li>Creates order items for each cart item</li>
     * <li>Decreases product stock</li>
     * <li>Creates low stock alerts if needed</li>
     * <li>Adds status history</li>
     * <li>Updates customer loyalty points</li>
     * </ol>
     * 
     * <p>
     * Uses getEffectivePrice (including discounts) for price_at_time in order
     * items.
     * The entire operation is atomic - if any step fails, the transaction is rolled
     * back.
     * 
     * @param username          The username of the customer placing the order
     * @param requestedDelivery The requested delivery timestamp
     * @param totalVatInc       The total order amount including VAT
     * @param items             The list of cart items to convert to order items
     * @return true if the order was created successfully, false if any step fails
     */
    public static boolean createCartOrder(String username,
            Timestamp requestedDelivery,
            double totalVatInc,
            List<CartItem> items) {

        String insertOrderSql = "INSERT INTO Orders(username, status, requested_delivery, total_cost, created_at) " +
                "VALUES(?, 'NEW', ?, ?, CURRENT_TIMESTAMP)";

        String insertItemSql = "INSERT INTO OrderItems(order_id, product_id, kg, price_at_time) " +
                "VALUES(?,?,?,?)";

        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            int orderId;

            // 1) Orders insert + generated order_id
            try (PreparedStatement ps = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setTimestamp(2, requestedDelivery);
                ps.setDouble(3, totalVatInc);

                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    orderId = keys.getInt(1);
                }
            }

            // 2) OrderItems insert + stock decrease
            // IMPORTANT: price_at_time must be getEffectivePrice (already set in
            // CartItem.unitPrice)
            try (PreparedStatement itemPs = con.prepareStatement(insertItemSql)) {
                for (CartItem i : items) {
                    itemPs.setInt(1, orderId);
                    itemPs.setInt(2, i.getProductId());
                    itemPs.setDouble(3, i.getKg());
                    // CartItem.unitPrice is already getEffectivePrice from CustomerController
                    itemPs.setDouble(4, i.getUnitPrice());
                    itemPs.addBatch();

                    boolean stockOk = ProductDAO.decreaseStockKg(con, i.getProductId(), i.getKg());
                    if (!stockOk) {
                        con.rollback();
                        return false;
                    }

                    // Check if stock fell below threshold and create alert for owner
                    model.Product updatedProduct = ProductDAO.getProductById(i.getProductId());
                    if (updatedProduct != null && updatedProduct.getStock() <= updatedProduct.getThreshold()) {
                        SystemMessageDAO.createLowStockAlert(
                                updatedProduct.getProductId(),
                                updatedProduct.getName(),
                                updatedProduct.getStock(),
                                updatedProduct.getThreshold());
                    }
                }
                itemPs.executeBatch();
            }

            // 3) Add status history
            String histSql = "INSERT INTO OrderStatusHistory(order_id, status, changed_by, notes) VALUES(?, 'NEW', ?, 'Order created')";
            try (PreparedStatement histPs = con.prepareStatement(histSql)) {
                histPs.setInt(1, orderId);
                histPs.setString(2, username);
                histPs.executeUpdate();
            } catch (Exception ignored) {
                // Table might not exist yet
            }

            con.commit();

            // Add loyalty points
            try {
                CustomerLoyaltyDAO.addPointsFromOrder(username, totalVatInc);
            } catch (Exception ignored) {
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (Exception ignored) {
            }
            return false;
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ===================== CARRIER VIEWS =====================

    /**
     * Get available orders with full details (for carrier).
     */
    public static List<OrderDetail> getAvailableOrderDetails() {
        List<OrderDetail> list = new ArrayList<>();

        String sql = """
                SELECT o.order_id, o.username, o.status, o.requested_delivery,
                       o.total_cost, o.created_at,
                       u.address, u.phone
                FROM Orders o
                LEFT JOIN UserInfo u ON o.username = u.username
                WHERE o.status='NEW' AND o.carrier_username IS NULL
                ORDER BY o.created_at DESC, o.requested_delivery
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderDetail od = new OrderDetail();
                od.setOrderId(rs.getInt("order_id"));
                od.setCustomerUsername(rs.getString("username"));
                od.setStatus(rs.getString("status"));
                od.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                od.setTotalVatIncluded(rs.getDouble("total_cost"));
                od.setCreatedAt(rs.getTimestamp("created_at"));
                od.setCustomerAddress(rs.getString("address"));
                od.setCustomerPhone(rs.getString("phone"));

                // Load items
                od.setItems(getOrderItems(od.getOrderId()));

                list.add(od);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get order items for an order.
     */
    public static List<OrderDetail.OrderItem> getOrderItems(int orderId) {
        List<OrderDetail.OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT oi.product_id, p.name, oi.kg, oi.price_at_time
                FROM OrderItems oi
                JOIN Product p ON oi.product_id = p.product_id
                WHERE oi.order_id = ?
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderDetail.OrderItem(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("kg"),
                            rs.getDouble("price_at_time")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Get available orders (simple).
     */
    public static List<Order> getAvailableOrders() {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT order_id, username, status, requested_delivery,
                       COALESCE(total_cost, 0) AS total_cost
                FROM Orders
                WHERE status='NEW' AND carrier_username IS NULL
                ORDER BY requested_delivery, created_at
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setUsername(rs.getString("username"));
                o.setStatus(rs.getString("status"));
                o.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                o.setTotal(rs.getDouble("total_cost"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Takes (assigns) orders to a carrier atomically.
     * 
     * <p>
     * Assigns multiple orders to a carrier in a single operation. Only orders
     * with status 'NEW' and no assigned carrier can be taken. Each successful
     * assignment updates the order status to 'IN_PROGRESS' and adds a status
     * history entry.
     * 
     * @param orderIds        The list of order IDs to assign
     * @param carrierUsername The username of the carrier taking the orders
     * @return The number of orders successfully assigned
     */
    public static int takeOrders(List<Integer> orderIds, String carrierUsername) {
        if (orderIds == null || orderIds.isEmpty())
            return 0;

        String sql = """
                UPDATE Orders
                SET carrier_username=?, status='IN_PROGRESS'
                WHERE order_id=? AND status='NEW' AND carrier_username IS NULL
                """;

        int success = 0;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            for (Integer id : orderIds) {
                ps.setString(1, carrierUsername);
                ps.setInt(2, id);
                int affected = ps.executeUpdate();
                success += affected;

                if (affected == 1) {
                    // Add status history
                    OrderStatusHistoryDAO.addHistory(id, "IN_PROGRESS", carrierUsername, "Order taken by carrier");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Gets current order details for a carrier.
     * 
     * <p>
     * Retrieves detailed information about orders currently assigned to a carrier
     * with status 'IN_PROGRESS', including customer information and order items.
     * 
     * @param carrierUsername The username of the carrier
     * @return A list of OrderDetail objects for current orders, ordered by
     *         requested delivery date
     */
    public static List<OrderDetail> getCurrentOrderDetails(String carrierUsername) {
        List<OrderDetail> list = new ArrayList<>();

        String sql = """
                SELECT o.order_id, o.username, o.status, o.requested_delivery,
                       o.total_cost, o.created_at,
                       u.address, u.phone
                FROM Orders o
                LEFT JOIN UserInfo u ON o.username = u.username
                WHERE o.status='IN_PROGRESS' AND o.carrier_username=?
                ORDER BY o.requested_delivery, o.created_at
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail od = new OrderDetail();
                    od.setOrderId(rs.getInt("order_id"));
                    od.setCustomerUsername(rs.getString("username"));
                    od.setStatus(rs.getString("status"));
                    od.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    od.setTotalVatIncluded(rs.getDouble("total_cost"));
                    od.setCreatedAt(rs.getTimestamp("created_at"));
                    od.setCustomerAddress(rs.getString("address"));
                    od.setCustomerPhone(rs.getString("phone"));
                    od.setItems(getOrderItems(od.getOrderId()));
                    list.add(od);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets current orders for a carrier.
     * 
     * <p>
     * Retrieves orders with status 'IN_PROGRESS' assigned to the specified carrier.
     * 
     * @param carrierUsername The username of the carrier
     * @return A list of Order objects, ordered by requested delivery date
     */
    public static List<Order> getCurrentOrdersByCarrier(String carrierUsername) {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT order_id, username, status, requested_delivery,
                       COALESCE(total_cost, 0) AS total_cost
                FROM Orders
                WHERE status='IN_PROGRESS' AND carrier_username=?
                ORDER BY requested_delivery, created_at
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setUsername(rs.getString("username"));
                    o.setStatus(rs.getString("status"));
                    o.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    o.setTotal(rs.getDouble("total_cost"));
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets completed (delivered) orders for a carrier.
     * 
     * <p>
     * Retrieves orders with status 'DELIVERED' that were delivered by the
     * specified carrier, ordered by delivery date (most recent first).
     * 
     * @param carrierUsername The username of the carrier
     * @return A list of Order objects, ordered by delivered_at DESC
     */
    public static List<Order> getCompletedOrdersByCarrier(String carrierUsername) {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT order_id, username, status, requested_delivery,
                       COALESCE(total_cost, 0) AS total_cost
                FROM Orders
                WHERE status='DELIVERED' AND carrier_username=?
                ORDER BY delivered_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrierUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setUsername(rs.getString("username"));
                    o.setStatus(rs.getString("status"));
                    o.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    o.setTotal(rs.getDouble("total_cost"));
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Marks an order as delivered with a specific delivery timestamp.
     * 
     * <p>
     * Updates the order status to 'DELIVERED' and sets the delivery timestamp.
     * Only orders with status 'IN_PROGRESS' assigned to the specified carrier
     * can be marked as delivered. Adds a status history entry upon success.
     * 
     * @param orderId         The ID of the order to mark as delivered
     * @param carrierUsername The username of the carrier (must match the order's
     *                        carrier)
     * @param deliveredAt     The timestamp when the order was delivered
     * @return true if the order was marked as delivered successfully, false
     *         otherwise
     */
    public static boolean markDeliveredWithDate(int orderId, String carrierUsername, Timestamp deliveredAt) {
        String sql = """
                UPDATE Orders
                SET status='DELIVERED', delivered_at=?
                WHERE order_id=? AND status='IN_PROGRESS' AND carrier_username=?
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, deliveredAt);
            ps.setInt(2, orderId);
            ps.setString(3, carrierUsername);

            boolean success = ps.executeUpdate() == 1;

            if (success) {
                OrderStatusHistoryDAO.addHistory(orderId, "DELIVERED", carrierUsername, "Order delivered");
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks an order as delivered using the current timestamp.
     * 
     * <p>
     * Convenience method that marks an order as delivered with the current
     * system time as the delivery timestamp.
     * 
     * @param orderId         The ID of the order to mark as delivered
     * @param carrierUsername The username of the carrier
     * @return true if the order was marked as delivered successfully, false
     *         otherwise
     */
    public static boolean markDeliveredByCarrier(int orderId, String carrierUsername) {
        return markDeliveredWithDate(orderId, carrierUsername, new Timestamp(System.currentTimeMillis()));
    }

    // ===================== CUSTOMER VIEWS =====================

    /**
     * Get orders by customer with full details.
     */
    public static List<OrderDetail> getOrderDetailsByUser(String username) {
        List<OrderDetail> list = new ArrayList<>();

        String sql = """
                SELECT o.order_id, o.username, o.status, o.requested_delivery,
                       o.total_cost, o.created_at, o.delivered_at, o.carrier_username,
                       o.cancelled_at, o.cancel_reason
                FROM Orders o
                WHERE o.username=?
                ORDER BY o.created_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail od = new OrderDetail();
                    od.setOrderId(rs.getInt("order_id"));
                    od.setCustomerUsername(rs.getString("username"));
                    od.setStatus(rs.getString("status"));
                    od.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    od.setTotalVatIncluded(rs.getDouble("total_cost"));
                    od.setCreatedAt(rs.getTimestamp("created_at"));
                    od.setDeliveredAt(rs.getTimestamp("delivered_at"));
                    od.setCarrierUsername(rs.getString("carrier_username"));
                    od.setCancelledAt(rs.getTimestamp("cancelled_at"));
                    od.setCancelReason(rs.getString("cancel_reason"));
                    od.setItems(getOrderItems(od.getOrderId()));
                    list.add(od);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets all orders for a user.
     * 
     * <p>
     * Retrieves all orders placed by the specified user, regardless of status,
     * ordered by creation date (most recent first).
     * 
     * @param username The username of the user
     * @return A list of Order objects, ordered by created_at DESC
     */
    public static List<Order> getOrdersByUser(String username) {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT order_id, username, status, requested_delivery,
                       COALESCE(total_cost, 0) AS total_cost
                FROM Orders
                WHERE username=?
                ORDER BY created_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setUsername(rs.getString("username"));
                    o.setStatus(rs.getString("status"));
                    o.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    o.setTotal(rs.getDouble("total_cost"));
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Cancels an order (within 1 hour of creation).
     * 
     * <p>
     * Cancels an order if it meets the following conditions:
     * <ul>
     * <li>Status is 'NEW'</li>
     * <li>Order belongs to the specified user</li>
     * <li>Order was created within the last hour</li>
     * </ul>
     * 
     * <p>
     * Upon successful cancellation:
     * <ul>
     * <li>Order status is set to 'CANCELLED'</li>
     * <li>Cancellation timestamp and reason are recorded</li>
     * <li>Status history entry is added</li>
     * <li>Product stock is restored for all items in the order</li>
     * </ul>
     * 
     * @param orderId  The ID of the order to cancel
     * @param username The username of the user canceling the order (must match
     *                 order owner)
     * @param reason   The reason for cancellation
     * @return true if the order was cancelled successfully, false if conditions are
     *         not met
     */
    public static boolean cancelOrder(int orderId, String username, String reason) {
        // Check if order can be cancelled (status=NEW and within 1 hour)
        String checkSql = """
                SELECT created_at FROM Orders
                WHERE order_id=? AND username=? AND status='NEW'
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement checkPs = con.prepareStatement(checkSql)) {

            checkPs.setInt(1, orderId);
            checkPs.setString(2, username);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next())
                    return false; // Order not found or not NEW

                Timestamp createdAt = rs.getTimestamp("created_at");
                long oneHourMs = 60 * 60 * 1000;
                long now = System.currentTimeMillis();

                if ((now - createdAt.getTime()) > oneHourMs) {
                    return false; // Too late to cancel
                }
            }

            // Cancel the order
            String cancelSql = """
                    UPDATE Orders
                    SET status='CANCELLED', cancelled_at=CURRENT_TIMESTAMP, cancel_reason=?
                    WHERE order_id=? AND username=? AND status='NEW'
                    """;

            try (PreparedStatement cancelPs = con.prepareStatement(cancelSql)) {
                cancelPs.setString(1, reason);
                cancelPs.setInt(2, orderId);
                cancelPs.setString(3, username);

                boolean success = cancelPs.executeUpdate() == 1;

                if (success) {
                    OrderStatusHistoryDAO.addHistory(orderId, "CANCELLED", username, reason);

                    // Restore stock for cancelled items
                    List<OrderDetail.OrderItem> items = getOrderItems(orderId);
                    for (OrderDetail.OrderItem item : items) {
                        ProductDAO.addStock(item.getProductId(), item.getKg());
                    }
                }

                return success;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== OWNER VIEWS =====================

    /**
     * Gets all order details (for owner view).
     * 
     * <p>
     * Retrieves detailed information about all orders in the system, including
     * customer information. Orders are ordered by creation date (most recent
     * first).
     * 
     * @return A list of OrderDetail objects for all orders, ordered by created_at
     *         DESC
     */
    public static List<OrderDetail> getAllOrderDetails() {
        List<OrderDetail> list = new ArrayList<>();

        String sql = """
                SELECT o.order_id, o.username, o.status, o.requested_delivery,
                       o.total_cost, o.created_at, o.delivered_at, o.carrier_username,
                       o.cancelled_at, o.cancel_reason,
                       u.address, u.phone
                FROM Orders o
                LEFT JOIN UserInfo u ON o.username = u.username
                ORDER BY o.created_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderDetail od = new OrderDetail();
                od.setOrderId(rs.getInt("order_id"));
                od.setCustomerUsername(rs.getString("username"));
                od.setStatus(rs.getString("status"));
                od.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                od.setTotalVatIncluded(rs.getDouble("total_cost"));
                od.setCreatedAt(rs.getTimestamp("created_at"));
                od.setDeliveredAt(rs.getTimestamp("delivered_at"));
                od.setCarrierUsername(rs.getString("carrier_username"));
                od.setCancelledAt(rs.getTimestamp("cancelled_at"));
                od.setCancelReason(rs.getString("cancel_reason"));
                od.setCustomerAddress(rs.getString("address"));
                od.setCustomerPhone(rs.getString("phone"));
                list.add(od);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Gets detailed information about a specific order.
     * 
     * <p>
     * Retrieves complete order information including customer details and order
     * items.
     * 
     * @param orderId The ID of the order
     * @return The OrderDetail object if found, or null if not found
     */
    public static OrderDetail getOrderDetail(int orderId) {
        String sql = """
                SELECT o.order_id, o.username, o.status, o.requested_delivery,
                       o.total_cost, o.created_at, o.delivered_at, o.carrier_username,
                       o.cancelled_at, o.cancel_reason,
                       u.address, u.phone
                FROM Orders o
                LEFT JOIN UserInfo u ON o.username = u.username
                WHERE o.order_id = ?
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrderDetail od = new OrderDetail();
                    od.setOrderId(rs.getInt("order_id"));
                    od.setCustomerUsername(rs.getString("username"));
                    od.setStatus(rs.getString("status"));
                    od.setRequestedDelivery(rs.getTimestamp("requested_delivery"));
                    od.setTotalVatIncluded(rs.getDouble("total_cost"));
                    od.setCreatedAt(rs.getTimestamp("created_at"));
                    od.setDeliveredAt(rs.getTimestamp("delivered_at"));
                    od.setCarrierUsername(rs.getString("carrier_username"));
                    od.setCancelledAt(rs.getTimestamp("cancelled_at"));
                    od.setCancelReason(rs.getString("cancel_reason"));
                    od.setCustomerAddress(rs.getString("address"));
                    od.setCustomerPhone(rs.getString("phone"));
                    od.setItems(getOrderItems(orderId));
                    return od;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ===================== REPORTS =====================

    /**
     * Gets daily order counts for the last N days.
     * 
     * <p>
     * Retrieves a map of dates to order counts for analytics/reporting purposes.
     * 
     * @param days The number of days to look back
     * @return A map where keys are dates (as strings) and values are order counts
     */
    public static Map<String, Integer> getDailyOrderCounts(int days) {
        Map<String, Integer> counts = new HashMap<>();

        String sql = """
                SELECT DATE(created_at) as order_date, COUNT(*) as cnt
                FROM Orders
                WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY)
                GROUP BY DATE(created_at)
                ORDER BY order_date
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, days);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("order_date"), rs.getInt("cnt"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * Get daily revenue for last N days.
     */
    public static Map<String, Double> getDailyRevenue(int days) {
        Map<String, Double> revenue = new HashMap<>();

        String sql = """
                SELECT DATE(created_at) as order_date, SUM(total_cost) as total
                FROM Orders
                WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY)
                  AND status != 'CANCELLED'
                GROUP BY DATE(created_at)
                ORDER BY order_date
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, days);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    revenue.put(rs.getString("order_date"), rs.getDouble("total"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return revenue;
    }

    /**
     * Get order status counts.
     */
    public static Map<String, Integer> getOrderStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();

        String sql = """
                SELECT status, COUNT(*) as cnt
                FROM Orders
                GROUP BY status
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return counts;
    }

    // Backward compatibility
    public static List<Order> getNewOrders() {
        return getAvailableOrders();
    }

    /**
     * Marks an order as delivered (backward compatibility method).
     * 
     * @param orderId         The ID of the order to mark as delivered
     * @param carrierUsername The username of the carrier
     * @return true if the order was marked as delivered successfully, false
     *         otherwise
     * @deprecated Use markDeliveredByCarrier() instead
     */
    @Deprecated
    public static boolean markDelivered(int orderId, String carrierUsername) {
        return markDeliveredByCarrier(orderId, carrierUsername);
    }
}
