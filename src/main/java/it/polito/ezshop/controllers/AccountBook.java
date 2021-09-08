package it.polito.ezshop.controllers;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.exceptions.UnauthorizedException;
import it.polito.ezshop.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountBook {

    /**
     * This method stores a transaction received as parameter in the database
     */
    static public void addTransactionToDB(BalanceOperationObject op){
        try {
            Connection connection = DB.getConnectionToDB();
            String query = "INSERT INTO transactions(id, date, money, type)" +
                    "VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, op.getBalanceId());
            preparedStatement.setString(2, op.getDate().toString());
            preparedStatement.setDouble(3, op.getMoney());
            preparedStatement.setString(4, op.getType());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
    }

    /**
     * This method computes the next available ID for a record to be inserted in
     * the database
     */
    static public int computeNextId(){
        int maxId = 0;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT MAX(id) as maxId FROM transactions";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                maxId = resultSet.getInt("maxId");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return maxId + 1;
    }

    /**
     * This method performs some checks on the Sale Transaction received as parameter
     * before storing it in the database
     *
     * @return  false if the price of the transaction is lower than zero
     *          true otherwise
     *
     * @throws InvalidTransactionIdException if the transaction ID is null
     */
    public static boolean recordSaleTransaction(SaleTransactionObject st) throws InvalidTransactionIdException
    {
        if (st.getTicketNumber() == null)
            throw new InvalidTransactionIdException();
        if (st.getPrice() < 0)
            return false;
        BalanceOperationObject op = new BalanceOperationObject(computeNextId(), st);
        addTransactionToDB(op);
        return true;
    }

    /**
     * This method performs some checks on the Return Transaction received as parameter
     * before storing it in the database
     *
     * @return  false if the price of the transaction is lower than zero or if the balance
     *              is not sufficient to perform the return
     *          true otherwise
     *
     * @throws InvalidTransactionIdException if the transaction ID is null
     */
    public static boolean recordReturnTransaction(ReturnTransactionObject rt) throws InvalidTransactionIdException {
        if (rt.getReturnId() == null)
            throw new InvalidTransactionIdException();
        if(rt.getMoney() < 0)
            return false;
        if (computeBalance() - rt.getMoney() >= 0.0){
            BalanceOperationObject op = new BalanceOperationObject(computeNextId(), rt);
            addTransactionToDB(op);
        }
        else
            return false;

        return true;
    }

    /**
     * This method performs some checks on the Order received as parameter
     * before storing it in the database
     *
     * @return  false if the price of the order is lower than zero, if the order ID
     *              is null or if the balance is not sufficient to perform the order
     *          true otherwise
     */
    public static boolean recordOrderTransaction(OrderObject or) {
        if (or.getOrderId() == null)
            return false;
        if (or.getPricePerUnit() * or.getQuantity() < 0)
            return false;
        BalanceOperationObject op = new BalanceOperationObject(computeNextId(), or);
        if (computeBalance() - (or.getPricePerUnit() * or.getQuantity()) >= 0.0){
            addTransactionToDB(op);
        }
        else
            return false;
        return true;
    }


    /**
     * This method returns the actual balance of the system.
     *
     * @return the value of the current balance
     */
    public static double computeBalance()
    {
        double currentBalance = 0.0;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM balance";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                currentBalance = resultSet.getDouble("currentBalance");
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
        return currentBalance;
    }

    /**
     * This method record a balance update. <toBeAdded> can be both positive and nevative. If positive the balance entry
     * should be recorded as CREDIT, if negative as DEBIT. The final balance after this operation should always be
     * positive.
     * It can be invoked only after a user with role "Administrator", "ShopManager" is logged in.
     *
     * @param toBeAdded the amount of money (positive or negative) to be added to the current balance. If this value
     *                  is >= 0 than it should be considered as a CREDIT, if it is < 0 as a DEBIT
     *
     * @return  true if the balance has been successfully updated
     *          false if toBeAdded + currentBalance < 0.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    public static boolean recordBalanceUpdate(double toBeAdded)
    {
        String type;

        if(computeBalance() + toBeAdded < 0.0)
            return false;

        if (toBeAdded >= 0)
            type = "Credit";
        else
            type = "Debit";
        BalanceOperationObject op = new BalanceOperationObject(computeNextId(), LocalDate.now(), toBeAdded, type);
        addTransactionToDB(op);

        return true;
    }

    /**
     * This method returns a list of all the balance operations (CREDIT,DEBIT,ORDER,SALE,RETURN) performed between two
     * given dates.
     * This method should understand if a user exchanges the order of the dates and act consequently to correct
     * them.
     * Both <from> and <to> are included in the range of dates and might be null. This means the absence of one (or
     * both) temporal constraints.
     *
     *
     * @param from the start date : if null it means that there should be no constraint on the start date
     * @param to the end date : if null it means that there should be no constraint on the end date
     *
     * @return All the operations on the balance whose date is <= to and >= from
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    public static List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to)
    {
        List<BalanceOperation> operations = new ArrayList<BalanceOperation>();

        if(from == null) from = LocalDate.of(0000, 1, 1);
        if(to == null) to = LocalDate.of(9999, 1, 1);

        if(from.compareTo(to) > 0){
            LocalDate temp = from;
            from = to;
            to = temp;
        }

        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM transactions WHERE date >= ? AND date <= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, from.toString());
            preparedStatement.setString(2, to.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate d = LocalDate.parse(resultSet.getString("date"));
                double money = resultSet.getDouble("money");
                operations.add(new BalanceOperationObject(id, d, money,
                        resultSet.getString("type")));
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
        return operations;
    }

}


