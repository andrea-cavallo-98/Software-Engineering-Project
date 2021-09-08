package it.polito.ezshop.controllers;
import it.polito.ezshop.data.Order;
import it.polito.ezshop.data.ProductType;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.OrderObject;
import it.polito.ezshop.model.ProductTypeObject;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class InventoryManagement {
    static public Map<Integer, ProductType> productMap = new HashMap<>();
    static public Map<Integer, Order> orderMap = new HashMap<>();
    static public Integer lastProductID = 0;
    static public Integer lastOrderID = 0;

    static public void loadProductsFromDB() {
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM products";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            lastProductID = 0;
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                productMap.put(id, new ProductTypeObject(resultSet.getString("description"), id,
                        resultSet.getString("barCode"), resultSet.getDouble("pricePerUnit"), resultSet.getString("note"),
                        resultSet.getString("location"), resultSet.getInt("quantity")));
                lastProductID++;
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
    }

    static public void loadOrdersMap() {
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT * FROM orders";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            lastOrderID = 0;
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate d = LocalDate.parse(resultSet.getString("date"));
                orderMap.put(id, new OrderObject(resultSet.getInt("balanceid"), resultSet.getString("barCode"), resultSet.getDouble("pricePerUnit"),
                        resultSet.getInt("quantity"), resultSet.getString("status"), id, d));
                lastOrderID++;
            }
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
    }

    static public int getLastProductId() {
        return lastProductID;
    }

    static public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException {
        if(description == null || description.equals(""))
            throw new InvalidProductDescriptionException();
        if(pricePerUnit <= 0)
            throw new InvalidPricePerUnitException();
        if(productCode == null || productCode.equals("") || !isBarcodeValid(productCode))
            throw new InvalidProductCodeException();
        if(productMap.values().stream().anyMatch(v -> v.getBarCode().equals(productCode)))
            return -1;
        ProductType p = new ProductTypeObject(description, ++lastProductID, productCode, pricePerUnit, note, 0);
        try {
            Connection connection = DB.getConnectionToDB();
            String query = "INSERT INTO products(id, description, barCode, pricePerUnit, note, location, quantity)" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, lastProductID);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, productCode);
            preparedStatement.setDouble(4, pricePerUnit);
            preparedStatement.setString(5, note);
            preparedStatement.setString(6, null);
            preparedStatement.setInt(7, 0);
            preparedStatement.executeUpdate();
            productMap.put(lastProductID, p);
            return lastProductID;
        } catch (SQLException exception) {
            lastProductID--;
            //exception.printStackTrace();
            return -1;
        }
    }

    static public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException {
        if(id == null || id <= 0)
            throw new InvalidProductIdException();
        if(newDescription == null || newDescription.equals(""))
            throw new InvalidProductDescriptionException();
        if(newCode == null || newCode.equals("") || !isBarcodeValid(newCode))
            throw new InvalidProductCodeException();
        if(newPrice <= 0)
            throw new InvalidPricePerUnitException();
        if(productMap.get(id) == null)
            return false;
        for(ProductType pt: productMap.values()){
            if(pt.getBarCode().equals(newCode) && !pt.getId().equals(id))
                return false;
        }
        try {
            Connection connection = DB.getConnectionToDB();
            String query = "UPDATE products SET description=?, barCode=?, pricePerUnit=?, note=? WHERE ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newDescription);
            preparedStatement.setString(2, newCode);
            preparedStatement.setDouble(3, newPrice);
            preparedStatement.setString(4, newNote);
            preparedStatement.setInt(5, id);
            preparedStatement.executeUpdate();
            ProductType p = productMap.get(id);
            p.setBarCode(newCode);
            p.setProductDescription(newDescription);
            p.setNote(newNote);
            p.setPricePerUnit(newPrice);
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    static public boolean deleteProductType(Integer id) throws InvalidProductIdException {
        if(id == null || id <= 0)
            throw new InvalidProductIdException();
        if(productMap.get(id) == null)
            return false;
        try {
            Connection connection = DB.getConnectionToDB();
            String query = "DELETE FROM products WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            productMap.remove(id);
            // lastProductID--;
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    static public List<ProductType> getAllProductTypes() {
        List<ProductType> ret = new ArrayList<>();
        productMap.values().stream().forEach(v -> ret.add(new ProductTypeObject(v.getProductDescription(), v.getId(), v.getBarCode(), v.getPricePerUnit(), v.getNote(), v.getLocation(), v.getQuantity())));
        return ret;
    }

    static public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException {
        if(barCode == null || barCode.equals("") || !isBarcodeValid(barCode))
            throw new InvalidProductCodeException();
        try {
            return productMap.values().stream().filter(v -> v.getBarCode().equals(barCode)).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    static public List<ProductType> getProductTypesByDescription(String description) {
        String r_description = (description!=null) ? description : "";
        return productMap.values().stream().filter(v -> v.getProductDescription().contains(r_description)).collect(Collectors.toList());
    }

    static public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException {
        if(productId == null || productId <= 0)
            throw new InvalidProductIdException();
        if(productMap.get(productId) == null || productMap.get(productId).getLocation() == null || productMap.get(productId).getLocation().equals(""))
            return false;

        try {
            if((productMap.get(productId).getQuantity() + toBeAdded) < 0)
                return false;
            Connection connection = DB.getConnectionToDB();
            productMap.get(productId).setQuantity(productMap.get(productId).getQuantity() + toBeAdded);
            String query = "UPDATE products SET quantity=? WHERE ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (productMap.get(productId).getQuantity()));
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    static public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException {
        if(productId == null || productId <= 0)
            throw new InvalidProductIdException();
        if(newPos == null || newPos.equals("") || !newPos.matches("[0-9]+-[a-zA-Z]-[0-9]+"))
            throw new InvalidLocationException();
        if(productMap.get(productId) == null || productMap.values().stream().filter(v -> v.getLocation() != null).anyMatch(v -> v.getLocation().equals(newPos)))
            return false;

        try {
            Connection connection = DB.getConnectionToDB();
            String query = "UPDATE products SET location=? WHERE ID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newPos);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();
            productMap.get(productId).setLocation(newPos);
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }
    }

    static public Order getOrder(int orderId) {
        return orderMap.get(orderId);
    }

    static public int getLasOrderId() {
        return lastOrderID;
    }

    static public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException {
        if(productCode == null || productCode.equals("") || !isBarcodeValid(productCode))
            throw new InvalidProductCodeException();
        if(quantity <= 0)
            throw new InvalidQuantityException();
        if(pricePerUnit <= 0)
            throw new InvalidPricePerUnitException();
        if(productMap.values().stream().noneMatch(v -> v.getBarCode().equals(productCode)))
            return -1;
        try {
            orderMap.put(++lastOrderID, new OrderObject(productCode, pricePerUnit, quantity, "ISSUED", lastOrderID));
            Connection connection = DB.getConnectionToDB();
            String sql = "INSERT INTO orders(id, balanceid, barCode, pricePerUnit, quantity, status, date)" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, lastOrderID);
            preparedStatement.setInt(2, orderMap.get(lastOrderID).getBalanceId());
            preparedStatement.setString(3, orderMap.get(lastOrderID).getProductCode());
            preparedStatement.setDouble(4, orderMap.get(lastOrderID).getPricePerUnit());
            preparedStatement.setInt(5, orderMap.get(lastOrderID).getQuantity());
            preparedStatement.setString(6, orderMap.get(lastOrderID).getStatus());
            preparedStatement.setString(7, LocalDate.now().toString());
            preparedStatement.executeUpdate();
            return lastOrderID;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            orderMap.remove(lastOrderID);
            lastOrderID--;
            return -1;
        }
    }

    static public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException {
        if(productCode == null || productCode.equals("") || !isBarcodeValid(productCode))
            throw new InvalidProductCodeException();
        if(quantity <= 0)
            throw new InvalidQuantityException();
        if(pricePerUnit <= 0)
            throw new InvalidPricePerUnitException();
        if(productMap.values().stream().noneMatch(v -> v.getBarCode().equals(productCode)))
            return -1;
        Order o = new OrderObject(productCode, pricePerUnit, quantity, "ISSUED", ++lastOrderID);
        orderMap.put(lastOrderID, o);
        //HANDLE PAYMENT

        try {
            if(AccountBook.computeBalance() < (orderMap.get(o.getOrderId()).getPricePerUnit() * orderMap.get(o.getOrderId()).getQuantity())) {
                lastOrderID--;
                return -1;
            }
            Connection connection = DB.getConnectionToDB();
            String sql = "INSERT INTO orders(id, balanceid, barCode, pricePerUnit, quantity, status, date)" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, lastOrderID);
            preparedStatement.setInt(2, orderMap.get(lastOrderID).getBalanceId());
            preparedStatement.setString(3, orderMap.get(lastOrderID).getProductCode());
            preparedStatement.setDouble(4, orderMap.get(lastOrderID).getPricePerUnit());
            preparedStatement.setInt(5, orderMap.get(lastOrderID).getQuantity());
            preparedStatement.setString(6, orderMap.get(lastOrderID).getStatus());
            preparedStatement.setString(7, LocalDate.now().toString());
            preparedStatement.executeUpdate();
            AccountBook.recordOrderTransaction((OrderObject) o);
            orderMap.get(lastOrderID).setStatus("PAYED");
            sql = "UPDATE orders SET status = 'PAYED' WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, lastOrderID);
            if(preparedStatement.executeUpdate() != 0)
                return lastOrderID;
            else
                throw new SQLException();
        } catch (SQLException exception) {
            lastOrderID--;
            //exception.printStackTrace();
            return -1;
        }
    }

    static public boolean payOrder(Integer orderId) throws InvalidOrderIdException {
        if(orderId == null || orderId <= 0)
            throw new InvalidOrderIdException();
        if(orderMap.get(orderId) == null)
            return false;
        if(!productMap.values().stream().anyMatch(v -> v.getBarCode().equals(orderMap.get(orderId).getProductCode())))
            return false;
        if(orderMap.get(orderId).getStatus().equals("COMPLETED") || orderMap.get(orderId).getStatus().equals("PAYED"))
            return false;
        //HANDLE PAYMENT
        if(AccountBook.computeBalance() < (orderMap.get(orderId).getPricePerUnit() * orderMap.get(orderId).getQuantity()))
            return false;

        try {
            orderMap.get(orderId).setStatus("PAYED");
            Connection connection = DB.getConnectionToDB();
            String sql = "UPDATE orders SET status=? WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "PAYED");
            preparedStatement.setInt(2, orderId);
            preparedStatement.executeUpdate();
            AccountBook.recordOrderTransaction((OrderObject) orderMap.get(orderId));
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            orderMap.get(orderId).setStatus("ISSUED");
            return false;
        }
    }

    static public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom) throws InvalidOrderIdException, InvalidLocationException, InvalidRFIDException {
        if(orderId == null || orderId <= 0)
            throw new InvalidOrderIdException();
        String product = orderMap.get(orderId).getProductCode();
        if(productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getLocation() == null)
            throw new InvalidLocationException();
        Pattern PATTERN = Pattern.compile("\\d{12}");
        if(RFIDfrom == null || RFIDfrom.equals("") || !PATTERN.matcher(RFIDfrom).matches())
            throw new InvalidRFIDException();
        int quantity = orderMap.get(orderId).getQuantity();
        if(orderMap.get(orderId).getStatus().equals("COMPLETED") || orderMap.get(orderId).getStatus().equals("ISSUED"))
            return false;
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT COUNT(*) FROM product WHERE RFID >= ? AND RFID < ?";
            connection.prepareStatement(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, RFIDfrom);
            preparedStatement.setString(2, StringUtils.leftPad(String.valueOf(Integer.parseInt(RFIDfrom) + quantity), 12, "0"));
            ResultSet res = preparedStatement.executeQuery();
            res.next();
            if(res.getInt(1) > 0)
                throw new InvalidRFIDException();
            sql = "INSERT INTO product(RFID, barCode, sold) VALUES(?, ?, false)";
            for(int i=0; i<quantity; i++) {
                connection.prepareStatement(sql);
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, StringUtils.leftPad(String.valueOf(Integer.parseInt(RFIDfrom) + i), 12, "0"));
                preparedStatement.setString(2, product);
                if(preparedStatement.executeUpdate() != 1)
                    throw new SQLException();
            }
            sql = "UPDATE orders SET status = 'COMPLETED' WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);
            if(preparedStatement.executeUpdate() == 0)
                throw new SQLException();
            updateQuantity(productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getId(), orderMap.get(orderId).getQuantity());
            orderMap.get(orderId).setStatus("COMPLETED");
            return true;
        } catch (SQLException | InvalidProductIdException e) {
            return false;
        }
    }

    static public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, InvalidLocationException {
        if(orderId == null || orderId <= 0)
            throw new InvalidOrderIdException();
        if(orderMap.get(orderId) == null)
            return false;
        String product = orderMap.get(orderId).getProductCode();
        if(productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getLocation() == null)
            throw new InvalidLocationException();
        if(orderMap.get(orderId).getStatus().equals("COMPLETED") || orderMap.get(orderId).getStatus().equals("ISSUED"))
            return false;
        int toBeSet = productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getQuantity() + orderMap.get(orderId).getQuantity();
        if(orderMap.get(orderId).getStatus().equals("PAYED")) {
            //productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getId();
            try {
                Connection connection = DB.getConnectionToDB();
                String sql = "UPDATE orders SET status=? WHERE id=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, "COMPLETED");
                preparedStatement.setInt(2, orderId);
                preparedStatement.executeUpdate();
                sql = "UPDATE orders SET status = 'COMPLETED' WHERE id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, orderId);
                if(preparedStatement.executeUpdate() == 0)
                    throw new SQLException();
                updateQuantity(productMap.values().stream().filter(v -> v.getBarCode().equals(product)).findFirst().get().getId(), orderMap.get(orderId).getQuantity());
                orderMap.get(orderId).setStatus("COMPLETED");
                return true;
            } catch(InvalidProductIdException e) {
                System.out.println(e.getMessage());
                orderMap.get(orderId).setStatus("PAYED");
                return false;
            } catch (SQLException exception) {
                //exception.printStackTrace();
                orderMap.get(orderId).setStatus("PAYED");
                return false;
            }
        }
        return false;
    }

    static public List<Order> getAllOrders() {
        return new ArrayList<>(orderMap.values());
    }

    public static boolean isBarcodeValid(String barcode) throws InvalidProductCodeException {
        boolean p = false;
        if(barcode == null || barcode.isEmpty()) throw new InvalidProductCodeException();
        int length = barcode.length();
        // Check number of digits
        if (length < 12 || length > 14 ) {throw new InvalidProductCodeException(); }
        else {
            p = true;
        };
        //Check if a number
        try {Long.parseLong(barcode); } catch (Exception ex) {throw new InvalidProductCodeException(); }

        // Barcode validity algorithm
        int[] i = new int[2] ;
        if (length == 12 || length == 14) { i[0] = 3; i[1] = 1;} else {i[0] = 1; i[1] = 3;}
        String[] digits = barcode.split("");
        int checkDigit = Integer.parseInt(digits[length-1]);
        int sum = 0;

        for (int k = 0; k < length -1 ; k+=2 ) {
            sum += Integer.parseInt(digits[k]) * i[0];
            sum += Integer.parseInt(digits[k+1]) * i[1];
        }

        int check = 10 - sum % 10;

        return p; //check == checkDigit;

    }
}
