package it.polito.ezshop.unitTests;

import it.polito.ezshop.controllers.CustomerManagement;
import it.polito.ezshop.controllers.DB;
import it.polito.ezshop.data.EZShop;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class CustomerManagementUnitTest {
    private static final EZShop ezshop = new EZShop();

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
}
