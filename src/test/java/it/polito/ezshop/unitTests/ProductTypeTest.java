package it.polito.ezshop.unitTests;

import it.polito.ezshop.model.ProductTypeObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ProductTypeTest {
    @Test
    public void testGetQuantity() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", 34);
        assertEquals(p.getQuantity(), (Integer) 34);
    }

    @Test
    public void testSetQuantity() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", 34);
        p.setQuantity(20);
        assertEquals(p.getQuantity(), (Integer) 20);
    }

    @Test
    public void testGetLocation() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getLocation(), "1-a-1");
    }

    @Test
    public void testSetLocation() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-b-1", 34);
        p.setLocation("1-a-1");
        assertEquals(p.getLocation(), "1-a-1");
    }

    @Test
    public void testGetNote() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getNote(), "note");
    }

    @Test
    public void testSetNote() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        p.setNote("newNote");
        assertEquals(p.getNote(), "newNote");
    }

    @Test
    public void testGetProductDescription() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getProductDescription(), "test");
    }

    @Test
    public void testSetProductDescription() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        p.setProductDescription("newTest");
        assertEquals(p.getProductDescription(), "newTest");
    }

    @Test
    public void testGetBarCode() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getBarCode(), "9788808182159");
    }

    @Test
    public void testSetBarCode() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        p.setBarCode("9788832360103");
        assertEquals(p.getBarCode(), "9788832360103");
    }

    @Test
    public void testGetPricePerUnit() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getPricePerUnit(), (Double) 1.0);
    }

    @Test
    public void testSetPricePerUnit() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        p.setPricePerUnit(36.78);
        assertEquals(p.getPricePerUnit(), (Double) 36.78);
    }

    @Test
    public void testGetId() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        assertEquals(p.getId(), (Integer) 1);
    }

    @Test
    public void testSetId() {
        ProductTypeObject p = new ProductTypeObject("test", 1, "9788808182159", 1.0, "note", "1-a-1", 34);
        p.setId(5);
        assertEquals(p.getId(), (Integer) 5);
    }
}
