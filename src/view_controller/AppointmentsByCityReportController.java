package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppointmentsByCityReportController implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private Button btnClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnClose.setOnAction(this::btnClosePressed);

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
