package dao;

import model.OrderStatusHistory;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Order Status History operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for order
 * status history records. It tracks all status changes for orders, providing
 * an audit trail of order lifecycle events.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Status history CRUD operations</li>
 * <li>Add status change records</li>
 * <li>Get status history for an order</li>
 * <li>Track who made status changes and when</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;OrderStatusHistory&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class OrderStatusHistoryDAO extends AbstractDAO<OrderStatusHistory> {

    /**
     * Singleton instance of OrderStatusHistoryDAO.
     */
    private static final OrderStatusHistoryDAO INSTANCE = new OrderStatusHistoryDAO();

    /**
     * Gets the singleton instance of OrderStatusHistoryDAO.
     * 
     * @return The single instance of OrderStatusHistoryDAO
     */
    public static OrderStatusHistoryDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "OrderStatusHistory";
    }

    @Override
    protected String getIdColumnName() {
        return "history_id";
    }

    @Override
    protected OrderStatusHistory mapResultSetToEntity(ResultSet rs) throws Exception {
        return new OrderStatusHistory(
                rs.getInt("history_id"),
                rs.getInt("order_id"),
                rs.getString("status"),
                rs.getTimestamp("changed_at"),
                rs.getString("changed_by"),
                rs.getString("notes"));
    }

    /**
     * Adds a status history entry for an order.
     * 
     * <p>
     * Creates a new history record tracking a status change, including
     * who made the change and optional notes.
     * 
     * @param orderId   The ID of the order whose status changed
     * @param status    The new status value
     * @param changedBy The username of the user who made the change
     * @param notes     Optional notes about the status change
     * @return true if the history entry was added successfully, false otherwise
     */
    public static boolean addHistory(int orderId, String status, String changedBy, String notes) {
        String sql = """
                INSERT INTO OrderStatusHistory(order_id, status, changed_by, notes)
                VALUES(?, ?, ?, ?)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.setString(3, changedBy);
            ps.setString(4, notes);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the status history for an order.
     * 
     * <p>
     * Retrieves all status change records for the specified order,
     * ordered by most recent first.
     * 
     * @param orderId The ID of the order
     * @return A list of status history records, ordered by changed_at DESC
     */
    public static List<OrderStatusHistory> getHistoryByOrder(int orderId) {
        List<OrderStatusHistory> list = new ArrayList<>();

        String sql = """
                SELECT * FROM OrderStatusHistory
                WHERE order_id = ?
                ORDER BY changed_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(INSTANCE.mapResultSetToEntity(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
