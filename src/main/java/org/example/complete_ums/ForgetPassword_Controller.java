package org.example.complete_ums;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ForgetPassword_Controller implements Initializable {
    NavigationManager navigationManager = NavigationManager.getInstance();
    Connection connection = DatabaseConnection.getConnection();
    public StackPane passwordStack;
    public ImageView eyeIcon;
    LoadFrame loadFrame;
    Button3DEffect button3DEffect;
    AlertManager alertManager = new AlertManager();

    @FXML
    private ImageView eyeIconConfirm;
    @FXML
    private TextField aadharNumberField, UsernameField, mobileNumberField, panNumberField, registeredEmailField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessageLabel, statusMessageLabel;
    @FXML
    private Button BackButton, forgetButton, QuitButton;

    public ForgetPassword_Controller() throws SQLException {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        button3DEffect.applyEffect(QuitButton, "/sound/error.mp3");
        button3DEffect.applyEffect(forgetButton, "/sound/sound2.mp3");
        button3DEffect.applyEffect(BackButton, "/sound/sound2.mp3");

    }

    @FXML
    void handleBackToLoginButton(ActionEvent event) throws IOException {
        navigationManager.navigateTo("Login.fxml");
    }

    @FXML
    void handleForgetPasswordButton(ActionEvent event) throws SQLException {
        String aadharRegex = "^[0-9]{12}$";
        String mobileRegex = "^[0-9]{10}$";

        loadFrame.setMessage(errorMessageLabel, "Processing your request...", "GREEN");

        final Boolean[] validUser = {false};
        final int[] userID = {0};
        final String[] password = {null};
        final String[] usernameFetched = {null};

        String pan = panNumberField.getText().trim();
        String username = UsernameField.getText().trim();
        String email = registeredEmailField.getText().trim();
        String mobileStr = mobileNumberField.getText().trim();
        String aadharStr = aadharNumberField.getText().trim();

        long mobile;
        long aadhar;

        // Step 1: Check for empty fields
        if (pan.isEmpty() || email.isEmpty() || username.isEmpty() || mobileStr.isEmpty() || aadharStr.isEmpty()) {
            loadFrame.setMessage(errorMessageLabel, "Please fill in all fields. None are optional.", "RED");
            return;
        }

        // Step 2: Validate numeric input
        try {
            aadhar = Long.parseLong(aadharStr);
            mobile = Long.parseLong(mobileStr);
        } catch (NumberFormatException e) {
            loadFrame.setMessage(errorMessageLabel, "Aadhar and Mobile Number must be numeric.", "RED");
            return;
        }

        // Step 3: Regex validation
        if (!aadharStr.matches(aadharRegex)) {
            loadFrame.setMessage(errorMessageLabel, "Aadhar number must be exactly 12 digits.", "RED");
            return;
        }
        if (!mobileStr.matches(mobileRegex)) {
            loadFrame.setMessage(errorMessageLabel, "Mobile number must be exactly 10 digits.", "RED");
            return;
        }

        // Step 4: SQL Queries
        String fetchUserNameQuery = "SELECT Password_Hash, User_Id FROM Authentication WHERE UserName = ?";
        String verifyQuery = "SELECT a.UserName FROM Authentication a " +
                "JOIN Users u ON u.User_Id = a.User_Id " +
                "WHERE u.Pan = ? AND u.Email = ? AND u.Aadhar = ? AND u.Mobile = ? AND a.User_Id = ?";

        // Step 5: Run in background thread
        Task<Void> forgetPasswordThread = new Task<>() {
            @Override
            protected Void call() {
                try (PreparedStatement pstmt = connection.prepareStatement(fetchUserNameQuery)) {
                    pstmt.setString(1, username);
                    ResultSet resultSet = pstmt.executeQuery();

                    if (!resultSet.next()) {
                        Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Username not found in system.", "RED"));
                        return null;
                    }

                    // Found username, now store details
                    password[0] = resultSet.getString("Password_Hash");
                    userID[0] = resultSet.getInt("User_Id");

                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Username found. Validating other details...", "GREEN"));

                    // Verify other fields
                    try (PreparedStatement pstmt2 = connection.prepareStatement(verifyQuery)) {
                        pstmt2.setString(1, pan);
                        pstmt2.setString(2, email);
                        pstmt2.setLong(3, aadhar);
                        pstmt2.setLong(4, mobile);
                        pstmt2.setInt(5, userID[0]);

                        ResultSet resultSet2 = pstmt2.executeQuery();

                        if (resultSet2.next()) {
                            usernameFetched[0] = resultSet2.getString("UserName");
                            if (usernameFetched[0].equals(username)) {
                                validUser[0] = true;
                                Platform.runLater(() -> {
                                    loadFrame.setMessage(errorMessageLabel, "We have found your credentials.", "GREEN");
                                    alertManager.showAlert(Alert.AlertType.CONFIRMATION, "User Found","We have found user with the provided details",
                                            "You are a valid user.\nYour password is: " + password[0]);
                                });
                            } else {
                                Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Username does not match with provided details.", "RED"));
                            }
                        } else {
                            Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "No matching record found for provided details.", "RED"));
                        }
                    } catch (Exception exception) {
                        Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Error validating details: " + exception.getMessage(), "RED"));
                    }

                } catch (Exception ex) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Error validating username: " + ex.getMessage(), "RED"));
                }

                // Final check after process
                if (!validUser[0]) {
                    Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Not a valid user. Double-check entered details.", "RED"));
                }

                return null;
            }
        };

        // Step 6: Start thread
        Thread thread = new Thread(forgetPasswordThread);
        thread.setDaemon(true);
        thread.start();
    }

    public void handleQuit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
