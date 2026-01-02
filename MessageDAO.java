package dao;

import model.Message;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Message operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for messages,
 * enabling communication between users (customers, owners, carriers). Messages
 * support threading through parent message references.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Message CRUD operations</li>
 * <li>Send messages</li>
 * <li>Get received and sent messages</li>
 * <li>Message threading (replies)</li>
 * <li>Read/unread status tracking</li>
 * <li>Unread message counting</li>
 * <li>Get owner username for messaging</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Message&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class MessageDAO extends AbstractDAO<Message> {

    /**
     * Singleton instance of MessageDAO.
     */
    private static final MessageDAO INSTANCE = new MessageDAO();

    /**
     * Gets the singleton instance of MessageDAO.
     * 
     * @return The single instance of MessageDAO
     */
    public static MessageDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "Messages";
    }

    @Override
    protected String getIdColumnName() {
        return "message_id";
    }

    @Override
    protected Message mapResultSetToEntity(ResultSet rs) throws Exception {
        Integer parentId = rs.getInt("parent_message_id");
        if (rs.wasNull())
            parentId = null;

        return new Message(
                rs.getInt("message_id"),
                rs.getString("sender_username"),
                rs.getString("receiver_username"),
                rs.getString("subject"),
                rs.getString("content"),
                rs.getBoolean("is_read"),
                parentId,
                rs.getTimestamp("sent_at"));
    }

    /**
     * Sends a message from one user to another.
     * 
     * <p>
     * Creates a new message in the database. If parentId is provided,
     * the message is treated as a reply to an existing message, enabling
     * message threading.
     * 
     * @param sender   The username of the sender
     * @param receiver The username of the receiver
     * @param subject  The message subject
     * @param content  The message content
     * @param parentId The ID of the parent message if this is a reply (null if new
     *                 thread)
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendMessage(String sender, String receiver,
            String subject, String content, Integer parentId) {
        String sql = """
                INSERT INTO Messages(sender_username, receiver_username, subject, content, parent_message_id)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, subject);
            ps.setString(4, content);

            if (parentId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, parentId);
            }

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all messages received by a user.
     * 
     * <p>
     * Retrieves messages where the specified user is the receiver,
     * ordered by most recent first.
     * 
     * @param username The username of the receiver
     * @return A list of received messages, ordered by sent_at DESC
     */
    public static List<Message> getReceivedMessages(String username) {
        List<Message> list = new ArrayList<>();

        String sql = """
                SELECT * FROM Messages
                WHERE receiver_username = ?
                ORDER BY sent_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

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

    /**
     * Gets all messages sent by a user.
     * 
     * <p>
     * Retrieves messages where the specified user is the sender,
     * ordered by most recent first.
     * 
     * @param username The username of the sender
     * @return A list of sent messages, ordered by sent_at DESC
     */
    public static List<Message> getSentMessages(String username) {
        List<Message> list = new ArrayList<>();

        String sql = """
                SELECT * FROM Messages
                WHERE sender_username = ?
                ORDER BY sent_at DESC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

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

    /**
     * Gets the count of unread messages for a user.
     * 
     * @param username The username to get the unread count for
     * @return The number of unread messages
     */
    public static int getUnreadCount(String username) {
        String sql = "SELECT COUNT(*) FROM Messages WHERE receiver_username = ? AND is_read = FALSE";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Marks a message as read.
     * 
     * @param messageId The ID of the message to mark as read
     * @return true if the update was successful, false otherwise
     */
    public static boolean markAsRead(int messageId) {
        String sql = "UPDATE Messages SET is_read = TRUE WHERE message_id = ?";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets a conversation thread starting from a message.
     * 
     * <p>
     * Retrieves the specified message and all its replies (messages with
     * this message as parent), ordered chronologically.
     * 
     * @param messageId The ID of the root message in the conversation
     * @return A list of messages in the conversation thread, ordered by sent_at ASC
     */
    public static List<Message> getConversation(int messageId) {
        List<Message> list = new ArrayList<>();

        // Get the message and all replies
        String sql = """
                SELECT * FROM Messages
                WHERE message_id = ? OR parent_message_id = ?
                ORDER BY sent_at ASC
                """;

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            ps.setInt(2, messageId);

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

    /**
     * Gets the first owner username for messaging purposes.
     * 
     * <p>
     * Retrieves a username of an owner user from the database. This is useful
     * when customers need to send messages to the store owner.
     * 
     * @return The username of an owner, or "owner" as default if none found
     */
    public static String getOwnerUsername() {
        String sql = "SELECT username FROM UserInfo WHERE role = 'owner' LIMIT 1";

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "owner"; // default
    }
}
