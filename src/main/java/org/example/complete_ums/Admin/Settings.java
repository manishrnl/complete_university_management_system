package org.example.complete_ums.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.complete_ums.Java_StyleSheet.Theme_Manager;

public class Settings {
    @FXML
    public AnchorPane root;
    @FXML
    public ComboBox themeComboBox;
    @FXML
    public Slider fontSizeSlider;
    Scene scene = null; // This should be set to the current scene where this controller is used
    Theme_Manager themeManager = new Theme_Manager();
    @FXML
    private ToggleButton themeToggle;

    @FXML
    private ComboBox<String> fontSizeComboBox;

    @FXML
    private ComboBox<String> fontStyleComboBox;
    @FXML
    private Label javaVersionText, osVersionText, osNameText;


    @FXML
    private void initialize() {
        javaVersionText.setText("Java Version: " + System.getProperty("java.version"));
        osVersionText.setText("OS Version: " + System.getProperty("os.version"));
        osNameText.setText("Operating System Using: " + System.getProperty("os.name"));
    }


    @FXML
    private void applyFontSize() {
        scene = root.getScene();
        int selectedFontSize = (int) (fontSizeSlider.getValue());
        themeManager.saveFontSizeToFile(String.valueOf(selectedFontSize));
        themeManager.applyFontSize(scene, String.valueOf(selectedFontSize));
    }


    public void applyTheme(ActionEvent event) {
        String selectedTheme = (String) themeComboBox.getValue();
        if (selectedTheme != null && !selectedTheme.isEmpty()) {

            if (selectedTheme.equals("Light Theme")) {
                selectedTheme = "/org/example/complete_ums/Stylesheet/Light-Theme.css";
            } else if (selectedTheme.equals("Dark Theme")) {
                selectedTheme = "/org/example/complete_ums/Stylesheet/Dark_Theme.css";
            } else if (selectedTheme.equals("System Default")) {
                selectedTheme = "/org/example/complete_ums/Stylesheet/Custom-Theme.css";
            }
            changeTheme(selectedTheme);

        }
    }


    private void changeTheme(String themePath) {
        if (root.getScene() != null) {
            Theme_Manager.setCurrentTheme(themePath);
            Theme_Manager.applyTheme(root.getScene());

        }
    }


    public void resetSettings(ActionEvent event) {
        themeComboBox.setValue("Dark Theme"); // Reset to default theme
        fontSizeSlider.setValue(12); // Reset to default font size
        themeManager.applyFontSize(root.getScene(), "12");
        themeManager.setCurrentTheme("/org/example/complete_ums/Stylesheet/Dark_Theme.css");
        themeManager.applyTheme(root.getScene(),"/org/example/complete_ums/Stylesheet/Dark_Theme.css");
    }

    public void saveAndExit(ActionEvent event) {
    }
}