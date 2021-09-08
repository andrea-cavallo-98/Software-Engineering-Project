package it.polito.ezshop.model;

import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.exceptions.InvalidProductCodeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnTransactionObject {
    private final Integer saleTransactionId;
    private final Integer returnId;
    private String status;
    private double price;
    private Map<String, TicketEntryObject> returnedItems = new HashMap<>();
    private LocalDate date;
    private double saleDiscountRate;

    public ReturnTransactionObject(Integer saleTransactionId, Integer returnId){
        this.status = "open";
        this.saleTransactionId = saleTransactionId;
        this.returnId = returnId;
        this.date = LocalDate.now();
        this.price = 0;
    }

    public double getMoney(){
        return price*(1-saleDiscountRate);
    }
    public void setMoney(double money){
        this.price = money;
    }
    public double updateMoney(double toAdd){
        this.price += toAdd;
        return price;
    }

    public LocalDate getDate(){ return date; }

    public Integer getSaleTransactionId() {
        return saleTransactionId;
    }

    public Integer getReturnId() {
        return returnId;
    }

    public List<TicketEntryObject> getReturnedItems() {
        return new ArrayList<>(returnedItems.values());
    }

    public void setReturnedItems(Map<String, TicketEntryObject> returnedItems) {
        this.returnedItems = returnedItems;
    }

    public void addItem(TicketEntryObject item){
        returnedItems.putIfAbsent(item.getBarCode(), item);
    }

    public TicketEntry getItem(String productCode)  {
        return returnedItems.get(productCode);
    }

    public boolean isOpen(){
        return (status.equals("open"));
    }

    public boolean isClosed(){
        return (status.equals("closed"));
    }

    public boolean isPayed(){
        return (status.equals("payed"));
    }

    public boolean paymentIssued(){
        if(this.isClosed()){
            this.status = "payed";
            return true;
        }
        else return false;
    }

    public boolean close(){
        if(this.isOpen()) {
            this.status = "closed";
            return true;
        }
        else return  false;
    }

    public void setSaleDiscountRate(double discountRate) {
        this.saleDiscountRate = discountRate;
    }

    public double getSaleDiscountRate() {
        return saleDiscountRate;
    }
}

