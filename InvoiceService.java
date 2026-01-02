package service;

import dao.InvoiceDAO;
import dao.OrderDAO;
import model.Invoice;
import model.OrderDetail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service class for generating PDF invoices for orders.
 * 
 * <p>This service provides functionality to create invoices for customer orders.
 * Since external PDF libraries (iText/PDFBox) cannot be added without user
 * confirmation, this service generates a simple text-based invoice and stores it
 * as a "pseudo-PDF" (text format). The content is stored as CLOB in the database
 * and can be converted to actual PDF format later if needed.
 * 
 * <p>The invoice includes:
 * <ul>
 *   <li>Customer information</li>
 *   <li>Delivery information</li>
 *   <li>Order items with quantities and prices</li>
 *   <li>Subtotal, VAT (18%), and total calculations</li>
 *   <li>Transaction log for audit purposes</li>
 * </ul>
 * 
 * <p>For a full PDF implementation, add iText or Apache PDFBox to the project.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class InvoiceService {

    /**
     * Value Added Tax (VAT) rate applied to invoice subtotals.
     * Currently set to 18% (0.18).
     */
    private static final double VAT_RATE = 0.18;
    
    /**
     * Date format used for displaying dates in invoices.
     * Format: "yyyy-MM-dd HH:mm:ss"
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates an invoice for a given order ID.
     * 
     * <p>This method retrieves the order details, generates the invoice content
     * as formatted text, creates a transaction log, converts the content to
     * pseudo-PDF format, and saves it to the database.
     * 
     * @param orderId The unique identifier of the order to generate invoice for
     * @return Invoice object containing the generated invoice data, or null if
     *         the order does not exist
     */
    public static Invoice generateInvoice(int orderId) {
        OrderDetail order = OrderDAO.getOrderDetail(orderId);
        if (order == null)
            return null;

        // Generate invoice content (text format)
        String invoiceContent = generateInvoiceContent(order);

        // Generate transaction log
        String transactionLog = generateTransactionLog(order);

        // Convert content to bytes (pseudo-PDF)
        byte[] pdfBytes = createPseudoPdf(invoiceContent);

        // Save to database
        return InvoiceDAO.createInvoice(orderId, pdfBytes, invoiceContent, transactionLog);
    }

    /**
     * Generates the formatted invoice content as a text string.
     * 
     * <p>Creates a formatted text representation of the invoice including:
     * <ul>
     *   <li>Invoice header with company name</li>
     *   <li>Invoice date and order information</li>
     *   <li>Customer information (name, address, phone)</li>
     *   <li>Delivery information (requested delivery date, status, carrier)</li>
     *   <li>Order items table with product names, quantities, prices, and totals</li>
     *   <li>Financial summary (subtotal, VAT, total)</li>
     * </ul>
     * 
     * @param order The OrderDetail object containing all order information
     * @return Formatted string representing the invoice content
     */
    private static String generateInvoiceContent(OrderDetail order) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                         GreenGrocer\n");
        sb.append("                           INVOICE\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        sb.append("Invoice Date: ").append(DATE_FORMAT.format(new Date())).append("\n");
        sb.append("Order ID: #").append(order.getOrderId()).append("\n");
        sb.append("Order Date: ")
                .append(order.getCreatedAt() != null ? DATE_FORMAT.format(order.getCreatedAt()) : "N/A").append("\n\n");

        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("CUSTOMER INFORMATION\n");
        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("Customer: ").append(order.getCustomerUsername()).append("\n");
        sb.append("Address: ").append(order.getCustomerAddress() != null ? order.getCustomerAddress() : "Not provided")
                .append("\n");
        sb.append("Phone: ").append(order.getCustomerPhone() != null ? order.getCustomerPhone() : "Not provided")
                .append("\n\n");

        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("DELIVERY INFORMATION\n");
        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("Requested Delivery: ")
                .append(order.getRequestedDelivery() != null ? DATE_FORMAT.format(order.getRequestedDelivery()) : "N/A")
                .append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        if (order.getCarrierUsername() != null) {
            sb.append("Carrier: ").append(order.getCarrierUsername()).append("\n");
        }
        if (order.getDeliveredAt() != null) {
            sb.append("Delivered At: ").append(DATE_FORMAT.format(order.getDeliveredAt())).append("\n");
        }
        sb.append("\n");

        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("ORDER ITEMS\n");
        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append(String.format("%-30s %8s %12s %12s\n", "Product", "Kg", "Unit Price", "Total"));
        sb.append("───────────────────────────────────────────────────────────────\n");

        double subtotal = 0;
        for (OrderDetail.OrderItem item : order.getItems()) {
            double lineTotal = item.getLineTotal();
            subtotal += lineTotal;
            sb.append(String.format("%-30s %8.2f %12.2f %12.2f\n",
                    truncate(item.getProductName(), 30),
                    item.getKg(),
                    item.getPriceAtTime(), // This is getEffectivePrice
                    lineTotal));
        }

        sb.append("───────────────────────────────────────────────────────────────\n");

        double vat = subtotal * VAT_RATE;
        double total = subtotal + vat;

        sb.append(String.format("%52s %12.2f\n", "Subtotal:", subtotal));
        sb.append(String.format("%52s %12.2f\n", "VAT (18%):", vat));
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append(String.format("%52s %12.2f\n", "TOTAL:", total));
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        sb.append("Thank you for shopping at GreenGrocer!\n");
        sb.append("For any questions, please contact us.\n\n");

        sb.append("───────────────────────────────────────────────────────────────\n");
        sb.append("This invoice was automatically generated.\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Generates a detailed transaction log for audit and record-keeping purposes.
     * 
     * <p>The transaction log includes:
     * <ul>
     *   <li>Generation timestamp and order ID</li>
     *   <li>Customer information</li>
     *   <li>Order status and dates (created, requested delivery)</li>
     *   <li>Detailed item list with product IDs, names, quantities, and prices</li>
     *   <li>Financial calculations (subtotal, VAT, total)</li>
     *   <li>Delivery information if carrier is assigned</li>
     *   <li>Cancellation information if order was cancelled</li>
     * </ul>
     * 
     * @param order The OrderDetail object containing all order information
     * @return Formatted string representing the transaction log
     */
    private static String generateTransactionLog(OrderDetail order) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("=== TRANSACTION LOG ===");
        pw.println("Generated: " + DATE_FORMAT.format(new Date()));
        pw.println("Order ID: " + order.getOrderId());
        pw.println("Customer: " + order.getCustomerUsername());
        pw.println();

        pw.println("--- ORDER DETAILS ---");
        pw.println("Status: " + order.getStatus());
        pw.println("Created: " + (order.getCreatedAt() != null ? DATE_FORMAT.format(order.getCreatedAt()) : "N/A"));
        pw.println("Requested Delivery: "
                + (order.getRequestedDelivery() != null ? DATE_FORMAT.format(order.getRequestedDelivery()) : "N/A"));
        pw.println();

        pw.println("--- ITEMS ---");
        for (OrderDetail.OrderItem item : order.getItems()) {
            pw.printf("Product ID: %d, Name: %s, Kg: %.2f, Price: %.2f (effective), Line Total: %.2f%n",
                    item.getProductId(),
                    item.getProductName(),
                    item.getKg(),
                    item.getPriceAtTime(),
                    item.getLineTotal());
        }
        pw.println();

        // Calculate totals
        double subtotal = order.getItems().stream()
                .mapToDouble(OrderDetail.OrderItem::getLineTotal)
                .sum();
        double vat = subtotal * VAT_RATE;

        pw.println("--- TOTALS ---");
        pw.printf("Subtotal: %.2f%n", subtotal);
        pw.printf("VAT (18%%): %.2f%n", vat);
        pw.printf("Total (DB): %.2f%n", order.getTotalVatIncluded());
        pw.println();

        if (order.getCarrierUsername() != null) {
            pw.println("--- DELIVERY ---");
            pw.println("Carrier: " + order.getCarrierUsername());
            pw.println("Delivered: "
                    + (order.getDeliveredAt() != null ? DATE_FORMAT.format(order.getDeliveredAt()) : "Not yet"));
        }

        if (order.getCancelledAt() != null) {
            pw.println("--- CANCELLATION ---");
            pw.println("Cancelled: " + DATE_FORMAT.format(order.getCancelledAt()));
            pw.println("Reason: " + order.getCancelReason());
        }

        pw.println();
        pw.println("=== END TRANSACTION LOG ===");

        return sw.toString();
    }

    /**
     * Converts invoice text content to pseudo-PDF format (byte array).
     * 
     * <p>This method adds a PDF-like header to the text content to identify it
     * as a pseudo-PDF document. The content is encoded as UTF-8 bytes and can
     * be stored in the database or converted to an actual PDF later using
     * external libraries.
     * 
     * @param content The invoice text content to convert
     * @return Byte array containing the pseudo-PDF formatted content
     */
    private static byte[] createPseudoPdf(String content) {
        // Add a simple header to identify as pseudo-PDF
        String header = "%PDF-PSEUDO-1.0\n% GreenGrocer Invoice\n% Convert with PDF library for actual PDF\n\n";
        String fullContent = header + content;
        return fullContent.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Truncates a string to a maximum length, appending "..." if truncated.
     * 
     * <p>If the string is longer than the maximum length, it is truncated
     * to (maxLen - 3) characters and "..." is appended. If the string is null,
     * an empty string is returned.
     * 
     * @param s The string to truncate
     * @param maxLen The maximum length for the resulting string
     * @return Truncated string with "..." appended if needed, or empty string if input is null
     */
    private static String truncate(String s, int maxLen) {
        if (s == null)
            return "";
        if (s.length() <= maxLen)
            return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    /**
     * Retrieves the invoice content as viewable text for a given order.
     * 
     * <p>This method first attempts to retrieve an existing invoice from the database.
     * If no invoice exists, it generates a new invoice on-the-fly using the order details.
     * If the order does not exist, it returns an error message.
     * 
     * @param orderId The unique identifier of the order
     * @return The invoice content as a formatted text string, or "Invoice not available."
     *         if the order does not exist
     */
    public static String getInvoiceText(int orderId) {
        Invoice invoice = InvoiceDAO.getInstance().findByOrderId(orderId);
        if (invoice != null && invoice.getInvoiceContent() != null) {
            return invoice.getInvoiceContent();
        }

        // Generate on the fly if not exists
        OrderDetail order = OrderDAO.getOrderDetail(orderId);
        if (order != null) {
            return generateInvoiceContent(order);
        }

        return "Invoice not available.";
    }
}
