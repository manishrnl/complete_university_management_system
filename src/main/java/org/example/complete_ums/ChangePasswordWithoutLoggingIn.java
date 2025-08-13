package org.example.complete_ums;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChangePasswordWithoutLoggingIn implements Initializable {
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

    public void handleChangePasswordButton(ActionEvent actionEvent) {
        showLoadingOverlay(); // show spinner immediately

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Long aadharNumber = Long.valueOf(aadharNumberField.getText());
                String username = UsernameField.getText().trim();
                Long mobileNumber = Long.parseLong(mobileNumberField.getText());
                String panNumber = panNumberField.getText().trim();
                String registeredEmail = registeredEmailField.getText().trim();
                String newPassword = passwordField.getText().trim();
                String confirmPassword = confirmPasswordHiddenField.getText().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty() || aadharNumber == 0 || username.isEmpty()
                        || mobileNumber == 0 || panNumber.isEmpty() || registeredEmail.isEmpty()) {
                    loadFrame.setMessage(errorMessageLabel, "Please fill in all fields.", "red");
                }
                if (!newPassword.matches(PASSWORD_REGEX)) {
                    loadFrame.setMessage(errorMessageLabel, "Password must be at least 10 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character.", "RED");
                }
                if (!newPassword.equals(confirmPassword)) {
                    loadFrame.setMessage(errorMessageLabel, "Passwords do not match.", "RED");
                }

                String query = "SELECT * FROM Users WHERE Aadhar = ? AND Mobile = ? AND Pan = ? AND Email = ?";
                String fetchUserName = "SELECT UserName FROM Authentication WHERE User_Id = ?";
                String updatePasswordQuery = "UPDATE Authentication SET Password_Hash= ? WHERE UserName=?";

                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setLong(1, aadharNumber);
                    pstmt.setLong(2, mobileNumber);
                    pstmt.setString(3, panNumber);
                    pstmt.setString(4, registeredEmail);
                    ResultSet resultSet = pstmt.executeQuery();

                    if (!resultSet.next()) {
                        loadFrame.setMessage(errorMessageLabel, "User details not found.",
                                "RED");
                    }

                    int userID = resultSet.getInt("User_Id");

                    try (PreparedStatement pstmt2 = connection.prepareStatement(fetchUserName)) {
                        pstmt2.setInt(1, userID);
                        ResultSet resultSet2 = pstmt2.executeQuery();

                        if (!resultSet2.next()) {
                            loadFrame.setMessage(errorMessageLabel, "Username not found.",
                                    "red");
                        }

                        String fetchedUsername = resultSet2.getString("UserName");
                        if (!fetchedUsername.equals(username)) {
                            loadFrame.setMessage(errorMessageLabel, "Username does not match" +
                                    " with the provided details.", "red");
                        }

                        try (PreparedStatement pstmt4 = connection.prepareStatement(updatePasswordQuery)) {
                            pstmt4.setString(1, newPassword);
                            pstmt4.setString(2, fetchedUsername);
                            int updated = pstmt4.executeUpdate();
                            if (updated <= 0) {
                                loadFrame.setMessage(errorMessageLabel, "Failed to update " +
                                        "password.", "red");
                            }
                        }
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                hideLoadingOverlay();
                Platform.runLater(() -> {
                    alertManager.showResponseAlert(
                            Alert.AlertType.INFORMATION,
                            "Success",
                            "Password Changed",
                            "Your password has been changed successfully."
                    );
                    navigationManager.navigateTo("Login.fxml");
                });
            }

            @Override
            protected void failed() {
                hideLoadingOverlay();
                Platform.runLater(() -> {
                    Throwable ex = getException();
                    loadFrame.setMessage(errorMessageLabel,
                            "Something went wrong " + ex.getMessage(), "RED");
                });
            }

            @Override
            protected void cancelled() {
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