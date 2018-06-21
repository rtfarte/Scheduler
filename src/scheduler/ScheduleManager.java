package scheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sun.security.util.Resources;
import util.DBManager;

import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;

/**
 *
 * @author mcken
 */
/*
 
C.  Provide the ability to add, update, and delete appointments, capturing the type of appointment and a link to the specific customer record in the database.
 
D.  Provide the ability to view the calendar by month and by week.
 
E.  Provide the ability to automatically adjust appointment times based on user time zones and daylight saving time.
 
F.  Write exception controls to prevent each of the following. You may use the same mechanism of exception control more than once, but you must incorporate at least two different mechanisms of exception control.
•   scheduling an appointment outside business hours
•   scheduling overlapping appointments
•   entering nonexistent or invalid customer data
•   entering an incorrect username and password
 
G.  Write two or more lambda expressions to make your program more efficient, justifying the use of each lambda expression with an in-line comment.
 
H.  Write code to provide an alert if there is an appointment within 15 minutes of the user’s log-in.
 
I.  Provide the ability to generate each of the following reports:
•   number of appointment types by month
•   the schedule for each consultant
•   one additional report of your choice
 
J.  Provide the ability to track user activity by recording timestamps for user log-ins in a .txt file. Each new record should be appended to the log file, if the file already exists.

 */
public class ScheduleManager extends Application {

    public static Connection connection;

    private static void openDB() {
        DBManager.openDB();
        connection = DBManager.getConnection();
    }

    private Stage primaryStage;
    private AnchorPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            rootLayout = FXMLLoader.load(ScheduleManager.class.getResource("/view_controller/Login.fxml"),
                    Resources.getBundle("resources/Strings", Locale.getDefault()));
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        Locale.setDefault(new Locale("fr", "FR"));
        openDB();
        launch(args);
    }

}