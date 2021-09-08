package it.polito.ezshop.model;

import it.polito.ezshop.data.Order;

import java.time.LocalDate;

public class OrderObject implements Order {
    Integer balanceId;
    String productCode;
    Double pricePerUnit;
    Integer quantity;
    String status;
    Integer orderId;
    public LocalDate date;

    public OrderObject(Integer balanceId, String productCode, Double pricePerUnit, Integer quantity, String status, Integer orderId, LocalDate date) {
        this.balanceId = balanceId;
        this.productCode = productCode;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.status = status;
        this.orderId = orderId;
        this.date = LocalDate.now();
    }

    public OrderObject(String productCode, Double pricePerUnit, Integer quantity, String status, Integer orderId) {
        this.balanceId = -1;
        this.productCode = productCode;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.status = status;
        this.orderId = orderId;
        this.date = LocalDate.now();
    }

    public OrderObject(String productCode, Double pricePerUnit, Integer quantity, String status, Integer orderId, Integer balanceId) {
        this.balanceId = balanceId;
        this.productCode = productCode;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.status = status;
        this.orderId = orderId;
        this.date = LocalDate.now();
    }

    @Override
    public Integer getBalanceId() {
        return balanceId;
    }

    @Override
    public void setBalanceId(Integer balanceId) {
        this.balanceId = balanceId;
    }

    @Override
    public String getProductCode() {
        return productCode;
    }

    @Override
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    @Override
    public double getPricePerUnit() {
        return pricePerUnit;
    }

    @Override
    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Integer getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setDate(LocalDate date) { this.date = date; }

    public LocalDate getDate(){ return date; }
}
