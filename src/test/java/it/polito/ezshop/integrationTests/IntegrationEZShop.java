package it.polito.ezshop.integrationTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AccountBookIntegrationTest.class,
        APITest.class,
        CustomerManagementIntegrationTest.class,
        InventoryManagementIntegrationTest.class,
        PaymentControllerTest.class,
        ReturnTransactionIntegrationTest.class,
        SaleTransactionIntegrationTest.class,
        SaleTransactionIntegrationTest.class,
        UserManagementTest.class,
})
public class IntegrationEZShop {

}
