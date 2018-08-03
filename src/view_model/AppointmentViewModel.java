package view_model;

import static view_controller.AppointmentController.DATE_TIME_FORMAT;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentViewModel {

    private String customerName;
    private String id;
    private String description;
    private String type;
    private ZonedDateTime stop;
    private ZonedDateTime start;

    public AppointmentViewModel(String customerName, String id, String description, String type, String start, String stop) {
        this.customerName = customerName;
        this.id = id;
        this.description = description;
        this.type = type;
        try {
            this.start = DATE_TIME_FORMAT.parse(start).toInstant().atZone(ZoneId.systemDefault());
            this.stop = DATE_TIME_FORMAT.parse(stop).toInstant().atZone(ZoneId.systemDefault());
        } catch (Exception e) {

        }
    }

    public String getCustomTimeDisplay() {
        String customDateTime = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm").format(start) + " - " + DateTimeFormatter.ofPattern("HH:mm").format(stop);
        return customDateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getId() {
        return Integer.parseInt(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

}
