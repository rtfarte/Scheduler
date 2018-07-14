package view_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.CountriesReportView;
import util.DBManager;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CountriesReportController implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private Button btnClose;
    @FXML
    private TableView<CountriesReportView> appointmentTableView;
    @FXML
    private TableColumn<CountriesReportView, String> countryColumn;
    @FXML
    private TableColumn<CountriesReportView, String> amountColumn;

    private ObservableList<CountriesReportView> appointmentList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnClose.setOnAction(this::btnClosePressed);

        appointmentTableView.getItems().setAll(parseAppointmentList());
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("Country"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("Amount"));
    }


    private ObservableList<CountriesReportView> parseAppointmentList() {

        String query = "SELECT c.country, COUNT(DISTINCT ci.city) AS \"Amount\" FROM country c INNER JOIN city ci ON c.countryid = ci.countryId GROUP BY c.country";

        ObservableList<CountriesReportView> appointmentList = FXCollections.observableArrayList();
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                appointmentList.add(new CountriesReportView(
                        rs.getString("Country"),
                        rs.getString("Amount")));
            }
            this.appointmentList = appointmentList;

        } catch (SQLException e) {
            System.out.println("SQL cust query error: " + e.getMessage());
        } catch (Exception e2) {
            System.out.println("Something besides the SQL went wrong." + e2.getMessage());
        }
        return appointmentList;
    }

    private void btnClosePressed(ActionEvent event) {
        goToMain();
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
