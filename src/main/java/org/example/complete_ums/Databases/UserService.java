package org.example.complete_ums.Databases;

import javafx.scene.control.Label;
import org.example.complete_ums.ToolsClasses.LoadFrame;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {
    LoadFrame loadFrame;
    User user;
    public static int PasswordCount = 0;
    private final DatabaseConnection databaseConnection;

    public UserService() {
        this.databaseConnection = new DatabaseConnection();
    }

    public <T> T fetchFieldValue(String tableName, String field, String value, Class<T> type, Label label) throws IOException {
        String query = "SELECT " + field + " FROM " + tableName + " WHERE " + field + " = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Object result;
                if (type == String.class) {
                    result = rs.getString(field);
                } else if (type == Integer.class) {
                    result = rs.getInt(field);
                } else if (type == Double.class) {
                    result = rs.getDouble(field);
                } else if (type == Boolean.class) {
                    result = rs.getBoolean(field);
                } else if (type == java.sql.Date.class) {
                    result = rs.getDate(field);
                } else {
                    result = rs.getObject(field); // fallback
                }
                return type.cast(result);  // cast safely
            }
        } catch (Exception e) {
            loadFrame.setMessage(label, "Something went wrong: " + e.getMessage(), "RED");
        }
        return null; // return null if no match or error
    }


    public boolean validateUserNameAuthTable(String UserNameEntered, String PasswordEntered,
                                             Label label) throws IOException {
        String query = "SELECT Password_Hash FROM Authentication WHERE UserName=? ";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query);) {
            pstmt.setString(1, UserNameEntered);

            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                String password = resultSet.getString("Password_Hash");
                if (password.equals(PasswordEntered)) {

                    return true;
                } else {
                    if (PasswordCount <= 5) {

                        // public <T> T fetchFieldValue(String tableName, String field, String value, Class<T> type, Label label) throws IOException {
                        //    String query = "SELECT " + field + " FROM " + tableName + " WHERE " + field + " = ?";
                        fetchFieldValue("Authentication", "UserName", UserNameEntered,
                                String.class, label);
                        loadFrame.setMessage(label, "You have made " + PasswordCount + " " +
                                "Incorrect Attempt out of 5 Attempts. Your User ID will be " +
                                "blocked on 5 Incorrect Attempts", "RED");

                    }
                }
            }
        } catch (Exception exception) {
            loadFrame.setMessage(label, "Something went Wrong", "RED");
        }

        return false;
    }

    public boolean validateUserFromAuthTable(String UserName, String Password_Hash,
                                             Label label) throws IOException {
        String query = "SELECT * FROM Authentication WHERE UserName=? AND Password_Hash=?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query);) {
            pstmt.setString(1, UserName);
            pstmt.setString(2, Password_Hash);

            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (Exception exception) {
            loadFrame.setMessage(label, "Something went Wrong", "RED");
        }

        return false;
    }

}
