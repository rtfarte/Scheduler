/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Appointment;
import model.City;
import model.Customer;
import util.DBManager;
import view_model.AppointmentViewModel;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * FXML Controller class
 *
 * @author mcken
 */
public class MainController implements Initializable {

    @FXML
    private AnchorPane root;

    @FXML
    private TableView<Customer> customerTableView;

    public TableView<Customer> getCustomerTableView() {
        return customerTableView;
    }

    @FXML
    private TableColumn<Customer, String> customerNameColumn;
    @FXML
    private TableColumn<Customer, String> customerAddressColumn;
    @FXML
    private TableColumn<Customer, City> customerCityColumn;
    @FXML
    private TableColumn<Customer, String> customerPhoneColumn;

    @FXML
    private TableView<AppointmentViewModel> appointmentTableView;
    @FXML
    private TableColumn<AppointmentViewModel, String> appointmentCustNameColumn;
    @FXML
    private TableColumn<AppointmentViewModel, String> appointmentDescriptionColumn;
    @FXML
    private TableColumn<AppointmentViewModel, String> appointmentDateColumn;
    @FXML
    private TableColumn<AppointmentViewModel, String> appointmentTypeColumn;

    @FXML
    private RadioButton monthViewRadio;
    @FXML
    private RadioButton weekViewRadio;
    @FXML
    private ToggleGroup appotintmentToggleGroup;

    @FXML
    private Button btnAddAppointment;
    @FXML
    private Button btnUpdateAppointment;
    @FXML
    private Button btnDeleteAppointment;
    @FXML
    private Button btnAddCustomer;
    @FXML
    private Button btnUpdateCustomer;
    @FXML
    private Button btnDeleteCustomer;
    @FXML
    private Button btnReports;

    private ObservableList<AppointmentViewModel> appointmentList;
    private static Customer selectedCustomer;
    private static Appointment selectedAppointment;
    private DateTimeFormatter dtfTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private LocalDate now = LocalDate.now();
    int offsetSeconds = ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //setting the buttons
        btnAddCustomer.setOnAction(this::addCustomerButtonPressed);
        btnUpdateCustomer.setDisable(true);
        btnDeleteCustomer.setDisable(true);
        btnAddAppointment.setDisable(true);
        btnUpdateAppointment.setDisable(true);
        btnDeleteAppointment.setDisable(true);
        //disable buttons until a row is selected
        customerTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                btnUpdateCustomer.setDisable(false);
                btnDeleteCustomer.setDisable(false);
                btnAddAppointment.setDisable(false);

