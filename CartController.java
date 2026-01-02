package controller;

import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.CartItem;
import model.Product;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller for the Shopping Cart screen in the GreenGrocer application.
 * *
 * <p>
 * This controller manages the shopping cart interface where customers can
 * review their selected items, modify quantities, apply coupons, and place
 * orders.
 * *
 * <p>
 * Constraints:
 * <ul>
 * <li>Minimum order amount: 100.00 TL (VAT included)</li>
 * <li>Delivery time: Minimum 10 minutes, Maximum 48 hours</li>
 * <li>VAT Rate: 18%</li>
 * </ul>
 * * @author GreenGrocer Team
 * 
 * @version 1.1
 */
public class CartController extends BaseController {

    private static final double VAT_RATE = 0.18;
    private static final double MIN_CART_TOTAL = 100.00;

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> nameCol;
    @FXML
    private TableColumn<CartItem, Double> kgCol;
    @FXML
    private TableColumn<CartItem, Double> unitPriceCol;
    @FXML
    private TableColumn<CartItem, Double> lineTotalCol;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label vatLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label infoLabel;

    @FXML
    private TextField couponField;
    @FXML
    private Label discountLabel;

    @FXML
    private Label couponInfoLabel;

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private Map<Integer, CustomerController.LocalCartItem> cartMapRef;

