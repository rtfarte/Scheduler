/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Customer;
import util.DBManager;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * FXML Controller class
 *
 * @author mcken
 */
public class AppointmentController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private ComboBox<String> titleComboBox;
    @FXML
    private ComboBox<String> descriptionComboBox;
    @FXML
    private ComboBox locationComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> startTimeHourComboBox;
    @FXML
    private ComboBox<String> startTimeMinuteComboBox;
    @FXML
    private ComboBox<String> endTimeHourComboBox;
    @FXML
    private ComboBox<String> endTimeMinuteComboBox;
    @FXML
    private ComboBox consultantComboBox;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private int appointmentId;
    private Customer currentCustomer;
    List<Integer> cityIds;
    List<Integer> customerIds;
    private SimpleDateFormat utcDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean isUpdate;
    int offsetSeconds = ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fillTitle();
        fillDescription();
        fillHours();
        fillMinutes();
        fillLocations();
        fillConsultant();
        btnCancel.setOnAction(this::cancelButtonPressed);
        btnSave.setOnAction(this::saveButtonPressed);
//        if (isUpdate){
//            setAppointmentDetails(MainController.getSelectedAppointment());
//        }
    }

    private void fillTitle() {
        titleComboBox.getItems().addAll("Meeting", "Consultation");
    }

    private void fillDescription() {
        descriptionComboBox.getItems().addAll("First Meeting", "Follow-Up", "Close-Account");
    }

    private void fillHours() {

        startTimeHourComboBox.getItems().addAll(
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16"
        );
        endTimeHourComboBox.getItems().addAll(
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17"
        );
    }

    private void fillMinutes() {
        startTimeMinuteComboBox.getItems().addAll(
                "00",
                "15",
                "30",
                "45"
        );
        endTimeMinuteComboBox.getItems().addAll(
                "00",
                "15",
                "30",
                "45"
        );
    }

