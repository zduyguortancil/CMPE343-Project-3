package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import dao.OrderDAO;
import model.OrderDetail;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service class for generating professional PDF invoices using the iText library.
 * 
 * <p>This service provides functionality to create formatted PDF invoices for orders.
 * The invoices include:
 * <ul>
 *   <li>Company header and branding</li>
 *   <li>Invoice and order information</li>
 *   <li>Customer information</li>
 *   <li>Delivery information</li>
 *   <li>Detailed order items table with quantities and prices</li>
 *   <li>Financial summary (subtotal, VAT, total)</li>
 *   <li>Footer with thank you message</li>
 * </ul>
 * 
 * <p>The service uses custom colors and formatting to create professional-looking
 * invoices. PDFs are generated in A4 format and saved to the specified file path.
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class PdfInvoiceService {

    /**
     * Value Added Tax (VAT) rate applied to invoice subtotals.
     * Currently set to 18% (0.18).
     */
    private static final double VAT_RATE = 0.18;
    
    /**
     * Date format used for displaying dates in PDF invoices.
     * Format: "dd/MM/yyyy HH:mm"
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /**
     * Header color used for titles and important elements.
     * Color: #2D7A4F (Green)
     */
    private static final BaseColor HEADER_COLOR = new BaseColor(45, 122, 79); // #2D7A4F
    
    /**
     * Text color for regular content.
     * Color: Dark Gray
     */
    private static final BaseColor TEXT_COLOR = BaseColor.DARK_GRAY;
    
    /**
     * Line color for borders and separators.
     * Color: #E5E7EB (Light Gray)
     */
    private static final BaseColor LINE_COLOR = new BaseColor(229, 231, 235); // #E5E7EB

    /**
     * Generates a PDF invoice for a given order and saves it to the specified file path.
     * 
     * <p>This method retrieves the order details, creates a PDF document in A4 format,
     * and adds all invoice sections including header, invoice info, customer info,
     * delivery info, order items, and footer. The PDF is then saved to the specified path.
     * 
     * @param orderId The unique identifier of the order to generate invoice for
     * @param outputPath The file path where the PDF invoice will be saved
     * @throws IllegalArgumentException If the order with the given ID is not found
     * @throws Exception If PDF generation fails (e.g., file I/O errors, document creation errors)
     */
    public static void generatePdfInvoice(int orderId, String outputPath) throws Exception {
        OrderDetail order = OrderDAO.getOrderDetail(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));

        document.open();

        // Add content
        addHeader(document);
        addInvoiceInfo(document, order);
        addCustomerInfo(document, order);
        addDeliveryInfo(document, order);
        addOrderItems(document, order);
        addFooter(document);

        document.close();
    }

    /**
     * Adds the header section to the PDF document.
     * 
     * <p>Creates and adds the company name "GreenGrocer" as a title and "INVOICE"
     * as a subtitle, both centered. Also adds a decorative line separator.
     * 
     * @param document The PDF document to add the header to
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addHeader(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, HEADER_COLOR);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, TEXT_COLOR);

        Paragraph title = new Paragraph("GreenGrocer", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);

        Paragraph subtitle = new Paragraph("INVOICE", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        addLine(document);
    }

    /**
     * Adds invoice information section to the PDF document.
     * 
     * <p>Creates a table displaying invoice date, order ID, and order creation date.
     * 
     * @param document The PDF document to add the information to
     * @param order The OrderDetail object containing order information
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addInvoiceInfo(Document document, OrderDetail order) throws DocumentException {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_COLOR);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_COLOR);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        addInfoRow(table, "Invoice Date:", DATE_FORMAT.format(new Date()), labelFont, valueFont);
        addInfoRow(table, "Order ID:", "#" + order.getOrderId(), labelFont, valueFont);
        addInfoRow(table, "Order Date:",
                order.getCreatedAt() != null ? DATE_FORMAT.format(order.getCreatedAt()) : "N/A",
                labelFont, valueFont);

        document.add(table);
    }

    /**
     * Adds customer information section to the PDF document.
     * 
     * <p>Displays customer username, address, and phone number. If address or phone
     * is not provided, displays "Not provided".
     * 
     * @param document The PDF document to add the information to
     * @param order The OrderDetail object containing customer information
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addCustomerInfo(Document document, OrderDetail order) throws DocumentException {
        addSectionTitle(document, "CUSTOMER INFORMATION");

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_COLOR);

        document.add(new Paragraph("Customer: " + order.getCustomerUsername(), font));
        document.add(new Paragraph("Address: " +
                (order.getCustomerAddress() != null ? order.getCustomerAddress() : "Not provided"), font));
        document.add(new Paragraph("Phone: " +
                (order.getCustomerPhone() != null ? order.getCustomerPhone() : "Not provided"), font));

        document.add(Chunk.NEWLINE);
    }

    /**
     * Adds delivery information section to the PDF document.
     * 
     * <p>Displays requested delivery date, order status, carrier username (if assigned),
     * and delivery timestamp (if delivered).
     * 
     * @param document The PDF document to add the information to
     * @param order The OrderDetail object containing delivery information
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addDeliveryInfo(Document document, OrderDetail order) throws DocumentException {
        addSectionTitle(document, "DELIVERY INFORMATION");

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_COLOR);

        document.add(new Paragraph("Requested Delivery: " +
                (order.getRequestedDelivery() != null ? DATE_FORMAT.format(order.getRequestedDelivery()) : "N/A"),
                font));
        document.add(new Paragraph("Status: " + order.getStatus(), font));

        if (order.getCarrierUsername() != null) {
            document.add(new Paragraph("Carrier: " + order.getCarrierUsername(), font));
        }
        if (order.getDeliveredAt() != null) {
            document.add(new Paragraph("Delivered At: " + DATE_FORMAT.format(order.getDeliveredAt()), font));
        }

        document.add(Chunk.NEWLINE);
    }

    /**
     * Adds order items table and totals section to the PDF document.
     * 
     * <p>Creates a table with columns for product name, quantity (kg), unit price,
     * and line total. Calculates and displays subtotal, VAT (18%), and grand total.
     * 
     * @param document The PDF document to add the items to
     * @param order The OrderDetail object containing order items
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addOrderItems(Document document, OrderDetail order) throws DocumentException {
        addSectionTitle(document, "ORDER ITEMS");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3, 1, 1.5f, 1.5f });
        table.setSpacingBefore(10);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_COLOR);

        // Header
        addTableHeader(table, "Product", headerFont);
        addTableHeader(table, "Kg", headerFont);
        addTableHeader(table, "Unit Price", headerFont);
        addTableHeader(table, "Total", headerFont);

        // Items
        double subtotal = 0;
        for (OrderDetail.OrderItem item : order.getItems()) {
            double lineTotal = item.getLineTotal();
            subtotal += lineTotal;

            addTableCell(table, truncate(item.getProductName(), 30), cellFont);
            addTableCell(table, String.format("%.2f", item.getKg()), cellFont);
            addTableCell(table, String.format("%.2f TL", item.getPriceAtTime()), cellFont);
            addTableCell(table, String.format("%.2f TL", lineTotal), cellFont);
        }

        document.add(table);

        // Totals
        double vat = subtotal * VAT_RATE;
        double total = subtotal + vat;

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(15);

        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_COLOR);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, HEADER_COLOR);

        addTotalRow(totalsTable, "Subtotal:", String.format("%.2f TL", subtotal), totalFont);
        addTotalRow(totalsTable, "VAT (18%):", String.format("%.2f TL", vat), totalFont);
        addTotalRow(totalsTable, "TOTAL:", String.format("%.2f TL", total), boldFont);

        document.add(totalsTable);
    }

    /**
     * Adds footer section to the PDF document.
     * 
     * <p>Adds a centered thank you message at the bottom of the invoice.
     * 
     * @param document The PDF document to add the footer to
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);

        Paragraph footer = new Paragraph("\nThank you for shopping with GreenGrocer!", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

    /**
     * Adds a decorative line separator to the PDF document.
     * 
     * @param document The PDF document to add the line to
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addLine(Document document) throws DocumentException {
        Paragraph line = new Paragraph("_________________________________________________________________");
        line.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8, LINE_COLOR));
        document.add(line);
    }

    /**
     * Adds a section title to the PDF document.
     * 
     * <p>Creates a formatted section title with appropriate spacing and styling.
     * 
     * @param document The PDF document to add the title to
     * @param title The section title text
     * @throws DocumentException If an error occurs while adding content to the document
     */
    private static void addSectionTitle(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, HEADER_COLOR);
        Paragraph section = new Paragraph(title, sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(8);
        document.add(section);
    }

    /**
     * Adds an information row to a PDF table.
     * 
     * <p>Creates a two-column row with a label and value, both without borders.
     * 
     * @param table The PDF table to add the row to
     * @param label The label text for the information
     * @param value The value text for the information
     * @param labelFont The font to use for the label
     * @param valueFont The font to use for the value
     */
    private static void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    /**
     * Adds a header cell to a PDF table.
     * 
     * <p>Creates a table header cell with header background color, centered text,
     * and appropriate padding.
     * 
     * @param table The PDF table to add the header to
     * @param text The header text
     * @param font The font to use for the header text
     */
    private static void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    /**
     * Adds a regular cell to a PDF table.
     * 
     * <p>Creates a table cell with borders, border color, and padding.
     * 
     * @param table The PDF table to add the cell to
     * @param text The cell text content
     * @param font The font to use for the cell text
     */
    private static void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(LINE_COLOR);
        cell.setPadding(6);
        table.addCell(cell);
    }

    /**
     * Adds a total row to a PDF table.
     * 
     * <p>Creates a two-column row for displaying totals (e.g., subtotal, VAT, total)
     * with right-aligned text and no borders.
     * 
     * @param table The PDF table to add the row to
     * @param label The label text (e.g., "Subtotal:", "VAT (18%):", "TOTAL:")
     * @param value The value text (formatted amount)
     * @param font The font to use for the row text
     */
    private static void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3);
        table.addCell(valueCell);
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
}
