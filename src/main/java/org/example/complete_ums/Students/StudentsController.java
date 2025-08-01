package org.example.complete_ums.Students;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.complete_ums.Databases.DatabaseConnection;
import org.example.complete_ums.Java_StyleSheet.Button3DEffect;
import org.example.complete_ums.Java_StyleSheet.RoundedImage;
import org.example.complete_ums.Java_StyleSheet.Theme_Manager;
import org.example.complete_ums.ToolsClasses.AlertManager;
import org.example.complete_ums.ToolsClasses.LoadFrame;
import org.example.complete_ums.ToolsClasses.NavigationManager;
import org.example.complete_ums.ToolsClasses.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class StudentsController implements Initializable {

    private final NavigationManager navigationManager = NavigationManager.getInstance();

    Connection connection = DatabaseConnection.getConnection();
    RoundedImage roundedImage = new RoundedImage();
    SessionManager sessionManager = SessionManager.getInstance();
    LoadFrame loadFrame;
    AlertManager alertManager;
    Button3DEffect button3DEffect;

    @FXML
    private ImageView profileImage, lightDarkThemeImage, BackImage, ForwardImage;
    @FXML
    public StackPane contentArea;
    @FXML
    public ScrollPane mainScrollPane;

    @FXML
    private Menu menuStudentsProfile;


    @FXML
    private VBox root;

    @FXML
    private Button btnDashboard, btnViewProfile, btnViewAttendance, btnViewLibrary,
            btnHostelTransport, btnFinance, btnNotification, btnEvents, btnTimeTable, btnExams, btnLogout;

    private String currentPage = ""; // add this at the class level
    int totalStudents = 0, totalStudents1 = 0, totalStudents2 = 0, totalStudents3 = 0;
    private static StudentsController instance;
    private File selectedImageFile;  // Class-level variable
    private Map<String, String> fxmlPathMap = new HashMap<>();
    private final Map<String, Runnable> functionMap = new LinkedHashMap<>();
    private final Map<String, String> pageMap = new LinkedHashMap<>();


    public StudentsController() throws SQLException {
        instance = this;
    }

    public static StudentsController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (profileImage != null)
            profileImage.setOnMouseClicked(uploadProfilePhoto -> updateProfilePhoto());
        profileImage.setStyle("-fx-cursor: hand");
        roundedImage.makeImageViewRounded(profileImage);
        roundedImage.makeImageViewRounded(lightDarkThemeImage);
        lightDarkThemeImage.setOnMouseClicked(toggleThemeChange());
        loadProfileImage(sessionManager.getUserID());
        loadPageIntoScrollPane("/org/example/complete_ums/Students/StudentsDashboardContent.fxml");

        Platform.runLater(() -> Theme_Manager.applyTheme(root.getScene()));
        apply3DEffectsAndText();


    }

    private EventHandler<? super MouseEvent> toggleThemeChange() {
        return event -> {
            String currentTheme = Theme_Manager.getCurrentTheme();
            if (currentTheme.equals("/org/example/complete_ums/Stylesheet/Dark_Theme.css")) {
                changeTheme("/org/example/complete_ums/Stylesheet/Light-Theme.css");
                // lightDarkThemeImage.setImage(new Image("/org/example/complete_ums/Images/light.png"));
            } else {
                changeTheme("/org/example/complete_ums/Stylesheet/Dark_Theme.css");
                //  lightDarkThemeImage.setImage(new Image("/org/example/complete_ums/Images/dark.png"));
            }
        };

    }


    private void apply3DEffectsAndText() {
        button3DEffect.applyEffect(menuStudentsProfile, "/sounds/hover.mp3");
        menuStudentsProfile.setText("👤 Welcome , " + sessionManager.getFirstName());
        button3DEffect.applyEffect(BackImage);
        button3DEffect.applyEffect(ForwardImage);
        button3DEffect.applyEffect(btnNotification, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnViewProfile, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnViewAttendance, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnViewLibrary, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnTimeTable, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnDashboard, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnEvents, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnExams, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnFinance, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnHostelTransport, "/sound/sound2.mp3");
        button3DEffect.applyEffect(btnLogout, "/sound/sound2.mp3");
    }

    private void loadProfileImage(int userID) {
        // Create a background task to avoid freezing the UI
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                String imageQuery = "SELECT Photo_URL FROM Users WHERE User_Id=?";
                try (PreparedStatement stmt = connection.prepareStatement(imageQuery)) {
                    stmt.setInt(1, userID);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        // Use try-with-resources to automatically close the InputStream
                        try (InputStream is = rs.getBinaryStream("Photo_URL")) {
                            if (is != null) {
                                return new Image(is); // Return the loaded image
                            }
                        }
                    }
                }
                return null; // Return null if no image is found
            }
        };

        // This code runs on the JavaFX thread AFTER the background task succeeds
        loadImageTask.setOnSucceeded(event -> {
            Image profileImagess = loadImageTask.getValue(); // Get the result from the task
            if (profileImagess != null) {
                profileImage.setImage(profileImagess);
                // Assuming roundedImage is an instance of a class you wrote
                // roundedImage.makeImageViewRounded(AdminImage);
            }
        });

        // This code runs on the JavaFX thread IF the background task fails
        loadImageTask.setOnFailed(event -> {
            Throwable e = loadImageTask.getException();
            System.err.println("Error loading profile image: " + e.getMessage());
            alertManager.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load profile image",
                    "An error occurred while retrieving the image from the database.");
        });

        // Start the background task
        new Thread(loadImageTask).start();
    }

    @FXML
    void loadPageIntoScrollPane(String fxmlFile) {
        if (fxmlFile.equals(currentPage)) {
            System.out.println("Page already loaded: " + fxmlFile);
            return;
        }
        StackPane loadingOverlay = createLoadingOverlay();
        // The cast to StackPane should be safe if your FXML structure is correct
        StackPane parentContainer = (StackPane) mainScrollPane.getParent();
        parentContainer.getChildren().add(loadingOverlay);

        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                URL resourceUrl = getClass().getResource(fxmlFile);

                if (resourceUrl == null) {
                    throw new java.io.FileNotFoundException("FXML file not found at path: " + fxmlFile);
                }
                return FXMLLoader.load(resourceUrl);
            }
        };

        loadTask.setOnSucceeded(event -> {
            Parent content = loadTask.getValue();
            mainScrollPane.setContent(content);
            currentPage = fxmlFile;
            parentContainer.getChildren().remove(loadingOverlay);
            // Optionally, add a success message here if desired
        });

        loadTask.setOnFailed(event -> {
            parentContainer.getChildren().remove(loadingOverlay);
            loadTask.getException().printStackTrace();
            // Show error message to user
            Platform.runLater(() -> {
                String errorMessage = "Failed to load page: " + (loadTask.getException() != null ? loadTask.getException().getMessage() : "Unknown error.");
                // Use alertManager or loadFrame.setMessage for user feedback
                // loadFrame.setMessage(errorMessageLabel, errorMessage, "RED");
                System.err.println(errorMessage);
            });
        });
        new Thread(loadTask).start();
    }

    private StackPane createLoadingOverlay() {
        ProgressIndicator pIndicator = new ProgressIndicator();
        pIndicator.setPrefSize(80, 80);
        pIndicator.setMaxSize(80, 80);
        StackPane overlay = new StackPane(pIndicator);
        overlay.setAlignment(Pos.CENTER);
        // Using a slightly darker overlay for better visibility on potentially mixed backgrounds
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // Darker, 40% transparent
        return overlay;
    }

    @FXML
    private void handleMenuOption(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();
        String text = clickedItem.getText();
        Scene scene = root.getScene(); // Get the scene from any Node
        String themePath;
        switch (text) {
            case "Dark Theme":
                changeTheme("/org/example/complete_ums/Stylesheet/Dark_Theme.css");
                break;
            case "Light Theme":
                changeTheme("/org/example/complete_ums/Stylesheet/Light-Theme.css");
                break;
            case "Change Password":
                changePassword();
                break;
            case "My Profile":
                viewProfile();
                break;
            case "Upload Profile Photo":
                updateProfilePhoto();
                break;
            case "Logout":
                handleLogout(event);
                break;
            default:
                System.out.println("Unknown option selected: " + text);
                break;
        }

    }

    private void changeTheme(String themePath) {
        if (root.getScene() != null) {
            Theme_Manager.setCurrentTheme(themePath);
            Theme_Manager.applyTheme(root.getScene());
        }
    }

    private void changePassword() {
        Optional<ButtonType> result =
                AlertManager.showResponseAlert(Alert.AlertType.CONFIRMATION, "Change " +
                        "Password", "Are you sure you want to change Password ?", "Your " +
                        "password will be updated to the desired value that must fullfill " +
                        "password criteria E.g., It must contains a combination of Upper " +
                        "case,Lower case , digits and a special character .\n\n And " +
                        "Password's length must be atleast 10 digits long" +
                        ".");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigationManager.navigateTo("changePassword.fxml");
        }
    }

    private void viewProfile() {
        alertManager.showAlert(Alert.AlertType.INFORMATION, "Profile Information",
                "User Profile", "Name: " + sessionManager.getFirstName() + " " +
                        sessionManager.getLastName() + "\nEmail: " + sessionManager.getEmail() +
                        "\nRole: " + sessionManager.getRole() + "\nUser ID: " + sessionManager.getUserID()
                        + "\n PAN : " + sessionManager.getPan() + "\n Aadhar : " + sessionManager.getAadhar() + "\n Mobile : " + sessionManager.getPhone() + "\n Address : " + sessionManager.getAddress() + "\n Date of Birth : " + sessionManager.getDOB());
    }

    private void updateProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload your Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            profileImage.setImage(new Image(file.toURI().toString()));
            roundedImage.makeImageViewRounded(profileImage); // Rounded Corners for the ImageView

            String updateQuery = "UPDATE Users SET Photo_URL=? WHERE User_Id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setBinaryStream(1, new FileInputStream(selectedImageFile));
                pstmt.setInt(2, sessionManager.getUserID());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Profile image updated successfully.");
                    AlertManager.showAlert(Alert.AlertType.INFORMATION, "Success", "Profile Image Updated",
                            "Your profile image has been updated successfully.");
                } else {
                    System.out.println("No record updated. User ID might be invalid.");
                    AlertManager.showAlert(Alert.AlertType.ERROR, "Error", "Profile Image Update Failed",
                            "An error occurred while updating your profile image. Please try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertManager.showAlert(Alert.AlertType.ERROR, "Error", "Profile Image Update Failed",
                        "An error occurred while updating your profile image. Please try again.");
            }
        } else {
            System.out.println("No image selected.");
        }


    }

    @FXML
    void navigateBackward(MouseEvent event) {
        navigationManager.goBack();
    }

    @FXML
    void navigateForward(MouseEvent event) {
        navigationManager.goForward();
    }

    public void handleButtonClick(ActionEvent actionEvent) throws IOException {
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        String navigateToPage = "";
        Button current = (Button) actionEvent.getSource();
        String currentId = current.getId();

        switch (currentId) {
            case "btnDashboard":
                navigateToPage = "StudentsDashboardContent.fxml";
                break;
            case "btnViewProfile":
                navigateToPage = "StudentsProfile.fxml";
                break;
            case "btnViewAttendance":
                loadPageIntoScrollPane("/org/example/complete_ums/ViewAttendance.fxml");
                break;
            case "btnViewLibrary":
                navigateToPage = "Library.fxml";
                break;
            case "btnHostelTransport":
                navigateToPage = "HostelTransport.fxml";
                break;
            case "btnFinance":
                navigateToPage = "Finance.fxml";
                break;
            case "btnEvents":
                loadPageIntoScrollPane("/org/example/complete_ums/Events.fxml");
                break;
            case "btnTimeTable":
                navigateToPage = "TimeTable.fxml";
                break;
            case "btnExams":
                navigateToPage = "Exams.fxml";
                break;
            case "btnNotification":
                loadFrame.addNewFrame(currentStage, getClass(), "Notifications.fxml",
                        "Notification Panel", false);
                break;
            default:
                System.out.println("Invalid response..");
                break;
        }
        if (navigateToPage != null) {
            String fxmlPath = fxmlPathMap.get(navigateToPage);
            if (fxmlPath != null) {
                loadPageIntoScrollPane(fxmlPath);
            } else if (navigateToPage.endsWith(".fxml")) {
                // Fallback for direct FXML names not in map, but better to put them in map
                loadPageIntoScrollPane("/org/example/complete_ums/Students/" + navigateToPage);
            }
        }
    }

    public void handleLogout(ActionEvent actionEvent) {
        Optional<ButtonType> result = AlertManager.showResponseAlert(Alert.AlertType.CONFIRMATION, "Confirm Logout",
                "Are you sure you want to log out?",
                "Your session will be ended and you will be returned to the login screen.");

        // Proceed only if the user confirms
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Use the correct data types for the PreparedStatement
                String query = "UPDATE Users u JOIN Authentication a ON u.User_Id = a.User_Id SET u.User_Status = 'Inactive', a.Last_Login = ? WHERE u.User_Id = ?;";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    // Use setTimestamp for date-time objects
                    pstmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    // Use setInt for integer IDs
                    pstmt.setInt(2, sessionManager.getUserID());
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                System.err.println("Failed to update last login time: and User Status :" + e.getMessage());
                // Optionally show an alert, but don't stop the logout process.
            } finally {
                // This block ALWAYS executes, ensuring proper cleanup
                DatabaseConnection.closeConnection();
                sessionManager.clearAll();

                // --- CHANGE ---
                // The two lines for closing the stage have been removed from here.
                // Let the NavigationManager handle closing the old stage and opening the new one.
                navigationManager.navigateTo("Login.fxml");
            }
        }
    }

    public void handleNotification(ActionEvent actionEvent) throws IOException {
        Stage currentStage = (Stage) btnNotification.getScene().getWindow();
        loadFrame.addNewFrame(currentStage, getClass(), "Notifications.fxml", "Notification_Controller Panel", false);

    }

}



