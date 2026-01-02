package model;

import java.sql.Timestamp;

/**
 * Entity class representing a message in the messaging system.
 * 
 * <p>This class extends Entity and represents a message sent between users
 * (e.g., customer to owner, owner to customer). Messages support threading
 * through parent message references, allowing for conversation threads.
 * 
 * <p>Features:
 * <ul>
 *   <li>Sender and receiver identification</li>
 *   <li>Subject and content</li>
 *   <li>Read/unread status tracking</li>
 *   <li>Parent message reference for threading</li>
 *   <li>Timestamp for when the message was sent</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Message extends Entity {

    /**
     * The username of the user who sent this message.
     */
    private String senderUsername;
    
    /**
     * The username of the user who should receive this message.
     */
    private String receiverUsername;
    
    /**
     * The subject line of the message.
     */
    private String subject;
    
    /**
     * The content/body of the message.
     */
    private String content;
    
    /**
     * Whether the message has been read by the receiver.
     */
    private boolean isRead;
    
    /**
     * The ID of the parent message if this is a reply (null if this is a new thread).
     */
    private Integer parentMessageId;
    
    /**
     * The timestamp when this message was sent.
     */
    private Timestamp sentAt;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Message instance with default values.
     */
    public Message() {
        super();
    }

    /**
     * Constructor with all parameters.
     * 
     * @param messageId The unique identifier for this message
     * @param senderUsername The username of the sender
     * @param receiverUsername The username of the receiver
     * @param subject The message subject
     * @param content The message content
     * @param isRead Whether the message has been read
     * @param parentMessageId The ID of the parent message (null if not a reply)
     * @param sentAt The timestamp when the message was sent
     */
    public Message(int messageId, String senderUsername, String receiverUsername,
            String subject, String content, boolean isRead,
            Integer parentMessageId, Timestamp sentAt) {
        super(messageId);
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.subject = subject;
        this.content = content;
        this.isRead = isRead;
        this.parentMessageId = parentMessageId;
        this.sentAt = sentAt;
    }

    /**
     * Gets the sender username.
     * 
     * @return The sender username
     */
    public String getSenderUsername() {
        return senderUsername;
    }

    /**
     * Sets the sender username.
     * 
     * @param senderUsername The sender username to set
     */
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    /**
     * Gets the receiver username.
     * 
     * @return The receiver username
     */
    public String getReceiverUsername() {
        return receiverUsername;
    }

    /**
     * Sets the receiver username.
     * 
     * @param receiverUsername The receiver username to set
     */
    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    /**
     * Gets the message subject.
     * 
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the message subject.
     * 
     * @param subject The subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the message content.
     * 
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content.
     * 
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Checks if the message has been read.
     * 
     * @return true if the message has been read, false otherwise
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets the read status of the message.
     * 
     * @param read true to mark as read, false to mark as unread
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Gets the parent message ID if this is a reply.
     * 
     * @return The parent message ID, or null if this is not a reply
     */
    public Integer getParentMessageId() {
        return parentMessageId;
    }

    /**
     * Sets the parent message ID.
     * 
     * @param parentMessageId The parent message ID to set (null if not a reply)
     */
    public void setParentMessageId(Integer parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    /**
     * Gets the timestamp when the message was sent.
     * 
     * @return The sent timestamp
     */
    public Timestamp getSentAt() {
        return sentAt;
    }

    /**
     * Sets the timestamp when the message was sent.
     * 
     * @param sentAt The sent timestamp to set
     */
    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Gets a display name for this message.
     * 
     * <p>Returns a formatted string containing the sender username and subject.
     * Format: "From: senderUsername - subject"
     * 
     * @return A formatted display string for this message
     */
    @Override
    public String getDisplayName() {
        return "From: " + senderUsername + " - " + subject;
    }
}
