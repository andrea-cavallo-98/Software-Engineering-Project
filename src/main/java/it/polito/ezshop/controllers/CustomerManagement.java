package it.polito.ezshop.controllers;

import it.polito.ezshop.data.Customer;
import it.polito.ezshop.exceptions.InvalidCustomerCardException;
import it.polito.ezshop.exceptions.InvalidCustomerIdException;
import it.polito.ezshop.exceptions.InvalidCustomerNameException;
import it.polito.ezshop.model.CustomerObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class CustomerManagement {
    private static final Pattern LOYALTY_CARD_PATTERN = Pattern.compile("\\d{10}");

    public static Integer defineCustomer(String customerName) throws InvalidCustomerNameException {
        if(customerName == null || customerName.equals("")) throw new InvalidCustomerNameException();
        try {
            Connection connection  = DB.getConnectionToDB();
            String sql = "INSERT INTO customers(customerName, customerCard, points) VALUES(?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, customerName);
            preparedStatement.setString(2, "");
            preparedStatement.setInt(3, 0);
            if(preparedStatement.executeUpdate() != 0) {
                Customer c = getCustomerByName(customerName);
                if(c != null) return c.getId();
            }
            return -1;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return -1;
        }
    }

    public static boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        if(id == null || id <= 0) throw new InvalidCustomerIdException();
        if(newCustomerName == null || newCustomerName.equals("")) throw new InvalidCustomerNameException();
        if(newCustomerCard == null || newCustomerCard.isEmpty() || !LOYALTY_CARD_PATTERN.matcher(newCustomerCard).matches()) throw new InvalidCustomerCardException();
        if(getCustomer(id) == null) return false;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "UPDATE customers SET customerName=?, customerCard=? WHERE rowid=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newCustomerName);
            preparedStatement.setString(2, newCustomerCard);
            preparedStatement.setInt(3, id);
            preparedStatement.executeUpdate();
            attachCardToCustomer(newCustomerCard, id);
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCustomer(Integer id) throws InvalidCustomerIdException {
        if(id == null || id <= 0) throw new InvalidCustomerIdException();
        if(getCustomer(id) == null) return false;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "DELETE FROM customers WHERE rowid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    public static Customer getCustomer(Integer id) throws InvalidCustomerIdException {
        if(id == null || id <= 0) throw new InvalidCustomerIdException();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT rowid, * FROM customers WHERE rowid=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String customerName = resultSet.getString("customerName");
                String customerCard = resultSet.getString("customerCard");
                int points = resultSet.getInt("points");
                return new CustomerObject(customerName, customerCard, id, points);
            }
            return null;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public static Customer getCustomerByName(String customerName) {
        if(customerName == null || customerName.isEmpty()) return null;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT rowid, * FROM customers WHERE customerName=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, customerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                int id = resultSet.getInt("rowid");
                String customerCard = resultSet.getString("customerCard");
                int points = resultSet.getInt("points");
                return new CustomerObject(customerName, customerCard, id, points);
            }
            return null;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public static List<Customer> getAllCustomers() {
        List<Customer> result = new ArrayList<>();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT rowid, * FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                int id = resultSet.getInt("rowid");
                String customerName = resultSet.getString("customerName");
                String customerCard = resultSet.getString("customerCard");
                int points = resultSet.getInt("points");
                result.add(new CustomerObject(customerName, customerCard, id, points));
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return null;
        }
        return result;
    }

    public static String generateCardCode() {
        StringBuilder res = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i < 10; i++) {
            int k = rand.nextInt(10);
            res.append(k);
        }
        return res.toString();
    }

    public static boolean cardExists(String cardCode) {
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM loyaltyCards WHERE cardCode=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cardCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    public static String createCard() {
        String newCard = generateCardCode();
        while(cardExists(newCard)) newCard = generateCardCode();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "INSERT INTO loyaltyCards(cardCode, customerName) VALUES(?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newCard);
            preparedStatement.setString(2, "");
            if(preparedStatement.executeUpdate() == 0) return "";
            return newCard;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return "";
        }
    }

    public static boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException {
        if(customerId == null || customerId <= 0) throw new InvalidCustomerIdException();
        if(customerCard == null || customerCard.isEmpty() || !LOYALTY_CARD_PATTERN.matcher(customerCard).matches()) throw new InvalidCustomerCardException();
        try {
            Customer customerToAssign = getCustomer(customerId);
            if(customerToAssign == null) return false;
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM loyaltyCards WHERE cardCode=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, customerCard);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String customerName = resultSet.getString("customerName");
                if(customerName != null && !customerName.isEmpty() && !customerName.equals(customerToAssign.getCustomerName())) return false;
                String sql1 = "UPDATE customers SET customerCard=? WHERE customerName=?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setString(1, customerCard);
                preparedStatement1.setString(2, customerToAssign.getCustomerName());
                preparedStatement1.executeUpdate();
                String sql2 = "UPDATE loyaltyCards SET customerName=? WHERE cardCode=?";
                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                preparedStatement2.setString(1, customerToAssign.getCustomerName());
                preparedStatement2.setString(2, customerCard);
                preparedStatement2.executeUpdate();
                return true;
            }
            return false;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    public static boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException {
        if(customerCard == null || customerCard.isEmpty() || !LOYALTY_CARD_PATTERN.matcher(customerCard).matches()) throw new InvalidCustomerCardException();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM loyaltyCards WHERE cardCode=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, customerCard);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String customerName = resultSet.getString("customerName");
                if(customerName == null || customerName.isEmpty()) return false;
                String sql1 = "SELECT rowid, * FROM customers WHERE customerName=?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setString(1, customerName);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                if(resultSet1.next()) {
                    int points = resultSet1.getInt("points");
                    if(pointsToBeAdded < 0 && points + pointsToBeAdded < 0) return false;
                    String sql2 = "UPDATE customers SET points=? WHERE customerName=?";
                    PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                    preparedStatement2.setInt(1, points + pointsToBeAdded);
                    preparedStatement2.setString(2, customerName);
                    preparedStatement2.executeUpdate();
                    return true;
                }
            }
            return false;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }
}