//    private void fillContacts() {
//        String contactsQuery =
//                "SELECT customerid, customerName " +
//                        "FROM customer " +
//                        "ORDER BY customerid";
//        try {
//            PreparedStatement statement = DBManager.getConnection().prepareStatement(contactsQuery);
//            try (ResultSet results = statement.executeQuery();) {
//                customerIds = new ArrayList<>();
//                while (results.next()) {
//                    final int id = results.getInt("customerid");
//                    final String display = results.getString("customerid") + " : " + results.getString("customerName");
//
//                    customerIds.add(id);
//                    customerComboBox.getItems().add(display);
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println("SQL cust query error: " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e2) {
//            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
//        }
//    }

    private void fillLocations() {
        String query = "SELECT city.cityId, city.city, country.country FROM city JOIN country ON city.countryId = country.countryId ORDER BY country,city";
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(query);
            try (ResultSet results = statement.executeQuery();) {
                cityIds = new ArrayList<>();
                while (results.next()) {
                    final int id = results.getInt("cityId");
                    final String display = results.getString("country") + " - " + results.getString("city");

                    cityIds.add(id);
                    locationComboBox.getItems().add(display);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL cust query error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e2) {
            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
        }
    }

    private void fillConsultant() {
        consultantComboBox.getItems().addAll("Tom", "Dick", "Harry");
    }

    private void cancelButtonPressed(ActionEvent event) {
        Parent tableViewParent = null;
        try {
            tableViewParent = FXMLLoader.load(getClass().getResource("Main.fxml"));
        } catch (IOException e) {
            return;
        }
        Scene tableViewScene = new Scene(tableViewParent);
        Stage window = (Stage) root.getScene().getWindow();
        window.setScene(tableViewScene);
        window.show();
    }

    private void saveButtonPressed(ActionEvent event) {

        //parse location
        String selectedLocation = (String) locationComboBox.getSelectionModel().getSelectedItem();
        String[] parsedLocation = selectedLocation.split(Pattern.quote("-"));
        String city = parsedLocation[1].trim();

        //appointment fields
        int customerId = currentCustomer.getCustomerId();
        String title = titleComboBox.getValue();
        String description = descriptionComboBox.getValue();
        int cityId = getCityIdFromCityName(city);
        String contact = consultantComboBox.getValue().toString();
        String startHour = startTimeHourComboBox.getValue();
        String startMinute = startTimeMinuteComboBox.getValue();
        String endHour = endTimeHourComboBox.getValue();
        String endMinute = endTimeMinuteComboBox.getValue();
        String user = LoginController.currentUser;
        DatePicker selectedDate = datePicker;

//        update or new logic
        DBManager.executeInTransaction((conn) -> {
            if (isUpdate) {
                updateExistingAppointment(title, description, cityId, contact, startHour, startMinute, endHour, endMinute, appointmentId, customerId);
            } else {
                createNewAppointment(customerId, title, description, cityId, contact, startHour, startMinute, endHour, endMinute, user, selectedDate);
            }
        });

        //Return to main
        Parent tableViewParent = null;
        try {
            tableViewParent = FXMLLoader.load(getClass().getResource("Main.fxml"));
        } catch (IOException e) {
            return;
        }
        Scene tableViewScene = new Scene(tableViewParent);
        Stage window = (Stage) root.getScene().getWindow();
        window.setScene(tableViewScene);
        window.show();
    }

    private void createNewAppointment(int customerId, String title, String description, int cityId, String contact, String startHour, String startMinute, String endHour, String endMinute, String user, DatePicker selectedDate) {
        int addressId;
        String startDateTime = convertToUTC(datePicker, startHour, startMinute);
        String endDateTime = convertToUTC(datePicker, endHour, endMinute);

        String query = "INSERT INTO appointment (customerId, title, description, location, contact, start, end, createDate, createdBy, lastUpdate, lastUpdateBy, url)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)";
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement((query));
            statement.setInt(1, customerId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setInt(4, cityId);
            statement.setString(5, contact);
            statement.setString(6, startDateTime);
            statement.setString(7, endDateTime);
            statement.setString(8, LoginController.currentUser);
            statement.setString(9, LoginController.currentUser);
            statement.setString(10, "");

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void updateExistingAppointment(String title, String description, int cityId, String contact, String startHour, String startMinute, String endHour, String endMinute, int selectedAddressLocationId, int selectedAppointmentCustomerId) {
        String startDateTime = convertToUTC(datePicker, startHour, startMinute);
        String endDateTime = convertToUTC(datePicker, endHour, endMinute);

        String query = "UPDATE appointment SET customerId = ?, title = ?, description = ?, location = ?, contact = ?, start = ?, end = ?,  lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy =?, url = ? " +
                "WHERE appointmentid = ?";
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement((query));
            statement.setInt(1, selectedAppointmentCustomerId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setInt(4, cityId);
            statement.setString(5, contact);
            statement.setString(6, startDateTime);
            statement.setString(7, endDateTime);
            statement.setString(8, LoginController.currentUser);
            statement.setString(9, "");
            statement.setInt(10, selectedAddressLocationId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private int getCityIdFromCityName(String name) {
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement("SELECT cityid FROM city WHERE city = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getInt("cityid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getCustomerIdFromName(String name) {
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement("SELECT customerid FROM customer WHERE customer = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getInt("customerid");
            }
        } catch (SQLException ew) {
            ew.printStackTrace();
        }
        return -1;
    }

    public void setCustomerDetails(Customer customer) {
        currentCustomer = customer;
    }

    private String convertToUTC(DatePicker datePicker, String hour, String minute) {
        ZonedDateTime dateTimeLocalTimeZone = datePicker.getValue().atTime(Integer.parseInt(hour), Integer.parseInt(minute)).atZone(TimeZone.getDefault().toZoneId());
        Date utcDate = Date.from(dateTimeLocalTimeZone.toInstant());
        utcDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcDateTimeFormatter.format(utcDate);
    }

    public void setAppointmentDetails(int appointmentIdToUpdate) {
        appointmentId = appointmentIdToUpdate;
        SimpleDateFormat pickerHour = new SimpleDateFormat("HH");
        SimpleDateFormat pickerMinute = new SimpleDateFormat("mm");

        isUpdate = true;
        String query = "SELECT * FROM appointment WHERE appointmentid = ?";
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(query);
            statement.setInt(1, appointmentIdToUpdate);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                titleComboBox.getSelectionModel().select(rs.getString("title"));
                descriptionComboBox.getSelectionModel().select(rs.getString("description"));
                locationComboBox.getSelectionModel().select(getAppointmentLocation(rs.getString("location")));
                Date startDate = Date.from(rs.getTimestamp("start").toLocalDateTime().minusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId()).toInstant());
                ZonedDateTime endDate = rs.getTimestamp("end").toLocalDateTime().minusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId());
                datePicker.setValue(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                startTimeHourComboBox.getSelectionModel().select(pickerHour.format(startDate));
                startTimeMinuteComboBox.getSelectionModel().select(pickerMinute.format(startDate));
                endTimeHourComboBox.getSelectionModel().select(DateTimeFormatter.ofPattern("HH").format(endDate));
                endTimeMinuteComboBox.getSelectionModel().select(DateTimeFormatter.ofPattern("mm").format(endDate));
                consultantComboBox.getSelectionModel().select(rs.getString("contact"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getAppointmentLocation(String locationId) {
        String locationQuery = "SELECT CONCAT(country, ' - ' , city) FROM city JOIN country ON city.countryId = country.countryid WHERE city.cityid = ?";
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(locationQuery);
            statement.setString(1, locationId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
