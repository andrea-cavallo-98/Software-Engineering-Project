package it.polito.ezshop.unitTests;
import static org.junit.Assert.*;

import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class SaleTransactionUnitTest {


    @Test
    public void testPrice(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        saleTransactionObject.setPrice(100);

        //assert returned value is equal to set one
        assertEquals(saleTransactionObject.getPrice(), 100.00, 0.001);
    }

    @Test
    public void testDiscountRate(){
        double price = 10;
        double discountRate = 0.5;

        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        saleTransactionObject.setDiscountRate(discountRate);

        //assert returned value is equal to set one
        assertEquals(saleTransactionObject.getDiscountRate(), discountRate, 0.01);

        //assert that the price is returned discounted
        saleTransactionObject.setPrice(price);
        assertEquals(saleTransactionObject.getPrice(), price*discountRate, 0.01);
    }

    @Test
    public void testOpenStatus(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);

        //assert that a just created transaction is open
        assertEquals(saleTransactionObject.getStatus(),"open");
        assertTrue(saleTransactionObject.isOpen());
        assertFalse(saleTransactionObject.isClosed());
        assertFalse(saleTransactionObject.isPayed());


    }

    @Test
    public void testClosedStatus(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);

        assertTrue(saleTransactionObject.close());
        assertTrue(saleTransactionObject.isClosed());
        assertFalse(saleTransactionObject.isOpen());
        assertFalse(saleTransactionObject.isPayed());
        assertEquals(saleTransactionObject.getStatus(), "closed");


        //cannot close an already-closed transaction
        assertFalse(saleTransactionObject.close());
    }

    @Test
    public void testPayedStatus(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        assertFalse(saleTransactionObject.paymentIssued());

        saleTransactionObject.close();

        assertTrue(saleTransactionObject.paymentIssued());
        assertTrue(saleTransactionObject.isPayed());
        assertFalse(saleTransactionObject.isOpen());
        assertFalse(saleTransactionObject.isClosed());
    }

    @Test
    public void testDate(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        assertEquals(saleTransactionObject.getDate(), LocalDate.now());

        LocalDate newDate = LocalDate.of(2021, 05, 19);
        saleTransactionObject.setDate(newDate);
        assertEquals(saleTransactionObject.getDate(), newDate);
    }

    @Test
    public void paymentRollbackTest(){
        SaleTransactionObject st = new SaleTransactionObject(0);
        st.close();
        st.paymentIssued();
        st.paymentRollback();
        assertTrue(st.isClosed());
    }

    @Test
    public void testTicketNumber(){
        SaleTransactionObject st = new SaleTransactionObject(0);
        assertEquals(st.getTicketNumber(), Integer.valueOf(0));

        Integer newId = 12;
        st.setTicketNumber(newId);
        assertEquals(st.getTicketNumber(), newId);

    }

}
