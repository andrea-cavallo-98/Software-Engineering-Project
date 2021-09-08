package it.polito.ezshop.model;

public class CreditCardObject {
    private String creditCardCode;
    private double balance;

    public CreditCardObject(String creditCardCode){
        this.creditCardCode = creditCardCode;
    }

    public CreditCardObject(String creditCardCode, double balance){
        this.creditCardCode = creditCardCode;
        this.balance = balance;
    }

    public double getBalance(){ return this.balance; }
    public double updateBalance(double amount) {return  this.balance += amount;}
    public String getCreditCardCode()  { return this.creditCardCode; }
}
