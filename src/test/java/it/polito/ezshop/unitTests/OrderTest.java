package it.polito.ezshop.unitTests;

import it.polito.ezshop.model.OrderObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.time.LocalDate;

public class OrderTest {
    @Test
    public void testGetBalanceId() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getBalanceId(), (Integer) 5);
    }

    @Test
    public void testSetBalanceId() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setBalanceId(10);
        assertEquals(o.getBalanceId(), (Integer) 10);
    }

    @Test
    public void testGetProductCode() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getProductCode(), "123123123123");
    }

    @Test
    public void testSetProductCode() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setProductCode("456456456456");
        assertEquals(o.getProductCode(), "456456456456");
    }

    @Test
    public void testGetPricePerUnit() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getPricePerUnit(), 1.0, 0.0001);
    }

    @Test
    public void testSetPricePerUnit() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setPricePerUnit(6.2);
        assertEquals(o.getPricePerUnit(), 6.2, 0.0001);
    }

    @Test
    public void testGetQuantity() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getQuantity(), 10);
    }

    @Test
    public void testSetQuantity() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setQuantity(50);
        assertEquals(o.getQuantity(), 50);
    }

    @Test
    public void testGetStatus() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getStatus(), "ISSUED");
    }

    @Test
    public void testSetStatus() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setStatus("PAYED");
        assertEquals(o.getStatus(), "PAYED");
    }

    @Test
    public void testGetOrderId() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        assertEquals(o.getOrderId(), (Integer) 1);
    }

    @Test
    public void testSetOrderId() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        o.setOrderId(8);
        assertEquals(o.getOrderId(), (Integer) 8);
    }

    @Test
    public void testGetDate() {
        OrderObject o = new OrderObject("123123123123", 1.0, 10, "ISSUED", 1, 5);
        LocalDate l = LocalDate.now();
        o.setDate(l);
        assertEquals(o.getDate(), l);
    }
}
