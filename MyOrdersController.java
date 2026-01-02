package controller;

import dao.CarrierRatingDAO;
import dao.OrderDAO;
import dao.OrderStatusHistoryDAO;
import service.InvoiceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.OrderDetail;
import model.OrderStatusHistory;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the My Orders screen in the GreenGrocer application.
 * 
 * <p>This controller allows customers to view their order history, track order
 * status, cancel orders (within time limits), view invoices, and rate carriers
 * after delivery.
 * 
 * <p>Features:
 * <ul>
 *   <li>View all orders in a table</li>
 *   <li>View order details and status history</li>
 *   <li>Cancel orders (within 1 hour of creation)</li>
 *   <li>View and download invoices</li>
 *   <li>Rate carriers after delivery</li>
 *   <li>Filter orders by status</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class MyOrdersController extends BaseController {

    @FXML
    private Label titleLabel;
    @FXML
    private TabPane ordersTabPane;

    // All Orders Tab
    @FXML
    private TableView<OrderDetail> orderTable;
    @FXML
    private TableColumn<OrderDetail, Integer> idCol;
    @FXML
    private TableColumn<OrderDetail, String> statusCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> createdCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> deliveryCol;
    @FXML
    private TableColumn<OrderDetail, Double> totalCol;
    @FXML
    private TableColumn<OrderDetail, String> carrierCol;
    @FXML
    private Label orderInfoLabel;

    // Deliveries Tab
    @FXML
    private TableView<OrderDetail> deliveriesTable;
    @FXML
    private TableColumn<OrderDetail, Integer> delIdCol;
    @FXML
    private TableColumn<OrderDetail, String> delCarrierCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> delDateCol;
    @FXML
    private TableColumn<OrderDetail, Double> delTotalCol;
    @FXML
    private TableColumn<OrderDetail, String> delRatingCol;
    @FXML
    private Label deliveryInfoLabel;

    // History Tab
    @FXML
    private ComboBox<String> orderComboBox;
    @FXML
    private TableView<OrderStatusHistory> historyTable;
    @FXML
    private TableColumn<OrderStatusHistory, String> histStatusCol;
    @FXML
    private TableColumn<OrderStatusHistory, Timestamp> histTimeCol;
    @FXML
    private TableColumn<OrderStatusHistory, String> histByCol;
    @FXML
    private TableColumn<OrderStatusHistory, String> histNotesCol;
    @FXML
    private Label historyInfoLabel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<OrderDetail> allOrders;

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        titleLabel.setText("My Orders - " + username);
        loadOrders();
    }

    @Override
    protected Label getUsernameLabel() {
        return titleLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "My Orders";
    }

    @FXML
    public void initialize() {
        // All Orders columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        deliveryCol.setCellValueFactory(new PropertyValueFactory<>("requestedDelivery"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalVatIncluded"));
        carrierCol.setCellValueFactory(new PropertyValueFactory<>("carrierUsername"));

        setupTimestampColumn(createdCol);
        setupTimestampColumn(deliveryCol);
        setupMoneyColumn(totalCol);

        // Status column with color
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "NEW" -> setStyle("-fx-text-fill: #2196F3;");
                        case "IN_PROGRESS" -> setStyle("-fx-text-fill: #FF9800;");
                        case "DELIVERED" -> setStyle("-fx-text-fill: #4CAF50;");
                        case "CANCELLED" -> setStyle("-fx-text-fill: #f44336;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Deliveries columns
        delIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        delCarrierCol.setCellValueFactory(new PropertyValueFactory<>("carrierUsername"));
        delDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveredAt"));
        delTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalVatIncluded"));

        setupTimestampColumn(delDateCol);
        setupMoneyColumn(delTotalCol);

        // Rating column (custom)
        delRatingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    OrderDetail od = getTableView().getItems().get(getIndex());
                    boolean rated = CarrierRatingDAO.isOrderRated(od.getOrderId());
                    setText(rated ? "✅ Rated" : "⏳ Not rated");
                }
            }
        });

        // History columns
        histStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        histTimeCol.setCellValueFactory(new PropertyValueFactory<>("changedAt"));
        histByCol.setCellValueFactory(new PropertyValueFactory<>("changedBy"));
        histNotesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        setupTimestampColumn(histTimeCol);

        clearInfoLabel(orderInfoLabel);
        clearInfoLabel(deliveryInfoLabel);
        clearInfoLabel(historyInfoLabel);
    }

    private <T> void setupTimestampColumn(TableColumn<T, Timestamp> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("-");
                else
                    setText(item.toLocalDateTime().format(DT_FMT));
            }
        });
    }

    private <T> void setupMoneyColumn(TableColumn<T, Double> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(String.format("%.2f", item));
            }
        });
    }

    private void loadOrders() {
        allOrders = OrderDAO.getOrderDetailsByUser(currentUsername);

        // All orders
        orderTable.setItems(FXCollections.observableArrayList(allOrders));

        // Deliveries (only DELIVERED)
        List<OrderDetail> delivered = allOrders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .toList();
        deliveriesTable.setItems(FXCollections.observableArrayList(delivered));

        // Order combo for history
        orderComboBox.getItems().clear();
        for (OrderDetail od : allOrders) {
            orderComboBox.getItems().add("Order #" + od.getOrderId() + " - " + od.getStatus());
        }
        if (!orderComboBox.getItems().isEmpty()) {
            orderComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        showInfoLabel(orderInfoLabel, "Refreshed.", false);
    }

    @FXML
    private void handleViewDetails() {
        OrderDetail selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(orderInfoLabel, "Select an order first!", true);
            return;
        }

        // Show order details in dialog
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(selected.getOrderId()).append("\n\n");
        sb.append("Status: ").append(selected.getStatus()).append("\n");
        sb.append("Created: ").append(
                selected.getCreatedAt() != null ? selected.getCreatedAt().toLocalDateTime().format(DT_FMT) : "-")
                .append("\n");
        sb.append("Requested Delivery: ")
                .append(selected.getRequestedDelivery() != null
                        ? selected.getRequestedDelivery().toLocalDateTime().format(DT_FMT)
                        : "-")
                .append("\n");
        sb.append("Carrier: ")
                .append(selected.getCarrierUsername() != null ? selected.getCarrierUsername() : "Not assigned")
                .append("\n\n");

        sb.append("--- ITEMS ---\n");
        for (OrderDetail.OrderItem item : selected.getItems()) {
            sb.append("• ").append(item.getProductName())
                    .append(" | ").append(String.format("%.2f kg", item.getKg()))
                    .append(" | ").append(String.format("%.2f TL/kg", item.getPriceAtTime()))
                    .append(" | ").append(String.format("%.2f TL", item.getLineTotal()))
                    .append("\n");
        }

        sb.append("\nTotal: ").append(String.format("%.2f TL", selected.getTotalVatIncluded()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + selected.getOrderId());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleViewInvoice() {
        OrderDetail selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(orderInfoLabel, "Select an order first!", true);
            return;
        }

        String invoiceText = InvoiceService.getInvoiceText(selected.getOrderId());

        TextArea textArea = new TextArea(invoiceText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace;");
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice");
        alert.setHeaderText("Invoice for Order #" + selected.getOrderId());
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(650);

        ButtonType exportBtn = new ButtonType("Export as PDF", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(0, exportBtn);

        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == exportBtn) {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Invoice as PDF");
            fileChooser.setInitialFileName("invoice_order_" + selected.getOrderId() + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(orderTable.getScene().getWindow());
            if (file != null) {
                try {
                    service.PdfInvoiceService.generatePdfInvoice(selected.getOrderId(), file.getAbsolutePath());
                    showInfoLabel(orderInfoLabel, "Invoice exported as PDF ✅", false);
                } catch (Exception e) {
                    e.printStackTrace();
                    showInfoLabel(orderInfoLabel, "PDF export failed: " + e.getMessage(), true);
                }
            }
        }
    }

    @FXML
    private void handleCancelOrder() {
        OrderDetail selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(orderInfoLabel, "Select an order first!", true);
            return;
        }

        // Check if can cancel (NEW status, within 1 hour)
        if (!"NEW".equalsIgnoreCase(selected.getStatus())) {
            showInfoLabel(orderInfoLabel, "Only NEW orders can be cancelled!", true);
            return;
        }

        if (!selected.canCancel()) {
            showInfoLabel(orderInfoLabel, "Cancel window expired! (1 hour from order)", true);
            return;
        }

        // Ask for reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancel Order");
        dialog.setHeaderText("Cancel Order #" + selected.getOrderId());
        dialog.setContentText("Reason (optional):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty())
            return;

        String reason = result.get().trim();
        if (reason.isEmpty())
            reason = "Cancelled by customer";

        boolean success = OrderDAO.cancelOrder(selected.getOrderId(), currentUsername, reason);

        if (success) {
            showInfoLabel(orderInfoLabel, "Order cancelled ✅", false);
            loadOrders();
        } else {
            showInfoLabel(orderInfoLabel, "Failed to cancel order!", true);
        }
    }

    @FXML
    private void handleRateCarrier() {
        OrderDetail selected = deliveriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(deliveryInfoLabel, "Select a delivered order!", true);
            return;
        }

        if (selected.getCarrierUsername() == null) {
            showInfoLabel(deliveryInfoLabel, "No carrier assigned!", true);
            return;
        }

        if (CarrierRatingDAO.isOrderRated(selected.getOrderId())) {
            showInfoLabel(deliveryInfoLabel, "Already rated!", true);
            return;
        }

        // Rating dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Rate Carrier");
        dialog.setHeaderText("Rate " + selected.getCarrierUsername() + " for Order #" + selected.getOrderId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
        ratingSpinner.setPrefWidth(100);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment...");
        commentArea.setPrefRowCount(3);

        grid.addRow(0, new Label("Rating (1-5 stars):"), ratingSpinner);
        grid.addRow(1, new Label("Comment:"), commentArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return ratingSpinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        if (result.isEmpty())
            return;

        int rating = result.get();
        String comment = commentArea.getText().trim();

        boolean success = CarrierRatingDAO.addRating(
                selected.getOrderId(),
                selected.getCarrierUsername(),
                currentUsername,
                rating,
                comment.isEmpty() ? null : comment);

        if (success) {
            showInfoLabel(deliveryInfoLabel, "Rating submitted ✅ " + "⭐".repeat(rating), false);
            loadOrders();
        } else {
            showInfoLabel(deliveryInfoLabel, "Failed to submit rating!", true);
        }
    }

    @FXML
    private void handleLoadHistory() {
        String selected = orderComboBox.getValue();
        if (selected == null || selected.isEmpty()) {
            showInfoLabel(historyInfoLabel, "Select an order!", true);
            return;
        }

        // Extract order ID
        try {
            int orderId = Integer.parseInt(selected.split("#")[1].split(" ")[0]);
            List<OrderStatusHistory> history = OrderStatusHistoryDAO.getHistoryByOrder(orderId);
            historyTable.setItems(FXCollections.observableArrayList(history));

            if (history.isEmpty()) {
                showInfoLabel(historyInfoLabel, "No history found.", false);
            } else {
                clearInfoLabel(historyInfoLabel);
            }
        } catch (Exception e) {
            showInfoLabel(historyInfoLabel, "Invalid selection!", true);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) orderTable.getScene().getWindow();
        stage.close();
    }
}
