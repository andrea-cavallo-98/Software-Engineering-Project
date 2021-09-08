package it.polito.ezshop.model;

import it.polito.ezshop.data.Customer;

public class CustomerObject implements Customer {
    private String customerName;
    private String customerCard;
    private Integer id;
    private Integer points;

    public CustomerObject(String customerName, String customerCard, Integer id, Integer points) {
        this.customerName = customerName;
        this.customerCard = customerCard;
        this.id = id;
        this.points = points;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCard() {
        return this.customerCard;
    }

    public void setCustomerCard(String customerCard) {
        this.customerCard = customerCard;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPoints() {
        return this.points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
