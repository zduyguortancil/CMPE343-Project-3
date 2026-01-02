package controller;

import dao.MessageDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Message;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the Messages screen in the GreenGrocer application.
 * 
 * <p>This controller manages the messaging system, allowing users to send and
 * receive messages. It provides separate views for inbox (received messages)
 * and sent messages, with support for message threading and read/unread status.
 * 
 * <p>Features:
 * <ul>
 *   <li>View inbox (received messages)</li>
 *   <li>View sent messages</li>
 *   <li>Compose new messages</li>
 *   <li>Reply to messages (threading support)</li>
 *   <li>Mark messages as read/unread</li>
 *   <li>Delete messages</li>
 *   <li>Message detail view</li>
 * </ul>
 * 
 * <p>INHERITANCE: Extends BaseController
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public class MessagesController extends BaseController {

    @FXML
    private Label titleLabel;

    // Inbox
    @FXML
    private TableView<Message> inboxTable;
    @FXML
    private TableColumn<Message, String> inFromCol;
    @FXML
    private TableColumn<Message, String> inSubjectCol;
    @FXML
    private TableColumn<Message, Timestamp> inDateCol;

    // Sent
    @FXML
    private TableView<Message> sentTable;
    @FXML
    private TableColumn<Message, String> sentToCol;
    @FXML
    private TableColumn<Message, String> sentSubjectCol;
    @FXML
    private TableColumn<Message, Timestamp> sentDateCol;

    // Message display
    @FXML
    private Label msgFromLabel;
    @FXML
    private Label msgSubjectLabel;
    @FXML
    private Label msgDateLabel;
    @FXML
    private TextArea msgContentArea;
    @FXML
    private Label msgInfoLabel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Message selectedMessage;

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        titleLabel.setText("Messages - " + username);
        loadMessages();
    }

    @Override
    protected Label getUsernameLabel() {
        return titleLabel;
    }

    @Override
    protected String getScreenTitle() {
        return "Messages";
    }

    @FXML
    public void initialize() {
        // Inbox columns
        inFromCol.setCellValueFactory(new PropertyValueFactory<>("senderUsername"));
        inSubjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        inDateCol.setCellValueFactory(new PropertyValueFactory<>("sentAt"));

        // Sent columns
        sentToCol.setCellValueFactory(new PropertyValueFactory<>("receiverUsername"));
        sentSubjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        sentDateCol.setCellValueFactory(new PropertyValueFactory<>("sentAt"));

        // Date formatters
        setupTimestampColumn(inDateCol);
        setupTimestampColumn(sentDateCol);

        // Row styling for unread
        inboxTable.setRowFactory(tv -> new TableRow<>() {
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

        // Selection listeners
        inboxTable.getSelectionModel().selectedItemProperty().addListener((obs, old, msg) -> {
            if (msg != null) {
                sentTable.getSelectionModel().clearSelection();
                displayMessage(msg);
                if (!msg.isRead()) {
                    MessageDAO.markAsRead(msg.getId());
                    msg.setRead(true);
                }
            }
        });

        sentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, msg) -> {
            if (msg != null) {
                inboxTable.getSelectionModel().clearSelection();
                displayMessage(msg);
            }
        });

        clearInfoLabel(msgInfoLabel);
    }

    private void setupTimestampColumn(TableColumn<Message, Timestamp> col) {
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

    private void loadMessages() {
        inboxTable.setItems(FXCollections.observableArrayList(
                MessageDAO.getReceivedMessages(currentUsername)));
        sentTable.setItems(FXCollections.observableArrayList(
                MessageDAO.getSentMessages(currentUsername)));
    }

    private void displayMessage(Message msg) {
        selectedMessage = msg;
        msgFromLabel.setText("From: " + msg.getSenderUsername());
        msgSubjectLabel.setText("Subject: " + (msg.getSubject() != null ? msg.getSubject() : "(no subject)"));
        msgDateLabel
                .setText("Date: " + (msg.getSentAt() != null ? msg.getSentAt().toLocalDateTime().format(DT_FMT) : ""));
        msgContentArea.setText(msg.getContent());
    }

    @FXML
    private void handleNewMessage() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("New Message");
        dialog.setHeaderText("Send message to store owner");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        subjectField.setPrefWidth(300);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Your message...");
        contentArea.setPrefRowCount(5);
        contentArea.setPrefWidth(300);

        grid.addRow(0, new Label("Subject:"), subjectField);
        grid.addRow(1, new Label("Message:"), contentArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String subject = subjectField.getText().trim();
                String content = contentArea.getText().trim();

                if (content.isEmpty()) {
                    showInfoLabel(msgInfoLabel, "Message cannot be empty!", true);
                    return false;
                }

                String owner = MessageDAO.getOwnerUsername();
                boolean success = MessageDAO.sendMessage(
                        currentUsername, owner,
                        subject.isEmpty() ? "Customer Message" : subject,
                        content, null);

                return success;
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            showInfoLabel(msgInfoLabel, "Message sent ✅", false);
            loadMessages();
        }
    }

    @FXML
    private void handleReply() {
        if (selectedMessage == null) {
            showInfoLabel(msgInfoLabel, "Select a message first!", true);
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Reply");
        dialog.setHeaderText("Reply to " + selectedMessage.getSenderUsername());

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Your reply...");
        contentArea.setPrefRowCount(5);
        contentArea.setPrefWidth(400);

        dialog.getDialogPane().setContent(contentArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String content = contentArea.getText().trim();

                if (content.isEmpty()) {
                    showInfoLabel(msgInfoLabel, "Reply cannot be empty!", true);
                    return false;
                }

                String subject = "Re: " + (selectedMessage.getSubject() != null ? selectedMessage.getSubject() : "");

                boolean success = MessageDAO.sendMessage(
                        currentUsername,
                        selectedMessage.getSenderUsername(),
                        subject,
                        content,
                        selectedMessage.getId());

                return success;
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            showInfoLabel(msgInfoLabel, "Reply sent ✅", false);
            loadMessages();
        }
    }

    @FXML
    private void handleRefresh() {
        loadMessages();
        showInfoLabel(msgInfoLabel, "Refreshed.", false);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) inboxTable.getScene().getWindow();
        stage.close();
    }
}
