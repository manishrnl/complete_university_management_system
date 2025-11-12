package org.example.complete_ums.Databases;

import org.example.complete_ums.ToolsClasses.AppProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;

    private static final String URL = AppProperties.get("SQL_URL");
    private static final String USER = AppProperties.get("SQL_USER");
    private static final String PASSWORD = AppProperties.get("SQL_PASSWORD");

//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }




    //Database Connection for Online accounts use -> manish......@zohomail.in

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://3ad27ZXqceAokYk.root:5tsc790POQmH8wkT@gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/university_management_system");
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
