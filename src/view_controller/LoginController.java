package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.DBManager;
import util.LoggerUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author mcken
 */
public class LoginController implements Initializable {

    private DBManager dbManager;

    @FXML
    private TextField usernameTextField;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private TextField passwordTextField;
    @FXML
    private AnchorPane root;
    @FXML
    Button submitButton;
    @FXML
    Button exitButton;
    @FXML
    Label loginLabel;
    public static String currentUser = "";
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());


    ResourceBundle resources = ResourceBundle.getBundle("resources/Strings", Locale.getDefault());

    //    REQ A.  Create a log-in form that can determine the user’s location and translate log-in and error control messages (e.g., “The username and password did not match.”) into two languages.
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        this.resources = resources;
        loginLabel.setText(resources.getString("login_label"));
        usernameLabel.setText(resources.getString("username_label"));
        passwordLabel.setText(resources.getString("password_label"));
        submitButton.setText(resources.getString("submit_button_label"));
        exitButton.setText(resources.getString("exit_button_label"));
    }

    @FXML
    public void submitButtonPressed(ActionEvent event) {
        if (DBManager.isLoginValid(usernameTextField.getText(), passwordTextField.getText())) {
            try {
                // Show main screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
                Parent mainScreenParent = loader.load();
                Scene mainScreenScene = new Scene(mainScreenParent);
                Stage mainScreenStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                mainScreenStage.setScene(mainScreenScene);
                mainScreenStage.show();
                currentUser = usernameTextField.getText();
                LOGGER.log(Level.INFO, "Login success: {0}", currentUser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(resources.getString("invalid_entry"));
            alert.setHeaderText(resources.getString("password_username_mismatch"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                alert.close();
            }
        }
    }

    @FXML
    public void exitButtonPressed(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("confirm_exit"));
        alert.setHeaderText(resources.getString("confirm_dialogue"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
        } else {
            alert.close();
        }
    }
}