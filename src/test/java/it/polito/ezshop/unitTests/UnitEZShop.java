package it.polito.ezshop.unitTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AccountBookUnitTest.class,
        BalanceOperationTest.class,
        CreditCardTest.class,
        CustomerManagementUnitTest.class,
        CustomerTest.class,
        InventoryManagementUnitTest.class,
        OrderTest.class,
        PaymentGatewayUnitTest.class,
        ProductTypeTest.class,
        ReturnTransactionUnitTest.class,
        SaleTransactionUnitTest.class,
        TicketEntryTest.class,
        UserTest.class
})
public class UnitEZShop {

}
