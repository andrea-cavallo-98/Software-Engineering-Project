package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.Customer;
import it.polito.ezshop.controllers.CustomerManagement;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.controllers.DB;
import it.polito.ezshop.exceptions.InvalidCustomerCardException;
import it.polito.ezshop.exceptions.InvalidCustomerIdException;
import it.polito.ezshop.exceptions.InvalidCustomerNameException;
import org.junit.Test;

import java.sql.*;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
@SuppressWarnings("all")
public class CustomerManagementIntegrationTest {
    private static final EZShop ezshop = new EZShop();
    @Test
    public void defineCustomerTest() throws InvalidCustomerNameException, InvalidCustomerIdException, SQLException {
        // Checks on customer name parameter
        assertThrows(InvalidCustomerNameException.class, () -> CustomerManagement.defineCustomer(""));
        assertThrows(InvalidCustomerNameException.class, () -> CustomerManagement.defineCustomer(null));
        // Check on db failures
        DB.alterJDBCUrl();
        assertEquals(Integer.valueOf(-1), CustomerManagement.defineCustomer("customerName"));
        DB.restoreJDBCUrl();
        // Check correct flow
        Integer id = CustomerManagement.defineCustomer("customerName");
        assertNotEquals(id, Integer.valueOf(-1));
        Customer customer = CustomerManagement.getCustomer(id);
        assertNotNull(customer);
        assertEquals(customer.getCustomerName(), "customerName");
        ezshop.reset();
    }
    @Test
    public void modifyCustomerTest() throws InvalidCustomerIdException, InvalidCustomerNameException, InvalidCustomerCardException, SQLException {
        ezshop.reset();
        // Checks on parameters
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.modifyCustomer(null, "newCustomerName", "newCustomerCard"));
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.modifyCustomer(0, "newCustomerName", "newCustomerCard"));
        assertThrows(InvalidCustomerNameException.class, () -> CustomerManagement.modifyCustomer(1, null, "newCustomerCard"));
        assertThrows(InvalidCustomerNameException.class, () -> CustomerManagement.modifyCustomer(1, "", "newCustomerCard"));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyCustomer(1, "newCustomerName", null));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyCustomer(1, "newCustomerName", ""));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyCustomer(1, "newCustomerName", "1234"));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyCustomer(1, "newCustomerName", "123456789123"));
        // Check on non-existing customer
        assertFalse(CustomerManagement.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", "1234567891"));
        Integer idAdded = CustomerManagement.defineCustomer("customerName");
        // Check on db failures
        DB.alterJDBCUrl();
        assertFalse(CustomerManagement.modifyCustomer(idAdded, "newCustomerName", "1234567891"));
        DB.restoreJDBCUrl();
        // Check of correct flow
        assertTrue(CustomerManagement.modifyCustomer(idAdded, "newCustomerName", "1234567891"));
        ezshop.reset();
    }
    @Test
    public void deleteCustomerTest() throws InvalidCustomerNameException, InvalidCustomerIdException, SQLException {
        ezshop.reset();
        // Checks on parameters
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.deleteCustomer(null));
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.deleteCustomer(0));
        // Check on non-existing customer
        assertFalse(CustomerManagement.deleteCustomer(Integer.MAX_VALUE));
        Integer idAdded = CustomerManagement.defineCustomer("customerName");
        // Check on db failures
        DB.alterJDBCUrl();
        assertFalse(CustomerManagement.deleteCustomer(idAdded));
        DB.restoreJDBCUrl();
        // Check on correct flow
        assertTrue(CustomerManagement.deleteCustomer(idAdded));
        ezshop.reset();
    }
    @Test
    public void getCustomerByIdTest() throws InvalidCustomerIdException, SQLException, InvalidCustomerNameException {
        ezshop.reset();
        // Checks on parameters
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.getCustomer(null));
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.getCustomer(0));
        // Check on non-existing customer
        assertNull(CustomerManagement.getCustomer(Integer.MAX_VALUE));
        Integer idAdded = CustomerManagement.defineCustomer("customerName");
        // Check on db failures
        DB.alterJDBCUrl();
        assertNull(CustomerManagement.getCustomer(idAdded));
        DB.restoreJDBCUrl();
        // Check on correct flow
        Customer customerRetrieved = CustomerManagement.getCustomer(idAdded);
        assertNotNull(customerRetrieved);
        assertEquals(idAdded, customerRetrieved.getId());
        assertEquals("customerName", customerRetrieved.getCustomerName());
        ezshop.reset();
    }
    @Test
    public void getCustomerByNameTest() throws InvalidCustomerNameException, SQLException {
        ezshop.reset();
        // Checks on parameters
        assertNull(CustomerManagement.getCustomerByName(null));
        assertNull(CustomerManagement.getCustomerByName(""));
        // Check on non-existing customer
        assertNull(CustomerManagement.getCustomerByName("customerNameNotPresent"));
        String customerName = "customerName";
        Integer idAdded = CustomerManagement.defineCustomer(customerName);
        // Check on db failures
        DB.alterJDBCUrl();
        assertNull(CustomerManagement.getCustomerByName(customerName));
        DB.restoreJDBCUrl();
        // Check on correct flow
        Customer customerRetrieved = CustomerManagement.getCustomerByName(customerName);
        assertNotNull(customerRetrieved);
        assertEquals(idAdded, customerRetrieved.getId());
        assertEquals(customerName, customerRetrieved.getCustomerName());
        ezshop.reset();
    }
    @Test
    public void getAllCustomersTest() throws SQLException, InvalidCustomerNameException {
        ezshop.reset();
        // Check on correct flow
        Integer[] idsAdded = new Integer[10];
        int i;
        for(i = 0; i < 10; i++) {
            idsAdded[i] = CustomerManagement.defineCustomer("customerName" + i);
        }

        // Check on db failures
        DB.alterJDBCUrl();
        assertNull(CustomerManagement.getAllCustomers());
        DB.restoreJDBCUrl();

        List<Customer> customersRetrieved = CustomerManagement.getAllCustomers();
        assertNotNull(customersRetrieved);
        assertEquals(10, customersRetrieved.size());
        for(i = 0; i < 10; i++) {
            Customer customerRetrieved = customersRetrieved.get(i);
            assertEquals(idsAdded[i], customerRetrieved.getId());
            assertEquals("customerName" + i, customerRetrieved.getCustomerName());
        }
        ezshop.reset();
    }
    @Test
    public void generateCardCodeTest() {
        String generatedCardCode = CustomerManagement.generateCardCode();
        assertNotNull(generatedCardCode);
        assertTrue(Pattern.compile("\\d{10}").matcher(generatedCardCode).matches());
    }
    @Test
    public void cardExistsTest() throws SQLException {
        ezshop.reset();
        // Check on db failures
        DB.alterJDBCUrl();
        assertFalse(CustomerManagement.cardExists("1234567891"));
        DB.restoreJDBCUrl();
        // Check that returns false if a card does not exist
        assertFalse(CustomerManagement.cardExists("1234567891"));
        // Check that returns true if a card exists
        String cardCodeToAdd = CustomerManagement.generateCardCode();
        Connection connection = DB.getConnectionToDB();
        String sql = "INSERT INTO loyaltyCards(cardCode, customerName) VALUES(?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, cardCodeToAdd);
        preparedStatement.setString(2, "");
        assertNotEquals(0, preparedStatement.executeUpdate());
        assertTrue(CustomerManagement.cardExists(cardCodeToAdd));
        ezshop.reset();
    }
    @Test
    public void createCardTest() throws SQLException {
        ezshop.reset();
        // Check on db failures
        DB.alterJDBCUrl();
        assertEquals("", CustomerManagement.createCard());
        DB.restoreJDBCUrl();
        // Check on correct flow
        assertNotEquals("", CustomerManagement.createCard());
        ezshop.reset();
    }
    @Test
    public void attachCardToCustomerTest() throws SQLException, InvalidCustomerIdException, InvalidCustomerCardException, InvalidCustomerNameException {
        ezshop.reset();
        // Checks on parameters
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.attachCardToCustomer("1234567891", null));
        assertThrows(InvalidCustomerIdException.class, () -> CustomerManagement.attachCardToCustomer("1234567891", 0));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.attachCardToCustomer(null, 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.attachCardToCustomer("", 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.attachCardToCustomer("1234", 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.attachCardToCustomer("12345678912", 1));
        // Check on db failures
        DB.alterJDBCUrl();
        assertFalse(CustomerManagement.attachCardToCustomer("1234567891", 1));
        DB.restoreJDBCUrl();
        // Check on non-existing customer
        assertFalse(CustomerManagement.attachCardToCustomer("1234567891", Integer.MAX_VALUE));
        Integer idAdded = CustomerManagement.defineCustomer("customerName");
        // Check on non-existing card
        assertFalse(CustomerManagement.attachCardToCustomer("1234567891", idAdded));
        String cardCodeAdded = CustomerManagement.createCard();
        // Check that it assigns the customer to the card
        assertTrue(CustomerManagement.attachCardToCustomer(cardCodeAdded, idAdded));
        // Check that it returns false if another customer is already assigned to the card
        idAdded = CustomerManagement.defineCustomer("newCustomerName");
        assertFalse(CustomerManagement.attachCardToCustomer(cardCodeAdded, idAdded));
        ezshop.reset();
    }
    @Test
    public void modifyPointsOnCardTest() throws SQLException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        ezshop.reset();
        // Checks on parameter
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyPointsOnCard(null, 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyPointsOnCard("", 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyPointsOnCard("1234", 1));
        assertThrows(InvalidCustomerCardException.class, () -> CustomerManagement.modifyPointsOnCard("12345678912", 1));
        // Check on db failures
        DB.alterJDBCUrl();
        assertFalse(CustomerManagement.modifyPointsOnCard("1234567891", 1));
        DB.restoreJDBCUrl();
        // Check that it returns false if card does not exist
        assertFalse(CustomerManagement.modifyPointsOnCard("1234567891", 1));
        String cardCodeAdded = CustomerManagement.createCard();
        // Check that it returns false if card has no assigned customer
        assertFalse(CustomerManagement.modifyPointsOnCard(cardCodeAdded, 1));
        Integer idAdded = CustomerManagement.defineCustomer("customerName");
        assertTrue(CustomerManagement.attachCardToCustomer(cardCodeAdded, idAdded));
        // Check that it returns false if points would go negative
        assertFalse(CustomerManagement.modifyPointsOnCard(cardCodeAdded, -1));
        // Check correct flow
        assertTrue(CustomerManagement.modifyPointsOnCard(cardCodeAdded, 5));
        // Check again consistency of points
        assertFalse(CustomerManagement.modifyPointsOnCard(cardCodeAdded, -10));
        ezshop.reset();
    }
}
