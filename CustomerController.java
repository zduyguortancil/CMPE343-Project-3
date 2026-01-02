package controller;

import dao.MessageDAO;
import dao.ProductDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Product;

import java.io.ByteArrayInputStream;
import java.util.Comparator; // Added for sorting
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList; // Added for mutable list
import java.util.Map;
import java.util.stream.Collectors; // Added for mutable list
import javafx.collections.FXCollections; // Added for ComboBox

/**
 * Controller for the Customer dashboard in the GreenGrocer application.
 * 
 * <p>This controller manages the main shopping interface for customers, providing
 * functionality to browse products, search, sort, add items to cart, and navigate
 * to other customer features like cart, orders, messages, and profile.
 * 
 * <p>Features:
 * <ul>
 *   <li>Product browsing (fruits and vegetables in accordion layout)</li>
 *   <li>Product search and filtering</li>
 *   <li>Product sorting (name, price ascending/descending)</li>
 *   <li>Product selection and detail display</li>
 *   <li>Add products to shopping cart</li>
 *   <li>Navigation to cart, orders, messages, and profile screens</li>
 *   <li>Unread message notifications</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * <p>POLYMORPHISM: Overrides abstract methods from BaseController
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class CustomerController extends BaseController {

    @FXML
    private Label usernameLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Label infoLabel;

    // TitledPane structure
    @FXML
    private Accordion productAccordion;
    @FXML
    private TitledPane fruitsPane;
    @FXML
    private TitledPane vegetablesPane;
    @FXML
    private FlowPane fruitsFlowPane;
    @FXML
    private FlowPane vegetablesFlowPane;

    // Selected product display
    @FXML
    private ImageView productImageView;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productPriceLabel;
    @FXML
    private Label productStockLabel;
    @FXML
    private Spinner<Double> kgSpinner;
    @FXML
    private Label addResultLabel;

    // Cart storage
    private final Map<Integer, LocalCartItem> cart = new LinkedHashMap<>();

    // Currently selected product
    private Product selectedProduct = null;

    // ========== POLYMORPHISM: Override abstract methods ==========

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameLabel.setText("Welcome, " + username);

        // Check for unread messages
        int unread = MessageDAO.getUnreadCount(username);
        if (unread > 0) {
            showInfoLabel(infoLabel, "You have " + unread + " unread message(s)!", false);
        }
    }

    @Override
    protected Label getUsernameLabel() {
        return usernameLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Group30 GreenGrocer";
    }

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    public void initialize() {
        // Setup spinner
        kgSpinner.setValueFactory(new DoubleSpinnerValueFactory(0.25, 50.0, 1.0, 0.25));
        kgSpinner.setEditable(true);

        clearInfoLabel(infoLabel);
        clearInfoLabel(addResultLabel);

        // Sorting setup
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Name (A-Z)",
                "Price (Low to High)",
                "Price (High to Low)"));
        sortComboBox.setValue("Name (A-Z)"); // Default
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshProducts());

        // Load products
        refreshProducts();

        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshProducts();
        });

        // Expand fruits by default
        productAccordion.setExpandedPane(fruitsPane);
    }

    private void refreshProducts() {
        String query = searchField.getText();
        query = (query == null) ? "" : query.trim().toLowerCase();

        // Load fruits (stock > 0, sorted)
        List<Product> fruits = ProductDAO.getFruits();
        List<Product> filFruits = filterProducts(fruits, query);
        sortProducts(filFruits);
        populateFlowPane(fruitsFlowPane, filFruits);
        fruitsPane.setText("🍎 Fruits (" + filFruits.size() + ")");

        // Load vegetables (stock > 0, sorted)
        List<Product> vegetables = ProductDAO.getVegetables();
        List<Product> filVegs = filterProducts(vegetables, query);
        sortProducts(filVegs);
        populateFlowPane(vegetablesFlowPane, filVegs);
        vegetablesPane.setText("🥕 Vegetables (" + filVegs.size() + ")");
    }

    private List<Product> filterProducts(List<Product> products, String query) {
        if (query.isEmpty())
            return new ArrayList<>(products); // Mutable copy

        return products.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(query))
                .collect(Collectors.toList()); // Mutable list
    }

    private void sortProducts(List<Product> list) {
        String criteria = sortComboBox.getValue();
        if (criteria == null)
            return;

        switch (criteria) {
            case "Price (Low to High)":
                list.sort(Comparator.comparingDouble(Product::getEffectivePrice));
                break;
            case "Price (High to Low)":
                list.sort(Comparator.comparingDouble(Product::getEffectivePrice).reversed());
                break;
            case "Name (A-Z)":
            default:
                list.sort(Comparator.comparing(Product::getName));
                break;
        }
    }

    private void populateFlowPane(FlowPane flowPane, List<Product> products) {
        flowPane.getChildren().clear();

        for (Product product : products) {
            VBox card = createProductCard(product);
            flowPane.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(160);
        card.setPrefHeight(220);

        String baseStyle = "-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2); -fx-cursor: hand;";
        card.setStyle(baseStyle);

        javafx.scene.Node imageNode;

        if (product.getImage() != null && product.getImage().length > 0) {
            ImageView imageView = new ImageView();
            imageView.setFitWidth(90);
            imageView.setFitHeight(70);
            imageView.setPreserveRatio(true);
            try {
                imageView.setImage(new Image(new ByteArrayInputStream(product.getImage())));
            } catch (Exception e) {
                imageView.setImage(null);
            }
            imageNode = imageView;
        } else {
            String emoji = getProductEmoji(product.getName(), product.getType());
            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 38;");
            emojiLabel.setPrefHeight(70);
            emojiLabel.setAlignment(Pos.CENTER);
            imageNode = emojiLabel;
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13; -fx-text-fill: #1A1D21;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);
        nameLabel.setAlignment(Pos.CENTER);

        double effectivePrice = product.getEffectivePrice();
        double originalPrice = product.getOriginalPrice();
        double discountPercent = product.getDiscountPercent();
        boolean isOutOfStock = product.getStock() == 0;
        boolean isLowStock = product.getStock() <= product.getThreshold() && !isOutOfStock;
        boolean hasDiscount = discountPercent > 0;

        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER);

        if (hasDiscount) {
            HBox discountRow = new HBox(5);
            discountRow.setAlignment(Pos.CENTER);

            Label oldPriceLabel = new Label(String.format("%.2f TL", originalPrice));
            oldPriceLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF; -fx-strikethrough: true;");

            Label discountBadge = new Label(String.format("-%.0f%%", discountPercent));
            discountBadge.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-padding: 1 4; " +
                    "-fx-background-radius: 3; -fx-font-size: 9; -fx-font-weight: bold;");

            discountRow.getChildren().addAll(oldPriceLabel, discountBadge);
            priceBox.getChildren().add(discountRow);
        }

        Label priceLabel = new Label(String.format("%.2f TL", effectivePrice));
        if (isLowStock) {
            priceLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-font-size: 14;");
        } else if (hasDiscount) {
            priceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700; -fx-font-size: 14;");
        } else {
            priceLabel.setStyle("-fx-text-fill: #2D7A4F; -fx-font-weight: 700; -fx-font-size: 14;");
        }
        priceBox.getChildren().add(priceLabel);

        Label stockLabel;
        if (isOutOfStock) {
            stockLabel = new Label("🚫 Out of Stock");
            stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #DC2626; -fx-font-weight: bold; " +
                    "-fx-background-color: #FEE2E2; -fx-padding: 2 6; -fx-background-radius: 3;");
        } else if (isLowStock) {
            stockLabel = new Label("🔥 Last Items!");
            stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
        } else {
            stockLabel = new Label("✓ In Stock");
            stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #10B981;");
        }

        card.getChildren().addAll(imageNode, nameLabel, priceBox, stockLabel);

        // Handle out-of-stock products differently
        if (isOutOfStock) {
            card.setOpacity(0.6);
            card.setStyle(baseStyle + "-fx-cursor: not-allowed;");
            card.setOnMouseClicked(e -> {
                clearInfoLabel(addResultLabel);
                showInfoLabel(addResultLabel, "This product is out of stock", true);
            });
        } else {
            card.setOnMouseClicked(e -> selectProduct(product));

            String hoverStyle = "-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                    "-fx-border-color: #2D7A4F; -fx-border-radius: 12; -fx-border-width: 1.5; " +
                    "-fx-effect: dropshadow(gaussian, rgba(45,122,79,0.12), 16, 0, 0, 6); -fx-cursor: hand;";
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited(e -> card.setStyle(baseStyle));
        }

        return card;
    }

    private void selectProduct(Product product) {
        this.selectedProduct = product;

        productNameLabel.setText(product.getName());

        double effectivePrice = product.getEffectivePrice();
        boolean isLowStock = product.getStock() <= product.getThreshold();

        if (isLowStock) {
            productPriceLabel.setText(String.format("%.2f TL/kg (2x price!)", effectivePrice));
            productPriceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #f44336; -fx-font-weight: bold;");
        } else {
            productPriceLabel.setText(String.format("%.2f TL/kg", effectivePrice));
            productPriceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #4CAF50;");
        }

        if (isLowStock) {
            productStockLabel.setText("⚠️ Limited Stock - Order Soon!");
            productStockLabel.setStyle("-fx-text-fill: #DC2626;");
        } else {
            productStockLabel.setText("✓ In Stock");
            productStockLabel.setStyle("-fx-text-fill: #10B981;");
        }

        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                productImageView.setImage(new Image(new ByteArrayInputStream(product.getImage())));
            } catch (Exception e) {
                productImageView.setImage(null);
            }
        } else {
            productImageView.setImage(null);
        }

        kgSpinner.getValueFactory().setValue(1.0);
        clearInfoLabel(addResultLabel);
    }

    @FXML
    private void handleAddToCart() {
        clearInfoLabel(addResultLabel);

        if (selectedProduct == null) {
            showInfoLabel(addResultLabel, "Select a product first!", true);
            return;
        }

        Double kgVal = kgSpinner.getValue();
        double kg = (kgVal == null) ? 0.0 : kgVal;

        if (kg <= 0) {
            showInfoLabel(addResultLabel, "Quantity must be > 0", true);
            return;
        }

        double availableStock = selectedProduct.getStock();
        int pid = selectedProduct.getProductId();
        LocalCartItem existingItem = cart.get(pid);
        double alreadyInCart = (existingItem != null) ? existingItem.getKg() : 0;
        double totalRequested = alreadyInCart + kg;

        if (totalRequested > availableStock) {
            double maxCanAdd = availableStock - alreadyInCart;
            if (maxCanAdd <= 0) {
                showInfoLabel(addResultLabel, "Maximum quantity already in cart!", true);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Insufficient Stock");
            alert.setHeaderText("Not enough stock available");
            alert.setContentText(String.format(
                    "You requested %.2f kg but only %.2f kg can be added.\n" +
                            "Would you like to add the maximum available amount?",
                    kg, maxCanAdd));

            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                kg = maxCanAdd;
            } else {
                showInfoLabel(addResultLabel, "Item not added.", false);
                return;
            }
        }

        if (existingItem != null) {
            existingItem.kg += kg;
            showInfoLabel(addResultLabel,
                    "Updated: " + selectedProduct.getName() + " (" + existingItem.kg + " kg total)",
                    false);
        } else {
            cart.put(pid, new LocalCartItem(selectedProduct, kg));
            showInfoLabel(addResultLabel, "Added: " + selectedProduct.getName() + " (" + kg + " kg)", false);
        }
    }

    @FXML
    private void handleOpenCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cart.fxml"));
            Scene scene = new Scene(loader.load(), 850, 550);

            CartController cc = loader.getController();
            cc.init(currentUsername, cart);

            Stage stage = new Stage();
            stage.setTitle("Shopping Cart - " + currentUsername);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showInfoLabel(infoLabel, "Cannot open cart!", true);
        }
    }

    @FXML
    private void handleMyOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/myorders.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            MyOrdersController c = loader.getController();
            c.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("My Orders - " + currentUsername);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoLabel(infoLabel, "Cannot open My Orders!", true);
        }
    }

    @FXML
    private void handleMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/messages.fxml"));
            Scene scene = new Scene(loader.load(), 800, 500);

            MessagesController c = loader.getController();
            c.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Messages - " + currentUsername);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoLabel(infoLabel, "Cannot open Messages!", true);
        }
    }

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile.fxml"));
            Scene scene = new Scene(loader.load(), 450, 350);

            ProfileController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile - " + currentUsername);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoLabel(infoLabel, "Cannot open profile!", true);
        }
    }

    @FXML
    private void handleLogout() {
        performLogout();
    }

    // ========== INNER CLASS ==========

    /**
     * Local cart item for temporary storage.
     * IMPORTANT: unitPrice must be getEffectivePrice()
     */
    public static class LocalCartItem {
        private final Product product;
        private double kg;

        public LocalCartItem(Product product, double kg) {
            this.product = product;
            this.kg = kg;
        }

        public Product getProduct() {
            return product;
        }

        public double getKg() {
            return kg;
        }

        public void setKg(double kg) {
            this.kg = kg;
        }

        /**
         * Get effective price (2x if low stock).
         * This ensures price_at_time in OrderItems uses getEffectivePrice.
         */
        public double getEffectivePrice() {
            return product.getEffectivePrice();
        }
    }

    /**
     * Get emoji for product based on name or type.
     */
    private String getProductEmoji(String name, String type) {
        if (name == null)
            name = "";
        name = name.toLowerCase();

        // Fruits
        if (name.contains("apple") || name.contains("elma"))
            return "🍎";
        if (name.contains("banana") || name.contains("muz"))
            return "🍌";
        if (name.contains("orange") || name.contains("portakal"))
            return "🍊";
        if (name.contains("grape") || name.contains("üzüm"))
            return "🍇";
        if (name.contains("strawberry") || name.contains("çilek"))
            return "🍓";
        if (name.contains("watermelon") || name.contains("karpuz"))
            return "🍉";
        if (name.contains("peach") || name.contains("şeftali"))
            return "🍑";
        if (name.contains("pear") || name.contains("armut"))
            return "🍐";
        if (name.contains("cherry") || name.contains("kiraz"))
            return "🍒";
        if (name.contains("lemon") || name.contains("limon"))
            return "🍋";
        if (name.contains("melon") || name.contains("kavun"))
            return "🍈";
        if (name.contains("pineapple") || name.contains("ananas"))
            return "🍍";
        if (name.contains("mango"))
            return "🥭";
        if (name.contains("kiwi"))
            return "🥝";
        if (name.contains("coconut") || name.contains("hindistan"))
            return "🥥";

        // Vegetables
        if (name.contains("tomato") || name.contains("domates"))
            return "🍅";
        if (name.contains("carrot") || name.contains("havuç"))
            return "🥕";
        if (name.contains("potato") || name.contains("patates"))
            return "🥔";
        if (name.contains("cucumber") || name.contains("salatalık"))
            return "🥒";
        if (name.contains("pepper") || name.contains("biber"))
            return "🌶️";
        if (name.contains("broccoli") || name.contains("brokoli"))
            return "🥦";
        if (name.contains("corn") || name.contains("mısır"))
            return "🌽";
        if (name.contains("onion") || name.contains("soğan"))
            return "🧅";
        if (name.contains("garlic") || name.contains("sarımsak"))
            return "🧄";
        if (name.contains("eggplant") || name.contains("patlıcan"))
            return "🍆";
        if (name.contains("lettuce") || name.contains("marul"))
            return "🥬";
        if (name.contains("cabbage") || name.contains("lahana"))
            return "🥬";
        if (name.contains("spinach") || name.contains("ıspanak"))
            return "🥬";
        if (name.contains("avocado") || name.contains("avokado"))
            return "🥑";

        // Default by type
        if ("fruit".equalsIgnoreCase(type))
            return "🍎";
        if ("vegetable".equalsIgnoreCase(type))
            return "🥕";

        return "🛒"; // Default grocery icon
    }

    @FXML
    private void handleMyCoupons() {
        java.util.List<model.Coupon> coupons = dao.CouponDAO.getActiveCoupons();

        if (coupons.isEmpty()) {
            showInfoLabel(infoLabel, "No coupons available at this time", false);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available coupons you can use at checkout:\n\n");

        for (model.Coupon c : coupons) {
            sb.append("🎟️ ").append(c.getCode()).append("\n");

            if (c.getDiscountPercent() > 0) {
                sb.append("   Discount: ").append(String.format("%.0f%%", c.getDiscountPercent())).append("\n");
            } else if (c.getDiscountAmount() > 0) {
                sb.append("   Discount: ").append(String.format("%.0f TL", c.getDiscountAmount())).append("\n");
            }

            if (c.getMinOrderAmount() > 0) {
                sb.append("   Min order: ").append(String.format("%.0f TL", c.getMinOrderAmount())).append("\n");
            }

            if (c.getValidUntil() != null) {
                sb.append("   Valid until: ").append(c.getValidUntil().toLocalDateTime().toLocalDate()).append("\n");
            }

            int remaining = c.getMaxUses() - c.getUsedCount();
            if (remaining > 0 && remaining < 10) {
                sb.append("   ⚠️ Only ").append(remaining).append(" uses left!\n");
            }

            sb.append("\n");
        }

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace;");
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(350);

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("My Coupons");
        alert.setHeaderText("🎟️ Available Coupons (" + coupons.size() + ")");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }

    @FXML
    private void handleViewProfile() {
        model.Person user = dao.UserDAO.getUserInfo(currentUsername);

        if (user == null) {
            showInfoLabel(infoLabel, "Could not load profile!", true);
            return;
        }

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-padding: 20;");

        grid.add(new javafx.scene.control.Label("Username:"), 0, 0);
        grid.add(new javafx.scene.control.Label(user.getUsername()), 1, 0);

        grid.add(new javafx.scene.control.Label("Role:"), 0, 1);
        grid.add(new javafx.scene.control.Label(user.getRole()), 1, 1);

        grid.add(new javafx.scene.control.Label("Address:"), 0, 2);
        String addr = user.getAddress() != null ? user.getAddress() : "(not set)";
        grid.add(new javafx.scene.control.Label(addr), 1, 2);

        grid.add(new javafx.scene.control.Label("Phone:"), 0, 3);
        String phone = user.getPhone() != null ? user.getPhone() : "(not set)";
        grid.add(new javafx.scene.control.Label(phone), 1, 3);

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("My Profile");
        alert.setHeaderText("👤 " + user.getUsername());
        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setPrefWidth(400);

        javafx.scene.control.ButtonType editBtn = new javafx.scene.control.ButtonType("Edit Profile",
                javafx.scene.control.ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(0, editBtn);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == editBtn) {
            openProfileEditor();
        }
    }

    private void openProfileEditor() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/profile.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            controller.ProfileController controller = loader.getController();
            controller.setUsername(currentUsername);

            javafx.stage.Stage profileStage = new javafx.stage.Stage();
            profileStage.setTitle("Edit Profile");
            profileStage.setScene(scene);
            profileStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            profileStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showInfoLabel(infoLabel, "Could not open profile editor!", true);
        }
    }
}
