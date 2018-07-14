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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class CustomerController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField address2TextField;
    @FXML
    private TextField postalCodeTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private ComboBox locationComboBox;

    boolean isUpdate;
    private String errorMessage = "";

    List<Integer> cityIds;
    private int selectedCustomerAddressId;
    private int selectedCustomerId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (isUpdate) {
            setCustomerDetails(MainController.getSelectedCustomer());
        }
        btnSave.setOnAction(this::saveButtonPressed);
        btnCancel.setOnAction(this::cancelButtonPressed);
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

    public void cancelButtonPressed(ActionEvent event) {
       goToMain();
    }

    public void saveButtonPressed(ActionEvent event) {
        int id = selectedCustomerId;
        String name = nameTextField.getText();
        String address = addressTextField.getText();
        String address2 = address2TextField.getText();
        String postalCode = postalCodeTextField.getText();
        String phone = phoneTextField.getText();
        String selectedLocation = (String) locationComboBox.getSelectionModel().getSelectedItem();
        String[] parsedLocation = selectedLocation.split(Pattern.quote("-"));
        String city = parsedLocation[1].trim();
        int cityId = getCityIdFromCityName(city);

        /* [2,3,1] */
        /* ["Australia - Melbourne", "France - Paris", "USA - New York"] */
        DBManager.executeInTransaction((conn) -> {
            if (validateCustomer()) {
                if (isUpdate) {
                    updateExistingCustomer(name, address, address2, postalCode, phone, cityId, selectedCustomerAddressId, selectedCustomerId);
                } else {
                    createNewCustomer(name, address, address2, postalCode, phone, cityId);
                }
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
//             Show the error message.
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Entries");
                alert.setHeaderText("Please correct the entries for this customer.");
                alert.setContentText(errorMessage);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    errorMessage = "";
                    alert.close();
                }
            }
        });
    }

    private int getCityIdFromCityName(String name) {
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement("SELECT cityid FROM city WHERE city = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getInt("cityid");
            }
        } catch (SQLException ew) {
            ew.printStackTrace();
        }

        return -1;

    }

    private boolean updateExistingCustomer(String name, String address, String address2, String postalCode, String phone, int cityId, int selectedCustomerAddressId, int selectedCustomerId) {
        String updateAddressQuery = "UPDATE address "
                + "SET address.address = ?, address.address2 = ?, address.cityId = ?, address.postalCode = ?, "
                + "address.phone = ?, address.lastUpdateBy = ? WHERE address.addressid = ?";
        String updateCustomerQuery = "UPDATE customer "
                + "SET customerName = ?, active = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? "
                + "WHERE customerId = ?";
        try {
            PreparedStatement updateAddressStatement = DBManager.getConnection().prepareStatement(updateAddressQuery);
            //TODO setstatment to set columns for ?s execute
            updateAddressStatement.setString(1, address);
            updateAddressStatement.setString(2, address2);
            updateAddressStatement.setInt(3, cityId);
            updateAddressStatement.setString(4, postalCode);
            updateAddressStatement.setString(5, phone);
            updateAddressStatement.setString(6, LoginController.currentUser);
            updateAddressStatement.setInt(7, selectedCustomerAddressId);
            //update customer table and commit transaction and/or rollback if it fails
            if (updateAddressStatement.executeUpdate() > 0) {
                PreparedStatement updateCustomerStatement = DBManager.getConnection().prepareStatement(updateCustomerQuery);
                updateCustomerStatement.setString(1, name);
                updateCustomerStatement.setInt(2, 1);
                updateCustomerStatement.setString(3, LoginController.currentUser);
                updateCustomerStatement.setInt(4, selectedCustomerId);
                if (updateCustomerStatement.executeUpdate() > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createNewCustomer(String name, String address, String address2, String postalCode, String phone, int cityId) throws SQLException {
        int addressId;
        PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)",
                Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, address);
        statement.setString(2, address2);
        statement.setInt(3, cityId);
        statement.setString(4, postalCode);
        statement.setString(5, phone);
        statement.setString(6, LoginController.currentUser);
        statement.setString(7, LoginController.currentUser);
        statement.execute();

        try (ResultSet results = statement.getGeneratedKeys()) {
            results.next();
            addressId = results.getInt(1);
        }

        statement = DBManager.getConnection()
                .prepareStatement("INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy)"
                        + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
        statement.setString(1, name);
        statement.setInt(2, addressId);
        statement.setInt(3, 1);
        statement.setString(4, LoginController.currentUser);
        statement.setString(5, LoginController.currentUser);
        statement.execute();
    }

    public void setCustomerDetails(Customer selectedCustomer) {
        isUpdate = true;
        nameTextField.setText(selectedCustomer.getCustomerName());
        addressTextField.setText(selectedCustomer.getAddress());
        address2TextField.setText(selectedCustomer.getAddress2());
        phoneTextField.setText(selectedCustomer.getPhone());
        postalCodeTextField.setText(selectedCustomer.getPostalCode());
        locationComboBox.getSelectionModel().select(selectedCustomer.getCountry() + " - " + selectedCustomer.getCity().getCityName());
        selectedCustomerId = selectedCustomer.getCustomerId();
        selectedCustomerAddressId = selectedCustomer.getAddressId();
    }

    private boolean validateCustomer() {
        String selectedLocation = (String) locationComboBox.getSelectionModel().getSelectedItem();
        String[] parsedLocation = selectedLocation.split(Pattern.quote("-"));
        String city = parsedLocation[1].trim();
        int cityId = getCityIdFromCityName(city);


        String name = nameTextField.getText();
        String address = addressTextField.getText();
        String location = (String) locationComboBox.getSelectionModel().getSelectedItem();
        String zip = postalCodeTextField.getText();
        String phone = phoneTextField.getText();


        //first checks to see if inputs are null
        if (name.isEmpty()) {
            errorMessage += "Please enter a name for the customer.\n";
        }
        if (address.isEmpty()) {
            errorMessage += "Please enter an address for the customer.\n";
        }
        if (location.equals("-")) {
            errorMessage += "Please choose a location for the customer.\n";
        }
        if (zip.isEmpty()) {
            errorMessage += "Please enter the postal code for the customer\n";
        }
        //Authors note:
        //There were other measures to validate postal codes, but given that this is
        //an international program, and with postal codes that vary wildly between different
        //countries (eg: US: "84016-9642" and UK: "SW1W 0NY", as long as there is
        // SOMETHING in the field, I have counted it as a valid postal code.
        if (phone.isEmpty()) {
            errorMessage += "Please enter a 10-digit number for the customer.\n";
        } else if (phone.length() < 10 || phone.length() > 15) {
            errorMessage += "The phone number must be 10-15 digits long.\n";
        } else if(!phone.matches("\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}")){
            errorMessage += "Please only use numbers, hyphens, and/or parenthesis for the phone number.";
        }
        return errorMessage.isEmpty();
    }


    private void goToMain() {
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
}