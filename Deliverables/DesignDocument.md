# Design Document 


Authors: Andrea Cavallo, Giulio Carota, Angelo Oscar Piccirillo, Matteo Biffoni

Date: June 9, 2021

Version: 1.2



# Contents

- [High level design](#high-level-design)
- [Low level design](#low-level-design)
- [Verification traceability matrix](#verification-traceability-matrix)
- [Verification sequence diagrams](#verification-sequence-diagrams)

# Instructions

The design must satisfy the Official Requirements document, notably functional and non functional requirements

# High level design 

#### Architectural pattern
The software is based on the MVC pattern and is organized in a 3-tier architecture: 
- presentation (the way of presenting the application to the users)
- application logic 
- data (DBMS)



```plantuml
package "it.polito.ezshop.controllers" { 
    class UserManagement 
    class InventoryManagement 
    class AccountBook
    class CustomersManagement
    class PaymentController
    class PaymentGateway
    class DB
}

package "it.polito.ezshop.data" {
    class EzShop
    circle EzShopInterface
    circle User
    circle ProductType
    circle SaleTransaction
    circle Order
    circle BalanceOperation
    circle TicketEntry
    circle Customer
}


package "it.polito.ezshop.model"{
    class UserObject
    class ProductTypeObject
    class SaleTransactionObject
    class ReturnTransactionObject
    class OrderObject
    class BalanceOperationObject
    class CreditCardObject
    class TicketEntryObject
    class CustomerObject
    
}
package "it.polito.ezshop.gui" {
    class GUI 

}

package "it.polito.ezshop.exceptions"{
    class InvalidUsernameException
    class InvalidPasswordException
    class InvalidRoleException 
    class InvalidUserIdException 
    class UnauthorizedException
    class InvalidProductDescriptionException
    class InvalidProductCodeException
    class InvalidPricePerUnitException
    class InvalidProductIdException
    class InvalidQuantityException
    class InvalidOrderIdException
    class InvalidLocationException
    class InvalidCustomerNameException
    class InvalidCustomerCardException
    class InvalidCustomerIdException
    class InvalidTransactionIdException
    class InvalidDiscountRateException
}


it.polito.ezshop.data -|> it.polito.ezshop.model
it.polito.ezshop.data -|> it.polito.ezshop.controllers
it.polito.ezshop.exceptions ------|> it.polito.ezshop.controllers
it.polito.ezshop.model -|> it.polito.ezshop.controllers
it.polito.ezshop.gui <|---- it.polito.ezshop.controllers

```

# Low level design

### Class that implements API interface

```plantuml

    class EzShopController{
        + void reset()
        + Integer createUser(String username, String password, String role)
        + boolean deleteUser(Integer id)
        + List<User> getAllUsers()
        + User getUser(Integer id)
        + boolean updateUserRights(Integer id, String role)
        + User login(String username, String password)
        + boolean logout()
        + Integer createProductType(String description, String productCode, double pricePerUnit, String note)
        + boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote)
        + boolean deleteProductType(Integer id)
        + List<ProductType> getAllProductTypes()
        + ProductType getProductTypeByBarCode(String barCode)
        + List<ProductType> getProductTypesByDescription(String description)
        + boolean updateQuantity(Integer productId, int toBeAdded)
        + boolean updatePosition(Integer productId, String newPos)
        + Integer issueOrder(String productCode, int quantity, double pricePerUnit)
        + Integer payOrderFor(String productCode, int quantity, double pricePerUnit)
        + boolean payOrder(Integer orderId)
        + boolean recordOrderArrival(Integer orderId)
        + boolean recordOrderArrival(Integer orderId, String RFID)
        + List<Order> getAllOrders() 
        + Integer defineCustomer(String customerName)
        + boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard)
        + boolean deleteCustomer(Integer id)
        + Customer getCustomer(Integer id) 
        + List<Customer> getAllCustomers() 
        + String createCard()
        + boolean attachCardToCustomer(String customerCard, Integer customerId)
        + boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded)
        + Integer startSaleTransaction()
        + boolean addProductToSale(Integer transactionId, String productCode, int amount)
        + boolean addProductToSaleRFID(Integer transactionId, String RFID)
        + boolean deleteProductFromSale(Integer transactionId, String productCode, int amount)
        + boolean deleteProductFromSaleRFID(Integer transactionId, String RFID)
        + boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate)
        + boolean applyDiscountRateToSale(Integer transactionId, double discountRate)
        + int computePointsForSale(Integer transactionId)
        + boolean endSaleTransaction(Integer transactionId)
        + boolean deleteSaleTransaction(Integer transactionId)
        + SaleTransaction getSaleTransaction(Integer transactionId)
        + Integer startReturnTransaction(Integer tradsactionId)
        + boolean returnProduct(Integer returnId, String productCode, int amount)
        + boolean returnProductRFID(Integer returnId, String RFID)
        + boolean endReturnTransaction(Integer returnId, boolean commit)
        + boolean deleteReturnTransaction(Integer returnId)
        + double receiveCashPayment(Integer transactionId, double cash)
        + boolean receiveCreditCardPayment(Integer transactionId, String creditCard) 
        + double returnCashPayment(Integer returnId) 
        + double returnCreditCardPayment(Integer returnId, String creditCard)
        + boolean lanceUpdate(double toBeAdded)
        + List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to)
        + double computeBalance()
        +
    }


circle EzShopInterface
EzShopController -- EzShopInterface
    

```
### Classes that communicate with EzShopController
<div align="center">
    <b>1/3</b>
</div>

```plantuml
  
    'account book stores closed and payed transactions which effectively affect
    'the shop balance
   
    class AccountBook {
        - List<ReturnTransaction> returnTransaction
        - List<SaleTransaction> saleTransaction
        - double debits
        - double credits
        - double currentBalance

        + boolean deleteSaleTransaction(Integer transactionID)
        + SaleTransaction getSaleTransaction(Integer transactionId)
        + boolean deleteReturnTransaction(Integer returnId)
        + SaleTransaction getReturnTransaction(Integer transactionId)
        + boolean deleteOrderTransaction(Integer returnId)
        + SaleTransaction getOrderTransaction(Integer transactionId)

        + boolean recordSaleTransaction(SaleTransaction transaction)
        + boolean recordReturnTransaction(ReturnTransaction transaction)
        + boolean recordOrderTransaction(OrderTransaction transaction)
        + boolean recordOrderTransaction(OrderTransaction transaction)

        + double getCurrentBalance()
        + boolean balanceUpdate(double toBeAdded)
        + List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to)

    }
    note right of AccountBook
        Persistent (only credits, debits and currentBalance)
    end note
    class CustomersManagement {
        -List<Customer> customers
        -List<String> cards
        + Integer defineCustomer(String customerName)
        + boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard)
        + boolean deleteCustomer(Integer id)
        + Customer getCustomer(Integer id) 
        + List<Customer> getAllCustomers() 
        + String createCard()
        + boolean attachCardToCustomer(String customerCard, Integer customerId)
        + boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded)
    }

    class Customer {
        -Integer points
        -Integer id
        -String customerName
        -String card
    }
    note left of Customer
        Persistent
    end note
    'this class manages payments. Can check credit card validity through the payment gateway and issue transactions using CCs. Offers also cash payments. Each operation affects directly the balance calling specific accountbook's methods.
    class PaymentController {
        - List<SaleTransaction> pendingSaleTransactions
        - List<ReturnTransaction> pendingReturnTransactions
   
        + Integer startSaleTransaction()
        + boolean addProductToSale(Integer transactionId, String productCode, int amount)
        + boolean deleteProductFromSale(Integer transactionId, String productCode, int amount)
        + boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate)
        + boolean applyDiscountRateToSale(Integer transactionId, double discountRate)
        + boolean endSaleTransaction(Integer transactionId)
        + boolean addProductToSaleRFID(Integer transactionId, String RFID)
        + boolean deleteProductFromSale(Integer transactionId,  String RFID)

        + Integer startReturnTransaction(Integer transactionID)
        + boolean returnProduct(Integer returnId, String productCode, int amount)
        + boolean returnProductRFID(Integer returnId, String RFID)
        + boolean endReturnTransaction(Integer returnId, boolean commit)
        + boolean deleteReturnTransaction(Integer returnId)
     
        + SaleTransactionObject getSaleTransaction(Integer transactionID)
        - SaleTransactionObject getOpenSaleTransaction(Integer transactionID)
        - SaleTransactionObject getNotPayedSaleTransaction(Integer transactionID)
        + ReturnTransactionObject getOpenReturnTransaction(Integer returnId)
        - ReturnTransactionObject getReturnTransaction(Integer transactionId)
        
        + double issueCashPayment(Integer transactionID, double cashAmount)
        + boolean issueCardPayment(Integer transactionID, String creditCard)
        + double issueReturnCashPayment(Integer transactionId)
        + boolean issueReturnCardPayment(Integer transactionID, String creditCard)
        
        - void validateTransactionId(Integer transactionId)
        - boolean pushSoldItemsToDB(SaleTransactionObject st)
        - boolean recordPayment(SaleTransactionObject st)
    } 

    class PaymentGateway {
        + boolean verifyCard(String creditCard)
        + boolean issueCardPayment(String creditCard, double price)
        + CreditCardObject getCreditCard(String creditCard)
    } 
    note left of PaymentController
        Persistent (only shopCreditCard)
    end note

    class BalanceOperation {
        - double moneyIn
        - double moneyOut
        - String status

        + boolean isPending()
    }
    note right of BalanceOperation
        Persistent (to all derivate classes)
    end note

    class SaleTransactionObject {
        - String status; 
        - Integer ticketNumber
        - List <TicketEntry> items
        - double discountRate
        - LocalDate date
        - double price
    
        + boolean isOpen()
        + boolean isClosed()
        + boolean isPayed()
        + boolean close()
        + boolean paymentIssued()
        + boolean paymentRollback()
        + List<TicketEntryObject> getEntries()
        + TicketEntryObject getEntry(String productCode)
        + Integer getTicketNumber()
        + double getDiscountRate()
        + LocalDate getDate()
        + double getPrice()
        + void setEntries(List<TicketEntryObject> items)
        + void addEntry(TicketEntryObject entry)
        + boolean deleteEntry(String productCode)
        + void setDiscountRate(double newDiscountRate)
        + void setPrice(double newPrice)
    }

    class ReturnTransactionObject {
        - final Integer saleTransactionId;
        - final Integer returnId;
        - String status;
        - double price;
        - Map<String, TicketEntryObject> returnedItems = new HashMap<>();
        - LocalDate date;
        - double saleDiscountRate;
       
        + boolean isOpen()
        + boolean isClosed()
        + boolean isPayed()
        + boolean close()
        + boolean paymentIssued()
        + void setMoney(double money)
        + double getMoney()
        + double updateMoney(double money)
        + LocalDate getDate(){ return date; }
        + Integer getSaleTransactionId() 
        + Integer getReturnId() 
        + List<TicketEntryObject> getReturnedItems() 
        + void setReturnedItems(Map<String, TicketEntryObject> returnedItems)
        + void addItem(TicketEntryObject item)
        + TicketEntry getItem(String productCode)
    }
    
    class TicketEntryObject {
        - String productDescription;
        - String productCode;
        - Integer productAmount;
        - double discountRate;
        - double pricePerUnit;
        
        + String getBarCode()
        + void setBarCode(String barCode)
        + String getProductDescription()
        + void setProductDescription(String productDescription)
        + int getAmount()
        + void setAmount(int amount)
        + double getPricePerUnit() 
        + void setPricePerUnit(double pricePerUnit)
        + double getDiscountRate()
        + void setDiscountRate(double discountRate)
    }

    PaymentGateway --> PaymentController

    Customer "*"<-- CustomersManagement
    SaleTransactionObject -- TicketEntryObject
    ReturnTransactionObject -- TicketEntryObject
    SaleTransactionObject -- BalanceOperation
    ReturnTransactionObject -- BalanceOperation
    PaymentController <-- EzShopController

    BalanceOperation <-- EzShopController
    BalanceOperation "*"<-- PaymentController
   
    AccountBook -->"*" BalanceOperation
    AccountBook <-- EzShopController
    CustomersManagement <-- EzShopController




```
<div align="center">
    <b>2/3</b>
</div>

```plantuml
    class UserManagement {
    'inventory management class can create a product type, place it into the warehouse and manages products. When an order arrives is recorded into db and for each product in the      order a new product instance is created and placed into the respective product type.
        - List<User> users
        
        + Integer createUser(String username, String password, String role)
        + boolean deleteUser(Integer id)
        + List<User> getAllUsers()
        + User getUser(Integer id)
        + boolean updateUserRights(Integer id, String role)
    }
    class InventoryManagement {
        
        - List<ProductType> productTypes
        - List<Order> orders

        + void loadProductsFromDB
        + void loadOrdersFromDB
        + Integer createProductType(String description, String productCode, double pricePerUnit, String note)
        + boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote)
        + boolean deleteProductType(Integer id)
        + List<ProductType> getAllProductTypes()
        + ProductType getProductTypeByBarCode(String barCode)
        + List<ProductType> getProductTypesByDescription(String description)
        + boolean updateQuantity(Integer productId, int toBeAdded)
        + boolean updatePosition(Integer productId, String newPos)
        + Integer issueOrder(String productCode, int quantity, double pricePerUnit)
        + boolean recordOrderArrivalRFID(Integer orderId, String RFID)
        + boolean recordOrderArrival(Integer orderId)
        + List<Order> getAllOrders() 
    }
    'product type contains informations about a specific type of product and a list of product which belong to that type.
    'The products are created and added to ProductType by the invenotry manager
    class ProductType {
        - String description
        - String id
        - String productCode
        - double pricePerUnit
        - String note
        - List<Product> products
        - Position currentPosition

        + boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote)
        + Integer updateQuantity(Integer productId, Integer toBeAdded)
        + Position updatePosition(Position newPosition)
        + Product addNewProduct(Product newProduct)

    }
    note right of ProductType
        Persistent
    end note
    ' class Product {
    '     - String serialCode
    '     - ProductType type
    ' }

    'an order contains info about a product type
    class Order {
        - String OrderID
        - String productCode
        - Integer quantity
        - double pricePerUnit
        - String state

        + String updateState(String newState)
    }
    note left of Order
        Persistent
    end note

    class User {
        - String username
        - String password
        - String role
        - Integer id

    }
    note left of User
        Persistent
    end note
    UserManagement -->"*" User
    UserManagement <-- EzShopController
    InventoryManagement -->"*" ProductType
  '  ProductType "1"--"*" Product
    InventoryManagement <-- EzShopController
    
    Order "*"<--"1" InventoryManagement
    GUI --> EzShopController  

```
<div align="center">
    <b>3/3</b>
</div>

```plantuml

 class DB {
        - String jdbcUrl
        - Connection connection

        + Connection getConnectionToDB()
        + void alterJDBCUrl() 
        + void restoreJDBCUrl()
        + void cleanDatabase()
    }
 note left of DB
    Class exploited by all persistent data based Classes
end note
```





# Verification traceability matrix


|                          | <sup>EZShop Controller</sup> | <sup>Inventory Management</sup> | <sup>Product Type</sup> | <sup>Order</sup> | <sup>Payment Gateway</sup> | <sup>Customers Management</sup> | <sup>Customer</sup> | <sup>TicketEntry</sup> | <sup>User Management</sup> | <sup>User</sup> | <sup>Account Book</sup> | <sup>Payment Controller</sup> | <sup>Sale Transaction</sup> | <sup>Balance Operation</sup> | <sup>Return Transaction</sup> |
| :----------------------: | :--------------------------: | :-----------------------------: | :---------------------: | :--------------: | :------------------------: | :-----------------------------: | :-----------------: | :--------------------: | :------------------------: | :-------------: | :---------------------: | :---------------------------: | :-------------------------: | ---------------------------- | ----------------------------- |
| <sup><b>FR1.1 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |             X              |        X        |                         |                               |                             |                              |                               |
| <sup><b>FR1.2 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |             X              |                 |                         |                               |                             |                              |                               |
| <sup><b>FR1.3 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |             X              |                 |                         |                               |                             |                              |                               |
| <sup><b>FR1.4 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |             X              |                 |                         |                               |                             |                              |                               |
| <sup><b>FR1.5 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |             X              |        X        |                         |                               |                             |                              |                               |
| <sup><b>FR3.1 </b></sup> |              X               |                X                |            X            |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR3.2 </b></sup> |              X               |                X                |                         |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR3.3 </b></sup> |              X               |                X                |                         |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR3.4 </b></sup> |              X               |                X                |                         |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR4.1 </b></sup> |              X               |                X                |            X            |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR4.2 </b></sup> |              X               |                X                |            X            |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR4.3 </b></sup> |              X               |                X                |                         |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR4.4 </b></sup> |              X               |                X                |                         |        X         |                            |                                 |                     |                        |                            |                 |            X            |               X               |                             | X                            |                               |
| <sup><b>FR4.5 </b></sup> |              X               |                X                |                         |        X         |                            |                                 |                     |                        |                            |                 |            X            |               X               |                             | X                            |                               |
| <sup><b>FR4.6 </b></sup> |              X               |                X                |                         |        X         |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR4.7 </b></sup> |              X               |                X                |                         |        X         |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.1 </b></sup> |              X               |                                 |                         |                  |                            |                X                |          X          |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.2 </b></sup> |              X               |                                 |                         |                  |                            |                X                |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.3 </b></sup> |              X               |                                 |                         |                  |                            |                X                |          X          |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.4 </b></sup> |              X               |                                 |                         |                  |                            |                X                |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.5 </b></sup> |              X               |                                 |                         |                  |                            |                X                |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.6 </b></sup> |              X               |                                 |                         |                  |                            |                X                |          X          |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR5.7 </b></sup> |              X               |                                 |                         |                  |                            |                X                |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR6.1 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |              X              | X                            |                               |
| <sup><b>FR6.2 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR6.3 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR6.4 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR6.5 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR6.6 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR6.7 </b></sup> |              X               |                X                |            X            |                  |                            |                                 |                     |                        |                            |                 |                         |                               |                             |                              |                               |
| <sup><b>FR6.8 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |               X               |                             |                              |                               |
| <sup><b>FR6.9 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |                               |                             |                              |                               |
| <sup><b>FR6.10</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |              X              | X                            |                               |
| <sup><b>FR6.11</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |               X               |                             |                              |                               |
| <sup><b>FR6.12</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             | X                            | X                             |
| <sup><b>FR6.13</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |           X            |                            |                 |                         |               X               |                             | X                            | X                             |
| <sup><b>FR6.14</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |                             | X                            | X                             |
| <sup><b>FR6.15</b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |               X               |                             |                              |                               |
| <sup><b>FR7.1 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR7.2 </b></sup> |              X               |                                 |                         |                  |             X              |                                 |                     |                        |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR7.3 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR7.4 </b></sup> |              X               |                                 |                         |                  |             X              |                                 |                     |                        |                            |                 |                         |               X               |                             |                              |                               |
| <sup><b>FR8.1 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |                               |                             |                              |                               |
| <sup><b>FR8.2 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |                               |                             |                              |                               |
| <sup><b>FR8.3 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |                               |                             |                              |                               |
| <sup><b>FR8.4 </b></sup> |              X               |                                 |                         |                  |                            |                                 |                     |                        |                            |                 |            X            |                               |                             |                              |                               |










# Verification sequence diagrams 

### Scenario 1.1 - Create product type X

```plantuml
participant UserC

UserC -> EzShopController: 1. createProductType()
activate EzShopController

EzShopController -> InventoryManagement: 2. createProductType()
activate InventoryManagement

InventoryManagement -> ProductType: 3. ProductType()
activate ProductType
ProductType -> InventoryManagement: 4. ProductType object is returned
InventoryManagement -> EzShopController: 5. return true
EzShopController -> UserC: 6. return true

UserC -> EzShopController: 7. updatePosition()
EzShopController -> InventoryManagement: 8. updatePosition()
InventoryManagement -> ProductType: 9. updatePosition()
ProductType -> InventoryManagement: 10. Position is returned
InventoryManagement -> EzShopController: 11. return true
EzShopController -> UserC: 11. return true

deactivate EzShopController 
deactivate InventoryManagement
deactivate ProductType
```

### Scenario 2.1 - Create user and define rights


```plantuml
participant AdminA

AdminA -> EzShopController: 1. createUser()
activate EzShopController

EzShopController -> UserManagement: 2. createUser()
activate UserManagement

UserManagement -> User: 3. User()
activate User

User -> UserManagement: 4. User object is returned
deactivate User

UserManagement -> EzShopController: 5. return true
deactivate UserManagement

EzShopController -> AdminA: 6. return true

deactivate EzShopController
```


### Scenario 3.1 - Order of a product type X issued
```plantuml

participant UserC
activate UserC
UserC -> EzShopController: 1. issueOrder(prodID, qty, pricePerUnit, supplier)
activate EzShopController
EzShopController -> InventoryManagement : 2. issueOrder(prodID, qty, pricePerUnit, supplier)
activate InventoryManagement
InventoryManagement -> Order:3. Order(id, prodID, qty, pricePerUnit, supplier)
activate Order
Order -> InventoryManagement: 4. Order object is returned
InventoryManagement -> InventoryManagement: 5. stores pending Order
deactivate Order
InventoryManagement -> PaymentController: 6. startOrderTransaction(orderID)
InventoryManagement-> EzShopController: 6. OrderID is returned
deactivate InventoryManagement

EzShopController->UserC: 7. OrderID is returned
deactivate EzShopController
```

### Scenario 4.1 - Create customer record
```plantuml
participant UserC

UserC -> EzShopController: 1. defineCustomer()
activate EzShopController

EzShopController -> CustomersManagement: 2. defineCustomer()
activate CustomersManagement

CustomersManagement -> Customer: 3. Customer()
activate Customer
Customer -> CustomersManagement: 4. return Customer object
deactivate Customer
CustomersManagement -> CustomersManagement: 5. store new Customer object
CustomersManagement -> EzShopController: 6. return true
deactivate CustomersManagement
EzShopController -> UserC: 7. return true
deactivate EzShopController
```


### Scenario 4.2 - Attach Loyalty Card to customer record
```plantuml
participant UserC

UserC -> EzShopController: 1. createCard()
activate EzShopController

EzShopController -> CustomersManagement: 2. createCard()
activate CustomersManagement

CustomersManagement -> EzShopController: 3. return cardCode

EzShopController -> UserC: 4. return cardCode

UserC -> EzShopController: 5. attachCardToCustomer()

EzShopController -> CustomersManagement: 6. attachCardToCustomer()
CustomersManagement -> EzShopController: return true
deactivate CustomersManagement
EzShopController -> UserC: return true
deactivate EzShopController


```


### Scenario 5.1 - Login

```plantuml

participant UserC
activate UserC
UserC -> EzShopController: 1. login(user,pass)
activate EzShopController

EzShopController -> UserManagement: 2. getUser(id)
activate UserManagement
UserManagement-> EzShopController: 3. return user
deactivate UserManagement
EzShopController -> EzShopController: 4. verifies submitted password with stored one
EzShopController -> UserC: 5. return JWT token
deactivate EzShopController


```

### Scenario 6.1 - Sale of product type X completed

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: 1. startSaleTransaction()
activate EzShopController
EzShopController -> PaymentController: 2. startSaleTransaction()
activate PaymentController
PaymentController -> EzShopController: 3. return startSaleTransaction
CashierC -> EzShopController: 4. addProductToSale(transactionId, barCode, N)
EzShopController -> PaymentController: 5. addProductToSale(transactionId, barCode, N)

activate InventoryManagement
PaymentController -> InventoryManagement: 6. getProductTypeByBarCode(productCode)
InventoryManagement -> PaymentController : 7. return getProductTypeByBarCode(productCode)
PaymentController -> InventoryManagement: 8. updateQuantity(productId, -N)
InventoryManagement -> PaymentController: 9. return updateQuantity

PaymentController -> EzShopController: 10. return addProductToSale
EzShopController -> CashierC: 11. return addProductToSale

CashierC -> EzShopController:12. endSaleTransaction(transactionId)
EzShopController -> PaymentController:13. endSaleTransaction(transactionId)
PaymentController -> EzShopController:14. return endSaleTransaction()
EzShopController -> CashierC:15. return endSaleTransaction()
CashierC -> EzShopController:16. payment(see UC7)
EzShopController -> PaymentController: 17. payment(see UC 7)
PaymentController -> PaymentController:18. pushSoldItemsToDB();
PaymentController -> PaymentController:19. recordTransaction();
PaymentController -> AccountBook: 20. recordSaleTransaction(transactionId)
activate AccountBook
AccountBook -> AccountBook: 21. updateBalance(N*unitPrice)
AccountBook -> AccountBook: 22. return updateBalance

AccountBook -> PaymentController: 23. return recordSaleTransaction()
PaymentController -> EzShopController:24 return Payment Status
EzShopController -> CashierC:25 return Payment Status 

```

### Scenario 6.2 - Sale of product type X with product discount

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: 1. startSaleTransaction()
activate EzShopController
EzShopController -> PaymentController: 2. startSaleTransaction()
activate PaymentController
PaymentController -> EzShopController: 3. return startSaleTransaction
CashierC -> EzShopController: 4. addProductToSale(transactionId, barCode, N)
EzShopController -> PaymentController: 5. addProductToSale(transactionId, barCode, N)

activate InventoryManagement
PaymentController -> InventoryManagement: 6. getProductTypeByBarCode(productCode)
InventoryManagement -> PaymentController : 7. return getProductTypeByBarCode(productCode)
PaymentController -> InventoryManagement: 8. updateQuantity(productId, -N)
InventoryManagement -> PaymentController: 9. return updateQuantity

PaymentController -> EzShopController: 10. return addProductToSale
EzShopController -> CashierC: 11. return addProductToSale

CashierC -> EzShopController:12. applyDiscountRateToProduct(transactionId,discountRate)
EzShopController -> PaymentController:13. applyDiscountRateToProduct(transactionId, discountRate))
PaymentController -> EzShopController:14. return applyDiscountRateToProduct()
EzShopController -> CashierC:15. return applyDiscountRateToProduct()

CashierC -> EzShopController:16. endSaleTransaction(transactionId)
EzShopController -> PaymentController:17. endSaleTransaction(transactionId)
PaymentController -> EzShopController:18. return endSaleTransaction()
EzShopController -> CashierC:19. return endSaleTransaction()
CashierC -> EzShopController:20. payment(see UC7)
EzShopController -> PaymentController:21. payment(see UC 7)
PaymentController -> PaymentController:22. pushSoldItemsToDB();
PaymentController -> PaymentController:23. recordTransaction();
PaymentController -> AccountBook:24. recordSaleTransaction(transactionId)
activate AccountBook
AccountBook -> AccountBook: 25. updateBalance(N*unitPrice)
AccountBook -> AccountBook: 26. return updateBalance

AccountBook -> PaymentController: 27. return recordSaleTransaction()
PaymentController -> EzShopController:28 return Payment Status
EzShopController -> CashierC:29 return Payment Status 
```

### Scenario 6.4 - Sale of product type X with Loyalty Card update
```plantuml


participant CashierC
activate CashierC
CashierC -> EzShopController: 1. startSaleTransaction()
activate EzShopController
EzShopController -> PaymentController: 2. startSaleTransaction()
activate PaymentController
PaymentController -> EzShopController: 3. return startSaleTransaction
CashierC -> EzShopController: 4. addProductToSale(transactionId, barCode, N)
EzShopController -> PaymentController: 5. addProductToSale(transactionId, barCode, N)

activate InventoryManagement
PaymentController -> InventoryManagement: 6. getProductTypeByBarCode(productCode)
InventoryManagement -> PaymentController : 7. return getProductTypeByBarCode(productCode)
PaymentController -> InventoryManagement: 8. updateQuantity(productId, -N)
InventoryManagement -> PaymentController: 9. return updateQuantity

PaymentController -> EzShopController: 10. return addProductToSale
EzShopController -> CashierC: 11. return addProductToSale

CashierC -> EzShopController:12. endSaleTransaction(transactionId)
EzShopController -> PaymentController:13. endSaleTransaction(transactionId)
PaymentController -> EzShopController:14. return endSaleTransaction()
EzShopController -> CashierC:15. return endSaleTransaction()
CashierC -> EzShopController:16. payment(see UC7)
EzShopController -> PaymentController: 17. payment(see UC 7)
PaymentController -> PaymentController:18. pushSoldItemsToDB();
PaymentController -> PaymentController:19. recordTransaction();
PaymentController -> AccountBook: 20. recordSaleTransaction(transactionId)
activate AccountBook
AccountBook -> AccountBook: 21. updateBalance(N*unitPrice)
AccountBook -> AccountBook: 22. return updateBalance

AccountBook -> PaymentController: 23. return recordSaleTransaction()
PaymentController -> EzShopController:24. return Payment Status
EzShopController -> CashierC:25. return Payment Status 
CashierC -> EzShopController: 26. computePointsForSale(transactionId)
EzShopController -> PaymentController: 27. getSaleTransaction(transactionId)
PaymentController -> EzShopController: 28. return getSaleTransaction()
EzShopController -> CashierC: 29: return computePointsForSale(transactionId)
CashierC -> EzShopController: 30.modifyPointsOnCard(customerCard, pointsToBeAdded)
EzShopController -> CustomerManagement: 31.modifyPointsOnCard(customerCard, pointsToBeAdded)
CustomerManagement-> EzShopController:32. return modifyPointsOnCard()
EzShopController -> CashierC: 33. return modifyPointsOnCard()

```

### Scenario 7.1 - Manage payment by valid credit card
```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: 1. receiveCardPayement(transactionId, creditCard)
activate EzShopController
EzShopController -> PaymentController: 2. issueCardPayment(transactionId, creditCard)
activate PaymentController
PaymentController -> PaymentGateway: 3. issuePayment(creditCard, amount)
activate PaymentGateway
PaymentGateway -> PaymentGateway: 4. verifyCard(creditCard)
PaymentGateway -> PaymentController: 5. return issuePayment()
deactivate PaymentGateway
PaymentController-> PaymentController:6. pushSoldItemsToDB()
PaymentController-> PaymentController:7. recordPayment()
PaymentController -> AccountBook: 8. recordSaleTransaction(transaction)
activate AccountBook
AccountBook -> PaymentController: 9. return recordSaleTransaction
deactivate AccountBook
PaymentController -> EzShopController: 10. return issueCardPayment
EzShopController -> CashierC: 11. return receiveCreditCardPayment

```

### Scenario 7.2 - Manage payment by invalid credit card

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: 1. receiveCardPayement(transactionId, creditCard)
activate EzShopController
EzShopController -> PaymentController: 2. issueCardPayment(transactionId, creditCard)
activate PaymentController
PaymentController -> PaymentGateway: 3. issuePayment(creditCard, amount)
activate PaymentGateway
PaymentGateway -> PaymentGateway: 4. verifyCard(creditCard)
PaymentGateway -> PaymentController: 5. throws InvalidCreditCardException
deactivate PaymentGateway
PaymentController -> EzShopController: 6. throws InvalidCreditCardException
EzShopController -> CashierC: 7. throws InvalidCreditCardException
CashierC -> EzShopController: 8. deleteSaleTransaction(transactionId)
EzShopController -> PaymentController: 9. deleteSaleTransaction(transactionId)
PaymentController -> InventoryManagement: 10. updateProductQuantity(pId, amount)
activate InventoryManagement
InventoryManagement -> PaymentController: 11 return updateProductQuantity()
deactivate InventoryManagement
PaymentController -> EzShopController: 12. return deleteSaleTransaction()
EzShopController -> CashierC: 13. return deleteSaleTransaction()


```

### Scenario 7.3 - Manage credit card payment with not enough credit

```plantuml


participant CashierC
activate CashierC
CashierC -> EzShopController:  receiveCardPayement(transactionId, creditCard)
activate EzShopController
EzShopController -> PaymentController:  issueCardPayment(transactionId, creditCard)
activate PaymentController
PaymentController -> PaymentGateway:  issuePayment(creditCard, amount)
activate PaymentGateway
PaymentGateway -> PaymentGateway: verifyCard(creditCard)
PaymentGateway -> PaymentController:  return issuePayment()
deactivate PaymentGateway
PaymentController -> EzShopController:  return issueCardPayment
EzShopController -> CashierC:  return receiveCardPayment
CashierC -> EzShopController:  deleteSaleTransaction(transactionId)
EzShopController -> PaymentController:  deleteSaleTransaction(transactionId)
PaymentController -> InventoryManagement:  updateProductQuantity(pId, amount)
activate InventoryManagement
InventoryManagement -> PaymentController:  return updateProductQuantity()
deactivate InventoryManagement
PaymentController -> EzShopController:  return deleteSaleTransaction()
EzShopController -> CashierC: return deleteSaleTransaction()
```

### Scenario 7.4 - Manage cash payment

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: receiveCashPayement(transactionId, creditCard)
activate EzShopController
EzShopController -> PaymentController:  issueCashPayment(transactionId, cash)
activate PaymentController
PaymentController-> PaymentController: pushSoldItemsToDB()
PaymentController-> PaymentController: recordPayment()
PaymentController -> AccountBook: recordSaleTransaction(transaction)
activate AccountBook
AccountBook -> PaymentController: return recordSaleTransaction
deactivate AccountBook
PaymentController -> EzShopController:  return issueCashPayment
EzShopController -> CashierC:  return receiveCashPayment


```

### Scenario 8.1 - Return transaction of product type X completed, credit card

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: startReturnTransaction(transactionId)
activate EzShopController
activate PaymentController
EzShopController -> PaymentController:  startReturnTransaction(returnId)
PaymentController -> PaymentController:  getSaleTransaction(returnId)
PaymentController -> EzShopController: return startReturnTransaction
EzShopController -> CashierC:  return startReturnTransaction
CashierC-> EzShopController: returnProduct(returnId, productCode, N)
EzShopController-> PaymentController: returnProduct(returnId, productCode, N)

EzShopController <- PaymentController:  return returnProduct()
EzShopController -> CashierC: return returnProduct()

CashierC -> EzShopController: endReturnTransaction(returnId, true)
EzShopController -> PaymentController: endReturnTransaction(returnId, true)
EzShopController <- PaymentController: return endReturnTransaction()
EzShopController -> CashierC:  return endReturnTransaction()
CashierC-> EzShopController: returnCrediCardPayment(returnId, creditCard)
EzShopController -> PaymentController: returnCrediCardPayment(returnId, creditCard)
PaymentController -> PaymentGateway:  issuePayment(creditCard, amount)
activate PaymentGateway
PaymentGateway -> PaymentGateway:  verifyCard(creditCard)
PaymentGateway -> PaymentController:  return issuePayment()
deactivate PaymentGateway
PaymentController->AccountBook: recordReturnTransaction(returnTransaction)
activate AccountBook
PaymentController<-AccountBook: return recordReturnTransaction()
deactivate AccountBook
EzShopController <- PaymentController:return returnCrediCardPayment(returnId, creditCard)
EzShopController -> CashierC :return returnCrediCardPayment(returnId, creditCard)
```

### Scenario 9.1 - List credits and debits

```plantuml

participant ManagerC
activate ManagerC
ManagerC -> EzShopController: 1. getCreditsAndDebits(from, to)
activate EzShopController
EzShopController -> AccountBook: 2. getCreditsAndDebits(from, to)
activate AccountBook
AccountBook -> EzShopController: 3. return getCreditsAndDebits
deactivate AccountBook
EzShopController -> ManagerC: 4. return getCreditsAndDebits

```

### Scenario 10.1 - Return payment by  credit card

```plantuml

participant CashierC
activate CashierC
CashierC -> EzShopController: endReturnTransaction(returnId, true)
activate EzShopController
EzShopController -> PaymentController: endReturnTransaction(returnId, true)
activate PaymentController
EzShopController <- PaymentController:  return endReturnTransaction()
EzShopController -> CashierC:  return endReturnTransaction()
CashierC-> EzShopController: returnCrediCardPayment(returnId, creditCard)
EzShopController -> PaymentController: returnCrediCardPayment(returnId, creditCard)
PaymentController -> PaymentGateway: issuePayment(creditCard, amount)
activate PaymentGateway
PaymentGateway -> PaymentGateway:  verifyCard(creditCard)
PaymentGateway -> PaymentController: return issuePayment()
deactivate PaymentGateway
PaymentController->AccountBook: recordReturnTransaction(returnTransaction)
activate AccountBook
PaymentController<-AccountBook: return recordReturnTransaction()
deactivate AccountBook
EzShopController <- PaymentController:return returnCrediCardPayment(returnId, creditCard)
EzShopController -> CashierC :return returnCrediCardPayment(returnId, creditCard)

```

### Scenario 10.2 - return  cash payment

```plantuml
participant CashierC
activate CashierC
CashierC -> EzShopController: endReturnTransaction(returnId, true)
activate EzShopController
EzShopController -> PaymentController: endReturnTransaction(returnId, true)
activate PaymentController
EzShopController <- PaymentController: 8. return endReturnTransaction()
EzShopController -> CashierC: 5. return endReturnTransaction()
CashierC-> EzShopController: returnCashPayment(returnId)
EzShopController -> PaymentController: returnCashPayment(returnId)

PaymentController->AccountBook: recordReturnTransaction(returnTransaction)
activate AccountBook
PaymentController<-AccountBook: return recordReturnTransaction()
deactivate AccountBook
EzShopController <- PaymentController:return returnCrediCardPayment()
EzShopController -> CashierC :return returnCashPayment()


```