    private double appliedDiscount = 0;
    private model.Coupon appliedCoupon = null;

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        titleLabel.setText("Shopping Cart - " + username);
    }

    @Override
    protected Label getUsernameLabel() {
        return titleLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Shopping Cart";
    }

    /**
     * Initializes the controller, sets up table columns and cell factories.
     */
    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        kgCol.setCellValueFactory(new PropertyValueFactory<>("kg"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        lineTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        unitPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(String.format("%.2f", item));
            }
        });
        lineTotalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText("");
                else
                    setText(String.format("%.2f", item));
            }
        });

        cartTable.setItems(cartItems);
        clearInfoLabel(infoLabel);

        if (discountLabel != null)
            discountLabel.setText("");

        updateTotals();
    }

    /**
     * Initializes the data for the cart view.
     * 
     * @param username The current user's name
     * @param cartMap  The map of items currently in the local cart
     */
    public void init(String username, Map<Integer, CustomerController.LocalCartItem> cartMap) {
        this.currentUsername = username;
        this.cartMapRef = cartMap;

        titleLabel.setText("Shopping Cart - " + username);
        rebuildUiListFromMap();
        updateTotals();
    }

    /**
     * Synchronizes the UI list with the internal cart map.
     */
    private void rebuildUiListFromMap() {
        cartItems.clear();
        if (cartMapRef == null)
            return;

        for (CustomerController.LocalCartItem local : cartMapRef.values()) {
            Product p = local.getProduct();
            double effectivePrice = p.getEffectivePrice();
            cartItems.add(new CartItem(
                    p.getProductId(),
                    p.getName(),
                    effectivePrice,
                    local.getKg()));
        }
    }

    /**
     * Calculates the raw subtotal of all items in the cart.
     * 
     * @return Sum of line totals
     */
    private double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem i : cartItems)
            subtotal += i.getSubTotal();
        return subtotal;
    }

    /**
     * Updates all monetary labels on the UI including VAT and Discounts.
     */
    private void updateTotals() {
        double subtotal = getSubtotal();
        double discount = appliedDiscount;
        double afterDiscount = Math.max(0, subtotal - discount);

        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        subtotalLabel.setText(String.format("%.2f", subtotal));
        if (discountLabel != null && discount > 0) {
            discountLabel.setText(String.format("-%.2f", discount));
        }
        vatLabel.setText(String.format("%.2f", vat));
        totalLabel.setText(String.format("%.2f", total));
    }

    /**
     * Handles the coupon application logic.
     */
    @FXML
    private void handleApplyCoupon() {
        if (couponField == null)
            return;

        String code = couponField.getText();
        if (code == null || code.trim().isEmpty()) {
            showCouponInfo("Enter a coupon code!", true);
            return;
        }

        code = code.trim().toUpperCase();
        double subtotal = getSubtotal();
        if (subtotal <= 0) {
            showCouponInfo("Add items to cart first!", true);
            return;
        }

        model.Coupon coupon = dao.CouponDAO.getCouponByCode(code);

        if (coupon == null) {
            showCouponInfo("Invalid coupon code!", true);
            resetDiscount();
            return;
        }

        if (!coupon.isActive()) {
            showCouponInfo("This coupon is no longer active!", true);
            resetDiscount();
            return;
        }

        if (coupon.getValidUntil() != null &&
                coupon.getValidUntil().toLocalDateTime().isBefore(LocalDateTime.now())) {
            showCouponInfo("This coupon has expired!", true);
            resetDiscount();
            return;
        }

        double totalWithVat = subtotal * (1 + VAT_RATE);
        if (totalWithVat < coupon.getMinOrderAmount()) {
            showCouponInfo(String.format("Min order: %.2f TL (Current: %.2f TL)",
                    coupon.getMinOrderAmount(), totalWithVat), true);
            resetDiscount();
            return;
        }

        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            showCouponInfo("Coupon usage limit reached!", true);
            resetDiscount();
            return;
        }

        appliedCoupon = coupon;
        appliedDiscount = subtotal * (coupon.getDiscountPercent() / 100.0);

        showCouponInfo(String.format("✅ %s applied! (%.0f%% off)", code, coupon.getDiscountPercent()), false);
        if (discountLabel != null) {
            discountLabel.setText(String.format("Coupon: -%.2f TL", appliedDiscount));
        }
        updateTotals();
    }

    private void resetDiscount() {
        appliedDiscount = 0;
        appliedCoupon = null;
        updateTotals();
    }

    private void showCouponInfo(String msg, boolean isError) {
        if (couponInfoLabel != null) {
            couponInfoLabel.setStyle(isError ? "-fx-text-fill: #F87171;" : "-fx-text-fill: #10B981;");
            couponInfoLabel.setText(msg);
        }
    }

    /**
     * Removes the selected item from the cart table and internal map.
     */
    @FXML
    private void handleRemoveSelected() {
        CartItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfoLabel(infoLabel, "Select an item!", true);
            return;
        }
        cartItems.remove(sel);
        if (cartMapRef != null)
            cartMapRef.remove(sel.getProductId());
        updateTotals();
        showInfoLabel(infoLabel, "Removed.", false);
    }

    /**
     * Clears all items from the cart.
     */
    @FXML
    private void handleClear() {
        cartItems.clear();
        if (cartMapRef != null)
            cartMapRef.clear();
        appliedDiscount = 0;
        updateTotals();
        showInfoLabel(infoLabel, "Cart cleared.", false);
    }

    /**
     * Finalizes the purchase, validates delivery time, and creates the order in DB.
     */
    @FXML
    private void handleCheckout() {
        clearInfoLabel(infoLabel);

        if (cartItems.isEmpty()) {
            showInfoLabel(infoLabel, "Cart is empty!", true);
            return;
        }

        double subtotal = getSubtotal();
        double afterDiscount = Math.max(0, subtotal - appliedDiscount);
        double totalVatInc = afterDiscount + (afterDiscount * VAT_RATE);

        if (totalVatInc < MIN_CART_TOTAL) {
            showInfoLabel(infoLabel,
                    "Minimum cart total is " + String.format("%.2f", MIN_CART_TOTAL) + " TL (VAT inc.)", true);
            return;
        }

        LocalDateTime requested = pickDeliveryDateTime();
        if (requested == null)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(currentUsername).append("\n");
        sb.append("Requested delivery: ").append(requested).append("\n\n");

        for (CartItem i : cartItems) {
            sb.append("• ").append(i.getName())
                    .append(" | ").append(String.format("%.2f", i.getKg())).append(" kg")
                    .append(" | ").append(String.format("%.2f TL", i.getSubTotal()))
                    .append("\n");
        }

        sb.append("\nTOTAL: ").append(String.format("%.2f", totalVatInc)).append(" TL");
        sb.append("\n\nConfirm purchase?");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Order Confirmation");
        confirm.setHeaderText("Review Order Summary");
        confirm.setContentText(sb.toString());

        ButtonType ok = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(ok, cancel);

        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ok)
            return;

        boolean saved = OrderDAO.createCartOrder(currentUsername, Timestamp.valueOf(requested), totalVatInc, cartItems);

        if (!saved) {
            showInfoLabel(infoLabel, "Checkout failed! Stock issue or DB error.", true);
            return;
        }

        if (appliedCoupon != null) {
            dao.CouponDAO.useCoupon(appliedCoupon.getCode());
        }

        cartItems.clear();
        if (cartMapRef != null)
            cartMapRef.clear();
        updateTotals();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText("✅ Order Received!");
        successAlert.setContentText("Your order has been placed successfully.");
        successAlert.showAndWait();

        handleClose();
    }

    /**
     * Opens a dialog for the user to select a delivery time.
     * *
     * <p>
     * <b>Validation Rules:</b>
     * </p>
     * <ul>
     * <li>Minimum: 10 minutes from current time.</li>
     * <li>Maximum: 48 hours from current time.</li>
     * <li>Precision: 48 hours 0 minutes is allowed; 48 hours 1 minute is
     * rejected.</li>
     * </ul>
     * * @return Selected LocalDateTime or null if invalid/cancelled.
     */
    private LocalDateTime pickDeliveryDateTime() {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Delivery Time");
        dialog.setHeaderText("Select a time between 10 minutes and 48 hours.");

        Spinner<Integer> hoursFromNow = new Spinner<>(0, 100, 2);
        hoursFromNow.setEditable(true);
        Spinner<Integer> minutes = new Spinner<>(0, 59, 0);
        minutes.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Hours:"), hoursFromNow);
        grid.addRow(1, new Label("Minutes:"), minutes);
        dialog.getDialogPane().setContent(grid);

        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != okBtn)
                return null;

            try {
                int h = Integer.parseInt(hoursFromNow.getEditor().getText().trim());
                int m = Integer.parseInt(minutes.getEditor().getText().trim());

                int totalMinutes = (h * 60) + m;

                // Max 48 hours (2880 mins)
                if (totalMinutes > 2880) {
                    showWarning("Limit Exceeded", "Maximum delivery time is 48 hours.",
                            "You entered: " + h + "h " + m + "m. Please reduce it.");
                    return null;
                }

                // Min 10 minutes
                if (totalMinutes < 10) {
                    showWarning("Too Soon", "Minimum delivery time is 10 minutes.",
                            "We need at least 10 minutes to prepare your order.");
                    return null;
                }

                return LocalDateTime.now().plusMinutes(totalMinutes);
            } catch (NumberFormatException e) {
                showWarning("Input Error", "Invalid numbers", "Please enter valid integers for time.");
                return null;
            }
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) cartTable.getScene().getWindow();
        stage.close();
    }
}