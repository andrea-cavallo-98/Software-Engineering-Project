package it.polito.ezshop.controllers;

import it.polito.ezshop.data.ProductType;
import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.ReturnTransactionObject;
import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
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

public class PaymentController {
    static public Map<Integer, SaleTransactionObject> pendingSaleTransactions = new HashMap<>();
    static public Map<Integer, ReturnTransactionObject> pendingReturnTransactions = new HashMap<>();
    static public int lastId = 0;

    /*===============SALES MANAGEMENT==================*/
    /**
     * A new SaleTransaction is created and added to the transaction list, where pending transactions are waiting for
     * an actor to modify or close them. Also an entry is stored into db.
     *
     * @return the transaction ID
     */
    public static Integer startSaleTransaction() {
        try {
            Connection conn = DB.getConnectionToDB();
            String insert = "INSERT INTO saleTransactions DEFAULT VALUES;";
            String getID = "SELECT MAX(transactionId) FROM saleTransactions WHERE status='new'";
            if (conn.prepareStatement(insert).executeUpdate() != 0) {
                PreparedStatement prep = conn.prepareStatement(getID);
                ResultSet res = prep.executeQuery();
                if (res.next()) {
                    Integer id = res.getInt("MAX(transactionId)");
                    SaleTransactionObject transaction = new SaleTransactionObject(id);
                    pendingSaleTransactions.put(id, transaction);
                    return id;
                }
            }
            return -1;

        } catch (SQLException exception) {
            return -1;
        }
    }

    /**
     * Insert a new TicketEntry into a valid open transaction. This operation modifies db and stores the
     * the current product to the db. Temporary entries are stored to allow rollback if app fails.
     *
     * @param transactionId the Id of the transaction you want to act on
     * @param productCode   the barcode of the product you want to add
     * @param amount        the quantity
     * @return true if the product is successfully added to the requested transaction
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than 0
     */
    public static boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
        validateTransactionId(transactionId);
        if (amount < 1) throw new InvalidQuantityException();
        ProductType pt = InventoryManagement.getProductTypeByBarCode(productCode);
        SaleTransactionObject st = getOpenSaleTransaction(transactionId);
        if (pt == null || st == null || pt.getQuantity()<amount) return false;

