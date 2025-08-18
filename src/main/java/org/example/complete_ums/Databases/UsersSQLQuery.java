package org.example.complete_ums.Databases;

import javafx.scene.control.Label;
import org.example.complete_ums.ToolsClasses.LoadFrame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersSQLQuery {
    public UsersSQLQuery() throws SQLException {
    }

    String query = "";
    Connection connection = DatabaseConnection.getConnection();
    LoadFrame loadFrame = new LoadFrame();

    public String getUsersFirstName(int userId, Label errorMessageLabel) {
        query = "SELECT First_Name FROM Users WHERE User_Id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("First_Name");
            }

        } catch (Exception e) {
            loadFrame.setMessage(errorMessageLabel, "Something weired Happened at class " +
                    "UsersSQLQuery.getUsersFirstName , Message:" + e.getMessage(), "RED");
        }
        return null;
    }

    public String getRoleType(int userID, Label errorMessageLabel) {
        String roleType = "";
        query = "SELECT Role FROM Users WHERE User_Id = ? ";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("Role");
            }
        } catch (Exception ex) {
            loadFrame.setMessage(errorMessageLabel, "Unable to getRole Type : " + ex.getMessage(), "RED");
        }

        return roleType;
    }
}
