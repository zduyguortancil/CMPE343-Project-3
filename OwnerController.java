package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the Owner dashboard in the GreenGrocer application.
 * 
 * <p>
 * This controller provides comprehensive management features for store owners,
 * including product management, order oversight, customer management, carrier
 * management, analytics, and messaging.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Product management (add, edit, delete, update stock, set discounts)</li>
 * <li>Order management and oversight</li>
 * <li>Customer management and loyalty program</li>
 * <li>Carrier management</li>
 * <li>Analytics and statistics (charts, sales data)</li>
 * <li>Messaging system</li>
 * <li>Coupon management</li>
 * </ul>
 * 
 * <p>
 * INHERITANCE: Extends BaseController
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class OwnerController extends BaseController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label messageCountLabel;
    @FXML
    private TabPane mainTabPane;

    // Products Tab
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> prodIdCol;
    @FXML
    private TableColumn<Product, String> prodNameCol;
    @FXML
    private TableColumn<Product, String> prodTypeCol;
    @FXML
    private TableColumn<Product, Double> prodPriceCol;
    @FXML
    private TableColumn<Product, Double> prodStockCol;
    @FXML
    private TableColumn<Product, Integer> prodThreshCol;
    @FXML
    private Label productInfoLabel;

    // Carriers Tab
    @FXML
    private TableView<CarrierInfo> carrierTable;
    @FXML
    private TableColumn<CarrierInfo, String> carUsernameCol;
    @FXML
    private TableColumn<CarrierInfo, String> carPhoneCol;
    @FXML
    private TableColumn<CarrierInfo, String> carAddressCol;
    @FXML
    private TableColumn<CarrierInfo, Double> carRatingCol;
    @FXML
    private TableColumn<CarrierInfo, Integer> carOrdersCol;
    @FXML
    private Label carrierInfoLabel;

    // Orders Tab
    @FXML
    private ComboBox<String> orderFilterCombo;
    @FXML
    private TableView<OrderDetail> allOrdersTable;
    @FXML
    private TableColumn<OrderDetail, Integer> ordIdCol;
    @FXML
    private TableColumn<OrderDetail, String> ordCustomerCol;
    @FXML
    private TableColumn<OrderDetail, String> ordStatusCol;
    @FXML
    private TableColumn<OrderDetail, String> ordCarrierCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> ordCreatedCol;
    @FXML
    private TableColumn<OrderDetail, Timestamp> ordDeliveryCol;
    @FXML
    private TableColumn<OrderDetail, Double> ordTotalCol;
    @FXML
    private Label orderInfoLabel;

    // Messages Tab
    @FXML
    private TableView<Message> messagesTable;
    @FXML
    private TableColumn<Message, String> msgFromCol;
    @FXML
    private TableColumn<Message, String> msgSubjectCol;
    @FXML
    private TableColumn<Message, Timestamp> msgDateCol;
    @FXML
    private Label msgDetailLabel;
    @FXML
    private TextArea msgContentArea;
    @FXML
    private Label msgInfoLabel;

    // Coupons Tab
    @FXML
    private TableView<Coupon> couponTable;
    @FXML
    private TableColumn<Coupon, String> cpnCodeCol;
    @FXML
    private TableColumn<Coupon, String> cpnDiscountCol;
    @FXML
    private TableColumn<Coupon, Double> cpnMinCol;
    @FXML
    private TableColumn<Coupon, Timestamp> cpnValidCol;
    @FXML
    private TableColumn<Coupon, String> cpnUsesCol;
    @FXML
    private TableColumn<Coupon, Boolean> cpnActiveCol;
    @FXML
    private Label couponInfoLabel;

    // Alerts Tab
    @FXML
    private TableView<dao.SystemMessageDAO.SystemMessage> alertsTable;
    @FXML
    private TableColumn<dao.SystemMessageDAO.SystemMessage, String> alertTypeCol;
    @FXML
    private TableColumn<dao.SystemMessageDAO.SystemMessage, String> alertTitleCol;
    @FXML
    private TableColumn<dao.SystemMessageDAO.SystemMessage, String> alertMessageCol;
    @FXML
    private TableColumn<dao.SystemMessageDAO.SystemMessage, Timestamp> alertDateCol;
    @FXML
    private TableColumn<dao.SystemMessageDAO.SystemMessage, Boolean> alertReadCol;
    @FXML
    private Label alertInfoLabel;

    // Reports Tab
    @FXML
    private PieChart statusPieChart;
    @FXML
    private LineChart<String, Number> dailyOrdersLineChart;
    @FXML
    private Label statTotalOrdersLabel;
    @FXML
    private Label statTotalRevenueLabel;
    @FXML
    private Label statActiveCarriersLabel;
    @FXML
    private Label statLowStockLabel;
    @FXML
    private ListView<String> topCarriersList;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private Message selectedMessage;
    private List<OrderDetail> allOrders;

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameLabel.setText("Owner: " + username);
        loadAll();
    }

    @Override
    protected Label getUsernameLabel() {
        return usernameLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Owner Dashboard";
    }

    @FXML
    public void initialize() {
        setupProductTable();
        setupCarrierTable();
        setupOrdersTable();
        setupMessagesTable();
        setupCouponsTable();
        setupAlertsTable();

        // Order filter
        orderFilterCombo.getItems().addAll("All", "NEW", "IN_PROGRESS", "DELIVERED", "CANCELLED");
        orderFilterCombo.setValue("All");

        clearInfoLabel(productInfoLabel);
        clearInfoLabel(carrierInfoLabel);
        clearInfoLabel(orderInfoLabel);
        clearInfoLabel(msgInfoLabel);
        clearInfoLabel(couponInfoLabel);
    }

    private void setupProductTable() {
        prodIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        prodNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        prodTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        prodPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        prodStockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        prodThreshCol.setCellValueFactory(new PropertyValueFactory<>("threshold"));

        // Highlight low stock
        prodStockCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f", stock));
                    Product p = getTableView().getItems().get(getIndex());
                    if (stock <= p.getThreshold()) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupCarrierTable() {
        carUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        carPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        carAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        carRatingCol.setCellValueFactory(new PropertyValueFactory<>("avgRating"));
        carOrdersCol.setCellValueFactory(new PropertyValueFactory<>("deliveryCount"));

        carRatingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null || rating == 0) {
                    setText("-");
                } else {
                    setText(String.format("%.1f ⭐", rating));
                }
            }
        });
    }

    private void setupOrdersTable() {
        ordIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        ordCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        ordStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        ordCarrierCol.setCellValueFactory(new PropertyValueFactory<>("carrierUsername"));
        ordCreatedCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        ordDeliveryCol.setCellValueFactory(new PropertyValueFactory<>("requestedDelivery"));
        ordTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalVatIncluded"));

        setupTimestampColumn(ordCreatedCol);
        setupTimestampColumn(ordDeliveryCol);

        ordStatusCol.setCellFactory(col -> new TableCell<>() {
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
    }

    private void setupMessagesTable() {
        msgFromCol.setCellValueFactory(new PropertyValueFactory<>("senderUsername"));
        msgSubjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        msgDateCol.setCellValueFactory(new PropertyValueFactory<>("sentAt"));

        setupMsgTimestampColumn(msgDateCol);

        messagesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, msg) -> {
            if (msg != null) {
                selectedMessage = msg;
                msgDetailLabel.setText("From: " + msg.getSenderUsername() + " - " +
                        (msg.getSubject() != null ? msg.getSubject() : ""));
                msgContentArea.setText(msg.getContent());

                if (!msg.isRead()) {
                    MessageDAO.markAsRead(msg.getId());
                    msg.setRead(true);
                    loadMessages();
                }
            }
        });

        messagesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setStyle("");
                } else if (!msg.isRead()) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupCouponsTable() {
        cpnCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        cpnMinCol.setCellValueFactory(new PropertyValueFactory<>("minOrderAmount"));
        cpnValidCol.setCellValueFactory(new PropertyValueFactory<>("validUntil"));

        cpnDiscountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Coupon c = getTableView().getItems().get(getIndex());
                    if (c.getDiscountPercent() > 0) {
                        setText(c.getDiscountPercent() + "%");
                    } else {
                        setText(c.getDiscountAmount() + " TL");
                    }
                }
            }
        });

        cpnUsesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Coupon c = getTableView().getItems().get(getIndex());
                    setText(c.getUsedCount() + "/" + c.getMaxUses());
                }
            }
        });

        cpnActiveCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    setText(active ? "✅" : "❌");
                }
            }
        });

        setupCpnTimestampColumn(cpnValidCol);
    }

    private void setupTimestampColumn(TableColumn<OrderDetail, Timestamp> col) {
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

    private void setupMsgTimestampColumn(TableColumn<Message, Timestamp> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(item.toLocalDateTime().format(DT_FMT));
            }
        });
    }

    private void setupCpnTimestampColumn(TableColumn<Coupon, Timestamp> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("No limit");
                else
                    setText(item.toLocalDateTime().format(DT_FMT));
            }
        });
    }

    private void loadAll() {
        loadProducts();
        loadCarriers();
        loadOrders();
        loadMessages();
        loadCoupons();
        loadReports();
    }

    private void loadProducts() {
        productTable.setItems(FXCollections.observableArrayList(ProductDAO.getAllProducts()));
    }

    private void loadCarriers() {
        List<Carrier> carriers = UserDAO.getAllCarriers();
        List<CarrierInfo> infoList = carriers.stream().map(c -> {
            double rating = CarrierRatingDAO.getCarrierAverageRating(c.getUsername());
            int count = CarrierRatingDAO.getCarrierRatingCount(c.getUsername());
            return new CarrierInfo(c.getUsername(), c.getPhone(), c.getAddress(), rating, count);
        }).toList();

        carrierTable.setItems(FXCollections.observableArrayList(infoList));
    }

    private void loadOrders() {
        allOrders = OrderDAO.getAllOrderDetails();
        allOrdersTable.setItems(FXCollections.observableArrayList(allOrders));
    }

    private void loadMessages() {
        List<Message> messages = MessageDAO.getReceivedMessages(currentUsername);
        messagesTable.setItems(FXCollections.observableArrayList(messages));

        int unread = MessageDAO.getUnreadCount(currentUsername);
        if (unread > 0) {
            messageCountLabel.setText("📬 " + unread + " unread");
        } else {
            messageCountLabel.setText("");
        }
    }

    private void loadCoupons() {
        couponTable.setItems(FXCollections.observableArrayList(CouponDAO.getAllCoupons()));
        loadAlerts();
    }

    private void loadReports() {
        // Status Pie Chart
        Map<String, Integer> statusCounts = OrderDAO.getOrderStatusCounts();
        statusPieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            statusPieChart.getData()
                    .add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        // Daily Orders Line Chart
        Map<String, Integer> dailyOrders = OrderDAO.getDailyOrderCounts(7);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders");
        for (Map.Entry<String, Integer> entry : dailyOrders.entrySet()) {
            String dateLabel = entry.getKey().substring(5);
            series.getData().add(new XYChart.Data<>(dateLabel, entry.getValue()));
        }
        if (dailyOrdersLineChart != null) {
            dailyOrdersLineChart.getData().clear();
            dailyOrdersLineChart.getData().add(series);
        }

        // Statistics - shorter text to prevent truncation
        int totalOrders = allOrders != null ? allOrders.size() : 0;
        double totalRevenue = allOrders != null ? allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(OrderDetail::getTotalVatIncluded).sum() : 0;
        int activeCarriers = UserDAO.getAllCarriers().size();
        int lowStock = ProductDAO.getLowStockProducts().size();

        statTotalOrdersLabel.setText(String.valueOf(totalOrders));
        statTotalRevenueLabel.setText(String.format("%.0f TL", totalRevenue));
        statActiveCarriersLabel.setText(String.valueOf(activeCarriers));
        statLowStockLabel.setText(String.valueOf(lowStock));

        // Top Carriers
        topCarriersList.getItems().clear();
        List<CarrierRatingDAO.CarrierRatingSummary> topCarriers = CarrierRatingDAO.getAllCarrierRatings();
        for (CarrierRatingDAO.CarrierRatingSummary cs : topCarriers) {
            topCarriersList.getItems().add(String.format("%s - %.1f ⭐ (%d reviews)",
                    cs.getCarrierUsername(), cs.getAverageRating(), cs.getRatingCount()));
        }
    }

    // ========== PRODUCT HANDLERS ==========

    @FXML
    private void handleAddProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add Product");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        Label nameHintLabel = new Label();
        nameHintLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 9;");
        nameHintLabel.setWrapText(true);
        nameHintLabel.setPrefWidth(300);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("fruit", "vegetable");
        typeCombo.setValue("fruit");

        TextField priceField = new TextField();
        TextField stockField = new TextField();
        Label stockHintLabel = new Label("Max: 10,000 kg");
        stockHintLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 9;");

        Spinner<Integer> thresholdSpinner = new Spinner<>(1, 100, 5);

        grid.addRow(0, new Label("Name:"), nameField);
        grid.add(nameHintLabel, 1, 1);
        grid.addRow(2, new Label("Type:"), typeCombo);
        grid.addRow(3, new Label("Price (TL):"), priceField);
        grid.addRow(4, new Label("Stock (kg):"), stockField);
        grid.add(stockHintLabel, 1, 5);
        grid.addRow(6, new Label("Threshold (min 1):"), thresholdSpinner);

        // Update hint when type changes
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            nameHintLabel.setText(util.ValidationUtil.getValidProductExamples(newVal));
        });
        nameHintLabel.setText(util.ValidationUtil.getValidProductExamples("fruit"));

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    String type = typeCombo.getValue();

                    if (name.isEmpty()) {
                        showInfoLabel(productInfoLabel, "Product name required!", true);
                        return null;
                    }

                    // Capitalize first letter for consistency (e.g., "banana" -> "Banana")
                    name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

                    // Validate product name against known fruits/vegetables
                    if (!util.ValidationUtil.isValidProductName(name, type)) {
                        showInfoLabel(productInfoLabel,
                                util.ValidationUtil.getProductNameError(type) + "\n" +
                                        util.ValidationUtil.getValidProductExamples(type),
                                true);
                        return null;
                    }

                    // Check for duplicate product
                    if (dao.ProductDAO.productExists(name, type)) {
                        showInfoLabel(productInfoLabel,
                                "Product '" + name + "' already exists in " + type + " category!",
                                true);
                        return null;
                    }

                    double price = Double.parseDouble(priceField.getText().trim());
                    double stock = Double.parseDouble(stockField.getText().trim());

                    // Validate price
                    if (!util.ValidationUtil.isValidPrice(price)) {
                        showInfoLabel(productInfoLabel,
                                String.format("Price must be between %.2f and %.0f TL!",
                                        util.ValidationUtil.MIN_PRICE,
                                        util.ValidationUtil.MAX_PRICE),
                                true);
                        return null;
                    }

                    // Validate stock quantity
                    if (!util.ValidationUtil.isValidStock(stock)) {
                        showInfoLabel(productInfoLabel,
                                String.format("Stock must be between %.0f and %.0f kg!",
                                        util.ValidationUtil.MIN_STOCK,
                                        util.ValidationUtil.MAX_STOCK),
                                true);
                        return null;
                    }

                    int threshold = thresholdSpinner.getValue();
                    if (threshold < 1)
                        threshold = 1;

                    // Auto-load image from images_market folder
                    byte[] imageData = util.ImageUtil.loadProductImage(name);
                    if (imageData != null) {
                        ProductDAO.addProduct(name, price, stock, type, threshold, imageData);
                        System.out.println("✅ Product added with image: " + name);
                    } else {
                        ProductDAO.addProduct(name, price, stock, type, threshold);
                        System.out.println("⚠ Product added without image: " + name);
                    }

                    return new Product();
                } catch (NumberFormatException e) {
                    showInfoLabel(productInfoLabel, "Invalid number format!", true);
                    return null;
                } catch (Exception e) {
                    showInfoLabel(productInfoLabel, "Error: " + e.getMessage(), true);
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            showInfoLabel(productInfoLabel, "Product added ✅", false);
            loadProducts();
        }
    }

    @FXML
    private void handleEditPrice() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getPrice()));
        dialog.setTitle("Edit Price");
        dialog.setHeaderText("Edit price for " + selected.getName());
        dialog.setContentText("New price:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double newPrice = Double.parseDouble(result.get().trim());

                // Validate price
                if (!util.ValidationUtil.isValidPrice(newPrice)) {
                    showInfoLabel(productInfoLabel,
                            String.format("Price must be between %.2f and %.0f TL!",
                                    util.ValidationUtil.MIN_PRICE,
                                    util.ValidationUtil.MAX_PRICE),
                            true);
                    return;
                }

                if (ProductDAO.updatePrice(selected.getProductId(), newPrice)) {
                    showInfoLabel(productInfoLabel, "Price updated ✅", false);
                    loadProducts();
                } else {
                    showInfoLabel(productInfoLabel, "Failed to update!", true);
                }
            } catch (NumberFormatException e) {
                showInfoLabel(productInfoLabel, "Invalid price!", true);
            }
        }
    }

    @FXML
    private void handleEditThreshold() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getThreshold()));
        dialog.setTitle("Edit Threshold");
        dialog.setHeaderText("Edit threshold for " + selected.getName());
        dialog.setContentText("New threshold (min 1):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newThreshold = Integer.parseInt(result.get().trim());
                if (newThreshold < 1) {
                    showInfoLabel(productInfoLabel, "Threshold must be at least 1!", true);
                    return;
                }

                if (ProductDAO.updateThreshold(selected.getProductId(), newThreshold)) {
                    showInfoLabel(productInfoLabel, "Threshold updated ✅", false);
                    loadProducts();
                } else {
                    showInfoLabel(productInfoLabel, "Failed to update!", true);
                }
            } catch (NumberFormatException e) {
                showInfoLabel(productInfoLabel, "Invalid number!", true);
            }
        }
    }

    @FXML
    private void handleAddStock() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock for " + selected.getName());
        dialog.setContentText("Add kg:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double addKg = Double.parseDouble(result.get().trim());
                if (ProductDAO.addStock(selected.getProductId(), addKg)) {
                    showInfoLabel(productInfoLabel, "Stock added ✅", false);
                    loadProducts();
                } else {
                    showInfoLabel(productInfoLabel, "Failed to add!", true);
                }
            } catch (NumberFormatException e) {
                showInfoLabel(productInfoLabel, "Invalid number!", true);
            }
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (ProductDAO.deleteProduct(selected.getProductId())) {
                showInfoLabel(productInfoLabel, "Product deleted ✅", false);
                loadProducts();
            } else {
                showInfoLabel(productInfoLabel, "Failed to delete!", true);
            }
        }
    }

    @FXML
    private void handleDecreaseStock() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Decrease Stock");
        dialog.setHeaderText("Decrease stock for " + selected.getName() + "\nCurrent: "
                + String.format("%.1f kg", selected.getStock()));
        dialog.setContentText("Decrease by (kg):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double decreaseKg = Double.parseDouble(result.get().trim());
                if (decreaseKg <= 0) {
                    showInfoLabel(productInfoLabel, "Enter a positive number!", true);
                    return;
                }
                if (decreaseKg > selected.getStock()) {
                    showInfoLabel(productInfoLabel, "Cannot decrease below 0!", true);
                    return;
                }
                if (ProductDAO.decreaseStock(selected.getProductId(), decreaseKg)) {
                    showInfoLabel(productInfoLabel, "Stock decreased ✅", false);
                    loadProducts();
                } else {
                    showInfoLabel(productInfoLabel, "Failed to decrease!", true);
                }
            } catch (NumberFormatException e) {
                showInfoLabel(productInfoLabel, "Invalid number!", true);
            }
        }
    }

    @FXML
    private void handleApplyDiscount() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(productInfoLabel, "Select a product!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getDiscountPercent()));
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Apply discount for " + selected.getName() + "\nCurrent price: "
                + String.format("%.2f TL", selected.getPrice()));
        dialog.setContentText("Discount % (0-100, 0 to remove):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double discountPercent = Double.parseDouble(result.get().trim());
                if (discountPercent < 0 || discountPercent > 100) {
                    showInfoLabel(productInfoLabel, "Discount must be 0-100%!", true);
                    return;
                }
                if (ProductDAO.updateDiscount(selected.getProductId(), discountPercent)) {
                    if (discountPercent > 0) {
                        showInfoLabel(productInfoLabel, discountPercent + "% discount applied ✅", false);
                    } else {
                        showInfoLabel(productInfoLabel, "Discount removed ✅", false);
                    }
                    loadProducts();
                } else {
                    showInfoLabel(productInfoLabel, "Failed to apply discount!", true);
                }
            } catch (NumberFormatException e) {
                showInfoLabel(productInfoLabel, "Invalid number!", true);
            }
        }
    }

    // ========== CARRIER HANDLERS ==========

    @FXML
    private void handleEmployCarrier() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Employ Carrier");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField phoneField = new TextField();
        TextField addressField = new TextField();

        grid.addRow(0, new Label("Username:"), usernameField);
        grid.addRow(1, new Label("Password:"), passwordField);
        grid.addRow(2, new Label("Phone:"), phoneField);
        grid.addRow(3, new Label("Address:"), addressField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    showInfoLabel(carrierInfoLabel, "Username and password required!", true);
                    return false;
                }

                // Validate username (must contain at least one letter)
                if (!util.ValidationUtil.isValidUsername(username)) {
                    showInfoLabel(carrierInfoLabel,
                            "Invalid username! Must contain at least one letter (e.g. 'carrier1' OK, '123' NOT OK)",
                            true);
                    return false;
                }

                // Check for duplicate username
                if (dao.UserDAO.usernameExists(username)) {
                    showInfoLabel(carrierInfoLabel,
                            "Username already exists! Choose a different username.",
                            true);
                    return false;
                }

                // Validate address if provided (must contain letters)
                if (!address.isEmpty() && !util.ValidationUtil.isValidAddress(address)) {
                    showInfoLabel(carrierInfoLabel,
                            "Invalid address! Must contain letters, not just numbers (e.g. 'Maltepe 111' OK, '111' NOT OK)",
                            true);
                    return false;
                }

                // Validate phone if provided
                if (!phone.isEmpty() && !util.ValidationUtil.isValidPhoneNumber(phone)) {
                    showInfoLabel(carrierInfoLabel,
                            "Invalid phone! Use Turkish mobile format: 0555 555 5555 or 05555555555",
                            true);
                    return false;
                }

                // Check for duplicate phone
                if (!phone.isEmpty() && dao.UserDAO.phoneExists(phone)) {
                    showInfoLabel(carrierInfoLabel,
                            "Phone number already registered! Use a different number.",
                            true);
                    return false;
                }

                return UserDAO.employCarrier(username, password,
                        address.isEmpty() ? null : address,
                        phone.isEmpty() ? null : phone);
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            showInfoLabel(carrierInfoLabel, "Carrier employed ✅", false);
            loadCarriers();
        }
    }

    @FXML
    private void handleFireCarrier() {
        CarrierInfo selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(carrierInfoLabel, "Select a carrier!", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Fire Carrier");
        confirm.setHeaderText("Fire " + selected.getUsername() + "?");
        confirm.setContentText("This cannot be undone. Carrier with active orders cannot be fired.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.fireCarrier(selected.getUsername())) {
                showInfoLabel(carrierInfoLabel, "Carrier fired ✅", false);
                loadCarriers();
            } else {
                showInfoLabel(carrierInfoLabel, "Cannot fire (has active orders?)", true);
            }
        }
    }

    @FXML
    private void handleViewRatings() {
        CarrierInfo selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(carrierInfoLabel, "Select a carrier!", true);
            return;
        }

        List<CarrierRating> ratings = CarrierRatingDAO.getRatingsByCarrier(selected.getUsername());

        StringBuilder sb = new StringBuilder();
        sb.append("Carrier: ").append(selected.getUsername()).append("\n");
        sb.append("Average Rating: ").append(String.format("%.1f", selected.getAvgRating())).append(" ⭐\n\n");

        for (CarrierRating r : ratings) {
            sb.append("Order #").append(r.getOrderId())
                    .append(" - ").append("⭐".repeat(r.getRating()))
                    .append(" by ").append(r.getCustomerUsername())
                    .append("\n");
            if (r.getComment() != null) {
                sb.append("   \"").append(r.getComment()).append("\"\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Carrier Ratings");
        alert.setHeaderText(selected.getUsername() + " Ratings");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ========== ORDER HANDLERS ==========

    @FXML
    private void handleFilterOrders() {
        String filter = orderFilterCombo.getValue();

        if ("All".equals(filter)) {
            allOrdersTable.setItems(FXCollections.observableArrayList(allOrders));
        } else {
            List<OrderDetail> filtered = allOrders.stream()
                    .filter(o -> filter.equalsIgnoreCase(o.getStatus()))
                    .toList();
            allOrdersTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void handleOrderDetails() {
        OrderDetail selected = allOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(orderInfoLabel, "Select an order!", true);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(selected.getOrderId()).append("\n\n");
        sb.append("Customer: ").append(selected.getCustomerUsername()).append("\n");
        sb.append("Address: ").append(selected.getCustomerAddress()).append("\n");
        sb.append("Status: ").append(selected.getStatus()).append("\n");
        sb.append("Carrier: ")
                .append(selected.getCarrierUsername() != null ? selected.getCarrierUsername() : "Not assigned")
                .append("\n\n");

        sb.append("Products:\n");
        for (OrderDetail.OrderItem item : selected.getItems()) {
            sb.append("  • ").append(item.getProductName())
                    .append(" - ").append(item.getKg()).append(" kg")
                    .append(" @ ").append(String.format("%.2f", item.getPriceAtTime())).append(" TL")
                    .append(" = ").append(String.format("%.2f", item.getLineTotal())).append(" TL\n");
        }

        sb.append("\nTotal: ").append(String.format("%.2f TL", selected.getTotalVatIncluded()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + selected.getOrderId());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    // ========== MESSAGE HANDLERS ==========

    @FXML
    private void handleReplyMessage() {
        if (selectedMessage == null) {
            showInfoLabel(msgInfoLabel, "Select a message!", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply");
        dialog.setHeaderText("Reply to " + selectedMessage.getSenderUsername());
        dialog.setContentText("Your reply:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String subject = "Re: " + (selectedMessage.getSubject() != null ? selectedMessage.getSubject() : "");
            boolean success = MessageDAO.sendMessage(
                    currentUsername,
                    selectedMessage.getSenderUsername(),
                    subject,
                    result.get().trim(),
                    selectedMessage.getId());

            if (success) {
                showInfoLabel(msgInfoLabel, "Reply sent ✅", false);
            } else {
                showInfoLabel(msgInfoLabel, "Failed to send!", true);
            }
        }
    }

    // ========== COUPON HANDLERS ==========

    @FXML
    private void handleCreateCoupon() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Create Coupon");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. SAVE10");

        TextField percentField = new TextField();
        percentField.setPromptText("e.g. 10");

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 50");

        TextField minField = new TextField("0");
        Spinner<Integer> maxUsesSpinner = new Spinner<>(1, 1000, 100);
        Spinner<Integer> validDaysSpinner = new Spinner<>(1, 365, 30);

        grid.addRow(0, new Label("Code:"), codeField);
        grid.addRow(1, new Label("Discount %:"), percentField);
        grid.addRow(2, new Label("OR Discount TL:"), amountField);
        grid.addRow(3, new Label("Min Order:"), minField);
        grid.addRow(4, new Label("Max Uses:"), maxUsesSpinner);
        grid.addRow(5, new Label("Valid Days:"), validDaysSpinner);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String code = codeField.getText().trim().toUpperCase();
                    double percent = percentField.getText().isEmpty() ? 0 : Double.parseDouble(percentField.getText());
                    double amount = amountField.getText().isEmpty() ? 0 : Double.parseDouble(amountField.getText());
                    double minOrder = Double.parseDouble(minField.getText());
                    int maxUses = maxUsesSpinner.getValue();
                    int validDays = validDaysSpinner.getValue();

                    Timestamp validUntil = Timestamp.valueOf(LocalDateTime.now().plusDays(validDays));

                    return CouponDAO.createCoupon(code, percent, amount, minOrder,
                            Timestamp.valueOf(LocalDateTime.now()), validUntil, maxUses);
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            showInfoLabel(couponInfoLabel, "Coupon created ✅", false);
            loadCoupons();
        }
    }

    @FXML
    private void handleDeactivateCoupon() {
        Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(couponInfoLabel, "Select a coupon!", true);
            return;
        }

        if (CouponDAO.deactivateCoupon(selected.getId())) {
            showInfoLabel(couponInfoLabel, "Coupon deactivated ✅", false);
            loadCoupons();
        } else {
            showInfoLabel(couponInfoLabel, "Failed!", true);
        }
    }

    // ========== COMMON HANDLERS ==========

    @FXML
    private void handleRefresh() {
        loadAll();
        showInfoLabel(productInfoLabel, "Refreshed.", false);
    }

    @FXML
    private void handleLogout() {
        performLogout();
    }

    // ========== HELPER CLASSES ==========

    public static class CarrierInfo {
        private final String username;
        private final String phone;
        private final String address;
        private final double avgRating;
        private final int deliveryCount;

        public CarrierInfo(String username, String phone, String address, double avgRating, int deliveryCount) {
            this.username = username;
            this.phone = phone;
            this.address = address;
            this.avgRating = avgRating;
            this.deliveryCount = deliveryCount;
        }

        public String getUsername() {
            return username;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public double getAvgRating() {
            return avgRating;
        }

        public int getDeliveryCount() {
            return deliveryCount;
        }
    }

    // ========== ALERTS HANDLERS ==========

    private void setupAlertsTable() {
        if (alertsTable == null)
            return;

        alertTypeCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessageType()));
        alertTitleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        alertMessageCol
                .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessage()));
        alertDateCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCreatedAt()));
        alertReadCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().isRead()));

        alertDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp ts, boolean empty) {
                super.updateItem(ts, empty);
                if (empty || ts == null) {
                    setText("");
                } else {
                    setText(ts.toLocalDateTime().format(DT_FMT));
                }
            }
        });
        alertReadCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean read, boolean empty) {
                super.updateItem(read, empty);
                if (empty || read == null) {
                    setText("");
                } else {
                    setText(read ? "✓" : "●");
                    setStyle(read ? "-fx-text-fill: #9CA3AF;" : "-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadAlerts() {
        if (alertsTable == null)
            return;

        dao.SystemMessageDAO.checkAndCreateLowStockAlerts();

        java.util.List<dao.SystemMessageDAO.SystemMessage> messages = dao.SystemMessageDAO.getAllMessages();
        alertsTable.setItems(FXCollections.observableArrayList(messages));

        int unread = dao.SystemMessageDAO.getUnreadCount();
        if (alertInfoLabel != null) {
            alertInfoLabel.setText(unread > 0 ? unread + " unread alert(s)" : "No new alerts");
        }
    }

    @FXML
    private void handleMarkAlertRead() {
        if (alertsTable == null)
            return;
        dao.SystemMessageDAO.SystemMessage selected = alertsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(alertInfoLabel, "Select an alert first!", true);
            return;
        }
        dao.SystemMessageDAO.markAsRead(selected.getId());
        loadAlerts();
        showInfoLabel(alertInfoLabel, "Marked as read ✓", false);
    }

    @FXML
    private void handleDeleteAlert() {
        if (alertsTable == null)
            return;
        dao.SystemMessageDAO.SystemMessage selected = alertsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfoLabel(alertInfoLabel, "Select an alert first!", true);
            return;
        }
        dao.SystemMessageDAO.deleteMessage(selected.getId());
        loadAlerts();
        showInfoLabel(alertInfoLabel, "Alert deleted", false);
    }

    @FXML
    private void handleRefreshAlerts() {
        loadAlerts();
        showInfoLabel(alertInfoLabel, "Alerts refreshed", false);
    }
}
