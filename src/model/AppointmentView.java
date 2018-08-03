package model;

public class AppointmentView {

    private String month;
    private String amount;
    private String type;

    public AppointmentView(String month, String amount, String type) {
        this.month = month;
        this.amount = amount;
        this.type = type;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}