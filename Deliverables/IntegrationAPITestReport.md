# Integration and API Test Documentation

Authors: Biffoni Matteo, Carota Giulio, Cavallo Andrea, Piccirillo Angelo Oscar

Date: 24/05/2021

Version: 1.0

# Contents

- [Dependency graph](#dependency graph)

- [Integration approach](#integration)

- [Tests](#tests)

- [Scenarios](#scenarios)

- [Coverage of scenarios and FR](#scenario-coverage)
- [Coverage of non-functional requirements](#nfr-coverage)



# Dependency graph 


```plantuml

     class EZShop
     class AccountBook
     class CustomerManagement
     class InventoryManagement
     class PaymentController
     class PaymentGateway
     class UserManagement
     class BalanceOperationObject
     class CreditCardObject
     class CustomerObject
     class OrderObject
     class ProductTypeObject
     class ReturnTransactionObject
     class SaleTransactionObject
     class TicketEntryObject
     class UserObject

     EZShop --> UserManagement
     EZShop --> InventoryManagement
     EZShop --> CustomerManagement
     EZShop --> PaymentController
     EZShop --> SaleTransactionObject
     EZShop --> AccountBook

     AccountBook --> BalanceOperationObject
     AccountBook --> EZShop
     AccountBook --> SaleTransactionObject
     AccountBook --> ReturnTransactionObject
     AccountBook --> OrderObject

     CustomerManagement --> EZShop
     CustomerManagement --> CustomerObject

     InventoryManagement --> EZShop
     InventoryManagement --> ProductTypeObject
     InventoryManagement --> OrderObject
     InventoryManagement --> AccountBook

     PaymentController --> EZShop
     PaymentController --> SaleTransactionObject
     PaymentController --> ReturnTransactionObject
     PaymentController --> InventoryManagement
     PaymentController --> ProductTypeObject
     PaymentController --> TicketEntryObject
     PaymentController --> AccountBook

     PaymentGateway --> EZShop
     PaymentGateway --> CreditCardObject

     UserManagement --> EZShop
     UserManagement --> UserObject

     SaleTransactionObject --> TicketEntryObject

```

# Integration approach

The integration sequence we have adopetd is bottom up with the following steps:
 * Step1: all classes in the UnitTestReport.md
 * Step2: step1 + all management classes (e.g. InventoryManagement, PaymentController etc.)
 * Step3: step2 + APITest



#  Tests

## Step 1
| Classes  | JUnit test cases |
|--|--|
|Unit tests: BalanceOperationObject, CreditCardObject, CustomerObject, ProductTypeObject, ReturnTransactionObject, SaleTransactionObject, TicketEntryObject, UserObject, PaymentGateway, OrderObject|BalanceOperationTest, CreditCardTest, CustomerTest, ProductTypeTest, ReturnTransactionUnitTest, SaleTransactionUnitTest, TicketEntryTest, UserTest, PaymentGatewayUnitTest, OrderTest, CustomerManagementUnitTest,InventoryManagementUnitTest,AccountBookUnitTest|


## Step 2
| Classes  | JUnit test cases |
|--|--|
|AccountBook, CustomerManagement, InventoryManagement, PaymentController, UserManagement|AccountBookIntegrationTest, CustomerManagementIntegrationTest, InventoryManagementIntegrationTest, PaymentControllerTest, UserManagementTest, ReturnTransactionIntegrationTest,SaleTransactionIntegrationTest|


## Step 3 

   

| Classes  | JUnit test cases |
|--|--|
|API test: EZShop|APITest|



# Coverage of Scenarios and FR




| Scenario ID | Functional Requirements covered | JUnit  Test(s) |
| ----------- | ------------------------------- | ----------- |
|  1.1      | FR3.1, FR4.1, FR4.2   | createProductTypeTest, updateProductTypeTest, updateQuantityTest, updatePositionTest |
|  1.2      | FR3.4, FR4.2                   | updateProductTypeTest, updatePositionTest, getProductTypeByBarcodeTest |
| 1.3      | FR3.1, FR3.4 | updateProductTypeTest, getProductTypeByBarcodeTest |
| 2.1     | FR1.1 | createUserTest |
| 2.2      | FR1.2 | deleteUserTest |
| 2.3      | FR1.5 | updateUserRightsTest |
| 3.1 | FR4.3, FR4.4 | issueOrderTest |
| 3.2 | FR4.4, FR4.5 | payOrderTest, payOrderForTest |
| 3.3 | FR4.6 | recordOrderArrivalTest |
| 4.1 | FR5.1 | defineCustomerTest |
| 4.2 | FR5.5, FR5.6 | createCardTest, attachCardToCustomerTest |
| 4.3 | FR5.1 | modifyCustomerTest |
| 4.4 | FR5.1 | modifyCustomerTest |
| 5.1 | FR1.5 | loginTest |
| 5.2 | FR1.5 | logoutTest |
| 6.1 | FR6.1, FR6.2, FR6.7, FR6.8, FR6.9, FR6.10, FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction |
| 6.2 | FR6.1, FR6.2, FR6.5,  FR6.7, FR6.8, FR6.9, FR6.10, FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction, receiveCashPaymentTest, receiveCreditCardPaymentTest, applyDiscountRateToProductTest |
| 6.3 | FR6.1, FR6.2, FR6.4,  FR6.7, FR6.8, FR6.9, FR6.10, FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction, receiveCashPaymentTest, receiveCreditCardPaymentTest, applyDiscountRateToSaleTest |
| 6.4 | FR5.7, FR6.1, FR6.2, FR6.6, FR6.7, FR6.8, FR6.9, FR6.10, FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction, receiveCashPaymentTest, receiveCreditCardPaymentTest, computePointsForSaleTest, modifyPointsOnCardTest |
| 6.5 | FR6.1, FR6.2, FR6.7, FR6.8, FR6.9, FR6.10, FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction, receiveCashPaymentTest, receiveCreditCardPaymentTest, deleteSaleTransactionTest |
| 6.6 | FR6.1, FR6.2, FR6.7, FR6.8, FR6.9, FR6.10, FR7.3,  FR6.11 | startSaleTransactionTest, addProductToSaleTest, endSaleTransaction, receiveCashPaymentTest |
| 7.1 | FR7.2 | receiveCreditCardPaymentTest |
| 7.2 | FR7.2 | receiveCreditCardPaymentTest |
| 7.3 | FR7.2 | receiveCreditCardPaymentTest |
| 7.4 | FR7.1 | receiveCashPaymentTest |
| 8.1 | FR6.12, FR6.13, FR6.14, FR6.15, FR7.4 | startReturnTransactionTest, returnProductTest, endReturnTransactionTest, returnCreditCardPaymentTest |
| 8.2 | FR6.12, FR6.13, FR6.14, FR6.15, FR7.3 | startReturnTransactionTest, returnProductTest, endReturnTransactionTest, returnCashPaymentTest |
| 9.1 | FR8.3 | getCreditsAndDebitsTest |
| 10.1 | FR7.4 | returnCreditCardPaymentTest |
| 10.2 | FR7.3 | returnCashPaymentTest |



# Coverage of Non Functional Requirements

### 

| Non Functional Requirement       | Test name                                                    |
| -------------------------------- | ------------------------------------------------------------ |
| NFR4: Domain on barcode          | InventoryManagementUnitTest.isBarcodeValidTest               |
| NFR5: Domain on credit card      | PaymentGatewayUnitTest.verifyLength, PaymentGatewayUnitTest.verifyValidity, PaymentGatewayUnitTest.verifyCombinations |
| NFR6: Domain on customer's cards | CustomerManagementUnitTest.generateCardCodeTest              |

