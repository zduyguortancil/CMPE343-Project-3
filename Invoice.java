package model;

/**
 * Entity class representing an invoice for an order.
 * 
 * <p>This class extends Entity and stores invoice information including the PDF
 * document, formatted text content, and a detailed transaction log. Invoices are
 * generated when orders are placed and can be viewed or downloaded by customers.
 * 
 * <p>The invoice contains:
 * <ul>
 *   <li>PDF document (stored as BLOB) for download and printing</li>
 *   <li>Formatted text content (stored as CLOB) for display</li>
 *   <li>Detailed transaction log (stored as CLOB) for audit purposes</li>
 *   <li>Creation timestamp</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends Entity
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class Invoice extends Entity {

    /**
     * The unique identifier of the order this invoice is for.
     */
    private int orderId;
    
    /**
     * The PDF document as a byte array (BLOB).
     * Contains the formatted invoice in PDF format.
     */
    private byte[] invoicePdf; // BLOB
    
    /**
     * The formatted invoice content as text (CLOB).
     * Contains a human-readable text representation of the invoice.
     */
    private String invoiceContent; // CLOB
    
    /**
     * The detailed transaction log (CLOB).
     * Contains comprehensive information about the order for audit purposes.
     */
    private String transactionLog; // CLOB
    
    /**
     * The timestamp when this invoice was created.
     */
    private java.sql.Timestamp createdAt;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Invoice instance with default values.
     */
    public Invoice() {
        super();
    }

    /**
     * Constructor with all parameters.
     * 
     * @param invoiceId The unique identifier for this invoice
     * @param orderId The order ID this invoice is for
     * @param invoicePdf The PDF document as a byte array
     * @param invoiceContent The formatted invoice content as text
     * @param transactionLog The detailed transaction log
     */
    public Invoice(int invoiceId, int orderId, byte[] invoicePdf,
            String invoiceContent, String transactionLog) {
        super(invoiceId);
        this.orderId = orderId;
        this.invoicePdf = invoicePdf;
        this.invoiceContent = invoiceContent;
        this.transactionLog = transactionLog;
    }

    /**
     * Gets the order ID this invoice is for.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID this invoice is for.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the PDF document as a byte array.
     * 
     * @return The PDF document bytes, or null if not set
     */
    public byte[] getInvoicePdf() {
        return invoicePdf;
    }

    /**
     * Sets the PDF document.
     * 
     * @param invoicePdf The PDF document as a byte array
     */
    public void setInvoicePdf(byte[] invoicePdf) {
        this.invoicePdf = invoicePdf;
    }

    /**
     * Gets the formatted invoice content as text.
     * 
     * @return The invoice content, or null if not set
     */
    public String getInvoiceContent() {
        return invoiceContent;
    }

    /**
     * Sets the formatted invoice content.
     * 
     * @param invoiceContent The invoice content to set
     */
    public void setInvoiceContent(String invoiceContent) {
        this.invoiceContent = invoiceContent;
    }

    /**
     * Gets the detailed transaction log.
     * 
     * @return The transaction log, or null if not set
     */
    public String getTransactionLog() {
        return transactionLog;
    }

    /**
     * Sets the detailed transaction log.
     * 
     * @param transactionLog The transaction log to set
     */
    public void setTransactionLog(String transactionLog) {
        this.transactionLog = transactionLog;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return The creation timestamp
     */
    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets a display name for this invoice.
     * 
     * <p>Returns a formatted string containing the invoice ID and order ID.
     * Format: "Invoice #invoiceId for Order #orderId"
     * 
     * @return A formatted display string for this invoice
     */
    @Override
    public String getDisplayName() {
        return "Invoice #" + getId() + " for Order #" + orderId;
    }
}
