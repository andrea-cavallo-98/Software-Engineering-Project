package it.polito.ezshop.unitTests;

import it.polito.ezshop.controllers.AccountBook;
import it.polito.ezshop.controllers.DB;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class AccountBookUnitTest {

    @Test
    public void testComputeNextId() throws SQLException {

        // TC 1: empty database
        Connection connection = DB.getConnectionToDB();
        String sql = "DELETE FROM transactions";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

        int nextId = AccountBook.computeNextId();
        assertEquals(1, nextId);

        // TC 2: non-empty database
        String query = "INSERT INTO transactions(id, date, money, type)" +
                "VALUES(?, ?, ?, ?)";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, 1);
        preparedStatement.setString(2, "2021-05-17");
        preparedStatement.setDouble(3, 10.0);
        preparedStatement.setString(4, "Sale Transaction");
        preparedStatement.executeUpdate();

        nextId = AccountBook.computeNextId();
        assertEquals(2, nextId);

    }

    @Test
    public void testComputeBalance() throws SQLException {

        // TC1: empty transactions table
        Connection connection = DB.getConnectionToDB();
        String sql = "DELETE FROM transactions";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

        double balance = AccountBook.computeBalance();
        assertEquals(0.0, balance, 0.0001);

        // TC2: non-empty transactions table
        String query = "INSERT INTO transactions(id, date, money, type)" +
                "VALUES(?, ?, ?, ?)";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, 1);
        preparedStatement.setString(2, "2021-05-17");
        preparedStatement.setDouble(3, 10.0);
        preparedStatement.setString(4, "Sale Transaction");
        preparedStatement.executeUpdate();

        query = "INSERT INTO transactions(id, date, money, type)" +
                "VALUES(?, ?, ?, ?)";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, 2);
        preparedStatement.setString(2, "2021-05-18");
        preparedStatement.setDouble(3, 20.0);
        preparedStatement.setString(4, "Sale Transaction");
        preparedStatement.executeUpdate();

        balance = AccountBook.computeBalance();
        assertEquals(30.0, balance, 0.0001);
    }

}
