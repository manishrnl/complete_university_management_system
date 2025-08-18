package org.example.complete_ums;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.complete_ums.Java_StyleSheet.Theme_Manager;
import org.example.complete_ums.ToolsClasses.NavigationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Main_Application extends Application {
    Theme_Manager theme_Manager = new Theme_Manager();
    NavigationManager navigationManager = NavigationManager.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        Properties properties = new Properties();
        File file = new File("Save_Themes.properties");
        String currentTheme = null, currentFontSize = null;


        if (!file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos, "Created new theme settings file");
            }
        }

        try (FileInputStream inputStream = new FileInputStream("Save_Themes.properties")) {
            properties.load(inputStream);
            currentTheme = properties.getProperty("current.theme");
            currentFontSize = properties.getProperty("current.font.size");

        }

        NavigationManager.getInstance().setPrimaryStage(stage);
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/org/example/complete_ums/Login.fxml")));

        if (currentTheme != null && !currentTheme.isEmpty()) {
            theme_Manager.setCurrentTheme(currentTheme);
        }
        if (currentFontSize != null && !currentFontSize.isEmpty()) {
            theme_Manager.setCurrentFontSize(currentFontSize);
        }
        theme_Manager.applyTheme(scene, theme_Manager.getCurrentTheme());
        theme_Manager.applyFontSize(scene, theme_Manager.getCurrentFontSize());
        navigationManager.getInstance().navigateTo("Login.fxml");
    }
}