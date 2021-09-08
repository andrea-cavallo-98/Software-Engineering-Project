package it.polito.ezshop.integrationTests;

import it.polito.ezshop.controllers.AccountBook;
import it.polito.ezshop.controllers.DB;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.controllers.InventoryManagement;
import it.polito.ezshop.controllers.PaymentController;
import it.polito.ezshop.data.*;
import it.polito.ezshop.exceptions.*;

import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Objects;

import static org.junit.Assert.*;


public class PaymentControllerTest {
    private static final EZShop ezshop = new EZShop();

    @Test
    public void createSaleTransaction() throws InvalidTransactionIdException, SQLException {
        ezshop.reset();

       DB.alterJDBCUrl();
        Integer transactionId0 = PaymentController.startSaleTransaction();
        DB.restoreJDBCUrl();
        assertEquals(transactionId0, Integer.valueOf(-1));

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        //verifies that can be correctly retrieved from database
        assertNotNull(PaymentController.getSaleTransaction(transactionId));
        assertTrue(PaymentController.deleteSaleTransaction(transactionId));

        ezshop.reset();
    }

    @Test
    public void addProductToSaleTransactionRFIDTest() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidQuantityException, InvalidRFIDException, InvalidOrderIdException, InvalidTransactionIdException, SQLException {
        ezshop.reset();

        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addProductToSaleRFID(null, null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addProductToSaleRFID(0, null));

        assertThrows(InvalidRFIDException.class, () -> PaymentController.addProductToSaleRFID(1, null));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.addProductToSaleRFID(1, ""));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.addProductToSaleRFID(1, "1234567"));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.addProductToSaleRFID(1, "ABCDEFGHIJ"));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.addProductToSaleRFID(1, "1234567891234"));

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 12.0;
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        AccountBook.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor("9788832360103", pQuantity, 5)), java.util.Optional.ofNullable(1));
        assertTrue(InventoryManagement.recordOrderArrivalRFID(1, "000000010000"));

        DB.alterJDBCUrl();
        assertFalse(PaymentController.addProductToSaleRFID(transactionId, "000000010000"));
        DB.restoreJDBCUrl();

        assertTrue(PaymentController.addProductToSaleRFID(transactionId, "000000010000"));

        ezshop.reset();
    }

    @Test
    public void addProductToSaleTransaction() throws InvalidQuantityException, InvalidTransactionIdException, InvalidProductCodeException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, SQLException {
        ezshop.reset();

        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addProductToSale(null, pBarCode, 1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addProductToSale(-1, pBarCode, 1));
        assertThrows(InvalidQuantityException.class, () -> PaymentController.addProductToSale(transactionId, pBarCode, 0));
        assertThrows(InvalidQuantityException.class, () -> PaymentController.addProductToSale(transactionId, pBarCode, -1));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.addProductToSale(transactionId, "barcodeplaceholder", 4));
        assertFalse(PaymentController.addProductToSale(transactionId, pBarCode, pQuantity + 1));

        //add the existing product to the sale
        int addedQuantity = 1;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //get the related ticketEntry
        TicketEntry ticketEntry = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);

        assertNotNull(ticketEntry);
        assertEquals(ticketEntry.getAmount(), addedQuantity);
        assertEquals(ticketEntry.getBarCode(), Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getBarCode());
        assertEquals(Double.valueOf(ticketEntry.getPricePerUnit()), Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getPricePerUnit());
        assertEquals(ticketEntry.getProductDescription(), Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getProductDescription());

        double correctPrice = pPricePerUnit * addedQuantity;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));

        //try to add more product disconnecting the db and then deleting the product from the inventory
        DB.alterJDBCUrl();
        assertFalse(PaymentController.addProductToSale(productID, pBarCode, addedQuantity));
        DB.restoreJDBCUrl();
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), addedQuantity);

        //now deletes product from inventory end tries to add it
        assertTrue(InventoryManagement.deleteProductType(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getId()));
        assertFalse(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), addedQuantity);


        ezshop.reset();
    }

    @Test
    public void deleteProductFromSale() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException {
        ezshop.reset();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);


        //now delete the product 1
        int deletedQuantity = 20;
        assertThrows(InvalidQuantityException.class, () -> PaymentController.deleteProductFromSale(transactionId, pBarCode, -1));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.deleteProductFromSale(transactionId, "invalidbarcode", deletedQuantity));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteProductFromSale(null, pBarCode, deletedQuantity));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteProductFromSale(-1, pBarCode, deletedQuantity));


        assertTrue(PaymentController.deleteProductFromSale(transactionId, pBarCode, deletedQuantity));

        //check whether the ticket entry has been updated or not
        TicketEntry ticketEntryAfterDelete = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertEquals(ticketEntryAfterDelete.getBarCode(), Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getBarCode());
        assertEquals(ticketEntryAfterDelete.getAmount(), addedQuantity - deletedQuantity);

        double correctPrice = pPricePerUnit * (addedQuantity - deletedQuantity) + pPricePerUnit2 * addedQuantity2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        //assert that inventory quantities are update after the delete
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity + deletedQuantity));
        //add a little bit of product and delete all the remaining product
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertTrue(PaymentController.deleteProductFromSale(transactionId, pBarCode, 2 * addedQuantity - deletedQuantity));
        assertNull(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), pQuantity);
        correctPrice = pPricePerUnit2 * addedQuantity2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        //delete a product that doesn't belong to the transaction
        assertFalse(PaymentController.deleteProductFromSale(transactionId, pBarCode, deletedQuantity));

        //add a little bit of product, then disconnect db and try to remove it
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
       DB.alterJDBCUrl();
        assertFalse(PaymentController.deleteProductFromSale(transactionId, pBarCode, deletedQuantity));
        DB.restoreJDBCUrl();

        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), addedQuantity);

        //now deletes product from inventory end tries to delete it
        assertTrue(InventoryManagement.deleteProductType(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getId()));
        assertFalse(PaymentController.deleteProductFromSale(transactionId, pBarCode, deletedQuantity));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), addedQuantity);

        ezshop.reset();
    }
    @Test
    public void deleteProductFromSaleRFID() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException, InvalidRFIDException, InvalidOrderIdException {
        ezshop.reset();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 20;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        AccountBook.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode, pQuantity, pPricePerUnit)), java.util.Optional.ofNullable(1));
        String RFID1 = "000000010000";
        String RFID1a = "000000010001";

        assertTrue(InventoryManagement.recordOrderArrivalRFID(1, RFID1));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode2, pQuantity2, pPricePerUnit2)), java.util.Optional.ofNullable(2));
        String RFID2 = "000000020000";
        assertTrue(InventoryManagement.recordOrderArrivalRFID(2, RFID2));

        //add item1 to the sale
        assertTrue(PaymentController.addProductToSaleRFID(transactionId, RFID1));
        assertTrue(PaymentController.addProductToSaleRFID(transactionId, RFID1a));

        //add item2 to the sale
        assertTrue(PaymentController.addProductToSaleRFID(transactionId, RFID2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);


        //now delete the product 1
        assertThrows(InvalidRFIDException.class, () -> PaymentController.deleteProductFromSaleRFID(transactionId, ""));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.deleteProductFromSaleRFID(transactionId, null));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.deleteProductFromSaleRFID(transactionId, "011as11111"));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteProductFromSaleRFID(null, RFID1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteProductFromSaleRFID(-1, RFID1));


        assertTrue(PaymentController.deleteProductFromSaleRFID(transactionId, RFID1));

        //check whether the ticket entry has been updated or not
        TicketEntryObject ticketEntryAfterDelete = (TicketEntryObject) Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertEquals(ticketEntryAfterDelete.getBarCode(), Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getBarCode());
        assertEquals(ticketEntryAfterDelete.getAmount(), 1);
        assertFalse(ticketEntryAfterDelete.getRFIDs().stream().anyMatch(e -> e.equals(RFID1)));
        assertTrue(ticketEntryAfterDelete.getRFIDs().stream().anyMatch(e -> e.equals(RFID1a)));

        double correctPrice = pPricePerUnit + pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        //assert that inventory quantities are update after the delete
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - 1));
        //add a little bit of product and delete all the remaining product
        assertTrue(PaymentController.deleteProductFromSaleRFID(transactionId, RFID1a));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), pQuantity );
        correctPrice = pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        assertTrue(PaymentController.addProductToSaleRFID(transactionId, RFID1a));

        //delete a product that doesn't belong to the transaction
        assertFalse(PaymentController.deleteProductFromSaleRFID(transactionId, "000000400000"));

        //add a little bit of product, then disconnect db and try to remove it
        DB.alterJDBCUrl();
        assertFalse(PaymentController.deleteProductFromSaleRFID(transactionId, RFID1a));
        DB.restoreJDBCUrl();

        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - 1));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), 1);

        //now deletes product from inventory end tries to delete it
        assertTrue(InventoryManagement.deleteProductType(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getId()));
        assertFalse(PaymentController.deleteProductFromSaleRFID(transactionId, RFID1a));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), 1);

        ezshop.reset();
    }


    @Test
    public void applyDiscountRateToItem() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidDiscountRateException, SQLException {
        ezshop.reset();

        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);

        //check if current transaction price is correct
        double currentPrice = addedQuantity * pPricePerUnit + addedQuantity2 * pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);


        //apply the discountRate to the product
        double discountRate1 = 0.2;
        double discountRate2 = 2.0;
        double discountRate3 = 0.0;
        double discountRate4 = 0.5;
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.addDiscountRateToProduct(transactionId, "invalidbarcode", discountRate1));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.addDiscountRateToProduct(transactionId, "", discountRate1));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.addDiscountRateToProduct(transactionId, null, discountRate1));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addDiscountRateToProduct(null, pBarCode, discountRate1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addDiscountRateToProduct(-1, pBarCode, discountRate1));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode, discountRate1));
        currentPrice = addedQuantity * pPricePerUnit * (1 - discountRate1) + addedQuantity2 * pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, () -> PaymentController.addDiscountRateToProduct(transactionId, pBarCode2, discountRate2));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode2, discountRate1));
        currentPrice = addedQuantity * pPricePerUnit * (1 - discountRate1) + addedQuantity2 * pPricePerUnit2 * (1 - discountRate1);
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode2, discountRate3));
        currentPrice = addedQuantity * pPricePerUnit * (1 - discountRate1) + addedQuantity2 * pPricePerUnit2 * (1 - discountRate3);
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode2, discountRate4));
        currentPrice = addedQuantity * pPricePerUnit * (1 - discountRate1) + addedQuantity2 * pPricePerUnit2 * (1 - discountRate4);
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);


        ezshop.reset();
    }

    @Test
    public void addDiscountRateToSale() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidTransactionIdException, InvalidQuantityException, InvalidDiscountRateException, SQLException {
        ezshop.reset();

        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);

        //check if current transaction price is correct
        double currentPrice = addedQuantity * pPricePerUnit + addedQuantity2 * pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);

        double saleDiscount1 = 1.0;
        double saleDiscount2 = 2.0;
        double saleDiscount3 = 0.2;
        double saleDiscount4 = 0.5;
        assertFalse(PaymentController.addDiscountRateToSale(transactionId + 1, saleDiscount4));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addDiscountRateToSale(null, saleDiscount1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addDiscountRateToSale(-1, saleDiscount1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.addDiscountRateToSale(-1, saleDiscount1));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, () -> PaymentController.addDiscountRateToSale(transactionId, saleDiscount1));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, () -> PaymentController.addDiscountRateToSale(transactionId, saleDiscount2));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        currentPrice = (1 - saleDiscount3) * (addedQuantity * pPricePerUnit + addedQuantity2 * pPricePerUnit2);
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, saleDiscount3));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);
        currentPrice = (1 - saleDiscount4) * (addedQuantity * pPricePerUnit + addedQuantity2 * pPricePerUnit2);
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, saleDiscount4));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), currentPrice, 0.01);


        ezshop.reset();
    }

    @Test
    public void deleteSaleTransaction() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException {
        ezshop.reset();

        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //delete the transaction
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteSaleTransaction(null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.deleteSaleTransaction(-1));

       DB.alterJDBCUrl();
        assertFalse(PaymentController.deleteSaleTransaction(transactionId));
        DB.restoreJDBCUrl();
        //check if products' quantities are not changed into db
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), Integer.valueOf(pQuantity2 - addedQuantity2));

        //delete product from Inventory then add it again
        assertTrue(InventoryManagement.deleteProductType(productID2));
        assertFalse(PaymentController.deleteSaleTransaction(transactionId));

        productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2 - addedQuantity2));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), pQuantity);
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), Integer.valueOf(pQuantity2 - addedQuantity2));


        assertTrue(PaymentController.deleteSaleTransaction(transactionId));

        assertFalse(PaymentController.deleteSaleTransaction(transactionId));

        //check if products' quantities are correctly updated into db
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), pQuantity);
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), pQuantity2);

        assertNull(PaymentController.getSaleTransaction(transactionId));

        ezshop.reset();
    }

    @Test
    public void endSaleTransaction() throws InvalidQuantityException, InvalidProductCodeException, InvalidTransactionIdException, InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidLocationException, SQLException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.endSaleTransaction(null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.endSaleTransaction(-1));
        assertFalse(PaymentController.endSaleTransaction(transactionId + 1));
       DB.alterJDBCUrl();
        assertFalse(PaymentController.endSaleTransaction(transactionId));
        DB.restoreJDBCUrl();
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertFalse(PaymentController.endSaleTransaction(transactionId));

        assertTrue(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).isClosed());

        ezshop.reset();
    }

    @Test
    public void issueCashPayment() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidTransactionIdException, InvalidPaymentException, SQLException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertTrue(PaymentController.endSaleTransaction(transactionId));

        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() - 1), -1, 0.01);

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.issueCashPayment(-1, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice()));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.issueCashPayment(null, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice()));

        assertThrows(InvalidPaymentException.class, () -> PaymentController.issueCashPayment(transactionId, -1));

        //create last one transaction that won't fail
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);

        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice()), -1, 0.01);

        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), Integer.valueOf(pQuantity2 - addedQuantity2));
        assertTrue(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).isPayed());

        ezshop.reset();
    }

    @Test
    public void issueCardPayment() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidTransactionIdException, InvalidCreditCardException, SQLException {
        ezshop.reset();

        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;

        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertTrue(PaymentController.endSaleTransaction(transactionId));

        String validCreditCard = "4485370086510891";
        String invalidCreditCard = "4126178638225568";
        String insufficientCreditCard = "4716258050958645";

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.issueCardPayment(-1, validCreditCard));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.issueCardPayment(null, validCreditCard));

        assertThrows(InvalidCreditCardException.class, () -> PaymentController.issueCardPayment(transactionId, invalidCreditCard));

        assertFalse(PaymentController.issueCardPayment(transactionId, insufficientCreditCard));

       DB.alterJDBCUrl();
        assertFalse(PaymentController.issueCardPayment(transactionId, validCreditCard));
        DB.restoreJDBCUrl();

        assertTrue(PaymentController.issueCardPayment(transactionId, validCreditCard));

        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), Integer.valueOf(pQuantity2 - addedQuantity2));
        assertTrue(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).isPayed());


        ezshop.reset();
    }

    @Test
    public void getSaleTransaction() throws SQLException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidQuantityException, InvalidDiscountRateException {
        ezshop.reset();

        //retrieving an invalid transactionId
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getSaleTransaction(-1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getSaleTransaction(null));

        Integer transactionId = PaymentController.startSaleTransaction();
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        //retrieving an open transaction
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getSaleTransaction(null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getSaleTransaction(-1));
        assertNull(PaymentController.getNotPayedSaleTransaction(transactionId + 1));
        assertNotNull(PaymentController.getSaleTransaction(transactionId));
        SaleTransactionObject st = PaymentController.getSaleTransaction(transactionId);
        assert st != null;
        assertEquals(st.getTicketNumber(), transactionId);

        //add items to the current transactions
        double pDiscountRate = 0.2;
        double sDiscountRate = 0.1;
        int quantity = 100;
        double price = quantity * pPricePerUnit * (1 - pDiscountRate) * (1 - sDiscountRate);
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, quantity));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode, pDiscountRate));
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, sDiscountRate));

        //ends the transaction and then tries to retrieve it
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertNotNull(PaymentController.getSaleTransaction(transactionId));
        SaleTransactionObject st1 = PaymentController.getSaleTransaction(transactionId);
        assert st1 != null;
        assertEquals(st1.getTicketNumber(), transactionId);
        assertNotNull(st1.getEntry(pBarCode));
        assertEquals(st1.getDiscountRate(), sDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getDiscountRate(), pDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getAmount(), quantity);
        assertEquals(st1.getPrice(), price, 0.01);


        //pays for the sale. Now transaction will be retrieved from db
        assertEquals(PaymentController.issueCashPayment(transactionId, price), 0.00, 0.01);
       DB.alterJDBCUrl();
        assertNull(PaymentController.getSaleTransaction(transactionId));
        DB.restoreJDBCUrl();
        assertNotNull(PaymentController.getSaleTransaction(transactionId));
        //verifies that the transaction is correctly rebuilt starting from db data
        SaleTransactionObject st2 = PaymentController.getSaleTransaction(transactionId);
        assert st2 != null;
        assertEquals(st2.getTicketNumber(), transactionId);
        assertNotNull(st2.getEntry(pBarCode));
        assertEquals(st2.getDiscountRate(), sDiscountRate, 0.01);
        assertEquals(st2.getEntry(pBarCode).getDiscountRate(), pDiscountRate, 0.01);
        assertEquals(st2.getEntry(pBarCode).getAmount(), quantity);
        assertEquals(st2.getPrice(), price, 0.01);

        //assert null with a removed product
        assertTrue(InventoryManagement.deleteProductType(productID));
        assertNull(PaymentController.getSaleTransaction(transactionId));

        ezshop.reset();
    }

    @Test
    public void getOpenSaleTransaction() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidDiscountRateException, SQLException {
        ezshop.reset();

        //retrieving an invalid transactionId
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getOpenSaleTransaction(-1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getOpenSaleTransaction(null));

        Integer transactionId = PaymentController.startSaleTransaction();
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        //retrieving an open transaction
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getOpenSaleTransaction(null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getOpenSaleTransaction(-1));
        assertNull(PaymentController.getOpenSaleTransaction(transactionId + 1));
        assertNotNull(PaymentController.getOpenSaleTransaction(transactionId));
        SaleTransactionObject st = PaymentController.getOpenSaleTransaction(transactionId);
        assert st != null;
        assertEquals(st.getTicketNumber(), transactionId);


        //add items to the current transactions
        double pDiscountRate = 0.2;
        double sDiscountRate = 0.1;
        int quantity = 100;
        double price = quantity * pPricePerUnit * (1 - pDiscountRate) * (1 - sDiscountRate);
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, quantity));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode, pDiscountRate));
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, sDiscountRate));


        SaleTransactionObject st1 = PaymentController.getOpenSaleTransaction(transactionId);
        assert st1 != null;
        assertEquals(st1.getTicketNumber(), transactionId);
        assertNotNull(st1.getEntry(pBarCode));
        assertEquals(st1.getDiscountRate(), sDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getDiscountRate(), pDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getAmount(), quantity);
        assertEquals(st1.getPrice(), price, 0.01);

        //ends the transaction and then tries to retrieve it
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertNull(PaymentController.getOpenSaleTransaction(transactionId));
        ezshop.reset();

    }

    @Test
    public void getNotPayedSaleTransaction() throws InvalidQuantityException, InvalidProductCodeException, InvalidTransactionIdException, InvalidDiscountRateException, SQLException, InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidLocationException {
        ezshop.reset();

        //retrieving an invalid transactionId
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getNotPayedSaleTransaction(-1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getNotPayedSaleTransaction(null));

        Integer transactionId = PaymentController.startSaleTransaction();
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        //retrieving an open transaction
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getNotPayedSaleTransaction(null));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.getNotPayedSaleTransaction(-1));
        assertNull(PaymentController.getNotPayedSaleTransaction(transactionId + 1));

        //add items to the current transactions
        double pDiscountRate = 0.2;
        double sDiscountRate = 0.1;
        int quantity = 100;
        double price = quantity * pPricePerUnit * (1 - pDiscountRate) * (1 - sDiscountRate);
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, quantity));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId, pBarCode, pDiscountRate));
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, sDiscountRate));

        //ends the transaction and then tries to retrieve it
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        SaleTransactionObject st1 = PaymentController.getNotPayedSaleTransaction(transactionId);
        assert st1 != null;
        assertEquals(st1.getTicketNumber(), transactionId);
        assertNotNull(st1.getEntry(pBarCode));
        assertEquals(st1.getDiscountRate(), sDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getDiscountRate(), pDiscountRate, 0.01);
        assertEquals(st1.getEntry(pBarCode).getAmount(), quantity);
        assertEquals(st1.getPrice(), price, 0.01);


        ezshop.reset();
    }

    @Test
    public void startReturn() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));
        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        //create last one transaction that won't fail
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);

        Integer returnID = PaymentController.startReturnTransaction(transactionId + 1);
        assertEquals(returnID, Integer.valueOf(-1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.startReturnTransaction(-1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.startReturnTransaction(null));

        returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));
        ezshop.reset();
    }

    @Test
    public void returnProductRFID() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException, InvalidRFIDException, InvalidOrderIdException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 20;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        AccountBook.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode, pQuantity, pPricePerUnit)), java.util.Optional.ofNullable(1));
        String RFID1 = "000000010000";
        String RFID1a = "000000010001";
        String RFID1b = "000000010002";
        String RFID1c = "000000010003";

        assertTrue(InventoryManagement.recordOrderArrivalRFID(1, RFID1));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode2, pQuantity2, pPricePerUnit2)), java.util.Optional.ofNullable(2));
        String RFID2 = "000000020000";
        assertTrue(InventoryManagement.recordOrderArrivalRFID(2, RFID2));


        //add item1 to the sale
        assertTrue(PaymentController.addProductToSaleRFID(transactionId,RFID1));
        assertTrue(PaymentController.addProductToSaleRFID(transactionId,RFID1a));
        assertTrue(PaymentController.addProductToSaleRFID(transactionId,RFID1b));

        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);

        //create the return transaction
        Integer returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.returnProductRFID(-1, RFID1));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.returnProductRFID(null, RFID2));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.returnProductRFID(returnID, ""));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.returnProductRFID(returnID, "1233213a11"));
        assertThrows(InvalidRFIDException.class, () -> PaymentController.returnProductRFID(returnID, null));

        assertFalse(PaymentController.returnProductRFID(returnID, "000000300000"));
        assertFalse(PaymentController.returnProductRFID(returnID+1, RFID1));
        assertFalse(PaymentController.returnProductRFID(returnID, RFID2));
        assertFalse(PaymentController.returnProductRFID(returnID, RFID1c));



        assertTrue(PaymentController.returnProductRFID(returnID, RFID1));
        assertEquals(Objects.requireNonNull(PaymentController.getOpenReturnTransaction(returnID)).getItem(pBarCode).getAmount(),  1);
        assertFalse(PaymentController.returnProductRFID(returnID, RFID1));
        assertTrue(PaymentController.returnProductRFID(returnID, RFID1a));
        assertEquals(Objects.requireNonNull(PaymentController.getOpenReturnTransaction(returnID)).getItem(pBarCode).getAmount(), 2);


        ezshop.reset();

    }
    @Test
    public void returnProduct() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        //create last one transaction that won't fail
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);
        //create the return transaction
        Integer returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.returnProduct(-1, pBarCode, addedQuantity));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.returnProduct(null, pBarCode, addedQuantity));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.returnProduct(returnID, "wrongbarcode", addedQuantity));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.returnProduct(returnID, "", addedQuantity));
        assertThrows(InvalidProductCodeException.class, () -> PaymentController.returnProduct(returnID, null, addedQuantity));

        assertThrows(InvalidQuantityException.class, () -> PaymentController.returnProduct(returnID, pBarCode, 0));
        assertThrows(InvalidQuantityException.class, () -> PaymentController.returnProduct(returnID, pBarCode, -1));

        assertFalse(PaymentController.returnProduct(returnID, pBarCode, addedQuantity + 1));
        assertFalse(PaymentController.returnProduct(returnID, pBarCode2, 4));
        assertFalse(PaymentController.returnProduct(transactionId + 1, pBarCode, addedQuantity));
        assertFalse(PaymentController.returnProduct(transactionId, "0123456789012", addedQuantity));


        assertTrue(PaymentController.returnProduct(returnID, pBarCode, addedQuantity - 10));
        assertEquals(Objects.requireNonNull(PaymentController.getOpenReturnTransaction(returnID)).getItem(pBarCode).getAmount(), addedQuantity - 10);
        assertFalse(PaymentController.returnProduct(returnID, pBarCode, addedQuantity));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode, 10));
        assertEquals(Objects.requireNonNull(PaymentController.getOpenReturnTransaction(returnID)).getItem(pBarCode).getAmount(), addedQuantity);
        assertEquals(Objects.requireNonNull(PaymentController.getOpenReturnTransaction(returnID)).getItem(pBarCode).getAmount(), addedQuantity);


        ezshop.reset();

    }
    @Test
    public void endReturn() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add items to the sale
        int addedQuantity = 50;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        int addedQuantity2 = 70;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);


        //create the return transaction
        Integer returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));

        assertTrue(PaymentController.returnProduct(returnID, pBarCode, addedQuantity - 10));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode2, addedQuantity2));

        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.endReturnTransaction(-1, true));
        assertThrows(InvalidTransactionIdException.class, () -> PaymentController.endReturnTransaction(null, true));
        assertFalse(PaymentController.endReturnTransaction(returnID + 1, true));
        assertFalse(PaymentController.endReturnTransaction(returnID + 1, false));
       DB.alterJDBCUrl();
        assertFalse(PaymentController.endReturnTransaction(returnID, true));
        DB.restoreJDBCUrl();
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - addedQuantity));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), Integer.valueOf(pQuantity2 - addedQuantity2));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), addedQuantity);
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode2).getAmount(), addedQuantity2);


        assertTrue(PaymentController.endReturnTransaction(returnID, true));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - 10));
        assertEquals(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode2)).getQuantity(), pQuantity2);
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), 10);
        assertNull(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode2));
        ezshop.reset();
    }

    @Test
    public void issueReturnCashPayment() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException, InvalidDiscountRateException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add items to the sale
        int addedQuantity = 50;
        double discount1 = 0.2;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId,pBarCode,discount1));
        int addedQuantity2 = 70;
        double discount2 = 0.3;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId,pBarCode2,discount2));
        double discountS = 0.5;
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, discountS));

        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);

        //create the return transaction
        Integer returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode, addedQuantity - 10));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode2, addedQuantity2));

        double price = (pPricePerUnit*(addedQuantity-10)*(1-discount1) + pPricePerUnit2*addedQuantity2*(1-discount2))*(1-discountS);

        assertThrows(InvalidTransactionIdException.class,()->PaymentController.issueReturnCashPayment(-1));
        assertThrows(InvalidTransactionIdException.class,()->PaymentController.issueReturnCashPayment(null));
        assertEquals(PaymentController.issueReturnCashPayment(returnID+1), -1,0.01);
        assertEquals(Objects.requireNonNull(PaymentController.getReturnTransaction(returnID)).getMoney(), price, 0.01);
        assertEquals(PaymentController.issueReturnCashPayment(returnID), Objects.requireNonNull(PaymentController.getReturnTransaction(returnID)).getMoney(),0.01);

        ezshop.reset();
    }
    @Test
    public void issueReturnCardPayment() throws SQLException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException, InvalidDiscountRateException, InvalidCreditCardException {
        ezshop.reset();
        //pre-load local maps from db
        //InventoryManagement.loadProductsFromDB();
        Integer transactionId = PaymentController.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;
        //create mock products
        Integer productID = InventoryManagement.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(InventoryManagement.updatePosition(productID, "1-a-3"));
        assertTrue(InventoryManagement.updateQuantity(productID, pQuantity));

        Integer productID2 = InventoryManagement.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(InventoryManagement.updatePosition(productID2, "1-b-3"));
        assertTrue(InventoryManagement.updateQuantity(productID2, pQuantity2));

        //add items to the sale
        int addedQuantity = 50;
        double discount1 = 0.2;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId,pBarCode,discount1));
        int addedQuantity2 = 70;
        double discount2 = 0.3;
        assertTrue(PaymentController.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(PaymentController.addDiscountRateToProduct(transactionId,pBarCode2,discount2));
        double discountS = 0.5;
        assertTrue(PaymentController.addDiscountRateToSale(transactionId, discountS));

        assertTrue(PaymentController.endSaleTransaction(transactionId));
        assertEquals(PaymentController.issueCashPayment(transactionId, Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice() + 100), 100, 0.01);

        //create the return transaction
        Integer returnID = PaymentController.startReturnTransaction(transactionId);
        assertNotEquals(returnID, Integer.valueOf(-1));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode, addedQuantity - 10));
        assertTrue(PaymentController.returnProduct(returnID, pBarCode2, addedQuantity2));

        double price = (pPricePerUnit*(addedQuantity-10)*(1-discount1) + pPricePerUnit2*addedQuantity2*(1-discount2))*(1-discountS);

        assertThrows(InvalidTransactionIdException.class,()->PaymentController.issueReturnCreditCardPayment(-1,"4485370086510891"));
        assertThrows(InvalidTransactionIdException.class,()->PaymentController.issueReturnCreditCardPayment(null,"4485370086510891"));
        assertThrows(InvalidCreditCardException.class, ()-> PaymentController.issueReturnCreditCardPayment(returnID, "4485370086510881"));
        assertEquals(PaymentController.issueReturnCreditCardPayment(returnID, "6011547701099495"), -1,0.01);
        assertEquals(PaymentController.issueReturnCreditCardPayment(returnID+1, "4485370086510891"), -1,0.01);
        assertEquals(Objects.requireNonNull(PaymentController.getReturnTransaction(returnID)).getMoney(), price, 0.01);
        assertEquals(PaymentController.issueReturnCreditCardPayment(returnID,"4485370086510891"), Objects.requireNonNull(PaymentController.getReturnTransaction(returnID)).getMoney(),0.01);

        ezshop.reset();
    }
}

