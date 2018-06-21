package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class DBManager {

    public static final String DB_NAME = "U04oOs";
    public static final String DRIVER_URL = "com.mysql.jdbc.Driver";
    private static final String ALLOW_MULTI_QUERIES = "?allowMultiQueries=true";
    public static final String CONNECTION_STRING = "jdbc:mysql://52.206.157.109/" + DB_NAME; //+ ALLOW_MULTI_QUERIES;
    public static final String USER_NAME = "U04oOs";
    public static final String DB_PASSWORD = "53688301999";
    private static Connection conn;
    private Statement statement;

    private DBManager() {

    }

    public static Connection getConnection() {
        return conn;
    }

    public static boolean openDB() {
//    Connection String
//    Server name: 52.206.157.109
//    Database name: U04oOs
//    Username: U04oOs
//    Password: 53688301999
        try {
            Class.forName(DRIVER_URL);
            conn = DriverManager.getConnection(CONNECTION_STRING, USER_NAME, DB_PASSWORD);
            System.out.println("Connection established: " + DB_NAME);
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    public void closeDB() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close the connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isLoginValid(String userName, String password) {
        try {
            String query = "SELECT password FROM user WHERE userName = ?";//this is the query with a ? SQL placeholder
            PreparedStatement statement = conn.prepareStatement(query);//this preps the application to execute a SQLQuery
            statement.setString(1, userName);//this sets up a holder for the JDBC data
            try (ResultSet results = statement.executeQuery()) {//this starts the query
                if (results.next()) {//moves from row to row using the next line for the column
                    return Objects.equals(password, results.getString("password"));//gets the column name
                } else {
                    System.out.println("This user name doesn't exist, but what does it mean to exist, really?");
                    return false;
                }
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasUpcomingAppointment() {
        try {
            String query = ""; //TODO: <- figure how to write an if statement
            //that finds the system time + 15 minutes and sets an alert
            //if there is an appointment withing 15 minutes
            PreparedStatement ps = conn.prepareStatement(query);
            ps.executeQuery();

//            if(/* There is a true statement from the SQL query */){
//                return hasUpcomingAppointment();
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void executeInTransaction(ThrowingConsumer<Connection> action) {
        try {
            getConnection().setAutoCommit(false);
            action.accept(getConnection());
            getConnection().commit();
        } catch (Exception ex) {
            try {
                getConnection().rollback();
            } catch (SQLException e) {
            }
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {

        void accept(T thing) throws Exception;
    }
}
