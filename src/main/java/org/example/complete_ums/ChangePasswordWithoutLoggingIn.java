package org.example.complete_ums;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.complete_ums.Databases.DatabaseConnection;
import org.example.complete_ums.Java_StyleSheet.Button3DEffect;
import org.example.complete_ums.ToolsClasses.AlertManager;
import org.example.complete_ums.ToolsClasses.LoadFrame;
import org.example.complete_ums.ToolsClasses.NavigationManager;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ChangePasswordWithoutLoggingIn implements Initializable {
    Connection connection = DatabaseConnection.getConnection();
    public StackPane passwordStack;
    public ImageView eyeIcon;
    LoadFrame loadFrame = new LoadFrame();
    Button3DEffect button3DEffect;
    AlertManager alertManager = new AlertManager();
    @FXML
    private TextField showPasswordField, confirmPasswordField;
    @FXML
    private PasswordField confirmPasswordHiddenField;
    @FXML
    private ImageView eyeIconConfirm;

    private boolean showPassword = false;
    private boolean showConfirmPassword = false;
    @FXML
    private TextField aadharNumberField, UsernameField, mobileNumberField, panNumberField, registeredEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessageLabel;
    @FXML
    private Button BackButton, changeButton, QuitButton;
    NavigationManager navigationManager = NavigationManager.getInstance();
    String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
    // Optional<ButtonType> response=null;
    Boolean successStatus = false;

    public ChangePasswordWithoutLoggingIn() throws SQLException {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        button3DEffect.applyEffect(BackButton, "/sound/sound2.mp3");
        button3DEffect.applyEffect(changeButton, "/sound/sound2.mp3");
        button3DEffect.applyEffect(QuitButton, "/sound/error.mp3");
        showPasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordHiddenField.textProperty());
    }


    public void handleQuit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    public void handleBackToLoginButton(ActionEvent actionEvent) {
        navigationManager.navigateTo("Login.fxml");

    }

    @FXML
    private void togglePasswordVisibility() {
        showPassword = !showPassword;
        showPasswordField.setVisible(showPassword);
        showPasswordField.setManaged(showPassword);
        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        showConfirmPassword = !showConfirmPassword;
        confirmPasswordField.setVisible(showConfirmPassword);
        confirmPasswordField.setManaged(showConfirmPassword);
        confirmPasswordHiddenField.setVisible(!showConfirmPassword);
        confirmPasswordHiddenField.setManaged(!showConfirmPassword);
    }

    private int getUserId(String username) {
        String query = "SELECT User_Id FROM Authentication WHERE UserName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("User_Id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getUserIdByDetails(Long aadharNumber, Long mobileNumber, String panNumber, String registeredEmail) {
        String query = "SELECT User_Id FROM Users WHERE Aadhar = ? AND Mobile = ? AND Pan = ? AND Email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, aadharNumber);
            pstmt.setLong(2, mobileNumber);
            pstmt.setString(3, panNumber);
            pstmt.setString(4, registeredEmail);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("User_Id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if user not found or error occurs
    }

    public void handleChangePasswordButton(ActionEvent actionEvent) {
        errorMessageLabel.setText(""); // clear previous messages
        showLoadingOverlay(); // show spinner immediately
        String PAN_REGEX = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$"; // PAN number regex
        if (!panNumberField.getText().trim().matches(PAN_REGEX)) {
            loadFrame.setMessage(errorMessageLabel, "Please enter a valid PAN number.", "RED");
            hideLoadingOverlay();
            return;
        }
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Long aadharNumber;
                Long mobileNumber;

                try {
                    aadharNumber = Long.valueOf(aadharNumberField.getText());
                    mobileNumber = Long.parseLong(mobileNumberField.getText());
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Please enter valid Aadhar and mobile numbers.", "RED"));
                    return false;
                }

                String username = UsernameField.getText().trim();
                String panNumber = panNumberField.getText().trim();
                String registeredEmail = registeredEmailField.getText().trim();
                String newPassword = passwordField.getText().trim();
                String confirmPassword = confirmPasswordHiddenField.getText().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty() || aadharNumber == 0 || username.isEmpty() || mobileNumber == 0 || panNumber.isEmpty() || registeredEmail.isEmpty()) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Please fill in all fields.", "RED"));
                    return false;
                }
                if (!newPassword.matches(PASSWORD_REGEX)) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Password must be at least 10 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character.", "RED"));
                    return false;
                }
                if (!newPassword.equals(confirmPassword)) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Passwords do not match.", "RED"));
                    return false;
                }


                int userId = getUserId(username);
                int userIdByDetails = getUserIdByDetails(aadharNumber, mobileNumber, panNumber, registeredEmail);
                // System.out.println(userId);
                // System.out.println(userIdByDetails);
                if (userId == -1) {
                    loadFrame.setMessage(errorMessageLabel, "Username not found with " +
                            "provided details . Retry ... ", " RED");

                    return false;
                }
                if (userIdByDetails == -1) {
                    loadFrame.setMessage(errorMessageLabel, "User Name exists but the " +
                            "details you entered does not matches with our database . Please" +
                            " check your inputs.", "RED");

                    return false;
                }
                if (userId != userIdByDetails) {
                    loadFrame.setMessage(errorMessageLabel, "User Name and the " +
                            "provided details mismatched. Please check your inputs.", "RED");
                    return false;

                }
                String updatePasswordQuery = "UPDATE Authentication SET Password_Hash= ? " +
                        "WHERE User_Id=?";
                try (PreparedStatement pstmt = connection.prepareStatement(updatePasswordQuery)) {
                    pstmt.setString(1, newPassword);
                    pstmt.setInt(2, userId);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        loadFrame.setMessage(errorMessageLabel, "Password updated successfully.", "GREEN");

                    }

                }

                return true;
            }

            @Override
            protected void succeeded() {
                hideLoadingOverlay();
                if (getValue()) {
                    Platform.runLater(() -> {
                        alertManager.showResponseAlert(Alert.AlertType.INFORMATION, "Success", "Password Changed", "Your password has been changed successfully.");
                        navigationManager.navigateTo("Login.fxml");
                    });
                }
            }

            @Override
            protected void failed() {
                hideLoadingOverlay();
                Platform.runLater(() -> {
                    loadFrame.setMessage(errorMessageLabel, "An unexpected error occurred: " + getException().getMessage(), "RED");
                });
            }

            @Override
            protected void cancelled() {
                Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Password change request was cancelled.", "RED"));
                hideLoadingOverlay();
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true); // allows JVM to exit if thread is still running
        thread.start();
    }

    @FXML
    private AnchorPane rootPane; // Main container in your FXML

    // Create a reusable loading overlay for AnchorPane
    private AnchorPane createLoadingOverlay() {
        // Spinner setup
        ProgressIndicator pIndicator = new ProgressIndicator();
        pIndicator.setPrefSize(80, 80);
        pIndicator.setMaxSize(80, 80);

        // Status label
        Label statusLabel = new Label("Processing your Password Change Request... Please " +
                "wait a moment");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 17px;");
        statusLabel.setVisible(false); // show after short delay

        // VBox to hold spinner + label
        VBox content = new VBox(15, pIndicator, statusLabel);
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(rootPane.getWidth());
        content.setPrefHeight(rootPane.getHeight());

        // Make VBox fill parent AnchorPane
        AnchorPane overlay = new AnchorPane(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);

        // Semi-transparent background
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        // Timed label updates
        PauseTransition delay0 = new PauseTransition(Duration.seconds(0.5));
        delay0.setOnFinished(event -> statusLabel.setVisible(true));

        PauseTransition delay1 = new PauseTransition(Duration.seconds(4));
        delay1.setOnFinished(event -> statusLabel.setText(
                "Still loading, please wait. We are trying to change it sooner..."
        ));

        PauseTransition delay2 = new PauseTransition(Duration.seconds(7));
        delay2.setOnFinished(event -> statusLabel.setText(
                "It's taking longer than usual. Possible slow internet connection or slow database response."
        ));

        delay0.play();
        delay1.play();
        delay2.play();

        return overlay;
    }

    // Show overlay on the screen
    private void showLoadingOverlay() {
        AnchorPane overlay = createLoadingOverlay();
        rootPane.getChildren().add(overlay);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);
    }

    // Remove overlay when done
    private void hideLoadingOverlay() {
        rootPane.getChildren().removeIf(node -> node instanceof AnchorPane &&
                node != rootPane); // removes only overlays, not root
    }
}