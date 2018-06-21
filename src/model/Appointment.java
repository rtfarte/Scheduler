package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZonedDateTime;

public class Appointment {

    private ObservableList<Customer> associatedCustomers = FXCollections.observableArrayList();
    private int customerId;
    private int cityId;
    private int appointmentId;
    private Customer customer;
    private City city;
    private String title;
    private String description;
    private ZonedDateTime appointmentStartDate;
    private ZonedDateTime appointmentEndDate;
    private String startTime;
    private String endTime;
    private String user;

    public Appointment() {

    }

    public Appointment(ObservableList<Customer> associatedCustomers, int customerId, int cityId, int appointmentId, Customer customer, City city, String title, String description, String startTime, String endTime, String user) {
        this.associatedCustomers = associatedCustomers;
        this.customerId = customerId;
        this.cityId = cityId;
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.city = city;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
    }

    public ObservableList<Customer> getAssociatedCustomers() {
        return associatedCustomers;
    }

    public void setAssociatedCustomers(ObservableList<Customer> associatedCustomers) {
        this.associatedCustomers = associatedCustomers;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
