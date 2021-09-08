package it.polito.ezshop.model;

import it.polito.ezshop.data.ProductType;
import it.polito.ezshop.data.TicketEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TicketEntryObject implements TicketEntry {
    private  String productDescription;
    private String productCode;
    private Integer productAmount;
    private double discountRate;
    private double pricePerUnit;
    private ArrayList<String> rfidList;

    public TicketEntryObject(String productDescription, String productCode, Integer productAmount, double discountRate, double pricePerUnit) {
        this.productDescription = productDescription;
        this.productCode = productCode;
        this.productAmount = productAmount;
        this.discountRate = discountRate;
        this.pricePerUnit = pricePerUnit;
        rfidList = new ArrayList<>();
    }

    /**
     * A TicketEntry is created when an item needs to be insert into the ticket
     * It constructed starting from a product type
     * @param item the product type to which the scanned product belongs
     * @param amount
     */
    public TicketEntryObject(ProductType item, int amount) {
        this.productCode = item.getBarCode();
        this.productAmount = amount;
        this.pricePerUnit = item.getPricePerUnit();
        this.productDescription = item.getProductDescription();
        this.discountRate = 0;
        rfidList = new ArrayList<>();
    }

    public TicketEntryObject(TicketEntry item, int amount) {
        this.productCode = item.getBarCode();
        this.productAmount = amount;
        this.pricePerUnit = item.getPricePerUnit();
        this.productDescription = item.getProductDescription();
        this.discountRate = item.getDiscountRate();
        rfidList = new ArrayList<>();
    }

    @Override
    public String getBarCode() {
        return productCode;
    }

    @Override
    public void setBarCode(String barCode) {
        this.productCode = barCode;
    }

    @Override
    public String getProductDescription() {
        return productDescription;
    }

    @Override
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public int getAmount() {
        return productAmount;
    }

    @Override
    public void setAmount(int amount) {
        this.productAmount = amount;
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
    public double getDiscountRate() {
        return discountRate;
    }

    @Override
    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketEntryObject)) return false;
        TicketEntryObject that = (TicketEntryObject) o;
        return Double.compare(that.discountRate, discountRate) == 0 && Double.compare(that.pricePerUnit, pricePerUnit) == 0 && productDescription.equals(that.productDescription) && productCode.equals(that.productCode) && productAmount.equals(that.productAmount);
    }

    public void addRFID(String rfid) {
        rfidList.add(rfid);
    }
    public void setRFID(List<String> rfid) {
        rfidList = new ArrayList<>(rfid);
    }

    public ArrayList<String> getRFIDs() {
        return rfidList;
    }
}
