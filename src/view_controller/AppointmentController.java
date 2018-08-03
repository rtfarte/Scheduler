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
import javafx.scene.control.*;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * FXML Controller class
 *
 * @author mcken
 */
public class AppointmentController implements Initializable {

    private static final String DEFAULT_LOCATION = " - ";

    @FXML
    private AnchorPane root;
    @FXML
    private ComboBox<String> titleComboBox;
    @FXML
    private ComboBox<String> descriptionComboBox;
    @FXML
    private ComboBox<String> locationComboBox;
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
    private ComboBox<String> consultantComboBox;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private String errorMessage = "";

    private int appointmentId;
    private Customer currentCustomer;
    List<Integer> cityIds;
    List<Integer> customerIds;
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean isUpdate;

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
        if (selectedLocation == null) {
            selectedLocation = DEFAULT_LOCATION;
        }
        String[] parsedLocation = selectedLocation.split(Pattern.quote("-"));
        String city = parsedLocation[1].trim();

        //appointment fields
        int customerId = currentCustomer.getCustomerId();
        String title = titleComboBox.getValue();
        String description = descriptionComboBox.getValue();
        int cityId = getCityIdFromCityName(city);
        String contact = (String) consultantComboBox.getValue();
        String startHour = startTimeHourComboBox.getValue();
        String startMinute = startTimeMinuteComboBox.getValue();
        String endHour = endTimeHourComboBox.getValue();
        String endMinute = endTimeMinuteComboBox.getValue();
        String user = LoginController.currentUser;
        DatePicker selectedDate = datePicker;

//        update or new logic
        DBManager.executeInTransaction((conn) -> {
            if (validateAppointment()) {
                if (isUpdate) {
                    updateExistingAppointment(title, description, cityId, contact, startHour, startMinute, endHour, endMinute, appointmentId, customerId);
                } else {
                    createNewAppointment(customerId, title, description, cityId, contact, startHour, startMinute, endHour, endMinute, user, selectedDate);
                }
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
            } else {
                // Show the error message.
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Entries");
                alert.setHeaderText("Please correct the entries for this appointment.");
                alert.setContentText(errorMessage);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    errorMessage = "";
                    alert.close();
                }
            }
        });
    }

    private void createNewAppointment(int customerId, String title, String description, int cityId, String contact, String startHour, String startMinute, String endHour, String endMinute, String user, DatePicker selectedDate) {
        int addressId;
        if (validateAppointment()) {
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
        } else {

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
        return DATE_TIME_FORMAT.format(utcDate);
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
                Date startDate = DATE_TIME_FORMAT.parse(rs.getString("start"));
                Date endDate = DATE_TIME_FORMAT.parse(rs.getString("end"));
                datePicker.setValue(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                startTimeHourComboBox.getSelectionModel().select(pickerHour.format(startDate));
                startTimeMinuteComboBox.getSelectionModel().select(pickerMinute.format(startDate));
                endTimeHourComboBox.getSelectionModel().select(DateTimeFormatter.ofPattern("HH").format(endDate.toInstant().atZone(ZoneId.systemDefault())));
                endTimeMinuteComboBox.getSelectionModel().select(DateTimeFormatter.ofPattern("mm").format(endDate.toInstant().atZone(ZoneId.systemDefault())));
                consultantComboBox.getSelectionModel().select(rs.getString("contact"));
            }

        } catch (Exception e) {
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

    private boolean validateAppointment() throws NullPointerException {
        String title = titleComboBox.getValue();
        String description = descriptionComboBox.getValue();
        LocalDate localDate = datePicker.getValue();
        String startHour = startTimeHourComboBox.getValue();
        String startMinute = startTimeMinuteComboBox.getValue();
        String endHour = endTimeHourComboBox.getValue();
        String endMinute = endTimeMinuteComboBox.getValue();
//
//        //first checks to see if inputs are null

        List<String> errors = new ArrayList<>();

        if (title == null || title.length() == 0) {
            errors.add("Please enter an appointment title.");
        }
        if (description == null || description.length() == 0) {
            errors.add("Please select an appointment description.");
        }
        boolean datePresent = localDate != null;
        if (!datePresent) {
            errors.add("Please select a date for this appointment.");
        }
        if (locationComboBox.getValue() == null){
            errors.add("Please select a location for this appointment");
        }
        if (startTimeHourComboBox.getValue() == null || startTimeMinuteComboBox == null) {
            errors.add("Please select a valid start time.");
        }
        if (endTimeHourComboBox.getValue() == null || endTimeMinuteComboBox.getValue() == null) {
            errors.add("Please select an valid end time.");
        } else if ((Integer.parseInt(startTimeHourComboBox.getValue() + Integer.parseInt(startTimeMinuteComboBox.getValue()))) >= (Integer.parseInt(endTimeHourComboBox.getValue() + Integer.parseInt(endTimeMinuteComboBox.getValue())))) {
            errors.add("Your appointment start and end times are invalid.");
        }

        if (consultantComboBox.getValue() == null) {
            errors.add("Please select a consultant for this appointment");
        } else if (errors.isEmpty()) {

            String startDateTime = convertToUTC(datePicker, startHour, startMinute);
            String endDateTime = convertToUTC(datePicker, endHour, endMinute);

            //checks user's existing appointments for time conflicts
            try {
                if (hasAppointmentConflict(startDateTime, endDateTime)){
                    errors.add("Appointment times conflict with Consultant's existing appointments. Please select a new time.");
                }
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }

        errorMessage = String.join("\n", errors);

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasAppointmentConflict(String newStart, String newEnd) throws SQLException {
//        int apptID;
        String consultant = consultantComboBox.getValue();
        System.out.println("ApptID: " + this.appointmentId);

        try{
            PreparedStatement pst = DBManager.getConnection().prepareStatement(
                    "SELECT * FROM appointment "
                    + "WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < start AND ? > end) "
                    + "AND (createdBy = ? AND appointmentID != ?)");
            pst.setString(1, newStart);
            pst.setString(2, newEnd);
            pst.setString(3, newStart);
            pst.setString(4, newEnd);
            pst.setString(5, consultant);
            pst.setInt(6, appointmentId);
            ResultSet rs = pst.executeQuery();

            if(rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Check your SQL");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
            e.printStackTrace();
        }
        return false;
    }

}
