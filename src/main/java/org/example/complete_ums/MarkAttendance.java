package org.example.complete_ums;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.complete_ums.CommonTable.AttendanceTable;
import org.example.complete_ums.Databases.DatabaseConnection;
import org.example.complete_ums.Java_StyleSheet.Button3DEffect;
import org.example.complete_ums.ToolsClasses.AlertManager;
import org.example.complete_ums.ToolsClasses.LoadFrame;
import org.example.complete_ums.ToolsClasses.SessionManager;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class MarkAttendance implements Initializable {
    Button3DEffect button3DEffect = new Button3DEffect(); // Initialize here
    AlertManager alertManager = new AlertManager();
    @FXML
    private ImageView usersImage;
    @FXML
    private ComboBox<String> attendanceStatusComboBox;
    @FXML
    private Label attendanceIdField, userIdField, dateField, timeInField, timeOutField, fullNameField, errorMessageLabel, fathersNameField;
    @FXML
    private TextField remarksField;

    SessionManager sessionManager = SessionManager.getInstance();
    Connection connection = DatabaseConnection.getConnection();
    LoadFrame loadFrame = new LoadFrame(); // Initialize here

    @FXML
    private TableView<AttendanceTable> attendanceTable;

    @FXML
    private TableColumn<AttendanceTable, Integer> colUserId, colAttendanceId;
    @FXML
    private TableColumn<AttendanceTable, String> colRole, colFathersName, colFullName,
            colRemarks,
            colStatus;
    @FXML
    private TableColumn<AttendanceTable, LocalTime> colTimeIn,
            colTimeOut;
    @FXML
    private TableColumn<AttendanceTable, LocalDate> colDate;


    @FXML
    private ComboBox<String> filterTypeComboBox, roleComboBox, statusComboBox, monthComboBox;
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private Button btnCancel, btnMarkAllAttendance, btnApplyFilter;
    @FXML
    private DatePicker fromDatePicker, toDatePicker;

    private Map<Integer, AttendanceTable> markedAttendanceRecords = new HashMap<>();

    ObservableList<AttendanceTable> masterData = FXCollections.observableArrayList();
    ObservableList<Integer> yearList = FXCollections.observableArrayList();

    // Store the initial auto-commit state to restore it later
    private boolean initialAutoCommitState;

    public MarkAttendance() throws SQLException {
        // Constructor is empty as initialization happens in initialize method
        // Capture initial auto-commit state when the connection is established.
        // This is a safer place, assuming DatabaseConnection.getConnection()
        // provides a valid, open connection.
        try {
            if (connection != null && !connection.isClosed()) {
                this.initialAutoCommitState = connection.getAutoCommit();
            } else {
                // Handle case where connection is null or closed initially (e.g., log error)
                System.err.println("Warning: Database connection not available in MarkAttendance constructor.");
                this.initialAutoCommitState = true; // Assume default if connection not ready
            }
        } catch (SQLException e) {
            System.err.println("Error getting initial auto-commit state: " + e.getMessage());
            this.initialAutoCommitState = true; // Assume default on error
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewUsersIntoAttendanceTable();
        button3DEffect.applyEffect(btnCancel, "/sound/hover.mp3");
        button3DEffect.applyEffect(btnApplyFilter, "/sound/hover.mp3");
        button3DEffect.applyEffect(btnMarkAllAttendance, "/sound/hover.mp3");
        errorMessageLabel.setText("");

        // Populate combo boxes
        filterTypeComboBox.setItems(FXCollections.observableArrayList("All", "By Year", "Date Range", "By Month", "By Status", "By Role Type"));
        attendanceStatusComboBox.setItems(FXCollections.observableArrayList("Present", "Absent", "Leave", "Late"));
        statusComboBox.setItems(FXCollections.observableArrayList("All", "Present", "Absent", "Leave", "Late"));
        roleComboBox.setItems(FXCollections.observableArrayList("Student", "Teacher", "Staff", "Librarian", "Accountant", "Admin")); // Example roles
        monthComboBox.setItems(FXCollections.observableArrayList(
                "All Months", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        ));

        // Populate year combo box
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) { // Populate 5 years back and 5 years forward
            yearList.add(i);
        }
        yearComboBox.setItems(yearList.sorted());
        yearComboBox.setValue(currentYear); // Set current year as default

        loadAttendanceData(); // Load initial data

        colDate.setCellValueFactory(cellData -> cellData.getValue().attendanceDateProperty());
        colRemarks.setCellValueFactory(cellData -> cellData.getValue().remarksProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colTimeIn.setCellValueFactory(cellData -> cellData.getValue().timeInProperty());
        colTimeOut.setCellValueFactory(cellData -> cellData.getValue().timeOutProperty());
        colAttendanceId.setCellValueFactory(cellData -> cellData.getValue().attendanceIdProperty().asObject());
        colUserId.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        colFullName.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        colFathersName.setCellValueFactory(cellData -> cellData.getValue().fathersNameProperty());
        colRole.setCellValueFactory(cellData -> cellData.getValue().colRoleProperty());

        filterTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            errorMessageLabel.setText("");
            switch (newValue) {
                case "All":
                    enableAllFields();
                    loadFrame.setMessage(errorMessageLabel, "Please fill all filter details to proceed.", "GREEN");
                    break;
                case "By Year":
                    toggleEnableDisableOtherFields(true, true, false, true, true);
                    loadFrame.setMessage(errorMessageLabel, "Please select Filter By Year to proceed.", "GREEN");
                    break;
                case "Date Range":
                    toggleEnableDisableOtherFields(true, true, true, false, true);
                    loadFrame.setMessage(errorMessageLabel, "Please Select To Date and From Date to proceed.", "GREEN");
                    break;
                case "By Month":
                    toggleEnableDisableOtherFields(true, false, true, true, true);
                    loadFrame.setMessage(errorMessageLabel, "Please Select Filter by Month to proceed.", "GREEN");
                    break;
                case "By Status":
                    toggleEnableDisableOtherFields(false, true, true, true, true);
                    loadFrame.setMessage(errorMessageLabel, "Please Select Filter By Status to proceed.", "GREEN");
                    break;
                case "By Role Type":
                    toggleEnableDisableOtherFields(true, true, true, true, false);
                    loadFrame.setMessage(errorMessageLabel, "Please Select Filter By Role Type to proceed.", "GREEN");
                    break;
            }
        });

        attendanceTable.setOnMouseClicked(mouseEvent -> {
            AttendanceTable selected = attendanceTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int userID = selected.getUserId();
                LocalDate currentDate = LocalDate.now(); // Always use current date for marking new attendance
                LocalTime currentTime = LocalTime.now(); // Always use current time for marking new attendance

                String selectedStatus = attendanceStatusComboBox.getValue();
                if (selectedStatus == null) {
                    selectedStatus = "Present"; // Default to "Present" if not selected
                    attendanceStatusComboBox.setValue("Present");
                }
                String enteredRemarks = remarksField.getText();
                if (enteredRemarks == null || enteredRemarks.trim().isEmpty()) {
                    enteredRemarks = "On Time"; // Default remarks
                }

                // Update UI fields based on selected row or current defaults
                userIdField.setText(String.valueOf(userID));
                fullNameField.setText(selected.getFullName());
                fathersNameField.setText(selected.getFathersName());
                // attendanceIdField should reflect the selected record's ID, but for marking,
                // we might create a new one, or update an existing one for the current day.
                // Keeping it as selected.getAttendanceId() for display purposes is fine.
                attendanceIdField.setText(String.valueOf(selected.getAttendanceId()));

                // For timeIn/TimeOut and date, if you want to mark attendance for *today*,
                // these fields should reflect current time/date, not necessarily the historical ones.
                // However, the recordToMark will use the current date for the attendance entry.
                timeInField.setText(currentTime.toString().substring(0, 5)); // Display current time, truncated
                timeOutField.setText(currentTime.plusHours(8).toString().substring(0, 5)); // Example: 8 hours later
                dateField.setText(currentDate.toString());
                remarksField.setText(enteredRemarks);
                attendanceStatusComboBox.setValue(selectedStatus);


                // Create a new AttendanceTable object with updated status and remarks
                // This object will be used for insertion/update. The date and times
                // here reflect the *intended* attendance for the current marking action.
                AttendanceTable recordToMark = new AttendanceTable(
                        userID,
                        selected.getFullName(),
                        selected.getFathersName(),
                        selected.getAttendanceId(), // This ID might be ignored for INSERT, but useful for UPDATE
                        currentDate, // Mark attendance for TODAY
                        LocalTime.parse("09:00"), // Default time-in for new entry or update
                        LocalTime.parse("17:00"), // Default time-out for new entry or update
                        selected.getColRole(),
                        selectedStatus,
                        enteredRemarks
                );

                // Add or update the record in the map of marked attendances
                markedAttendanceRecords.put(userID, recordToMark);
                System.out.println("User " + selected.getFullName() + " (ID: " + userID + ") marked for attendance in map. Status: " + selectedStatus + ", Remarks: " + enteredRemarks);

                loadFrame.setMessage(errorMessageLabel, "Attendance data for " + selected.getFullName() + " set to: " + selectedStatus + " (Ready to save)", "GREEN");
                loadUsersImage(userID);
            }
        });
    }

    private void addNewUsersIntoAttendanceTable() {
        String query = "INSERT INTO Attendances (User_Id, Attendance_Date, Status, Remarks) " +
                "SELECT u.User_Id, CURDATE(), 'Present', 'Auto-added' " +
                "FROM Users u " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 FROM Attendances a " +
                "    WHERE a.User_Id = u.User_Id AND a.Attendance_Date = CURDATE()" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int rowsInserted = stmt.executeUpdate();
            System.out.println("Inserted " + rowsInserted + " new users into Attendances table.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersImage(int userID) {
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                String imageQuery = "SELECT Photo_URL FROM Users WHERE User_Id=?";
                // Use a try-with-resources for the PreparedStatement to ensure it's closed
                try (PreparedStatement stmt = connection.prepareStatement(imageQuery)) {
                    stmt.setInt(1, userID);
                    try (ResultSet rs = stmt.executeQuery()) { // Use try-with-resources for ResultSet
                        if (rs.next()) {
                            try (InputStream is = rs.getBinaryStream("Photo_URL")) {
                                if (is != null) {
                                    return new Image(is);
                                }
                            }
                        }
                    }
                }
                return null;
            }
        };
        loadImageTask.setOnSucceeded(event -> {
            Image profileImage1 = loadImageTask.getValue();
            if (profileImage1 != null) {
                usersImage.setImage(profileImage1);
            } else {
                Image defaultImage = new Image(getClass().getResourceAsStream("/org/example/complete_ums/Images/UserName.png"));
                usersImage.setImage(defaultImage);
            }
        });
        loadImageTask.setOnFailed(event -> {
            Throwable e = loadImageTask.getException();
            System.err.println("Error loading profile image for User ID " + userID + ": " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            Platform.runLater(() -> {
                alertManager.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load profile image",
                        "An error occurred while retrieving the image from the database.");
            });
        });
        new Thread(loadImageTask).start();
    }

    private void enableAllFields() {
        statusComboBox.setDisable(false);
        monthComboBox.setDisable(false);
        yearComboBox.setDisable(false);
        fromDatePicker.setDisable(false);
        toDatePicker.setDisable(false);
        roleComboBox.setDisable(false);
        clearFilterSelections();
    }

    private void toggleEnableDisableOtherFields(Boolean toggleStatusCombo, Boolean toggleMonthCombo, Boolean toggleyearCombo, Boolean toggleDateRange, Boolean toggleRoleCombo) {
        statusComboBox.setDisable(toggleStatusCombo);
        monthComboBox.setDisable(toggleMonthCombo);
        yearComboBox.setDisable(toggleyearCombo);
        fromDatePicker.setDisable(toggleDateRange);
        toDatePicker.setDisable(toggleDateRange);
        roleComboBox.setDisable(toggleRoleCombo);
        clearFilterSelections();
    }

    private void clearFilterSelections() {
        statusComboBox.getSelectionModel().clearSelection();
        monthComboBox.getSelectionModel().clearSelection();
        yearComboBox.getSelectionModel().clearSelection();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void loadAttendanceData() {
        masterData.clear();
        errorMessageLabel.setText("");
        System.out.println("Loading initial attendance data...");

        String userRole = sessionManager.getRole();
        int sessionUserId = sessionManager.getUserID();

        String query;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            String baseQueryCTE = "WITH RankedAttendances AS ( " +
                    "    SELECT a.User_Id, a.Attendance_Id, a.Attendance_Date, a.Time_In, a.Time_Out, a.Status, a.Remarks, " +
                    "           CONCAT(u.First_Name, ' ', u.Last_Name) AS Full_Name, u.Fathers_Name, u.Role, " +
                    "           ROW_NUMBER() OVER (PARTITION BY a.User_Id ORDER BY a.Attendance_Date DESC, a.Time_In DESC) as rn " +
                    "    FROM Attendances a " +
                    "    JOIN Users u ON a.User_Id = u.User_Id ";

            String roleFilterClause = "";
            boolean isStudentOrStaff = userRole.equals("Student") || userRole.equals("Staff") || userRole.equals("Librarian") || userRole.equals("Accountant");

            if (isStudentOrStaff) {
                roleFilterClause = "    WHERE a.User_Id = ? ";
                query = baseQueryCTE + roleFilterClause + " ) SELECT User_Id, Attendance_Id, Attendance_Date, Time_In, Time_Out, Status, Remarks, Full_Name, Fathers_Name, Role FROM RankedAttendances WHERE rn = 1 ORDER BY User_Id ASC ";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, sessionUserId);
                System.out.println("Loading data for user role: " + userRole + ", User ID: " + sessionUserId);
            } else if (userRole.equals("Teacher")) {
                roleFilterClause = "    WHERE u.Role = 'Student' OR a.User_Id = ? ";
                query = baseQueryCTE + roleFilterClause + " ) SELECT User_Id, Attendance_Id, Attendance_Date, Time_In, Time_Out, Status, Remarks, Full_Name, Fathers_Name, Role FROM RankedAttendances WHERE rn = 1 ORDER BY User_Id ASC ";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, sessionUserId);
                System.out.println("Loading data for user role: " + userRole + " (including students), User ID: " + sessionUserId);
            } else if (userRole.equals("Admin")) {
                query = baseQueryCTE + " ) SELECT User_Id, Attendance_Id, Attendance_Date, Time_In, Time_Out, Status, Remarks, Full_Name, Fathers_Name, Role FROM RankedAttendances WHERE rn = 1 ORDER BY User_Id ASC ";
                preparedStatement = connection.prepareStatement(query);
                System.out.println("Loading data for Admin role (all users).");
            } else {
                errorMessageLabel.setText("No attendance data available for your role.");
                System.out.println("No specific query path for role: " + userRole);
                return;
            }

            rs = preparedStatement.executeQuery();
            System.out.println("Query executed. Processing results...");

            while (rs.next()) {
                LocalDate attendanceDate = rs.getDate("Attendance_Date") != null ? rs.getDate("Attendance_Date").toLocalDate() : null;
                LocalTime timeIn = rs.getTime("Time_In") != null ? rs.getTime("Time_In").toLocalTime() : null;
                LocalTime timeOut = rs.getTime("Time_Out") != null ? rs.getTime("Time_Out").toLocalTime() : null;

                AttendanceTable record = new AttendanceTable(
                        rs.getInt("User_Id"),
                        rs.getString("Full_Name"),
                        rs.getString("Fathers_Name"),
                        rs.getInt("Attendance_Id"),
                        attendanceDate,
                        timeIn,
                        timeOut,
                        rs.getString("Role"),
                        rs.getString("Status"),
                        rs.getString("Remarks")
                );
                masterData.add(record);
                // System.out.println("Added record: User ID=" + record.getUserId() + ", Name=" + record.getFullName() + ", Date=" + record.getAttendanceDate() + ", Status=" + record.getStatus());
            }

            attendanceTable.setItems(masterData);
            if (masterData.isEmpty()) {
                errorMessageLabel.setText("No attendance records found.");
                System.out.println("Master data is empty after loading.");
            } else {
                errorMessageLabel.setText("");
                System.out.println("Successfully loaded " + masterData.size() + " attendance records.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                errorMessageLabel.setText("Failed to load attendance data due to a database error: " + e.getMessage());
            });
            System.err.println("SQLException during loadAttendanceData: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void applyFilteredResult(MouseEvent mouseEvent) {
        errorMessageLabel.setText("");
        System.out.println("Applying filter results...");
        ObservableList<AttendanceTable> currentFilteredList = FXCollections.observableArrayList(masterData); // Start with a fresh copy of masterData for filtering

        Integer year;
        String monthName, statusCombo, roleCombo;
        LocalDate fromDate, toDate;
        String filterdCombo = filterTypeComboBox.getSelectionModel().getSelectedItem();

        if (filterdCombo == null || filterdCombo.isEmpty()) {
            loadFrame.setMessage(errorMessageLabel, "Please select Type of filter you want to Apply from Filter By DropDown Menu", "RED");
            System.out.println("Filter type not selected.");
            return;
        }
        System.out.println("Selected filter type: " + filterdCombo);

        switch (filterdCombo) {
            case "All":
                roleCombo = roleComboBox.getSelectionModel().getSelectedItem();
                year = yearComboBox.getSelectionModel().getSelectedItem();
                fromDate = fromDatePicker.getValue();
                toDate = toDatePicker.getValue();
                monthName = monthComboBox.getSelectionModel().getSelectedItem();
                statusCombo = statusComboBox.getSelectionModel().getSelectedItem();

                if (year == null || fromDate == null || toDate == null || statusCombo == null || roleCombo == null ||
                        monthName == null || monthName.isEmpty() || statusCombo.isEmpty() || roleCombo.isEmpty()) {
                    loadFrame.setMessage(errorMessageLabel, "Please make sure all filters (Year, Date, Month, Status, Role Type) are selected.", "RED");
                    System.out.println("Not all 'All' filters selected.");
                    return;
                }

                if (fromDate.isAfter(toDate)) {
                    LocalDate swap = fromDate;
                    fromDate = toDate;
                    toDate = swap;
                    System.out.println("Swapped fromDate and toDate for 'All' filter.");
                }

                final Integer finalYear = year;
                currentFilteredList = currentFilteredList.filtered(record -> record.getAttendanceDate() != null && record.getAttendanceDate().getYear() == finalYear);
                System.out.println("Filtered by year: " + currentFilteredList.size() + " records.");

                final String finalRoleCombo = roleCombo;
                currentFilteredList = currentFilteredList.filtered(record -> record.getColRole() != null && record.getColRole().equals(finalRoleCombo));
                System.out.println("Filtered by role: " + currentFilteredList.size() + " records.");

                final LocalDate finalFromDate = fromDate;
                final LocalDate finalToDate = toDate;
                currentFilteredList = currentFilteredList.filtered(record -> {
                    LocalDate d = record.getAttendanceDate();
                    return d != null && (d.isEqual(finalFromDate) || d.isAfter(finalFromDate)) && (d.isEqual(finalToDate) || d.isBefore(finalToDate));
                });
                System.out.println("Filtered by date range: " + currentFilteredList.size() + " records.");

                final String finalStatusCombo = statusCombo;
                if (!finalStatusCombo.equalsIgnoreCase("All")) {
                    currentFilteredList = currentFilteredList.filtered(record -> record.getStatus() != null && record.getStatus().equalsIgnoreCase(finalStatusCombo));
                }
                System.out.println("Filtered by status: " + currentFilteredList.size() + " records.");

                final Month finalMonth = Month.valueOf(monthName.toUpperCase());
                currentFilteredList = currentFilteredList.filtered(record -> record.getAttendanceDate() != null && record.getAttendanceDate().getMonth() == finalMonth);
                System.out.println("Filtered by month: " + currentFilteredList.size() + " records.");

                break;

            case "By Year":
                year = yearComboBox.getSelectionModel().getSelectedItem();
                if (year == null) {
                    loadFrame.setMessage(errorMessageLabel, "Please select Year from Drop Down Menu to proceed.", "RED");
                    System.out.println("Year not selected for 'By Year' filter.");
                    return;
                }
                final Integer filterYear = year;
                currentFilteredList = currentFilteredList.filtered(record -> record.getAttendanceDate() != null && record.getAttendanceDate().getYear() == filterYear);
                System.out.println("Filtered by year (" + filterYear + "): " + currentFilteredList.size() + " records.");
                break;

            case "Date Range":
                fromDate = fromDatePicker.getValue();
                toDate = toDatePicker.getValue();

                if (fromDate == null || toDate == null) {
                    loadFrame.setMessage(errorMessageLabel, "Please choose both start and end dates.", "RED");
                    System.out.println("Dates not selected for 'Date Range' filter.");
                    return;
                }
                if (fromDate.isAfter(toDate)) { // Swap if dates are entered in wrong order
                    LocalDate temp = fromDate;
                    fromDate = toDate;
                    toDate = temp;
                    System.out.println("Swapped fromDate and toDate for 'Date Range' filter.");
                }
                final LocalDate filterFromDate = fromDate;
                final LocalDate filterToDate = toDate;
                currentFilteredList = currentFilteredList.filtered(record -> {
                    LocalDate d = record.getAttendanceDate();
                    return d != null && (d.isEqual(filterFromDate) || d.isAfter(filterFromDate)) && (d.isEqual(filterToDate) || d.isBefore(filterToDate));
                });
                System.out.println("Filtered by date range (" + filterFromDate + " to " + filterToDate + "): " + currentFilteredList.size() + " records.");
                break;

            case "By Month":
                monthName = monthComboBox.getSelectionModel().getSelectedItem();
                if (monthName == null || monthName.isEmpty()) {
                    loadFrame.setMessage(errorMessageLabel, "Select Month Name to Proceed with filtering processes.", "RED");
                    System.out.println("Month not selected for 'By Month' filter.");
                    return;
                }
                if (monthName.equalsIgnoreCase("All Months")) {
                    System.out.println("Filtering for 'All Months'. No further filtering applied.");
                    // currentFilteredList already contains masterData
                } else {
                    try {
                        Month selectedMonth = Month.valueOf(monthName.toUpperCase());
                        currentFilteredList = currentFilteredList.filtered(record -> record.getAttendanceDate() != null && record.getAttendanceDate().getMonth() == selectedMonth);
                        System.out.println("Filtered by month (" + monthName + "): " + currentFilteredList.size() + " records.");
                    } catch (IllegalArgumentException e) {
                        errorMessageLabel.setText("Invalid month selected: " + monthName);
                        System.err.println("Invalid month name for 'By Month' filter: " + monthName + " - " + e.getMessage());
                        return;
                    }
                }
                break;

            case "By Status":
                statusCombo = statusComboBox.getSelectionModel().getSelectedItem();
                if (statusCombo == null || statusCombo.isEmpty()) {
                    loadFrame.setMessage(errorMessageLabel, "Please Select Status from Status Box to proceed Filtering process.", "RED");
                    System.out.println("Status not selected for 'By Status' filter.");
                    return;
                }
                if (!statusCombo.equals("All")) {
                    final String filterStatusCombo = statusCombo;
                    currentFilteredList = currentFilteredList.filtered(record -> record.getStatus() != null && record.getStatus().equalsIgnoreCase(filterStatusCombo));
                }
                System.out.println("Filtered by status (" + statusCombo + "): " + currentFilteredList.size() + " records.");
                break;

            case "By Role Type":
                roleCombo = roleComboBox.getSelectionModel().getSelectedItem();
                if (roleCombo == null || roleCombo.isEmpty()) {
                    loadFrame.setMessage(errorMessageLabel, "Please select Role Type to proceed.", "RED");
                    System.out.println("Role type not selected for 'By Role Type' filter.");
                    return;
                }
                final String filterRoleCombo = roleCombo;
                currentFilteredList = currentFilteredList.filtered(record -> record.getColRole() != null && record.getColRole().equals(filterRoleCombo));
                System.out.println("Filtered by role type (" + roleCombo + "): " + currentFilteredList.size() + " records.");
                break;
        }
        if (currentFilteredList.isEmpty()) {
            errorMessageLabel.setText("No records found matching the filter criteria.");
            System.out.println("No records found after applying filter.");
        } else {
            errorMessageLabel.setText("Filter applied successfully. " + currentFilteredList.size() + " records found.");
            System.out.println("Filter applied. Showing " + currentFilteredList.size() + " records.");
        }
        attendanceTable.setItems(currentFilteredList); // Set the filtered list to the table
    }


    public void handleCancel(ActionEvent actionEvent) {
        System.out.println("Cancel button clicked. Resetting UI...");
        clearAttendanceFields();
        attendanceTable.setItems(masterData); // Reset to show all data
        filterTypeComboBox.getSelectionModel().clearSelection();
        enableAllFields(); // Re-enable all filters
        errorMessageLabel.setText("");
        markedAttendanceRecords.clear(); // Clear marked records
        System.out.println("UI reset complete. Marked attendance records cleared.");
    }

    private void clearAttendanceFields() {
        attendanceIdField.setText("");
        userIdField.setText("");
        dateField.setText("");
        timeInField.setText("");
        timeOutField.setText("");
        fullNameField.setText("");
        fathersNameField.setText("");
        remarksField.setText("");
        attendanceStatusComboBox.getSelectionModel().clearSelection();
        usersImage.setImage(null); // Clear image
        System.out.println("Attendance input fields cleared.");
    }

    @FXML
    public void handleMarkAllAttendance(ActionEvent actionEvent) {
        errorMessageLabel.setText("");
        System.out.println("handleMarkAllAttendance() called.");

        if (markedAttendanceRecords.isEmpty()) {
            loadFrame.setMessage(errorMessageLabel, "No attendance records have been marked to save.", "RED");
            System.out.println("No records in markedAttendanceRecords. Exiting.");
            return;
        }

        System.out.println("Attempting to save " + markedAttendanceRecords.size() + " marked attendance records.");

        // Use a background task to perform database operations
        Task<Void> markAttendanceTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Integer> failedUserIds = new ArrayList<>();
                int successCount = 0;
                boolean originalAutoCommit = true; // Default to true

                try {
                    // Store original auto-commit state and set to false for transaction
                    if (connection != null && !connection.isClosed()) {
                        originalAutoCommit = connection.getAutoCommit();
                        if (originalAutoCommit) {
                            connection.setAutoCommit(false);
                            System.out.println("Auto-commit temporarily set to FALSE for transaction.");
                        }
                    } else {
                        System.err.println("Database connection is closed or null. Cannot perform transaction.");
                        Platform.runLater(() -> loadFrame.setMessage(errorMessageLabel, "Database connection error. Cannot save attendance.", "RED"));
                        return null; // Exit task early
                    }

                    System.out.println("Starting background task to save attendance.");

                    for (AttendanceTable record : markedAttendanceRecords.values()) {
                        System.out.println("Processing attendance for User ID: " + record.getUserId() + ", Name: " + record.getFullName());
                        try {
                            insertOrUpdateAttendance(record);
                            successCount++;
                            System.out.println("Successfully processed User ID: " + record.getUserId());
                        } catch (SQLException e) {
                            failedUserIds.add(record.getUserId());
                            System.err.println("Failed to save attendance for User ID: " + record.getUserId() + " - " + e.getMessage());
                            e.printStackTrace(); // Print full stack trace for debugging
                            // Do NOT re-throw here if you want to continue processing other records
                            // The error is captured in failedUserIds
                        }
                    }
                    attendanceTable.refresh();
                    if (failedUserIds.isEmpty()) {
                        if (connection != null && !connection.isClosed()) {
                            connection.commit(); // Explicitly commit if all successful
                            System.out.println("All transactions committed successfully.");
                        }
                    } else {
                        if (connection != null && !connection.isClosed()) {
                            connection.rollback(); // Rollback if any failed
                            System.err.println("Rolled back transactions due to failures.");
                        }
                    }

                } catch (SQLException e) {
                    System.err.println("SQLException during transaction setup or commit/rollback: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        if (connection != null && !connection.isClosed()) {
                            connection.rollback(); // Ensure rollback on outer exception
                            System.err.println("Rolled back due to critical SQLException.");
                        }
                    } catch (SQLException ex) {
                        System.err.println("Error during rollback after critical SQLException: " + ex.getMessage());
                    }
                    // Re-throw to be caught by setOnFailed if this is a fatal error for the task
                    throw e;
                } finally {
                    // Always restore original auto-commit state
                    try {
                        if (connection != null && !connection.isClosed()) {
                            connection.setAutoCommit(originalAutoCommit);
                            System.out.println("Auto-commit restored to " + originalAutoCommit + ".");
                        }
                    } catch (SQLException e) {
                        System.err.println("Error restoring auto-commit: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                int finalSuccessCount = successCount;
                Platform.runLater(() -> {
                    System.out.println("Database operations complete. Updating UI.");
                    if (failedUserIds.isEmpty()) {
                        loadFrame.setMessage(errorMessageLabel, "All " + finalSuccessCount + " marked attendances have been successfully saved!", "GREEN");
                        System.out.println("All marked attendances saved successfully.");
                    } else {
                        String idsString = failedUserIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                        loadFrame.setMessage(errorMessageLabel, "Warning: Failed to save attendance for " + failedUserIds.size() + " User IDs: " + idsString + ". Please check console for details.", "RED");
                        System.err.println("Errors occurred for User IDs: " + idsString);
                    }
                    markedAttendanceRecords.clear(); // Clear the map after saving attempts
                    attendanceTable.refresh();

                    clearAttendanceFields();
                    System.out.println("UI reloaded and fields cleared after saving attendance.");
                });
                return null;
            }
        };
        // Handle failure of the entire task
        markAttendanceTask.setOnFailed(event -> {
            Throwable e = markAttendanceTask.getException();
            System.err.println("Critical error in markAttendanceTask: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                loadFrame.setMessage(errorMessageLabel, "An unexpected error occurred during attendance marking: " + e.getMessage() + ". See console.", "RED");
            });
        });
        new Thread(markAttendanceTask).start();
        System.out.println("Background task for marking attendance started.");
    }


    private void insertOrUpdateAttendance(AttendanceTable record) throws SQLException {
        String query;
        LocalDate attendanceDate = record.getAttendanceDate(); // Use the date from the record to mark attendance for
        LocalTime timeIn = record.getTimeIn();
        LocalTime timeOut = record.getTimeOut();
        int userID = record.getUserId();
        // Use the status and remarks from the 'record' object, not the UI fields.
        // The UI fields (`attendanceStatusComboBox.getValue()` and `remarksField.getText()`)
        // only reflect the *last clicked* user's settings, not the marked state of all users in the map.
        String status = record.getStatus();
        String remarks = record.getRemarks();


        System.out.println("Attempting to save/update attendance for User ID: " + userID + " on Date: " + attendanceDate + " with Status: " + status + " and Remarks: " + remarks);

        if (checkTodaysAttendanceExists(userID, attendanceDate)) {
            query = "UPDATE Attendances SET Time_In = ?, Time_Out = ?, Status = ?, Remarks = ? WHERE User_Id = ? AND Attendance_Date = ?";
            System.out.println("Executing UPDATE query for User ID: " + userID + " on Date: " + attendanceDate);
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, timeIn != null ? timeIn.toString() : "00:00:00"); // Ensure not null
                pstmt.setString(2, timeOut != null ? timeOut.toString() : "00:00:00"); // Ensure not null
                pstmt.setString(3, status);
                pstmt.setString(4, remarks);
                pstmt.setInt(5, userID);
                pstmt.setString(6, attendanceDate.toString());
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("UPDATE for User ID " + userID + " on " + attendanceDate + ": " + rowsAffected + " rows affected.");
            }
        } else {
            query = "INSERT INTO Attendances (User_Id, Attendance_Date, Time_In, Time_Out, Status, Remarks) VALUES (?,?,?,?,?,?)";
            System.out.println("Executing INSERT query for User ID: " + userID + " on Date: " + attendanceDate);
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, userID);
                pstmt.setString(2, attendanceDate.toString());
                pstmt.setString(3, timeIn != null ? timeIn.toString() : "00:00:00"); // Ensure not null
                pstmt.setString(4, timeOut != null ? timeOut.toString() : "00:00:00"); // Ensure not null
                pstmt.setString(5, status);
                pstmt.setString(6, remarks);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("INSERT for User ID " + userID + " on " + attendanceDate + ": " + rowsAffected + " rows affected.");
            }
        }
    }

    private boolean checkTodaysAttendanceExists(int userID, LocalDate date) throws SQLException {
        String query = "SELECT COUNT(*) FROM Attendances WHERE Attendance_Date = ? AND User_Id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, date.toString());
            pstmt.setInt(2, userID);
            System.out.println("Checking if attendance exists for User ID: " + userID + " on Date: " + date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Existence check for User ID " + userID + " on " + date + ": Count = " + count);
                    return count > 0;
                }
            }
        }
        return false;
    }
}