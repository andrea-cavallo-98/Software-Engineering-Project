package it.polito.ezshop.data;

import it.polito.ezshop.controllers.*;
import it.polito.ezshop.data.*;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.SaleTransactionObject;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;


public class EZShop implements EZShopInterface {

    private User logged;


    @Override
    public void reset() {
        try {
            DB.cleanDatabase();
            InventoryManagement.productMap.clear();
            InventoryManagement.orderMap.clear();
            InventoryManagement.lastProductID = 0;
            InventoryManagement.lastOrderID = 0;
            PaymentController.pendingSaleTransactions.clear();
            PaymentController.pendingReturnTransactions.clear();
            PaymentController.lastId = 0;
        } catch (SQLException exception) {
            //exception.printStackTrace();
        }
    }

    @Override
    public Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        return UserManagement.createUser(username, password, role);
    }

    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        String[] roles = new String[] { "Administrator" };
        checkUserAuth(roles);
        return UserManagement.deleteUser(id);
    }

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {
        String[] roles = new String[] { "Administrator" };
        checkUserAuth(roles);
        return UserManagement.getAllUsers();
    }

    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        String[] roles = new String[] { "Administrator" };
        checkUserAuth(roles);
        return UserManagement.getUser(id);
    }

    @Override
    public boolean updateUserRights(Integer id, String role) throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {
        String[] roles = new String[] { "Administrator" };
        checkUserAuth(roles);
        return UserManagement.updateUserRights(id, role);
    }

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {
        if(username == null || username.isEmpty()) throw new InvalidUsernameException();
        if(password == null || password.isEmpty()) throw new InvalidPasswordException();
        User u = UserManagement.getUserByUsername(username);
        if(u != null && u.getPassword().equals(password)) {
            logged = u;
            InventoryManagement.loadProductsFromDB();
            InventoryManagement.loadOrdersMap();
            return u;
        }
        else
            return null;
    }

    @Override
    public boolean logout() {
        if(logged == null) return false;
        logged = null;
        return true;
    }

    /*
    TODO: Oscar
     */
    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.createProductType(description, productCode, pricePerUnit, note);
    }

    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.updateProduct(id, newDescription, newCode, newPrice, newNote);
    }

    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.deleteProductType(id);
    }

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return InventoryManagement.getAllProductTypes();
    }

    @Override
    public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.getProductTypeByBarCode(barCode);
    }

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.getProductTypesByDescription(description);
    }

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.updateQuantity(productId, toBeAdded);
    }

    @Override
    public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.updatePosition(productId, newPos);
    }

    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.issueOrder(productCode, quantity, pricePerUnit);
    }

    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.payOrderFor(productCode, quantity, pricePerUnit);
    }

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.payOrder(orderId);
    }

    @Override
    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.recordOrderArrival(orderId);
    }

    @Override
    public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException, InvalidRFIDException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.recordOrderArrivalRFID(orderId, RFIDfrom);
    }

    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager" };
        checkUserAuth(roles);
        return InventoryManagement.getAllOrders();
    }


    /*
    TODO: Matteo
     */
    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.defineCustomer(customerName);
    }

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.modifyCustomer(id, newCustomerName, newCustomerCard);
    }

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.deleteCustomer(id);
    }

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.getCustomer(id);
    }

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.getAllCustomers();
    }

    @Override
    public String createCard() throws UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.createCard();
    }

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.attachCardToCustomer(customerCard, customerId);
    }

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {
        String[] roles = new String[] { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return CustomerManagement.modifyPointsOnCard(customerCard, pointsToBeAdded);
    }
    /*
    TODO: Giulio
     */
    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.startSaleTransaction();
    }

    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.addProductToSale(transactionId, productCode, amount);
    }

    @Override
    public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.addProductToSaleRFID(transactionId,RFID);
    }

    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.deleteProductFromSale(transactionId, productCode, amount);
    }

    @Override
    public boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.deleteProductFromSaleRFID(transactionId, RFID);    }

    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.addDiscountRateToProduct(transactionId, productCode, discountRate);
    }

    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.addDiscountRateToSale(transactionId,discountRate);
    }

    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        SaleTransactionObject st = PaymentController.getSaleTransaction(transactionId);
        if(st != null)
            return (int)st.getPrice()/10;
        return 0;
    }

    @Override
    public boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.endSaleTransaction(transactionId);
    }

    @Override
    public boolean deleteSaleTransaction(Integer saleNumber) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.deleteSaleTransaction(saleNumber);
    }

    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        SaleTransactionObject st = PaymentController.getSaleTransaction(transactionId);
        if(st == null || st.isOpen()) return null;
        return new SaleTransactionObject(st);
    }

    @Override
    public Integer startReturnTransaction(Integer saleNumber) throws /*InvalidTicketNumberException,*/InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.startReturnTransaction(saleNumber);
    }

    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.returnProduct(returnId, productCode, amount);
    }

    @Override
    public boolean returnProductRFID(Integer returnId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.returnProductRFID(returnId, RFID);    }

    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException, UnauthorizedException{
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.endReturnTransaction(returnId, commit);
    }

    @Override
    public boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.deleteReturnTransaction(returnId);
    }

    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.issueCashPayment(ticketNumber, cash);
    }

    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.issueCardPayment(ticketNumber, creditCard);
    }

    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.issueReturnCashPayment(returnId);
    }

    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        checkUserAuth(new String[] {"Administrator", "ShopManager", "Cashier"});
        return PaymentController.issueReturnCreditCardPayment(returnId, creditCard);
    }

/*
TODO: Andrea
 */
    @Override
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        String[] authorizedUsers = {"Administrator", "ShopManager"};
        checkUserAuth(authorizedUsers);

        return AccountBook.recordBalanceUpdate(toBeAdded);
    }

    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        String[] authorizedUsers = {"Administrator", "ShopManager"};
        checkUserAuth(authorizedUsers);
        return AccountBook.getCreditsAndDebits(from, to);
    }

    @Override
    public double computeBalance() throws UnauthorizedException {
        String[] roles = { "Administrator", "ShopManager", "Cashier" };
        checkUserAuth(roles);
        return AccountBook.computeBalance();
    }

    private void checkUserAuth(String[] roles) throws UnauthorizedException{
        if(logged != null) {
            for (String s : roles) {
                if (logged.getRole().equals(s)) return;
            }
        }
        throw new UnauthorizedException();
    }

}



