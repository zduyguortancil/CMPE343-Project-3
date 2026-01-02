package dao;

import model.Invoice;
import util.DBUtil;

import java.sql.*;

/**
 * Data Access Object (DAO) for Invoice operations.
 * 
 * <p>
 * This class extends AbstractDAO and provides database operations for invoices,
 * including storage and retrieval of PDF documents (BLOB) and invoice content
 * (CLOB). Invoices are generated for orders and stored with both PDF and text
 * representations.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Invoice CRUD operations</li>
 * <li>PDF document storage (BLOB)</li>
 * <li>Invoice content storage (CLOB)</li>
 * <li>Transaction log storage (CLOB)</li>
 * <li>Find invoice by order ID</li>
 * <li>Invoice creation and retrieval</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends AbstractDAO&lt;Invoice&gt;
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class InvoiceDAO extends AbstractDAO<Invoice> {

    /**
     * Singleton instance of InvoiceDAO.
     */
    private static final InvoiceDAO INSTANCE = new InvoiceDAO();

    /**
     * Gets the singleton instance of InvoiceDAO.
     * 
     * @return The single instance of InvoiceDAO
     */
    public static InvoiceDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return "Invoice";
    }

    @Override
    protected String getIdColumnName() {
        return "invoice_id";
    }

    @Override
    protected Invoice mapResultSetToEntity(ResultSet rs) throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("invoice_id"));
        invoice.setOrderId(rs.getInt("order_id"));
        invoice.setInvoicePdf(rs.getBytes("invoice_pdf"));
        invoice.setInvoiceContent(rs.getString("invoice_content"));
        invoice.setTransactionLog(rs.getString("transaction_log"));
        invoice.setCreatedAt(rs.getTimestamp("created_at"));
        return invoice;
    }

    /**
     * Saves an invoice with PDF and CLOB content to the database.
     * 
     * <p>
     * Stores the invoice PDF as a BLOB, invoice content as a CLOB, and
     * transaction log as a CLOB. Handles null values appropriately.
     * Sets the generated invoice ID on the invoice object if successful.
     * 
     * @param invoice The invoice to save
     * @return true if the save was successful, false otherwise
     */
    @Override
    public boolean save(Invoice invoice) {
        String sql = """
                INSERT INTO Invoice(order_id, invoice_pdf, invoice_content, transaction_log)
                VALUES(?, ?, ?, ?)
                """;

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, invoice.getOrderId());

            if (invoice.getInvoicePdf() == null) {
                ps.setNull(2, Types.BLOB);
            } else {
                ps.setBytes(2, invoice.getInvoicePdf());
            }

            if (invoice.getInvoiceContent() == null) {
                ps.setNull(3, Types.CLOB);
            } else {
                ps.setString(3, invoice.getInvoiceContent());
            }

            if (invoice.getTransactionLog() == null) {
                ps.setNull(4, Types.CLOB);
            } else {
                ps.setString(4, invoice.getTransactionLog());
            }

            int affected = ps.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        invoice.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds an invoice by its associated order ID.
     * 
     * @param orderId The order ID to search for
     * @return The invoice if found, or null if not found
     */
    public Invoice findByOrderId(int orderId) {
        String sql = "SELECT * FROM Invoice WHERE order_id = ?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the PDF document bytes for an invoice.
     * 
     * @param orderId The order ID to get the invoice PDF for
     * @return The PDF bytes, or null if the invoice is not found or has no PDF
     */
    public static byte[] getInvoicePdf(int orderId) {
        Invoice invoice = INSTANCE.findByOrderId(orderId);
        return invoice != null ? invoice.getInvoicePdf() : null;
    }

    /**
     * Creates and saves an invoice for an order.
     * 
     * <p>
     * Creates a new Invoice object with the provided data and saves it
     * to the database. Returns the saved invoice with its generated ID.
     * 
     * @param orderId        The order ID this invoice is for
     * @param pdfBytes       The PDF document as a byte array (can be null)
     * @param content        The invoice content as text (can be null)
     * @param transactionLog The transaction log as text (can be null)
     * @return The created and saved Invoice, or null if save failed
     */
    public static Invoice createInvoice(int orderId, byte[] pdfBytes,
            String content, String transactionLog) {
        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setInvoicePdf(pdfBytes);
        invoice.setInvoiceContent(content);
        invoice.setTransactionLog(transactionLog);

        if (INSTANCE.save(invoice)) {
            return invoice;
        }
        return null;
    }
}
