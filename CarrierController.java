package controller;

import dao.CarrierRatingDAO;
import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Order;
import model.OrderDetail;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Carrier dashboard in the GreenGrocer application.
 * 
 * <p>
 * This controller manages the delivery interface for carriers, providing
 * three main sections: available orders, assigned orders, and delivery history.
 * Carriers can view available orders, accept delivery assignments, mark orders
 * as delivered, and view their ratings.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>View available orders for delivery</li>
 * <li>Accept delivery assignments</li>
 * <li>View assigned orders</li>
 * <li>Mark orders as delivered</li>
 * <li>View delivery history</li>
 * <li>View customer ratings</li>
 * <li>Order details and customer information</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends BaseController
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CarrierController extends BaseController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label ratingLabel;

    // Available Orders
    @FXML
    private TableView<OrderDetail> availableTable;
    @FXML
    private TableColumn<OrderDetail, Integer> avIdCol;
    @FXML
    private TableColumn<OrderDetail, String> avCustomerCol;
    @FXML
    private TableColumn<OrderDetail, String> avAddressCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> avDeliveryCol;
    @FXML
    private TableColumn<OrderDetail, Double> avTotalCol;
    @FXML
    private Label availableInfoLabel;

    // Order details
    @FXML
    private Label detailCustomerLabel;
    @FXML
    private Label detailAddressLabel;
    @FXML
    private Label detailPhoneLabel;
    @FXML
    private Label detailDeliveryLabel;
    @FXML
    private Label detailTotalLabel;
    @FXML
    private ListView<String> detailProductsList;

    // Current Orders
    @FXML
    private TableView<OrderDetail> currentTable;
    @FXML
    private TableColumn<OrderDetail, Integer> curIdCol;
    @FXML
    private TableColumn<OrderDetail, String> curCustomerCol;
    @FXML
    private TableColumn<OrderDetail, String> curAddressCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> curDeliveryCol;
    @FXML
    private Label currentInfoLabel;

    // Completed Orders
    @FXML
    private TableView<Order> completedTable;
    @FXML
    private TableColumn<Order, Integer> compIdCol;
    @FXML
    private TableColumn<Order, String> compCustomerCol;
    @FXML
    private TableColumn<Order, Double> compTotalCol;
    @FXML
    private Label completedInfoLabel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameLabel.setText("Carrier: " + username);
        loadRating();
        loadAllOrders();
    }

    @Override
    protected Label getUsernameLabel() {
        return usernameLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Carrier Dashboard";
    }

    @FXML
    public void initialize() {
        // Available columns
        avIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        avCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        avAddressCol.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        avDeliveryCol.setCellValueFactory(new PropertyValueFactory<>("requestedDelivery"));
        avTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalVatIncluded"));

        setupTimestampColumnShort(avDeliveryCol);
        setupMoneyColumn(avTotalCol);

        // Current columns
        curIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        curCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        curAddressCol.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        curDeliveryCol.setCellValueFactory(new PropertyValueFactory<>("requestedDelivery"));

        setupTimestampColumnShort(curDeliveryCol);

        // Completed columns
        compIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        compCustomerCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        compTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        setupMoneyColumnOrder(compTotalCol);

        // Selection listener for available orders
        availableTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                displayOrderDetails(selected);
            }
        });

        clearInfoLabel(availableInfoLabel);
        clearInfoLabel(currentInfoLabel);
        clearInfoLabel(completedInfoLabel);
    }

    private void setupTimestampColumnShort(TableColumn<OrderDetail, Timestamp> col) {
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

    private void setupMoneyColumn(TableColumn<OrderDetail, Double> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(String.format("%.0f", item));
            }
        });
    }

    private void setupMoneyColumnOrder(TableColumn<Order, Double> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(String.format("%.0f", item));
            }
        });
    }

    private void loadRating() {
        double avgRating = CarrierRatingDAO.getCarrierAverageRating(currentUsername);
        int count = CarrierRatingDAO.getCarrierRatingCount(currentUsername);

        if (count > 0) {
            String stars = "⭐".repeat((int) Math.round(avgRating));
            ratingLabel.setText(String.format("Rating: %.1f %s (%d reviews)", avgRating, stars, count));
        } else {
            ratingLabel.setText("Rating: No reviews yet");
        }
    }

    private void loadAllOrders() {
        // Available (NEW, no carrier assigned)
        List<OrderDetail> available = OrderDAO.getAvailableOrderDetails();
        availableTable.setItems(FXCollections.observableArrayList(available));

        // Current (IN_PROGRESS, assigned to this carrier)
        List<OrderDetail> current = OrderDAO.getCurrentOrderDetails(currentUsername);
        currentTable.setItems(FXCollections.observableArrayList(current));

        // Completed (DELIVERED by this carrier)
        List<Order> completed = OrderDAO.getCompletedOrdersByCarrier(currentUsername);
        completedTable.setItems(FXCollections.observableArrayList(completed));

        // Clear details
        clearOrderDetails();
    }

    private void displayOrderDetails(OrderDetail od) {
        detailCustomerLabel.setText("Customer: " + od.getCustomerUsername());
        detailAddressLabel
                .setText("Address: " + (od.getCustomerAddress() != null ? od.getCustomerAddress() : "Not provided"));
        detailPhoneLabel.setText("Phone: " + (od.getCustomerPhone() != null ? od.getCustomerPhone() : "Not provided"));
        detailDeliveryLabel.setText("Delivery: " + (od.getRequestedDelivery() != null
                ? od.getRequestedDelivery().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "-"));
        detailTotalLabel.setText("Total (VAT inc): " + String.format("%.2f TL", od.getTotalVatIncluded()));

        // Products list
        detailProductsList.getItems().clear();
        for (OrderDetail.OrderItem item : od.getItems()) {
            String productLine = String.format("%s - %.2f kg @ %.2f TL/kg = %.2f TL",
                    item.getProductName(),
                    item.getKg(),
                    item.getPriceAtTime(),
                    item.getLineTotal());
            detailProductsList.getItems().add(productLine);
        }
    }

    private void clearOrderDetails() {
        detailCustomerLabel.setText("Customer: -");
        detailAddressLabel.setText("Address: -");
        detailPhoneLabel.setText("Phone: -");
        detailDeliveryLabel.setText("Delivery: -");
        detailTotalLabel.setText("Total: -");
        detailProductsList.getItems().clear();
    }

    @FXML
    private void handleTakeOrder() {
        OrderDetail selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(availableInfoLabel, "Select an order!", true);
            return;
        }

        // Atomically take order (only if carrier_username IS NULL)
        int success = OrderDAO.takeOrders(List.of(selected.getOrderId()), currentUsername);

        if (success == 1) {
            showInfoLabel(availableInfoLabel, "Order #" + selected.getOrderId() + " taken ✅", false);
            loadAllOrders();
        } else {
            showInfoLabel(availableInfoLabel, "Failed! Order may have been taken.", true);
            loadAllOrders();
        }
    }

    @FXML
    private void handleMarkDelivered() {
        OrderDetail selected = currentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(currentInfoLabel, "Select an order!", true);
            return;
        }

        Optional<Timestamp> deliveryTime = showDeliveryDateDialog(selected);

        if (deliveryTime.isEmpty()) {
            showInfoLabel(currentInfoLabel, "Delivery cancelled.", false);
            return;
        }

        boolean success = OrderDAO.markDeliveredWithDate(
                selected.getOrderId(),
                currentUsername,
                deliveryTime.get());

        if (success) {
            showInfoLabel(currentInfoLabel, "Order #" + selected.getOrderId() + " delivered ✅", false);
            loadAllOrders();
        } else {
            showInfoLabel(currentInfoLabel, "Failed to mark delivered!", true);
        }
    }

    private Optional<Timestamp> showDeliveryDateDialog(OrderDetail order) {
        Dialog<Timestamp> dialog = new Dialog<>();
        dialog.setTitle("Mark Delivered");
        dialog.setHeaderText("Confirm delivery for Order #" + order.getOrderId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        LocalDateTime now = LocalDateTime.now();
        Timestamp orderCreatedAt = order.getCreatedAt();
        LocalDateTime orderTime = (orderCreatedAt != null) ? orderCreatedAt.toLocalDateTime() : now.minusHours(1);

        Label infoLabel = new Label(
                "Order placed: " + orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        infoLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 11;");

        ComboBox<String> dateCombo = new ComboBox<>();
        dateCombo.getItems().add("Today");

        boolean orderWasYesterday = orderTime.toLocalDate().isBefore(now.toLocalDate());
        if (orderWasYesterday) {
            dateCombo.getItems().add("Yesterday");
        }
        dateCombo.setValue("Today");

        int minHour = 0;
        int minMinute = 0;

        if (orderTime.toLocalDate().equals(now.toLocalDate())) {
            minHour = orderTime.getHour();
            minMinute = orderTime.getMinute();
        }

        Spinner<Integer> hourSpinner = new Spinner<>(minHour, now.getHour(), now.getHour());
        hourSpinner.setPrefWidth(70);
        hourSpinner.setEditable(true);

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, now.getMinute());
        minuteSpinner.setPrefWidth(70);
        minuteSpinner.setEditable(true);

        Label validationLabel = new Label("Time must be between order time and now");
        validationLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10;");

        grid.add(infoLabel, 0, 0, 2, 1);
        grid.addRow(1, new Label("Date:"), dateCombo);
        javafx.scene.layout.HBox timeBox = new javafx.scene.layout.HBox(5, hourSpinner, new Label(":"), minuteSpinner);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        grid.addRow(2, new Label("Time:"), timeBox);
        grid.add(validationLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final LocalDateTime finalOrderTime = orderTime;
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalDateTime selectedDate = now.toLocalDate().atStartOfDay();
                    if ("Yesterday".equals(dateCombo.getValue())) {
                        selectedDate = now.minusDays(1).toLocalDate().atStartOfDay();
                    }

                    int hour = Integer.parseInt(hourSpinner.getEditor().getText().trim());
                    int minute = Integer.parseInt(minuteSpinner.getEditor().getText().trim());

                    if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                        showError("Invalid time format!");
                        return null;
                    }

                    LocalDateTime deliveryDateTime = selectedDate
                            .withHour(hour)
                            .withMinute(minute)
                            .withSecond(0)
                            .withNano(0);

                    if (deliveryDateTime.isBefore(finalOrderTime)) {
                        showError("Delivery time (" + deliveryDateTime.format(DT_FMT) + ") must be after order time ("
                                + finalOrderTime.format(DT_FMT) + ")");
                        return null;
                    }

                    // Check requested delivery time
                    Timestamp requestedDelivery = order.getRequestedDelivery();
                    if (requestedDelivery != null) {
                        LocalDateTime requestedTime = requestedDelivery.toLocalDateTime();
                        if (deliveryDateTime.isBefore(requestedTime)) {
                            showError("Cannot deliver before requested time!\n" +
                                    "Requested: "
                                    + requestedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                                    "Your selected: "
                                    + deliveryDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                            return null;
                        }
                    }

                    if (deliveryDateTime.isAfter(now)) {
                        showError("Delivery time cannot be in the future!");
                        return null;
                    }

                    return Timestamp.valueOf(deliveryDateTime);
                } catch (Exception e) {
                    showError("Invalid time format!");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadRating();
        loadAllOrders();
        showInfoLabel(availableInfoLabel, "Refreshed.", false);
    }

    @FXML
    private void handleLogout() {
        performLogout();
    }
}
