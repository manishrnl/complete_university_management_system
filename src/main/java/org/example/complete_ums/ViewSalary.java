package org.example.complete_ums;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.complete_ums.CommonTable.SalaryTable;
import org.example.complete_ums.Databases.DatabaseConnection;
import org.example.complete_ums.ToolsClasses.SessionManager;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ViewSalary implements Initializable {
    public ViewSalary() throws SQLException {
    }

    Connection connection = DatabaseConnection.getConnection();
    SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private TableView<SalaryTable> salaryTable;
    @FXML

    private ComboBox<String> roleComboBox;

    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button resetButton, searchButton;

    @FXML
    private TextField searchNameField;

    String roleLoggedIn = sessionManager.getRole();
    int userIdLoggedIn = sessionManager.getUserID();
    LocalDate currentDate = LocalDate.now();
    @FXML
    private TableColumn<SalaryTable, String> transactionIdColumn, nameColumn, roleColumn;
    @FXML
    private TableColumn<SalaryTable, Double> deductionsColumn, grossSalaryColumn, netSalaryColumn;
    @FXML
    private TableColumn<SalaryTable, Integer> userIDColumn;
    @FXML
    private TableColumn<SalaryTable, LocalDate> paymentDateColumn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameColumnProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleColumnProperty());
        deductionsColumn.setCellValueFactory(cellData -> cellData.getValue().deductionsColumnProperty().asObject());
        grossSalaryColumn.setCellValueFactory(cellData -> cellData.getValue().grossSalaryColumnProperty().asObject());
        netSalaryColumn.setCellValueFactory(cellData -> cellData.getValue().netSalaryColumnProperty().asObject());
        transactionIdColumn.setCellValueFactory(cellData -> cellData.getValue().transactionIdColumnProperty());
        userIDColumn.setCellValueFactory(cellData -> cellData.getValue().userIDColumnProperty().asObject());
        paymentDateColumn.setCellValueFactory(cellData -> cellData.getValue().paymentDateColumnProperty());

        loadSalaryData();

    }

    public void loadSalaryData() {
        Double grossAmt = 0.0, deduction = 0.0, netAmt = 0.0;
        ObservableList record = FXCollections.observableArrayList();
        String query = "SELECT sp.Salary_Payment_Id,u.User_Id, CONCAT(u.First_Name,'  ', u" +
                ".Last_Name) AS fullName,u.Role, sp.Gross_Amount, sp.Total_Deductions,\n" +
                "sp.Net_Amount_Paid,sp.Payment_Date,sp.Transaction_Id FROM Salary_Payments sp JOIN Users u ON sp.User_Id = u.User_Id\n" +
                "WHERE u.Role = ? ORDER BY sp.Payment_Date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, roleLoggedIn);
              ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grossAmt = rs.getDouble("Gross_Amount");
                deduction = rs.getDouble("Total_Deductions");
                netAmt = grossAmt - deduction;
                SalaryTable temp = new SalaryTable(rs.getString("Transaction_Id"), rs.getInt(
                        "User_Id"), rs.getString("fullName"), rs.getString("Role"),
                        deduction, rs.getDate("Payment_Date").toLocalDate(), grossAmt, netAmt);

                record.add(temp);
            }
            salaryTable.setItems(record);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}