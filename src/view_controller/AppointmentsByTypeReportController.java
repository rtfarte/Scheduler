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
import model.TypeReportView;
import util.DBManager;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AppointmentsByTypeReportController implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private Button btnClose;
    @FXML
    private TableView<TypeReportView> appointmentTableView;
    @FXML
    private TableColumn<TypeReportView, String> appointmentTypeColumn;
    @FXML
    private TableColumn<TypeReportView, String> appointmentAmountColumn;

    private ObservableList<TypeReportView> appointmentList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnClose.setOnAction(this::btnClosePressed);

        appointmentTableView.getItems().setAll(parseAppointmentList());
        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        appointmentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("Amount"));
    }

    private ObservableList<TypeReportView> parseAppointmentList() {

        ObservableList<TypeReportView> appointmentList = FXCollections.observableArrayList();
        try {
            PreparedStatement statement = DBManager.getConnection().prepareStatement("SELECT description AS \"Type\", COUNT(*) as \"Amount\" FROM appointment");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                appointmentList.add(new TypeReportView(
               rs.getString("Type"),
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









