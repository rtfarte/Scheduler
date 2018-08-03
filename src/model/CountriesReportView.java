package model;

public class CountriesReportView {
    private String country;
    private String amount;

    public CountriesReportView(String country, String amount){
        this.country = country;
        this.amount = amount;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
