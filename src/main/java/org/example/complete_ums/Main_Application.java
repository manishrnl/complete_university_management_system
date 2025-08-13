package org.example.complete_ums;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.complete_ums.Java_StyleSheet.Theme_Manager;
import org.example.complete_ums.ToolsClasses.LoadFrame;
import org.example.complete_ums.ToolsClasses.NavigationManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main_Application extends Application {
    NavigationManager navigationManager = NavigationManager.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        Properties properties = new Properties();
        String currentTheme = null;

        try (FileInputStream inputStream = new FileInputStream("Save_Themes.properties")) {
            properties.load(inputStream);
            currentTheme = properties.getProperty("Current_Theme");

        }

        NavigationManager.getInstance().setPrimaryStage(stage);
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/org/example/complete_ums/Login.fxml")));

        if (currentTheme != null && !currentTheme.isEmpty()) {
            Theme_Manager.setCurrentTheme(currentTheme);
        }

        Theme_Manager.applyTheme(scene, Theme_Manager.getCurrentTheme());
        navigationManager.getInstance().navigateTo("Login.fxml");


    }

}