                appointmentTableView.setItems(getCustomerSpecificAppointments(newValue.getCustomerId()));
            }
        });
        appointmentTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                btnUpdateAppointment.setDisable(false);
                btnDeleteAppointment.setDisable(false);
            }

        });

        //Column Initialize
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerCityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        customerTableView.getItems().setAll(parseCustomerList());

        appointmentCustNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        appointmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("customTimeDisplay"));
        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        appointmentTableView.getItems().setAll(parseAppointmentList());

        btnAddCustomer.setOnAction(event -> addCustomerButtonPressed(event));
        btnUpdateCustomer.setOnAction(event -> updateCustomerButtonPressed(event));
        btnDeleteCustomer.setOnAction(event -> deleteCustomerButtonPressed(event));
        btnAddAppointment.setOnAction(event -> addAppointmentButtonPressed(event));
        btnUpdateAppointment.setOnAction(event -> updateAppointmentButtonPressed(event));
        btnReports.setOnAction(event -> {
            reportsButtonPressed();
            event.consume();
        });

        //set toggle group and radio buttons
        appotintmentToggleGroup = new ToggleGroup();
        RadioButton monthRadio = this.monthViewRadio;
        RadioButton weekRadio = this.weekViewRadio;
        monthRadio.setToggleGroup(appotintmentToggleGroup);
        weekRadio.setToggleGroup(appotintmentToggleGroup);

        //

        System.out.println("initialize called");
    }

    public static Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    //    REQ B.  Provide the ability to add, update, and delete customer records in the database, including name, address, and phone number.
    protected List<Customer> parseCustomerList() {
        String custName;
        String custAddress;
        String custAddress2;
        String custCity;
        int custCityId;
        String custCountry;
        int custId;
        String custPhone;
        String custPostalCode;
        int custAddressId;

        ArrayList<Customer> custList = new ArrayList();
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT customer.customerName, customer.customerid, customer.addressId, "
                        + "address.address, address2, address.postalCode, address.phone, "
                        + "city.city, city.cityid, "
                        + "country.country "
                        + "FROM customer "
                        + "JOIN address ON customer.addressId = address.addressid "
                        + "JOIN city ON address.cityId = city.cityid "
                        + "JOIN country ON city.countryId = country.countryId "
                        + "ORDER BY customer.customerName");
             ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                custName = rs.getString("customerName");
                custAddress = rs.getString("address.address");
                custAddress2 = rs.getString("address2");
                custCity = rs.getString("city.city");
                custCityId = rs.getInt("city.cityid");
                custCountry = rs.getString("country.country");
                custPhone = rs.getString("phone");
                custPostalCode = rs.getString("address.postalCode");
                custId = rs.getInt("customer.customerid");
                custAddressId = rs.getInt("customer.addressId");
                custList.add(new Customer(custName, custAddress, custAddress2, new City(custCityId, custCity), custPhone, custPostalCode, custCountry, custId, custAddressId));
            }

        } catch (SQLException e) {
            System.out.println("SQL cust query error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e2) {
            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
        }
        return custList;
    }

    @FXML
    private void addCustomerButtonPressed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateCustomerButtonPressed(ActionEvent event) {
        try {
            selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);
            CustomerController controller = loader.getController();
            controller.setCustomerDetails(customerTableView.getSelectionModel().getSelectedItem());
            System.out.println(customerTableView.getSelectionModel().getSelectedItem());
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            //fallthrough
        }
    }

    @FXML
    private void deleteCustomerButtonPressed(ActionEvent event) {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this customer?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            deleteCustomer(selectedCustomer);
            customerTableView.getItems().setAll(parseCustomerList());
        } else {
            alert.close();
        }

    }

    private void deleteCustomer(Customer customer) {
        try {
            // Get address id for customer, delete customer DONT WORRY ABOUT ADDRESS, DO NOT DELETE
            PreparedStatement statement = DBManager.getConnection().prepareStatement("DELETE FROM customer WHERE customerid = ?");
            statement.setInt(1, customer.getCustomerId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //    REQ C.  Provide the ability to add, update, and delete appointments, capturing the type of appointment and a link to the specific customer record in the database.
    private ObservableList<AppointmentViewModel> parseAppointmentList() {

        ObservableList<AppointmentViewModel> appointmentList = FXCollections.observableArrayList();
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "SELECT customer.customerName, "
                            + "appointment.appointmentid, appointment.description, appointment.start, appointment.end, appointment.title "
                            + "FROM appointment "
                            + "JOIN customer ON customer.customerid = appointment.customerId "
                            + "ORDER BY start");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                appointmentList.add(new AppointmentViewModel(
                        rs.getString("customer.customerName"),
                        rs.getString("appointment.appointmentid"),
                        rs.getString("appointment.description"),
                        rs.getString("appointment.title"),
                        rs.getTimestamp("appointment.start").toLocalDateTime().plusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId()),
                        rs.getTimestamp("appointment.end").toLocalDateTime().plusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId())));
            }

        } catch (SQLException e) {
            System.out.println("SQL cust query error: " + e.getMessage());
        } catch (Exception e2) {
            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
        }
        return appointmentList;
    }

    private ObservableList<AppointmentViewModel> getCustomerSpecificAppointments(int customerId) {

        ObservableList<AppointmentViewModel> appointmentList = FXCollections.observableArrayList();
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "SELECT customer.customerName, "
                            + "appointment.appointmentid, appointment.description, appointment.start, appointment.end, appointment.title "
                            + "FROM appointment "
                            + "JOIN customer ON customer.customerid = appointment.customerId "
                            + "WHERE customer.customerid = ? "
                            + "ORDER BY start");
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                appointmentList.add(new AppointmentViewModel(
                        rs.getString("customer.customerName"),
                        rs.getString("appointment.appointmentid"),
                        rs.getString("appointment.description"),
                        rs.getString("appointment.title"),
                        rs.getTimestamp("appointment.start").toLocalDateTime().plusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId()),
                        rs.getTimestamp("appointment.end").toLocalDateTime().plusSeconds(offsetSeconds).atZone(TimeZone.getDefault().toZoneId())));

            }

        } catch (SQLException e) {
            System.out.println("SQL cust query error: " + e.getMessage());
        } catch (Exception e2) {
            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
        }
        return appointmentList;
    }

    public ObservableList<AppointmentViewModel> getCustomerAppointments(int customerID){
        ObservableList<AppointmentViewModel> customerAppointments = FXCollections.observableArrayList();
        String customerAppointmentsSQL = "SELECT appointmentId, customerId, userId, title, description, location, contact, type, start, end " +
                "FROM appointment " +
                "WHERE customerId = ? " +
                "ORDER BY start";
        try{
            PreparedStatement getCustomerAppointmentsStatement = DBManager.getConnection().prepareStatement(customerAppointmentsSQL);
            getCustomerAppointmentsStatement.setInt(1, customerID);
            ResultSet apptsQueryResults = getCustomerAppointmentsStatement.executeQuery();

            while(apptsQueryResults.next()){
                customerAppointments.add(new AppointmentViewModel(
                        "test",
                        apptsQueryResults.getString("appointmentId"),
                        apptsQueryResults.getString("description"),
                        apptsQueryResults.getString("title"),
                        apptsQueryResults.getTimestamp("start").toLocalDateTime().atZone(TimeZone.getDefault().toZoneId()),
                        apptsQueryResults.getTimestamp("end").toLocalDateTime().atZone(TimeZone.getDefault().toZoneId()
                        )));
            }
            return customerAppointments;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }



    @FXML
    private void addAppointmentButtonPressed(ActionEvent event) {
        try {
            selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Appointment.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);
            AppointmentController controller = loader.getController();
            controller.setCustomerDetails(customerTableView.getSelectionModel().getSelectedItem());
            System.out.println(customerTableView.getSelectionModel().getSelectedItem());
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            //fallthrough
        }
    }

    public static Appointment getSelectedAppointment() {
        return selectedAppointment;
    }

    @FXML
    private void updateAppointmentButtonPressed(ActionEvent event) {
        try {
            selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Appointment.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);
//            ObservableList<AppointmentViewModel> test = getCustomerAppointments(selectedCustomer.getCustomerId());
            AppointmentController controller = loader.getController();
            controller.setAppointmentDetails(appointmentTableView.getSelectionModel().getSelectedItem().getId());
            System.out.println(customerTableView.getSelectionModel().getSelectedItem());
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            //fallthrough
        }
    }

    @FXML
    private void deleteAppointmentButtonPressed(ActionEvent event) {
        AppointmentViewModel selectedAppointment = appointmentTableView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this customer?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            deleteAppointment(selectedAppointment);
            appointmentTableView.getItems().setAll(parseAppointmentList());
        } else {
            alert.close();
        }
    }

    private void deleteAppointment(AppointmentViewModel appointment) {
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement("DELETE FROM appointment WHERE appointment.appointmentid = ?");
            ps.setString(1, String.valueOf(appointment.getId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not delete appointment. " + e.getMessage());
        }
    }

    @FXML
    private void weekRadioButtonPressed(ActionEvent event) {

        LocalDate weekFromNow = now.plusDays(7);
        FilteredList<AppointmentViewModel> filteredData = new FilteredList<AppointmentViewModel>(parseAppointmentList());
//        filteredData.setPredicate(row -> {
//            LocalDate rowDate = LocalDate.parse(row.getStart(), dtfTime);
//            return rowDate.isEqual(now.minusDays(1)) && rowDate.isBefore(weekFromNow);
//        });
        appointmentTableView.setItems(filteredData);
    }

    @FXML
    private void monthRadioButtonPressed(ActionEvent event) {
        LocalDate monthFromNow = now.plusMonths(1);
        FilteredList<AppointmentViewModel> filteredData = new FilteredList<AppointmentViewModel>(parseAppointmentList());
//        filteredData.setPredicate(row -> {
//            LocalDate rowDate = LocalDate.parse(row.getStart(), dtfTime);
//            return rowDate.isAfter(now.minusDays(1)) && rowDate.isBefore(monthFromNow);
//        });
        appointmentTableView.setItems(filteredData);
    }

    @FXML
    private void reportsButtonPressed() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reports Selection");
        alert.setHeaderText("PLEASE CHOOSE A REPORT");

        ButtonType buttonOne = new ButtonType("Uno");
        ButtonType buttonTwo = new ButtonType("dos");
        ButtonType buttonThree = new ButtonType("tres");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonOne, buttonTwo, buttonThree, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonOne) {
            System.out.println("UNNNOOOOOO");
        } else if (result.get() == buttonTwo) {
            System.out.println("DOOOOOSSS");
        } else if (result.get() == buttonThree) {
            System.out.println("TRREEEEEFIDDDYY");
        } else {
            System.out.println("CANCELCANCELCANCEL");
        }
    }
}
