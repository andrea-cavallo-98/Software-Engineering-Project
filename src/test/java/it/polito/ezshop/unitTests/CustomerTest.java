package it.polito.ezshop.unitTests;

import it.polito.ezshop.model.CustomerObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CustomerTest {
    @Test
    public void testGetCustomerName() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        assertEquals(customerObject.getCustomerName(), customerName);
    }

    @Test
    public void testGetCustomerCard() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        assertEquals(customerObject.getCustomerCard(), customerCard);
    }

    @Test
    public void testGetCustomerId() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        assertEquals(customerObject.getId(), id);
    }

    @Test
    public void testGetCustomerPoints() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 20;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        assertEquals(customerObject.getPoints(), points);
    }

    @Test
    public void testSetCustomerName() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        String newCustomerName = "newCustomerName";
        customerObject.setCustomerName(newCustomerName);
        assertEquals(customerObject.getCustomerName(), newCustomerName);
    }

    @Test
    public void testSetCustomerCard() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        String newCustomerCard = "8396827512";
        customerObject.setCustomerCard(newCustomerCard);
        assertEquals(customerObject.getCustomerCard(), newCustomerCard);
    }

    @Test
    public void testSetCustomerId() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        Integer newId = 124;
        customerObject.setId(newId);
        assertEquals(customerObject.getId(), newId);
    }

    @Test
    public void testSetCustomerPoints() {
        String customerName = "customerName";
        String customerCard = "1954123593";
        Integer id = 123;
        Integer points = 0;
        CustomerObject customerObject = new CustomerObject(customerName, customerCard, id, points);
        Integer newCustomerPoints = 2;
        customerObject.setPoints(newCustomerPoints);
        assertEquals(customerObject.getPoints(), newCustomerPoints);
    }
}
