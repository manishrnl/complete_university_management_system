package org.example.complete_ums.ToolsClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class SessionManager {
    private static SessionManager instance;
    private int UserID;
    private String UserName, firstName, lastName, roleType, Password_Hash, email, pan, aadhar, phone, address, country;
    private Date DOB;

    public SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getUserID() {
        return UserID;
    }

    public String getUserName() {
        return UserName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setUserID(int UserID) {
        this.UserID = UserID;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;

    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRole() {
        return roleType;
    }

    public void setRole(String roleType) {
        this.roleType = roleType;
    }

    public String getPassword() {
        return Password_Hash;
    }

    public void setPassword(String Password_Hash) {
        this.Password_Hash = Password_Hash;
    }

    public String getMobile() {
        return null;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public Date getDOB() {
        return this.DOB;
    }

    public void clearAll() {

        // 🔹 Clear all session data from memory
        this.UserID = 0;
        this.UserName = null;
        this.firstName = null;
        this.lastName = null;
        this.roleType = null;
        this.Password_Hash = null;
        this.email = null;
        this.pan = null;
        this.aadhar = null;
        this.phone = null;
        this.address = null;
        this.country = null;
        this.DOB = null;

        System.out.println("🧹 Session cleared in memory, and data saved to properties file.");
    }

    public void setIntoProperties() {
        Properties props = new Properties();

        // Load existing properties if available
        try (FileInputStream fis = new FileInputStream("LoginDetails.properties")) {
            props.load(fis);
        } catch (Exception ex) {
            System.out.println("Could not load existing properties, will create new one.");
        }

        // 🔹 Update all session-related properties
        props.setProperty("userIdLoggedIn", String.valueOf(this.UserID));
        props.setProperty("userNameLoggedIn", this.UserName != null ? this.UserName : "");
        props.setProperty("firstNameLoggedIn", this.firstName != null ? this.firstName : "");
        props.setProperty("lastNameLoggedIn", this.lastName != null ? this.lastName : "");
        props.setProperty("fullNameLoggedIn", getFullName() != null ? getFullName() : "");
        props.setProperty("roleLoggedIn", this.roleType != null ? this.roleType : "");
        props.setProperty("passwordLoggedIn", this.Password_Hash != null ? this.Password_Hash : "");
        props.setProperty("emailLoggedIn", this.email != null ? this.email : "");
        props.setProperty("panLoggedIn", this.pan != null ? this.pan : "");
        props.setProperty("aadharLoggedIn", this.aadhar != null ? this.aadhar : "");
        props.setProperty("phoneLoggedIn", this.phone != null ? this.phone : "");
        props.setProperty("mobileLoggedIn", this.phone != null ? this.phone : ""); // alias for phone
        props.setProperty("addressLoggedIn", this.address != null ? this.address : "");
        props.setProperty("countryLoggedIn", this.country != null ? this.country : "");
        props.setProperty("DOBLoggedIn", this.DOB != null ? this.DOB.toString() : "");

        // 🔹 Save all updated data back to properties file
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream("LoginDetails.properties")) {
            props.store(fos, "Updated session info");
            System.out.println("✅ Session details saved successfully to LoginDetails.properties.");
        } catch (Exception ex) {
            throw new RuntimeException("Error saving LoginDetails.properties", ex);
        }

    }

    public void clearPropertiesData() {
        File file = new File("LoginDetails.properties");

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("🗑️ LoginDetails.properties deleted successfully.");
            } else {
                System.out.println("⚠️ Failed to delete LoginDetails.properties.");
            }
        } else {
            System.out.println("ℹ️ No properties file found to delete.");
        }
    }

    public void loadFromProperties() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("LoginDetails.properties")) {
            props.load(fis);

            this.UserID = Integer.parseInt(props.getProperty("userIdLoggedIn", "0"));
            this.UserName = props.getProperty("userNameLoggedIn", "");
            this.firstName = props.getProperty("firstNameLoggedIn", "");
            this.lastName = props.getProperty("lastNameLoggedIn", "");
            this.roleType = props.getProperty("roleLoggedIn", "");
            this.Password_Hash = props.getProperty("passwordLoggedIn", "");
            this.email = props.getProperty("emailLoggedIn", "");
            this.pan = props.getProperty("panLoggedIn", "");
            this.aadhar = props.getProperty("aadharLoggedIn", "");
            this.phone = props.getProperty("phoneLoggedIn", "");
            this.address = props.getProperty("addressLoggedIn", "");
            this.country = props.getProperty("countryLoggedIn", "");

            // Handle DOB safely
            String dobStr = props.getProperty("DOBLoggedIn", "");
            if (!dobStr.isEmpty()) {
                try {
                    this.DOB = new Date(dobStr);
                } catch (Exception e) {
                    System.out.println("Could not parse DOB from properties: " + dobStr);
                    this.DOB = null;
                }
            } else {
                this.DOB = null;
            }

            System.out.println("✅ Session loaded from properties successfully.");
        } catch (IOException e) {
            System.out.println("⚠️ Could not load session properties. Starting fresh.");
        }
    }


}
