package it.polito.ezshop.unitTests;

import it.polito.ezshop.model.ReturnTransactionObject;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class ReturnTransactionUnitTest {
    @Test
    public void testGetReturnId(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);
        assertEquals(returnTransactionObject.getReturnId(), Integer.valueOf(0));
    }

    @Test
    public void testGetSaleTransactionId(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);
        assertEquals(returnTransactionObject.getSaleTransactionId(), Integer.valueOf(0));
    }

    @Test
    public void testGetMoney(){
        double price = 111.10;
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);
        returnTransactionObject.setMoney(price);
        assertEquals(returnTransactionObject.getMoney(), price, 0.01);
    }
    @Test
    public void testSaleDiscountRate(){
        double price = 10;
        double discountRate = 0.5;

        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);;
        returnTransactionObject.setSaleDiscountRate(discountRate);

        //assert returned value is equal to set one
        assertEquals(returnTransactionObject.getSaleDiscountRate(), discountRate, 0.001);

        //assert that the price is returned discounted
        returnTransactionObject.setMoney(price);
        assertEquals(returnTransactionObject.getMoney(), price*discountRate, 0.01);
    }

    @Test
    public void testOpenStatus(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);

        //assert that a just created transaction is open
        assertTrue(returnTransactionObject.isOpen());
        assertFalse(returnTransactionObject.isClosed());
        assertFalse(returnTransactionObject.isPayed());


    }

    @Test
    public void testClosedStatus(){
        ReturnTransactionObject ReturnTransactionObject = new ReturnTransactionObject(0,0);

        assertTrue(ReturnTransactionObject.close());
        assertTrue(ReturnTransactionObject.isClosed());
        assertFalse(ReturnTransactionObject.isOpen());
        assertFalse(ReturnTransactionObject.isPayed());

        //cannot close an already-closed transaction
        assertFalse(ReturnTransactionObject.close());
    }

    @Test
    public void testPayedStatus(){
        ReturnTransactionObject ReturnTransactionObject = new ReturnTransactionObject(0,0);
        assertFalse(ReturnTransactionObject.paymentIssued());

        assertTrue(ReturnTransactionObject.close());

        assertTrue(ReturnTransactionObject.paymentIssued());
        assertTrue(ReturnTransactionObject.isPayed());
        assertFalse(ReturnTransactionObject.isOpen());
        assertFalse(ReturnTransactionObject.isClosed());
    }

    @Test
    public void updateMoney(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);
        double initMoney = 2.0;
        returnTransactionObject.setMoney(initMoney);
        double moneyToAdd = 5.6;
        assertEquals(returnTransactionObject.updateMoney(moneyToAdd), moneyToAdd + initMoney, 0.01);
    }
    @Test
    public void testDate(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);
        assertEquals(returnTransactionObject.getDate(), LocalDate.now());
    }

}
