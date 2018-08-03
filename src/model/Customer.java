/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author mcken
 */
public class Customer {

    private int custId;
    private String customerName;
    private String address;
    private String address2;
    private City city;
    private String country;
    private String postalCode;
    private String phone;
    private int custAddressId;

    public Customer() {

    }

    public Customer(String customerName, String address, City city, String phone) {
        this.customerName = customerName;
        this.address = address;
        this.city = city;
        this.phone = phone;
    }

    public Customer(String custName, String custAddress, String custAddress2, City city, String custPhone, String custPostalCode, String country, int custId, int custAddressId) {
        this.customerName = custName;
        this.address = custAddress;
        this.address2 = custAddress2;
        this.city = city;
        this.phone = custPhone;
        this.postalCode = custPostalCode;
        this.country = country;
        this.custId = custId;
        this.custAddressId = custAddressId;
    }

    public String getCustomerString() {
        return this.customerName;
    }

    public int getCustomerId() {
        return custId;
    }

    public void setCustomerId(int custId) {
        this.custId = custId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public int getAddressId() {
        return custAddressId;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
