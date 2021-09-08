package it.polito.ezshop.model;

import it.polito.ezshop.data.ProductType;

public class ProductTypeObject implements ProductType {
    String description;
    Integer id;
    String barCode;
    Double pricePerUnit;
    String note;
    String location;
    Integer quantity;

    public ProductTypeObject(String description, Integer id, String barCode, Double pricePerUnit, String note, String location, Integer quantity) {
        this.description = description;
        this.id = id;
        this.barCode = barCode;
        this.pricePerUnit = pricePerUnit;
        this.note = note;
        this.location = location;
        this.quantity = quantity;
    }

    public ProductTypeObject(String description, Integer id, String barCode, Double pricePerUnit, String note, Integer quantity) {
        this.description = description;
        this.id = id;
        this.barCode = barCode;
        this.pricePerUnit = pricePerUnit;
        this.note = note;
        this.location = null; // in the case the product type is not available in the inventory
        this.quantity = quantity;
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getProductDescription() {
        return description;
    }

    @Override
    public void setProductDescription(String productDescription) {
        this.description = productDescription;
    }

    @Override
    public String getBarCode() {
        return barCode;
    }

    @Override
    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    @Override
    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    @Override
    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
