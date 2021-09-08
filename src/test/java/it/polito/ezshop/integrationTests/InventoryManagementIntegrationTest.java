package it.polito.ezshop.integrationTests;

import it.polito.ezshop.controllers.AccountBook;
import it.polito.ezshop.controllers.DB;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.controllers.InventoryManagement;
import it.polito.ezshop.exceptions.*;
import org.junit.Test;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class InventoryManagementIntegrationTest {
    private static final EZShop ezshop = new EZShop();

    @Test
    public void createProductTypeTest() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, SQLException {
        ezshop.reset();

        String description = "testProduct";
        String productCode = "9788806222024";
        double pricePerUnit = 1.2;
        String note = "testNote";

       DB.alterJDBCUrl();
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.createProductType(description, "9788832360103", pricePerUnit, note)), java.util.Optional.ofNullable(-1));
        DB.restoreJDBCUrl();

        InventoryManagement.loadProductsFromDB();
        assertThrows(InvalidProductDescriptionException.class, () -> InventoryManagement.createProductType("", productCode, pricePerUnit, note));
        assertThrows(InvalidProductDescriptionException.class, () -> InventoryManagement.createProductType(null, productCode, pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.createProductType(description, "123", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.createProductType(description, "", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.createProductType(description, "12312312312312312312312", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.createProductType(description, null, pricePerUnit, note));
        assertThrows(InvalidPricePerUnitException.class, () -> InventoryManagement.createProductType(description, productCode, -2, note));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.createProductType(description, "9788832360103", pricePerUnit, note)), java.util.Optional.ofNullable(InventoryManagement.getLastProductId()));

        ezshop.reset();
    }

    @Test
    public void updateProductTypeTest() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidProductIdException, SQLException {
        ezshop.reset();

        String description = "testUpdateProduct";
        String productCode = "9788806222024";
        double pricePerUnit = 1.2;
        String note = "testNote";

        InventoryManagement.createProductType("testUpdateProd", "9788808182159", 1.2, "note");

       DB.alterJDBCUrl();
        assertFalse(InventoryManagement.updateProduct(InventoryManagement.getLastProductId(), description, "9788806222024", pricePerUnit, note));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductIdException.class, () -> InventoryManagement.updateProduct(-1, description, productCode, pricePerUnit, note));
        assertFalse(InventoryManagement.updateProduct(7, description, productCode, pricePerUnit, note));

        assertThrows(InvalidProductDescriptionException.class, () -> InventoryManagement.updateProduct(1, "", productCode, pricePerUnit, note));
        assertThrows(InvalidProductDescriptionException.class, () -> InventoryManagement.updateProduct(1, null, productCode, pricePerUnit, note));

        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.updateProduct(1, description, "123", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.updateProduct(1, description, "", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.updateProduct(1, description, "12312312312312312312312", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, ()-> InventoryManagement.updateProduct(1, description, null, pricePerUnit, note));

        assertThrows(InvalidPricePerUnitException.class, () -> InventoryManagement.updateProduct(1, description, productCode, -2, note));

        assertTrue(InventoryManagement.updateProduct(InventoryManagement.getLastProductId(), description, "9788806222024", pricePerUnit, note));
        assertTrue(InventoryManagement.updateProduct(InventoryManagement.getLastProductId(), description, "9788832360103", pricePerUnit, note));

        ezshop.reset();
    }

    @Test
    public void deleteProductTypeTest() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testDeleteProd", "9788806222024", 1.2, "note");

       DB.alterJDBCUrl();
        assertFalse(InventoryManagement.deleteProductType(InventoryManagement.getLastProductId()));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductIdException.class, () -> InventoryManagement.deleteProductType(-1));
        assertFalse(InventoryManagement.deleteProductType(7));
        assertTrue(InventoryManagement.deleteProductType(InventoryManagement.getLastProductId()));

        ezshop.reset();
    }

    @Test
    public void getProductTypeByBarCodeTest() throws InvalidProductCodeException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testgetProductByBC", "9788806222024", 1.2, "note");

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.getProductTypeByBarCode(null));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.getProductTypeByBarCode(""));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.getProductTypeByBarCode("456"));

        assertNull(InventoryManagement.getProductTypeByBarCode("9788808182159"));

        assertEquals(InventoryManagement.getProductTypeByBarCode("9788806222024").getBarCode(), "9788806222024");
        assertTrue(InventoryManagement.getProductTypeByBarCode("9788806222024").getProductDescription().equals("testgetProductByBC"));
        assertEquals(InventoryManagement.getProductTypeByBarCode("9788806222024").getPricePerUnit(), 1.2, 0.00001);
        assertEquals(InventoryManagement.getProductTypeByBarCode("9788806222024").getNote(), "note");


        ezshop.reset();
    }

    @Test
    public void updateQuantityTest() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, SQLException {
        ezshop.reset();
        InventoryManagement.createProductType("testUpdateQuantity", "9788806222024", 1.2, "note");

        int productId = InventoryManagement.getLastProductId();
        int toBeAdded = 10;

       DB.alterJDBCUrl();
        assertFalse(InventoryManagement.updateQuantity(productId, toBeAdded));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductIdException.class, () -> InventoryManagement.updateQuantity(-1, toBeAdded));

        assertFalse(InventoryManagement.updateQuantity(5, toBeAdded));
        assertFalse(InventoryManagement.updateQuantity(productId, toBeAdded)); //location == null

        InventoryManagement.updatePosition(productId, "1-a-4");
        assertFalse(InventoryManagement.updateQuantity(productId, -10));
        assertTrue(InventoryManagement.updateQuantity(productId, toBeAdded));

        ezshop.reset();
    }

    @Test
    public void updatePositionTest() throws InvalidProductIdException, InvalidLocationException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testUpdatePosition", "9788806222024", 1.2, "note");

        int productId = InventoryManagement.getLastProductId();
        String newPos = "1-p-3";

       DB.alterJDBCUrl();
        assertFalse(InventoryManagement.updatePosition(productId, newPos));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductIdException.class, () -> InventoryManagement.updatePosition(-1, newPos));

        assertThrows(InvalidLocationException.class, () -> InventoryManagement.updatePosition(productId, ""));
        assertThrows(InvalidLocationException.class, () -> InventoryManagement.updatePosition(productId, null));
        assertThrows(InvalidLocationException.class, () -> InventoryManagement.updatePosition(productId, "1-1-1"));
        assertThrows(InvalidLocationException.class, () -> InventoryManagement.updatePosition(productId, "ciao"));

        assertFalse(InventoryManagement.updatePosition(5, newPos));

        assertTrue(InventoryManagement.updatePosition(productId, newPos));

        ezshop.reset();
    }

    @Test
    public void issueOrderTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidQuantityException, InvalidProductIdException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testIssueOrder", "9788806222024", 1.2, "note");

       DB.alterJDBCUrl();
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.issueOrder("9788806222024", 10, 1.5)), java.util.Optional.ofNullable(-1));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.issueOrder("123", 10, 1.5));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.issueOrder("", 10, 1.5));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.issueOrder(null, 10, 1.5));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.issueOrder("9788808182159", 10, 1.5)), java.util.Optional.ofNullable(-1));

        assertThrows(InvalidQuantityException.class, () -> InventoryManagement.issueOrder("9788806222024", -10, 1.5));
        assertThrows(InvalidPricePerUnitException.class, () -> InventoryManagement.issueOrder("9788806222024", 10, -1.5));

        InventoryManagement.issueOrder("9788806222024", 10, 1.5);

        assertEquals(InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).getStatus(), "ISSUED");


        ezshop.reset();
    }

    @Test
    public void payOrderForTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductIdException, InvalidQuantityException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testPayOrder", "9788806222024", 1.2, "note");

       DB.alterJDBCUrl();
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor("9788806222024", 1, 1.5)), java.util.Optional.ofNullable(-1));
        DB.restoreJDBCUrl();

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.payOrderFor("123", 10, 1.5));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.payOrderFor("", 10, 1.5));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.payOrderFor(null, 10, 1.5));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor("9788808182159", 10, 1.5)), java.util.Optional.ofNullable(-1));

        assertThrows(InvalidQuantityException.class, () -> InventoryManagement.payOrderFor("9788806222024", -10, 1.5));
        assertThrows(InvalidPricePerUnitException.class, () -> InventoryManagement.payOrderFor("9788806222024", 10, -1.5));

        double b = AccountBook.computeBalance();
        AccountBook.recordBalanceUpdate(-b);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor("9788806222024", 10, 1.5)), java.util.Optional.ofNullable(-1));
        AccountBook.recordBalanceUpdate(b);

        AccountBook.recordBalanceUpdate(1.5);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor("9788806222024", 1, 1.5)), java.util.Optional.ofNullable(InventoryManagement.getLasOrderId()));


        ezshop.reset();
    }

    @Test
    public void payOrderTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductIdException, InvalidOrderIdException, InvalidQuantityException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testPayOrder", "9788806222024", 1.2, "note");
        InventoryManagement.issueOrder("9788806222024", 1, 1.5);

        AccountBook.recordBalanceUpdate(1.5);
       DB.alterJDBCUrl();
        assertFalse(InventoryManagement.payOrder(InventoryManagement.getLasOrderId()));
        DB.restoreJDBCUrl();

        assertThrows(InvalidOrderIdException.class, () -> InventoryManagement.payOrder(-1));
        assertFalse(InventoryManagement.payOrder(100));
        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setStatus("COMPLETED");
        assertFalse(InventoryManagement.payOrder(InventoryManagement.getLasOrderId()));
        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setStatus("ISSUED");

        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setProductCode("9788808182159");
        assertFalse(InventoryManagement.payOrder(InventoryManagement.getLasOrderId()));
        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setProductCode("9788806222024");

        double b = AccountBook.computeBalance();
        AccountBook.recordBalanceUpdate(-b);
        assertFalse(InventoryManagement.payOrder(InventoryManagement.getLasOrderId()));
        AccountBook.recordBalanceUpdate(b);


        assertTrue(InventoryManagement.payOrder(InventoryManagement.getLasOrderId()));


        ezshop.reset();
    }

    @Test
    public void recordOrderArrivalTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testRecordOrder", "9788806222024", 1.2, "note");
        AccountBook.recordBalanceUpdate(1.5);
        InventoryManagement.payOrderFor("9788806222024", 1, 1.5);

        assertThrows(InvalidOrderIdException.class, () -> InventoryManagement.recordOrderArrival(-1));
        assertFalse(InventoryManagement.recordOrderArrival(100));

        InventoryManagement.getProductTypeByBarCode(InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).getProductCode()).setLocation(null);
        assertThrows(InvalidLocationException.class, () -> InventoryManagement.recordOrderArrival(InventoryManagement.getLasOrderId()));
        InventoryManagement.getProductTypeByBarCode(InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).getProductCode()).setLocation("1-a-8");

        DB.alterJDBCUrl();
        assertFalse(InventoryManagement.recordOrderArrival(InventoryManagement.getLasOrderId()));
        DB.restoreJDBCUrl();

        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setStatus("COMPLETED");
        assertFalse(InventoryManagement.recordOrderArrival(InventoryManagement.getLasOrderId()));
        InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).setStatus("PAYED");

        assertTrue(InventoryManagement.recordOrderArrival(InventoryManagement.getLasOrderId()));

        ezshop.reset();
    }

    @Test
    public void recordOrderArrivalRFIDTest() throws InvalidQuantityException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductDescriptionException, InvalidRFIDException, InvalidLocationException, InvalidOrderIdException, SQLException {
        ezshop.reset();

        assertThrows(InvalidOrderIdException.class, () -> InventoryManagement.recordOrderArrivalRFID(null, ""));
        assertThrows(InvalidOrderIdException.class, () -> InventoryManagement.recordOrderArrivalRFID(0, ""));

        InventoryManagement.createProductType("testRecordOrder", "9788806222024", 1.2, "note");
        AccountBook.recordBalanceUpdate(500);
        InventoryManagement.payOrderFor("9788806222024", 10, 1.5);
        assertThrows(InvalidLocationException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, ""));
        InventoryManagement.getProductTypeByBarCode(InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).getProductCode()).setLocation("1-a-8");


        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, null));
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, ""));
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, "1234567"));
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, "ABCDEFGHIJ"));
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(1, "1234567891234"));

        assertEquals("PAYED", InventoryManagement.orderMap.get(1).getStatus());

        DB.alterJDBCUrl();
        assertFalse(InventoryManagement.recordOrderArrivalRFID(1, "000000010000"));
        DB.restoreJDBCUrl();

        InventoryManagement.orderMap.get(1).setStatus("COMPLETED");
        assertFalse(InventoryManagement.recordOrderArrivalRFID(1, "000000010000"));
        InventoryManagement.orderMap.get(1).setStatus("ISSUED");
        assertFalse(InventoryManagement.recordOrderArrivalRFID(1, "000000010000"));

        InventoryManagement.orderMap.get(1).setStatus("PAYED");

        assertTrue(InventoryManagement.recordOrderArrivalRFID(1, "000000010000"));

        InventoryManagement.payOrderFor("9788806222024", 10, 1.5);
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(2, "000000010009"));
        assertThrows(InvalidRFIDException.class, () -> InventoryManagement.recordOrderArrivalRFID(2, "000000009991"));

        ezshop.reset();
    }

    @Test
    public void isBarCodeValidTest() throws InvalidProductCodeException {
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("12312312312"));

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("123123123121111"));

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("456456456456456"));

        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("this is a barcode"));

        assertTrue(InventoryManagement.isBarcodeValid("9788808182159"));
    }

    @Test
    public void loadProductsMapFromDBTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, SQLException {
        ezshop.reset();

        InventoryManagement.createProductType("testUpdatePosition", "9788806222024", 1.2, "note");
        InventoryManagement.createProductType("testUpdateProd", "9788808182159", 1.2, "note");

        InventoryManagement.loadProductsFromDB();

        assertTrue(InventoryManagement.productMap.get(1).getBarCode().equals("9788806222024"));
        assertTrue(InventoryManagement.productMap.get(2).getBarCode().equals("9788808182159"));

        ezshop.reset();
    }

    @Test
    public void loadOrdersMapFromDBTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, SQLException, InvalidQuantityException {
        ezshop.reset();

        InventoryManagement.createProductType("testUpdatePosition", "9788806222024", 1.2, "note");
        InventoryManagement.createProductType("testUpdateProd", "9788808182159", 1.2, "note");
        InventoryManagement.issueOrder("9788806222024", 10, 1.5);
        InventoryManagement.issueOrder("9788808182159", 10, 1.5);

        InventoryManagement.loadOrdersMap();

        assertTrue(InventoryManagement.orderMap.get(1).getProductCode().equals("9788806222024"));
        assertTrue(InventoryManagement.orderMap.get(2).getProductCode().equals("9788808182159"));

        ezshop.reset();
    }

}