        try{
            ProductType productType = InventoryManagement.getProductTypeByBarCode(productCode);
            if(productType == null) throw new Exception();
            if(!InventoryManagement.updateQuantity(productType.getId(), -1*amount)) throw new Exception();

            TicketEntry te = st.getEntry(productCode);
            if(te!=null){
                te.setAmount(te.getAmount()+amount);
            }
            else {
                te = new TicketEntryObject(pt, amount);
                st.addEntry(te);
            }

            st.setPrice(st.getPrice()+(amount*te.getPricePerUnit()*(1 -te.getDiscountRate())));


        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * This method adds a product to a sale transaction receiving  its RFID, decreasing the temporary amount of product available on the
     * shelves for other customers.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param RFID the RFID of the product to be added
     * @return  true if the operation is successful
     *          false   if the RFID does not exist,
     *                  if the transaction id does not identify a started and open transaction.
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidRFIDException if the RFID code is empty, null or invalid
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    static public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException {
        validateTransactionId(transactionId);
        Pattern PATTERN = Pattern.compile("\\d{12}");
        if(RFID == null || RFID.equals("") || !PATTERN.matcher(RFID).matches())
            throw new InvalidRFIDException();
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT barcode FROM product WHERE RFID = ?";
            connection.prepareStatement(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, RFID);
            ResultSet res = preparedStatement.executeQuery();
            if(res.next()) {
                String productCode = res.getString(1);
                ProductType pt = InventoryManagement.getProductTypeByBarCode(productCode);
                SaleTransactionObject st = getOpenSaleTransaction(transactionId);
                if (pt == null || st == null) return false;
                ProductType productType = InventoryManagement.getProductTypeByBarCode(productCode);
                if(productType == null) throw new Exception();

                if(!InventoryManagement.updateQuantity(productType.getId(), -1)) throw new Exception();
                sql = "UPDATE product SET sold=true WHERE RFID = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, RFID);
                if(preparedStatement.executeUpdate() == 0)
                    throw new Exception();

                TicketEntryObject te = (TicketEntryObject) st.getEntry(productCode);
                if(te!=null){
                    te.setAmount(te.getAmount()+1);
                }
                else {
                    te = new TicketEntryObject(pt, 1);
                    st.addEntry(te);
                }
                te.addRFID(RFID);
                st.setPrice(st.getPrice()+(te.getPricePerUnit()*(1 -te.getDiscountRate())));
                return true;
            }
        }
        catch (Exception throwables) {
            //throwables.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * Delete a certain qty of a product or the entire stock from the given sale transaction.
     * @param transactionId is the sale on which operate
     * @param productCode is the product to modify
     * @param amount is the quantity to decrease
     * @return true if the product qty is successfully modified
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than 0
     */
    public static boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
        validateTransactionId(transactionId);

        if(amount < 0) throw new InvalidQuantityException();

        if(productCode == null || productCode.equals("") || !InventoryManagement.isBarcodeValid(productCode)) throw new InvalidProductCodeException();

        SaleTransactionObject st = getSaleTransaction(transactionId);
        if (st == null || st.isPayed()) return false;



        List<TicketEntry> entries = st.getEntries();
        for (TicketEntry te : entries) {
            if (te.getBarCode().equals(productCode)) {
                if (amount >= 0 && te.getAmount() == amount) {
                    st.deleteEntry(productCode);
                } else if (amount >= 0 && te.getAmount() > amount) {
                    te.setAmount(te.getAmount() - amount);
                } else return false;
            try {
                st.setPrice(st.getPrice() - (amount * te.getPricePerUnit() * (1 - te.getDiscountRate())));
                ProductType productType = InventoryManagement.getProductTypeByBarCode(te.getBarCode());
                if(productType == null) throw new Exception();
                if(!InventoryManagement.updateQuantity(productType.getId(), amount)) throw new Exception();
                return true;
            } catch (Exception e) {
                te.setAmount(te.getAmount() + amount);
                return false;
            }
        }
    }
        return false;
    }

    /**
     * This method deletes a product from a sale transaction , receiving its RFID, increasing the temporary amount of product available on the
     * shelves for other customers.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param RFID the RFID of the product to be deleted
     *
     * @return  true if the operation is successful
     *          false   if the product code does not exist,
     *                  if the transaction id does not identify a started and open transaction.
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidRFIDException if the RFID is empty, null or invalid
     */
    public static boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException {
        validateTransactionId(transactionId);
        Pattern PATTERN = Pattern.compile("\\d{12}");
        if (RFID == null || RFID.equals("") || !PATTERN.matcher(RFID).matches())
            throw new InvalidRFIDException();

        SaleTransactionObject st = getSaleTransaction(transactionId);
        if (st == null || st.isPayed()) return false;


        List<TicketEntryObject> entries = st.getEntries().stream().map(e -> (TicketEntryObject) e).collect(Collectors.toList());
        for (TicketEntryObject te : entries) {
            if (te.getRFIDs().stream().anyMatch(e->e.equals(RFID))) {
                te.setAmount(te.getAmount() - 1);
                try {
                    Connection connection = DB.getConnectionToDB();
                    String sql = "UPDATE product SET sold=false WHERE RFID = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, RFID);
                    if(preparedStatement.executeUpdate() == 0)
                        throw new Exception();
                    st.setPrice(st.getPrice() - (te.getPricePerUnit() * (1 - te.getDiscountRate())));
                    ProductType productType = InventoryManagement.getProductTypeByBarCode(te.getBarCode());
                    if (productType == null) throw new Exception();
                    if (!InventoryManagement.updateQuantity(productType.getId(), 1)) throw new Exception();
                    te.setRFID(te.getRFIDs().stream().filter(e->! e.equals(RFID)).collect(Collectors.toList()));
                    return true;
                } catch (Exception e) {
                    te.setAmount(te.getAmount() + 1);
                    return false;
                }
            }
        }
        return false;
    }
    /**
     * This method, given a certain open transaction, applies a discount rate on a specific product
     * @param transactionId is the requested open transaction
     * @param productCode is the product on which apply the discount
     * @param discountRate is the discount rate to set
     * @return true if correctly applied
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidDiscountRateException if the discount rate is less then 0
     */
    public static boolean addDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, InvalidProductCodeException {
        validateTransactionId(transactionId);
        if(productCode == null || productCode.isEmpty() || !InventoryManagement.isBarcodeValid(productCode))
            throw new InvalidProductCodeException();
        if (discountRate < 0 || discountRate>=1) throw new InvalidDiscountRateException();
        SaleTransactionObject st = getOpenSaleTransaction(transactionId);
        if (st == null) return false;
        TicketEntry te = st.getEntry(productCode);
        if(te == null) return false;
        st.setPrice(st.getPrice()+te.getAmount()*te.getPricePerUnit()*te.getDiscountRate());
        te.setDiscountRate(discountRate);
        st.setPrice(st.getPrice()-te.getAmount()*te.getPricePerUnit()*te.getDiscountRate());
        return true;
    }

    /**
     * This method, given a certain open transaction, applies a discount rate to it
     * @param transactionId is the requested open transaction
     * @param discountRate is the discount rate to set
     * @return true if correctly applied
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidDiscountRateException if the discount rate is less then 0
     */
    public static boolean addDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException {
        validateTransactionId(transactionId);
        if (discountRate < 0 || discountRate>=1) throw new InvalidDiscountRateException();
        SaleTransactionObject st = getOpenSaleTransaction(transactionId);
        if (st == null) return false;
        st.setDiscountRate(discountRate);
        return true;
    }

    /**
     * This method can delete a pending transaction with a safe rollback. All the items are restored back into the
     * inventory before deleting the SaleTransactionObject
     *
     * @param transactionId is the transaction to abort
     * @return true whether the transaction is successfully aborted
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     */
    public static boolean deleteSaleTransaction(Integer transactionId)  throws InvalidTransactionIdException{
        try{
            SaleTransactionObject st = getSaleTransaction(transactionId);
            if(st==null || st.isPayed()) return false;
            Connection conn = DB.getConnectionToDB();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM saleTransactions WHERE transactionId = ?";
            PreparedStatement prep = conn.prepareStatement(sql);
            prep.setInt(1, transactionId);
            if(prep.executeUpdate()!=0){
                TicketEntryObject teo = null;
                for(TicketEntry te: st.getEntries()){
                    try{
                        ProductType pt = InventoryManagement.getProductTypeByBarCode(te.getBarCode());
                        if(pt == null)
                            throw new Exception();
                        if(!InventoryManagement.updateQuantity(pt.getId(), te.getAmount()))
                            throw new Exception();
                        te.setAmount(0);
                        teo = (TicketEntryObject) te;
                        if(teo.getRFIDs().size() != 0){
                            for(String RFID : teo.getRFIDs()){
                                sql = "UPDATE product SET sold=false WHERE RFID = ?";
                                prep = conn.prepareStatement(sql);
                                prep.setString(1, RFID);
                                if(prep.executeUpdate() == 0)
                                    throw new Exception();
                            }
                        }
                    }
                    catch(Exception e) {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                }
                pendingSaleTransactions.remove(transactionId);
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
            return false;
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * This method closes an open transaction
     * @param transactionId the Id of the target transaction
     * @return true if successfully closed
     * @throws InvalidTransactionIdException id the transactionId is <= 0 or if it is null
     */
    public static boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException{
        validateTransactionId(transactionId);
        SaleTransactionObject st = getOpenSaleTransaction(transactionId);
        if(st!=null){
            try {
                Connection conn = DB.getConnectionToDB();
                String update = "UPDATE saleTransactions SET status='closed' WHERE transactionId = ?";
                PreparedStatement prep = conn.prepareStatement(update);
                prep.setInt(1, transactionId);
                if(prep.executeUpdate()!=0){
                    return st.close();
                }
                else return false;
            } catch (SQLException exception) {
            }
        }
        return false;
    }



    /**
     * This method return a transaction (both persistent and temporary)
     * @param transactionId the Id of the target transaction
     * @return the transaction object or null
     * @throws InvalidTransactionIdException id the transactionId is <= 0 or if it is null
     */
    static public SaleTransactionObject getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException{
        validateTransactionId(transactionId);
        if(pendingSaleTransactions.get(transactionId) != null) return pendingSaleTransactions.get(transactionId);
            try {
                Connection conn = DB.getConnectionToDB();
                PreparedStatement prep = conn.prepareStatement("SELECT * FROM saleTransactions WHERE transactionId=? AND status ='payed'");
                prep.setInt(1,transactionId);
                ResultSet res = prep.executeQuery();
                if(res.next()) {
                    double discountRate = res.getDouble("discountRate");
                    SaleTransactionObject st = new SaleTransactionObject(res.getInt("transactionId"));
                    PreparedStatement pr1 = conn.prepareStatement("SELECT * FROM soldProducts WHERE transactionId=?");
                    pr1.setInt(1,transactionId);
                    ResultSet res1 = pr1.executeQuery();
                    while(res1.next()){
                        ProductType productType = InventoryManagement.getProductTypeByBarCode(res1.getString("productId"));
                        if(productType == null) throw new Exception();
                        TicketEntryObject te = new TicketEntryObject(productType, res1.getInt("quantity"));
                        te.setDiscountRate(res1.getDouble("discountRate"));
                        st.addEntry(te);
                        st.setPrice(st.getPrice() + te.getAmount()*te.getPricePerUnit()*(1-te.getDiscountRate()));
                        PreparedStatement pr2 = conn.prepareStatement("SELECT RFID FROM product WHERE transactionId=? AND barCode = ? AND sold=true");
                        pr2.setInt(1, transactionId);
                        pr2.setString(2, te.getBarCode());
                        ResultSet res2 = pr2.executeQuery();
                        while(res2.next()) te.addRFID(res2.getString(1));
                    }

                    st.setDiscountRate(discountRate);
                    st.close();
                    st.paymentIssued();
                    return st;
                }
            } catch (Exception exception) {
                return null;
            }
        return null;
    }

    /**
     * The method retrieves an open transaction from the pendingTransactions list
     * @param transactionId is the requested transaction
     * @return a SaleTransactionObject
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     */
    static public SaleTransactionObject getOpenSaleTransaction(Integer transactionId) throws InvalidTransactionIdException {
        validateTransactionId(transactionId);
        SaleTransactionObject st = pendingSaleTransactions.get(transactionId);
        if (st != null && st.isOpen()) return st;
        else return null;
    }

    /**
     * The method retrieves a transaction from the pendingTransactions list
     * @param transactionId is the requested transaction
     * @return a SaleTransactionObject
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     */
    static public SaleTransactionObject getNotPayedSaleTransaction(Integer transactionId) throws InvalidTransactionIdException {
        validateTransactionId(transactionId);
        SaleTransactionObject st = pendingSaleTransactions.get(transactionId);
        if (st != null && (st.isOpen() || st.isClosed())) return st;
        else return null;
    }


    static public double issueCashPayment(Integer transactionId, double cash) throws InvalidTransactionIdException, InvalidPaymentException {
        validateTransactionId(transactionId);
        if(cash <= 0){
            throw new InvalidPaymentException();
        }

        SaleTransactionObject st = getNotPayedSaleTransaction(transactionId);
        if(st == null) {
            return -1;
        }
        if(cash - st.getPrice() >= 0 && pushSoldItemsToDB(st) && recordPayment(st)) {
            AccountBook.recordSaleTransaction(st);
            pendingSaleTransactions.remove(transactionId);
            return cash - st.getPrice();
        }
        return -1;
    }

    public static boolean issueCardPayment(Integer transactionId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException {
        if(creditCard == null || !PaymentGateway.verifyCard(creditCard))
            throw new InvalidCreditCardException();
        validateTransactionId(transactionId);
        SaleTransactionObject st = getNotPayedSaleTransaction(transactionId);
        if(st == null) return false;
        if(!PaymentGateway.issuePayment(creditCard, st.getPrice())){
            return false;
        }
        if(pushSoldItemsToDB(st) && recordPayment(st)) {
            AccountBook.recordSaleTransaction(st);
            pendingSaleTransactions.remove(transactionId);
            return true;
        }
        PaymentGateway.issueTransfer(creditCard, st.getPrice());
        return false;
    }


    /*==================RETURNS MANAGEMENT===========================*/

    /**
     * This method starts a new Return transaction and verifies if the linked sale transaction
     * is valid.
     * @param saleId the linked sale transaction
     * @return the returnId or -1 if fails
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     */
    public static Integer startReturnTransaction(Integer saleId) throws InvalidTransactionIdException {
        validateTransactionId(saleId);
        SaleTransactionObject st = getSaleTransaction(saleId);
        if( st != null && st.isPayed()) {
            ReturnTransactionObject rt = new ReturnTransactionObject(st.getTicketNumber(), ++lastId);
            rt.setSaleDiscountRate(st.getDiscountRate());
            pendingReturnTransactions.put(rt.getReturnId(), rt);
            return lastId;
        }
        else return -1;
    }

    /**
     * This method add a product to a valid return transaction.
     * @param returnId the current returnId
     * @param productCode the product to be added
     * @param amount the quantity to be returned
     * @return  true or false if it succeeds or fails
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than or equal to 0
     */
    public static boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
        validateTransactionId(returnId);
        if(productCode == null || productCode.isEmpty() || !InventoryManagement.isBarcodeValid(productCode)) {
            throw new InvalidProductCodeException();
        }
        if(amount <= 0) throw new InvalidQuantityException();
        if(InventoryManagement.getProductTypeByBarCode(productCode) == null) return false;
        ReturnTransactionObject rt = getOpenReturnTransaction(returnId);
        if(rt==null) return false;
        SaleTransactionObject st = getSaleTransaction(rt.getSaleTransactionId());
        if(st==null) return false;
        TicketEntry te = st.getEntry(productCode);
        if (te == null) return false;

        if (amount <= 0 ) throw new InvalidQuantityException();
        if ( te.getAmount() < amount) return false;
        TicketEntryObject r_te = (TicketEntryObject) rt.getItem(productCode);
        if (r_te != null) {
            if (r_te.getAmount() + amount > te.getAmount()) return false;
            r_te.setAmount(r_te.getAmount() + amount);
        }
        else {
            r_te = new TicketEntryObject(te, amount);
            rt.addItem(r_te);
        }
        rt.updateMoney(r_te.getAmount()*r_te.getPricePerUnit()*(1-r_te.getDiscountRate()));
        return true;

    }
    /**
     * This method adds a product to the return transaction, starting from its RFID
     * This method DOES NOT update the product quantity
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the id of the return transaction
     * @param RFID the RFID of the product to be returned
     *
     * @return  true if the operation is successful
     *          false   if the the product to be returned does not exists,
     *                  if it was not in the transaction,
     *                  if the transaction does not exist
     *
     * @throws InvalidTransactionIdException if the return id is less ther or equal to 0 or if it is null
     * @throws InvalidRFIDException if the RFID is empty, null or invalid
     */
    public static boolean returnProductRFID(Integer returnId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException {
        validateTransactionId(returnId);
        Pattern PATTERN = Pattern.compile("\\d{12}");

        if (RFID == null || RFID.equals("") || !PATTERN.matcher(RFID).matches())
            throw new InvalidRFIDException();
        ProductType pt = null;
        String productCode = "";
        try {
            Connection connection = DB.getConnectionToDB();
            String sql = "SELECT barcode FROM product WHERE RFID = ?";
            connection.prepareStatement(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, RFID);
            ResultSet res = preparedStatement.executeQuery();
            if (res.next()) {
                productCode = res.getString(1);
                pt = InventoryManagement.getProductTypeByBarCode(productCode);
            } else return false;

            if (pt == null) return false;
            ReturnTransactionObject rt = getOpenReturnTransaction(returnId);
            if (rt == null) return false;
            SaleTransactionObject st = getSaleTransaction(rt.getSaleTransactionId());
            if (st == null) return false;
            TicketEntryObject te = (TicketEntryObject) st.getEntry(productCode);
            if (te == null || !te.getRFIDs().contains(RFID.replaceFirst("^0+(?!$)", ""))) return false;
            TicketEntryObject r_te = (TicketEntryObject) rt.getItem(productCode);
            if (r_te != null) {
                if (r_te.getRFIDs().stream().anyMatch(e -> e.equals(RFID))) return false;
                if (r_te.getAmount() + 1 > te.getAmount()) return false;
                r_te.setAmount(r_te.getAmount() + 1);
            } else {
                r_te = new TicketEntryObject(te, 1);
                rt.addItem(r_te);
            }
            r_te.addRFID(RFID);
            rt.updateMoney(r_te.getAmount() * r_te.getPricePerUnit() * (1 - r_te.getDiscountRate()));
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    /**
     * This method cloese a return transaction and commit changes to the inventory.
     * If commit is false, transaction is safely deleted
     * @param returnId the target returnId
     * @param commit indicates whether to commit or not
     * @return true or false if fails or not
     * @throws InvalidTransactionIdException if the return id is less ther or equal to 0 or if it is null
     */
    public static boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException {
        validateTransactionId(returnId);
        if(commit){

            try {
                Connection conn = DB.getConnectionToDB();
                conn.setAutoCommit(false);
                ReturnTransactionObject rt = getOpenReturnTransaction(returnId);
                if(rt==null) return false;
                rt.close();


                for (TicketEntry te : rt.getReturnedItems()) {
                    String getQty = "SELECT quantity FROM soldProducts WHERE productId = ? AND transactionId = ?";
                    PreparedStatement prep = conn.prepareStatement(getQty);
                    prep.setString(1, te.getBarCode());
                    prep.setInt(2, rt.getSaleTransactionId());
                    ResultSet res = prep.executeQuery();
                    if(res.next()){
                        int quantity = res.getInt("quantity");
                        if(quantity > te.getAmount()) {
                            String update = "UPDATE soldProducts SET quantity = ? WHERE productId = ? AND transactionId = ?";
                            PreparedStatement prep1 = conn.prepareStatement(update);
                            prep1.setInt(1, quantity - te.getAmount());
                            prep1.setString(2, te.getBarCode());
                            prep1.setInt(3, rt.getSaleTransactionId());
                            if(prep1.executeUpdate() == 0) {
                                conn.setAutoCommit(true);
                                return false;
                            }
                        }
                        else if (quantity == te.getAmount()){
                            String update = "DELETE FROM soldProducts WHERE productId = ? AND transactionId = ?";
                            PreparedStatement prep1 = conn.prepareStatement(update);
                            prep1.setString(1, te.getBarCode());
                            prep1.setInt(2, rt.getSaleTransactionId());
                            if(prep1.executeUpdate() == 0 ){
                                conn.setAutoCommit(true);
                                return false;
                            }
                        }
                        TicketEntryObject teo = (TicketEntryObject) te;
                        for(String RFID : teo.getRFIDs()){
                            String update = "UPDATE product SET sold = false, transactionId = -1 WHERE RFID = ?";
                            prep = conn.prepareStatement(update);
                            prep.setString(1, RFID);
                            prep.executeUpdate();
                        }
                        ProductType pt = InventoryManagement.getProductTypeByBarCode(te.getBarCode());
                        if(pt==null || !InventoryManagement.updateQuantity(pt.getId(), te.getAmount())) {
                            conn.setAutoCommit(true);
                            throw  new Exception();
                        }
                    }
                }
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
            catch (Exception e){
                return false;
            }
        }
        else return deleteReturnTransaction(returnId);
    }

    /**
     * delete a return transaction from the list
     * @param returnId the transaction to be removed
     * @return true or false if it fails or not
     */
    public static boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException {
       validateTransactionId(returnId);
        return pendingReturnTransactions.remove(returnId)!=null;

    }

    public static ReturnTransactionObject getOpenReturnTransaction(Integer returnId) throws  InvalidTransactionIdException{
        validateTransactionId(returnId);
        ReturnTransactionObject  rt = pendingReturnTransactions.get(returnId);
        if (rt != null && (rt.isOpen())) return rt;
        else return null;
    }

    static private void validateTransactionId(Integer transactionId) throws InvalidTransactionIdException{
        if(transactionId == null || transactionId <= 0) throw new InvalidTransactionIdException();
    }



     public static double issueReturnCashPayment(Integer transactionId) throws InvalidTransactionIdException {
        ReturnTransactionObject rt = getOpenReturnTransaction(transactionId);
        if(rt == null) return -1;
        if(rt.getMoney() > AccountBook.computeBalance()) return -1;
        rt.close();
        if(rt.paymentIssued() && AccountBook.recordReturnTransaction(rt))
            return rt.getMoney();
        return -1;
    }
    public static double issueReturnCreditCardPayment(Integer transactionId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException {
        ReturnTransactionObject rt = getOpenReturnTransaction(transactionId);
        if(creditCard == null || !PaymentGateway.verifyCard(creditCard)) throw new InvalidCreditCardException();
        if(rt == null) return -1;
        if(rt.getMoney() > AccountBook.computeBalance()) return -1;
        if(!PaymentGateway.issueTransfer(creditCard, rt.getMoney())) return -1;
        rt.close();
        if(rt.paymentIssued()  && AccountBook.recordReturnTransaction(rt))
            return rt.getMoney();
        return -1;
    }

    public static ReturnTransactionObject getReturnTransaction(Integer transactionId) throws InvalidTransactionIdException {
        validateTransactionId(transactionId);
        return pendingReturnTransactions.get(transactionId);
    }

    private  static boolean pushSoldItemsToDB(SaleTransactionObject st) {
        try {
            Connection conn = DB.getConnectionToDB();
            conn.setAutoCommit(false);
            String insert = "INSERT INTO soldProducts VALUES (?,?,?,?,?)";
            assert st.getEntries() != null;
            for(TicketEntry te: st.getEntries()) {
                PreparedStatement prep = conn.prepareStatement(insert);
                prep.setString(1,te.getBarCode());
                prep.setInt(2,st.getTicketNumber());
                prep.setDouble(3, te.getPricePerUnit());
                prep.setInt(4, te.getAmount());
                prep.setDouble(5, te.getDiscountRate());
                if(prep.executeUpdate()==0) {
                    conn.setAutoCommit(true);
                    throw new Exception();
                }
                TicketEntryObject teo = (TicketEntryObject) te;
                String update = "UPDATE product SET transactionId = ? WHERE RFID = ? AND sold = true";
                for(String RFID: teo.getRFIDs()){
                    PreparedStatement prep1 = conn.prepareStatement(update);
                    prep1.setInt(1, st.getTicketNumber());
                    prep1.setString(2, RFID);
                    if(prep1.executeUpdate()==0) {
                        conn.setAutoCommit(true);
                        throw new Exception();
                    }
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean recordPayment(SaleTransactionObject st) {
        if(st.paymentIssued())
            try{
                Connection conn = DB.getConnectionToDB();
                String update = "UPDATE saleTransactions SET status ='payed', price = ?, discountRate = ? WHERE transactionId = ?";
                PreparedStatement prep = conn.prepareStatement(update);
                prep.setDouble(1, st.getPrice());
                prep.setDouble(2, st.getDiscountRate());
                prep.setInt(3, st.getTicketNumber());

                return prep.executeUpdate()!=0;
            } catch (SQLException exception) {
                st.paymentRollback();
                return false;
            }
        return false;
    }
}
