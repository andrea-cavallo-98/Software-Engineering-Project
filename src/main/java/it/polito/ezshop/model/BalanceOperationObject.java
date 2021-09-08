package it.polito.ezshop.model;

import it.polito.ezshop.data.BalanceOperation;

import java.time.LocalDate;

public class BalanceOperationObject implements BalanceOperation {

    private int balanceId;
    private LocalDate date;
    private double money;
    private String type;

    public BalanceOperationObject(Integer id, SaleTransactionObject st) {
        this.balanceId = id;
        this.date = st.getDate();
        this.money = st.getPrice();
        this.type = "Sale Transaction";
    }

    public BalanceOperationObject(Integer id, ReturnTransactionObject rt) {
        this.balanceId = id;
        this.date = rt.getDate();
        this.money = - rt.getMoney();
        this.type = "Return Transaction";
    }

    public BalanceOperationObject(Integer id, OrderObject or){
        this.balanceId = id;
        this.date = or.getDate();
        this.money = - or.getQuantity() * or.getPricePerUnit();
        this.type = "Order Transaction";
    }

    public BalanceOperationObject(Integer id, LocalDate date, double money, String type){
        this.balanceId = id;
        this.date = date;
        this.money = money;
        this.type = type;
    }


    @Override
    public int getBalanceId(){ return balanceId; }

    @Override
    public void setBalanceId(int balanceId){ this.balanceId = balanceId; }

    @Override
    public LocalDate getDate(){ return date; }

    @Override
    public void setDate(LocalDate date){ this.date = date; }

    @Override
    public double getMoney(){ return money; }

    @Override
    public void setMoney(double money) { this.money = money; }

    @Override
    public String getType(){ return type; }

    @Override
    public void setType(String type){ this.type = type; }


}
