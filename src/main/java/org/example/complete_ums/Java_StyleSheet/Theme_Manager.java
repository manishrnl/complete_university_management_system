package org.example.complete_ums.Java_StyleSheet;

import javafx.scene.Scene;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Theme_Manager {
    private static final String THEME_FILE = "Save_Themes.properties";
    private static String currentThemePath = "/org/example/complete_ums/Stylesheet/Light-Theme.css"; // Default theme
    private static String currentFontSize = "12px";

    static {
        try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
            Properties props = new Properties();
            props.load(fis);
            String savedTheme = props.getProperty("current.theme");
            String savedFontSize = props.getProperty("current.font.size");
            if (savedTheme != null && !savedTheme.isEmpty() && savedFontSize != null && !savedFontSize.isEmpty()) {
                currentFontSize = savedFontSize;
                currentThemePath = savedTheme;
            }
        } catch (Exception e) {
        }
    }

    public static void setCurrentTheme(String themeCssPath) {
        if (themeCssPath != null && !themeCssPath.isEmpty()) {
            if (!themeCssPath.equals(currentThemePath)) {
                currentThemePath = themeCssPath;
                saveThemeToFile(themeCssPath);
            }
        }
    }

    public static String getCurrentTheme() {
        return currentThemePath;
    }

    public static String getCurrentFontSize() {
        return currentFontSize;
    }

    public static void setCurrentFontSize(String fontSize) {
        if (fontSize != null && !fontSize.isEmpty()) {
            if (!fontSize.equals(currentFontSize)) {
                currentFontSize = fontSize;
                saveFontSizeToFile(fontSize);
            }
        }
    }

    public static void applyTheme(Scene scene) {
        applyTheme(scene, currentThemePath);
    }

    public static void applyTheme(Scene scene, String themeCssPath) {
        if (scene == null || themeCssPath == null || themeCssPath.isEmpty()) {
            return;
        }
        try {
            String themeUrl = Theme_Manager.class.getResource(themeCssPath).toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(themeUrl);
            if (!themeCssPath.equals(currentThemePath)) {
                currentThemePath = themeCssPath;
                saveThemeToFile(themeCssPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveThemeToFile(String themeCssPath) {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
                props.load(fis);
            } catch (Exception ignored) {
            }
            props.setProperty("current.theme", themeCssPath);
            try (FileOutputStream fos = new FileOutputStream(THEME_FILE)) {
                props.store(fos, "Last selected theme");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFontSizeToFile(String fontSize) {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
                props.load(fis);
            } catch (Exception ignored) {
            }
            props.setProperty("current.font.size", fontSize + "px");
            try (FileOutputStream fos = new FileOutputStream(THEME_FILE)) {
                props.store(fos, "Last selected theme and font size");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyFontSize(Scene scene, String fontSize) {
        if (scene == null || fontSize == null || fontSize.isEmpty()) {
            return;
        }
        try {
            scene.getRoot().setStyle("-fx-font-size: " + fontSize + ";");

            if (!fontSize.equals(currentFontSize)) {
                currentFontSize = fontSize;
                saveFontSizeToFile(fontSize);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}