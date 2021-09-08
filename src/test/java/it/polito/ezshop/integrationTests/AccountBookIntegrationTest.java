package it.polito.ezshop.integrationTests;

import it.polito.ezshop.controllers.AccountBook;
import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.model.BalanceOperationObject;
import it.polito.ezshop.model.OrderObject;
import it.polito.ezshop.model.ReturnTransactionObject;
import it.polito.ezshop.model.SaleTransactionObject;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AccountBookIntegrationTest {

    @Test
    public void addTransactionToDBTest() throws SQLException {

        EZShop ezshop = new EZShop(); ezshop.reset();

        int id = 10;
        LocalDate date = LocalDate.of(2021, 05, 25);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op1 = new BalanceOperationObject(id, date, money, type);

        // Correct Sale Transaction (how to check if SQLException occurs??)
        AccountBook.addTransactionToDB(op1);

        // Check if balance operation was properly stored
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(0);
        assertEquals(op.getBalanceId(), id);
        assertEquals(op.getDate(), date);
        assertEquals(op.getMoney(), money, 0.0001);
        assertEquals(op.getType(), type);

        ezshop.reset();
    }


    @Test
    public void recordSaleTransactionTest() throws SQLException, InvalidTransactionIdException {

        EZShop ezshop = new EZShop(); ezshop.reset();

        int id = 10;
        SaleTransactionObject st1 = new SaleTransactionObject(id);
        SaleTransactionObject st2 = new SaleTransactionObject(id);
        SaleTransactionObject st3 = new SaleTransactionObject(id);

        // Sale Transaction with Ticket Number == null
        st1.setTicketNumber(null);
        assertThrows(InvalidTransactionIdException.class, () -> AccountBook.recordSaleTransaction(st1));
        // Sale Transaction with price < 0
        st2.setPrice(-1.0);
        assertFalse(AccountBook.recordSaleTransaction(st2));
        // Correct Sale Transaction
        st3.setPrice(10.0);
        assertTrue(AccountBook.recordSaleTransaction(st3));

        // Check if transaction was properly inserted
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(0);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), 10.0, 0.0001);
        assertEquals(op.getType(), "Sale Transaction");

       ezshop.reset();
    }

    @Test
    public void recordReturnTransactionTest() throws SQLException, InvalidTransactionIdException {

        EZShop ezshop = new EZShop(); ezshop.reset();

        int saleTransactionID = 10, returnID = 20;
        double currentBalance = 50.0;
        ReturnTransactionObject rt1 = new ReturnTransactionObject(saleTransactionID, null);
        ReturnTransactionObject rt2 = new ReturnTransactionObject(saleTransactionID, returnID);
        ReturnTransactionObject rt3 = new ReturnTransactionObject(saleTransactionID, returnID);
        ReturnTransactionObject rt4 = new ReturnTransactionObject(saleTransactionID, returnID);

        // Add a Sale Transaction to the database to increase the balance
        SaleTransactionObject st = new SaleTransactionObject(1);
        st.setPrice(currentBalance);
        AccountBook.recordSaleTransaction(st);

        // Return Transaction with Transaction ID == null
        assertThrows(InvalidTransactionIdException.class, () -> AccountBook.recordReturnTransaction(rt1));
        // Return Transaction with price < 0
        rt2.setMoney(-1.0);
        assertFalse(AccountBook.recordReturnTransaction(rt2));
        // Return Transaction with price (100) > current balance (= 50)
        rt3.setMoney(100.0);
        assertFalse(AccountBook.recordReturnTransaction(rt3));
        // Correct Return Transaction (price = 30, current balance = 50)
        rt4.setMoney(30.0);
        assertTrue(AccountBook.recordReturnTransaction(rt4));

        // Check if transaction was properly inserted
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(1);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), -30.0, 0.0001);
        assertEquals(op.getType(), "Return Transaction");

        ezshop.reset();
    }


    @Test
    public void recordOrderTransactionTest() throws SQLException, InvalidTransactionIdException {

        EZShop ezshop = new EZShop(); ezshop.reset();

        double currentBalance = 50.0;
        OrderObject or1 = new OrderObject("ProductCode", 10.0, 5, "issued", null);
        OrderObject or2 = new OrderObject("ProductCode", 10.0, 5, "issued", 12);
        OrderObject or3 = new OrderObject("ProductCode", 10.0, 5, "issued", 12);
        OrderObject or4 = new OrderObject("ProductCode", 10.0, 5, "issued", 12);
        OrderObject or5 = new OrderObject("ProductCode", 10.0, 5, "issued", 12);

        // Add a Sale Transaction to the database to increase the balance
        SaleTransactionObject st = new SaleTransactionObject(1);
        st.setPrice(currentBalance);
        AccountBook.recordSaleTransaction(st);

        // Order Transaction with Order ID == null
        assertFalse(AccountBook.recordOrderTransaction(or1));
        // Order Transaction with price per unit < 0
        or2.setPricePerUnit(-1.0);
        assertFalse(AccountBook.recordOrderTransaction(or2));
        // Order Transaction with quantity < 0
        or3.setQuantity(-1);
        assertFalse(AccountBook.recordOrderTransaction(or3));
        // Return Transaction with total cost (100) > current balance (= 50)
        or4.setQuantity(10);
        assertFalse(AccountBook.recordOrderTransaction(or4));
        // Correct Order Transaction (total cost = 50, current balance = 50)
        assertTrue(AccountBook.recordOrderTransaction(or5));

        // Check if transaction was properly inserted
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(1);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), - 10.0 * 5, 0.0001);
        assertEquals(op.getType(), "Order Transaction");

       ezshop.reset();
    }

    @Test
    public void computeBalanceTest() throws SQLException, InvalidTransactionIdException {

        EZShop ezshop = new EZShop(); ezshop.reset();
        // check that balance is 0 if no transaction is stored in database
        assertEquals(AccountBook.computeBalance(), 0.0, 0.0001);
        // insert some transactions in the database
        SaleTransactionObject st1 = new SaleTransactionObject(1);
        SaleTransactionObject st2 = new SaleTransactionObject(2);
        double price1 = 50.0, price2 = 100.0;

        st1.setPrice(price1);
        st2.setPrice(price2);
        AccountBook.recordSaleTransaction(st1);
        AccountBook.recordSaleTransaction(st2);
        assertEquals(AccountBook.computeBalance(), price1 + price2, 0.00001);

        ezshop.reset();
    }

    @Test
    public void recordBalanceUpdateTest() throws SQLException, InvalidTransactionIdException {
        EZShop ezshop = new EZShop();
        ezshop.reset();
        double currentBalance = 50.0, price1 = currentBalance + 10.0, price2 = currentBalance - 10.0;
        // Add a Sale Transaction to the database to increase the balance
        SaleTransactionObject st = new SaleTransactionObject(1);
        st.setPrice(currentBalance);
        AccountBook.recordSaleTransaction(st);

        // Test a Debit balance update with price > current balance
        assertFalse(AccountBook.recordBalanceUpdate(-price1));

        // Test an acceptable Debit balance update
        assertTrue(AccountBook.recordBalanceUpdate(-price2));
        // Test an acceptable Credit balance update
        assertTrue(AccountBook.recordBalanceUpdate(price2));

        // Check that balance updates were properly stored
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(1);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), -price2, 0.0001);
        assertEquals(op.getType(), "Debit");

        op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(2);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), price2, 0.0001);
        assertEquals(op.getType(), "Credit");

       ezshop.reset();
    }

    @Test
    public void getCreditsAndDebitsTest() throws SQLException, InvalidTransactionIdException {

        EZShop ezshop = new EZShop(); ezshop.reset();

        // Insert some transactions in the database
        SaleTransactionObject st1 = new SaleTransactionObject(1);
        SaleTransactionObject st2 = new SaleTransactionObject(2);
        SaleTransactionObject st3 = new SaleTransactionObject(3);
        double price1 = 50.0, price2 = 100.0, price3 = 150.0;
        LocalDate date1 = LocalDate.of(2021, 05, 18);
        LocalDate date2 = LocalDate.of(2021, 05, 20);
        LocalDate date3 = LocalDate.of(2021, 05, 22);
        st1.setPrice(price1); st1.setDate(date1);
        st2.setPrice(price2); st2.setDate(date2);
        st3.setPrice(price3); st3.setDate(date3);
        AccountBook.recordSaleTransaction(st1);
        AccountBook.recordSaleTransaction(st2);
        AccountBook.recordSaleTransaction(st3);
        // Store transactions also in a local collection
        List<SaleTransactionObject> insertedOps = new ArrayList<SaleTransactionObject>();
        insertedOps.add(st1); insertedOps.add(st2); insertedOps.add(st3);

        // Try to get all transactions by inserting null dates
        List<BalanceOperation> ops =  AccountBook.getCreditsAndDebits(null, null);
        // Check that transactions are correct
        int index = 0;
        assertEquals(ops.size(), 3);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }

        // Check that the function works even if limit dates are inverted
        ops =  AccountBook.getCreditsAndDebits(LocalDate.of(2021, 05, 24), LocalDate.of(2021, 05, 18));
        // Check that transactions are correct
        index = 0;
        assertEquals(ops.size(), 3);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }

        // Check that filter on dates work properly
        ops =  AccountBook.getCreditsAndDebits(LocalDate.of(2021, 05, 18), LocalDate.of(2021, 05, 20));
        index = 0;
        assertEquals(ops.size(), 2);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }
         ezshop.reset();
    }
}
