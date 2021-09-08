package it.polito.ezshop.integrationTests;

import it.polito.ezshop.controllers.*;
import it.polito.ezshop.data.*;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.BalanceOperationObject;
import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class APITest {

    @Test
    public void createUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        assertThrows(InvalidUsernameException.class, () -> ezShop.createUser(null, "password", "Administrator"));
        assertThrows(InvalidUsernameException.class, () -> ezShop.createUser("", "password", "Administrator"));
        assertThrows(InvalidPasswordException.class, () -> ezShop.createUser("username", null, "Administrator"));
        assertThrows(InvalidPasswordException.class, () -> ezShop.createUser("username", "", "Administrator"));
        assertThrows(InvalidRoleException.class, () -> ezShop.createUser("username", "password", null));
        assertThrows(InvalidRoleException.class, () -> ezShop.createUser("username", "password", "NotAnAdmissibleRole"));
        Integer idAdded = ezShop.createUser("username", "password", "Administrator");
        assertNotEquals(Integer.valueOf(-1), idAdded);
        assertEquals(Integer.valueOf(-1), ezShop.createUser("username", "password", "Administrator"));
       DB.alterJDBCUrl();
        assertEquals(Integer.valueOf(-1), ezShop.createUser("newUsername", "password", "Cashier"));
        DB.restoreJDBCUrl();

       ezShop.reset();
    }

    @Test
    public void deleteUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.deleteUser(shopManagerAdded));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.deleteUser(adminAdded));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("admin_user", "password"));
        assertThrows(InvalidUserIdException.class, () -> ezShop.deleteUser(null));
        assertThrows(InvalidUserIdException.class, () -> ezShop.deleteUser(0));
       DB.alterJDBCUrl();
        assertFalse(ezShop.deleteUser(cashierAdded));
        DB.restoreJDBCUrl();
        assertTrue(ezShop.deleteUser(cashierAdded));
        // returns true even if the user was not there
        assertFalse(ezShop.deleteUser(Integer.MAX_VALUE));

       ezShop.reset();
    }

    @Test
    public void getAllUsersTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, ezShop::getAllUsers);
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(UnauthorizedException.class, ezShop::getAllUsers);
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("admin_user", "password"));
        List<User> usersRetrieved = ezShop.getAllUsers();
        assertNotNull(usersRetrieved);
        assertEquals(3, usersRetrieved.size());
        assertEquals("cashier_user", usersRetrieved.get(0).getUsername());
        assertEquals("password", usersRetrieved.get(0).getPassword());
        assertEquals("Cashier", usersRetrieved.get(0).getRole());
        assertEquals(cashierAdded, usersRetrieved.get(0).getId());
        assertEquals("shopmanager_user", usersRetrieved.get(1).getUsername());
        assertEquals("password", usersRetrieved.get(1).getPassword());
        assertEquals("ShopManager", usersRetrieved.get(1).getRole());
        assertEquals(shopManagerAdded, usersRetrieved.get(1).getId());
        assertEquals("admin_user", usersRetrieved.get(2).getUsername());
        assertEquals("password", usersRetrieved.get(2).getPassword());
        assertEquals("Administrator", usersRetrieved.get(2).getRole());
        assertEquals(adminAdded, usersRetrieved.get(2).getId());
       DB.alterJDBCUrl();
        assertNull(ezShop.getAllUsers());
        DB.restoreJDBCUrl();

       ezShop.reset();
    }

    @Test
    public void getUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.getUser(shopManagerAdded));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.getUser(adminAdded));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("admin_user", "password"));
        assertThrows(InvalidUserIdException.class, () -> ezShop.getUser(null));
        assertThrows(InvalidUserIdException.class, () -> ezShop.getUser(0));
        assertNull(ezShop.getUser(Integer.MAX_VALUE));
       DB.alterJDBCUrl();
        assertNull(ezShop.getUser(cashierAdded));
        DB.restoreJDBCUrl();
        User cashierRetrieved = ezShop.getUser(cashierAdded);
        assertNotNull(cashierRetrieved);
        assertEquals("cashier_user", cashierRetrieved.getUsername());
        assertEquals("password", cashierRetrieved.getPassword());
        assertEquals("Cashier", cashierRetrieved.getRole());
        assertEquals(cashierAdded, cashierRetrieved.getId());
        User shopManagerRetrieved = ezShop.getUser(shopManagerAdded);
        assertNotNull(shopManagerRetrieved);
        assertEquals("shopmanager_user", shopManagerRetrieved.getUsername());
        assertEquals("password", shopManagerRetrieved.getPassword());
        assertEquals("ShopManager", shopManagerRetrieved.getRole());
        assertEquals(shopManagerAdded, shopManagerRetrieved.getId());
        User adminRetrieved = ezShop.getUser(adminAdded);
        assertNotNull(adminRetrieved);
        assertEquals("admin_user", adminRetrieved.getUsername());
        assertEquals("password", adminRetrieved.getPassword());
        assertEquals("Administrator", adminRetrieved.getRole());
        assertEquals(adminAdded, adminRetrieved.getId());

       ezShop.reset();
    }

    @Test
    public void updateUserRightsTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.updateUserRights(cashierAdded, "Administrator"));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.updateUserRights(shopManagerAdded, "Administrator"));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("admin_user", "password"));

       DB.alterJDBCUrl();
        assertFalse(ezShop.updateUserRights(cashierAdded, "Administrator"));
        DB.restoreJDBCUrl();

        assertThrows(InvalidUserIdException.class, () -> ezShop.updateUserRights(null, "Administrator"));
        assertThrows(InvalidUserIdException.class, () -> ezShop.updateUserRights(0, "Administrator"));
        assertThrows(InvalidRoleException.class, () -> ezShop.updateUserRights(cashierAdded, null));
        assertThrows(InvalidRoleException.class, () -> ezShop.updateUserRights(cashierAdded, "NotAnAdmissibleRole"));
        assertFalse(ezShop.updateUserRights(Integer.MAX_VALUE, "Administrator"));
        assertTrue(ezShop.updateUserRights(cashierAdded, "Administrator"));
        User userRetrieved = ezShop.getUser(cashierAdded);
        assertNotNull(userRetrieved);
        assertEquals("Administrator", userRetrieved.getRole());

       ezShop.reset();
    }

    @Test
    public void loginTest() throws SQLException, InvalidPasswordException, InvalidUsernameException, InvalidRoleException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        assertThrows(InvalidUsernameException.class, () -> ezShop.login(null, "password"));
        assertThrows(InvalidUsernameException.class, () -> ezShop.login("", "password"));
        assertThrows(InvalidPasswordException.class, () -> ezShop.login("username", null));
        assertThrows(InvalidPasswordException.class, () -> ezShop.login("username", ""));
        assertNull(ezShop.login("username", "password"));

        Integer adminAdded = ezShop.createUser("username", "password", "Administrator");
        assertNotEquals(Integer.valueOf(-1), adminAdded);
        assertNull(ezShop.login("username", "wrongpassword"));
        User adminRetrieved = ezShop.login("username", "password");
        assertNotNull(adminRetrieved);
        assertEquals(adminAdded, adminRetrieved.getId());
        assertEquals("username", adminRetrieved.getUsername());
        assertEquals("password", adminRetrieved.getPassword());
        assertEquals("Administrator", adminRetrieved.getRole());

       ezShop.reset();
    }

    @Test
    public void logoutTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        assertFalse(ezShop.logout());
        Integer adminAdded = ezShop.createUser("username", "password", "Administrator");
        assertNotEquals(Integer.valueOf(-1), adminAdded);
        assertNotNull(ezShop.login("username", "password"));
        assertTrue(ezShop.logout());

       ezShop.reset();
    }

    @Test
    public void createProductTypeTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        String invalidBarCode = "978020137";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.createProductType(description, validBarCode, pricePerUnit, note));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductDescriptionException.class, () -> ezShop.createProductType(null, validBarCode, pricePerUnit, note));
        assertThrows(InvalidProductDescriptionException.class, () -> ezShop.createProductType("", validBarCode, pricePerUnit, note));
        assertThrows(InvalidPricePerUnitException.class, () -> ezShop.createProductType(description, validBarCode, 0, note));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.createProductType(description, null, pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.createProductType(description, "", pricePerUnit, note));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.createProductType(description, invalidBarCode, pricePerUnit, note));

        DB.alterJDBCUrl();
        assertEquals(minus1, ezShop.createProductType(description, validBarCode, pricePerUnit, note));
        DB.restoreJDBCUrl();

        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);

        assertEquals(minus1, ezShop.createProductType(description, validBarCode, pricePerUnit, note));

       ezShop.reset();
    }

    @Test
    public void updateProductTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String newDescription = "newDescription";
        String validBarCode = "9788832360103";
        String newValidBarCode = "9788808182159";
        String anotherValidBarCode = "9788817140966";
        String invalidBarCode = "978020137";
        double pricePerUnit = 1.0;
        double newPricePerUnit = 1.6;
        String note = "note";
        String newNote = "newNote";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.createProductType(description, validBarCode, pricePerUnit, note));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertFalse(ezShop.updateProduct(Integer.MAX_VALUE, description, validBarCode, pricePerUnit, note));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertThrows(InvalidProductIdException.class, () -> ezShop.updateProduct(null, newDescription, newValidBarCode, newPricePerUnit, newNote));
        assertThrows(InvalidProductIdException.class, () -> ezShop.updateProduct(0, newDescription, newValidBarCode, newPricePerUnit, newNote));
        assertThrows(InvalidProductDescriptionException.class, () -> ezShop.updateProduct(productAdded, null, newValidBarCode, newPricePerUnit, newNote));
        assertThrows(InvalidProductDescriptionException.class, () -> ezShop.updateProduct(productAdded, "", newValidBarCode, newPricePerUnit, newNote));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.updateProduct(productAdded, newDescription, null, newPricePerUnit, newNote));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.updateProduct(productAdded, newDescription, "", newPricePerUnit, newNote));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.updateProduct(productAdded, newDescription, invalidBarCode, newPricePerUnit, newNote));
        assertThrows(InvalidPricePerUnitException.class, () -> ezShop.updateProduct(productAdded, newDescription, newValidBarCode, -2, newNote));
        Integer anotherProductAdded = ezShop.createProductType("another" + description, anotherValidBarCode, 1.0, "another" + note);
        assertNotEquals(minus1, anotherProductAdded);
        assertFalse(ezShop.updateProduct(productAdded, newDescription, anotherValidBarCode, newPricePerUnit, newNote));

       DB.alterJDBCUrl();
        assertFalse(ezShop.updateProduct(productAdded, newDescription, newValidBarCode, newPricePerUnit, newNote));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.updateProduct(productAdded, newDescription, newValidBarCode, newPricePerUnit, newNote));

       ezShop.reset();
    }

    @Test
    public void deleteProductTypeTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezShop.deleteProductType(1));
        assertTrue(ezShop.logout());
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductIdException.class, () -> ezShop.deleteProductType(null));
        assertThrows(InvalidProductIdException.class, () -> ezShop.deleteProductType(0));
        assertFalse(ezShop.deleteProductType(Integer.MAX_VALUE));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);

       DB.alterJDBCUrl();
        assertFalse(ezShop.deleteProductType(productAdded));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.deleteProductType(productAdded));

       ezShop.reset();
    }

    @Test
    public void getAllProductTypes() throws SQLException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        ezShop.logout();
        assertThrows(UnauthorizedException.class, ezShop::getAllProductTypes);

        String description1 = "description1";
        String description2 = "description2";
        String description3 = "description3";
        String validBarCode1 = "9788832360103";
        String validBarCode2 = "9788808182159";
        String validBarCode3 = "9788817140966";
        double pricePerUnit1 = 0.5;
        double pricePerUnit2 = 0.6;
        double pricePerUnit3 = 0.7;
        String note1 = "note1";
        String note2 = "note2";
        String note3 = "note3";

        Integer minus1 = -1;
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("admin_user", "password"));

        assertEquals(0, ezShop.getAllProductTypes().size());

        Integer productAdded1 = ezShop.createProductType(description1, validBarCode1, pricePerUnit1, note1);
        assertNotEquals(minus1, productAdded1);
        Integer productAdded2 = ezShop.createProductType(description2, validBarCode2, pricePerUnit2, note2);
        assertNotEquals(minus1, productAdded2);
        Integer productAdded3 = ezShop.createProductType(description3, validBarCode3, pricePerUnit3, note3);
        assertNotEquals(minus1, productAdded3);

        List<ProductType> productsRetrieved = ezShop.getAllProductTypes();
        assertEquals(3, productsRetrieved.size());
        assertEquals(productAdded1, productsRetrieved.get(0).getId());
        assertEquals(description1, productsRetrieved.get(0).getProductDescription());
        assertEquals(validBarCode1, productsRetrieved.get(0).getBarCode());
        assertEquals(Double.valueOf(pricePerUnit1), productsRetrieved.get(0).getPricePerUnit());
        assertEquals(note1, productsRetrieved.get(0).getNote());
        assertEquals(productAdded2, productsRetrieved.get(1).getId());
        assertEquals(description2, productsRetrieved.get(1).getProductDescription());
        assertEquals(validBarCode2, productsRetrieved.get(1).getBarCode());
        assertEquals(Double.valueOf(pricePerUnit2), productsRetrieved.get(1).getPricePerUnit());
        assertEquals(note2, productsRetrieved.get(1).getNote());
        assertEquals(productAdded3, productsRetrieved.get(2).getId());
        assertEquals(description3, productsRetrieved.get(2).getProductDescription());
        assertEquals(validBarCode3, productsRetrieved.get(2).getBarCode());
        assertEquals(Double.valueOf(pricePerUnit3), productsRetrieved.get(2).getPricePerUnit());
        assertEquals(note3, productsRetrieved.get(2).getNote());

       ezShop.reset();
    }

    @Test
    public void getProductTypeByBarcodeTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        String anotherValidBarCode = "9788808182159";
        String invalidBarCode = "978020137";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.getProductTypeByBarCode(null));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.getProductTypeByBarCode(""));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.getProductTypeByBarCode(invalidBarCode));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertNull(ezShop.getProductTypeByBarCode(anotherValidBarCode));
        ProductType productRetrieved = ezShop.getProductTypeByBarCode(validBarCode);
        assertNotNull(productRetrieved);
        assertEquals(productAdded, productRetrieved.getId());
        assertEquals(description, productRetrieved.getProductDescription());
        assertEquals(validBarCode, productRetrieved.getBarCode());
        assertEquals(Double.valueOf(pricePerUnit), productRetrieved.getPricePerUnit());
        assertEquals(note, productRetrieved.getNote());

       ezShop.reset();
    }

    @Test
    public void getProductTypesByDescriptionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description1 = "description1";
        String description2 = "description1";
        String description3 = "description3";
        String validBarCode1 = "9788832360103";
        String validBarCode2 = "9788808182159";
        String validBarCode3 = "9788817140966";
        double pricePerUnit1 = 0.5;
        double pricePerUnit2 = 0.6;
        double pricePerUnit3 = 0.7;
        String note1 = "note1";
        String note2 = "note2";
        String note3 = "note3";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        Integer productAdded1 = ezShop.createProductType(description1, validBarCode1, pricePerUnit1, note1);
        assertNotEquals(minus1, productAdded1);
        Integer productAdded2 = ezShop.createProductType(description2, validBarCode2, pricePerUnit2, note2);
        assertNotEquals(minus1, productAdded2);
        Integer productAdded3 = ezShop.createProductType(description3, validBarCode3, pricePerUnit3, note3);
        assertNotEquals(minus1, productAdded3);
        List<ProductType> productsRetrieved = ezShop.getProductTypesByDescription(description1);
        assertEquals(2, productsRetrieved.size());
        assertEquals(productAdded1, productsRetrieved.get(0).getId());
        assertEquals(description1, productsRetrieved.get(0).getProductDescription());
        assertEquals(validBarCode1, productsRetrieved.get(0).getBarCode());
        assertEquals(Double.valueOf(pricePerUnit1), productsRetrieved.get(0).getPricePerUnit());
        assertEquals(note1, productsRetrieved.get(0).getNote());
        assertEquals(productAdded2, productsRetrieved.get(1).getId());
        assertEquals(description2, productsRetrieved.get(1).getProductDescription());
        assertEquals(validBarCode2, productsRetrieved.get(1).getBarCode());
        assertEquals(Double.valueOf(pricePerUnit2), productsRetrieved.get(1).getPricePerUnit());
        assertEquals(note2, productsRetrieved.get(1).getNote());

       ezShop.reset();
    }

    @Test
    public void updateQuantityTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductIdException.class, () -> ezShop.updateQuantity(null, 0));
        assertThrows(InvalidProductIdException.class, () -> ezShop.updateQuantity(0, 0));
        assertFalse(ezShop.updateQuantity(Integer.MAX_VALUE, 0));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertFalse(ezShop.updateQuantity(productAdded, 0));
        ezShop.updatePosition(productAdded, "1-a-1");

       DB.alterJDBCUrl();
        assertFalse(ezShop.updateQuantity(productAdded, 0));
        DB.restoreJDBCUrl();

        assertFalse(ezShop.updateQuantity(productAdded, -1));
        assertTrue(ezShop.updateQuantity(productAdded, 5));
        ProductType productRetrieved = ezShop.getProductTypeByBarCode(validBarCode);
        assertEquals(Integer.valueOf(5), productRetrieved.getQuantity());
        assertTrue(ezShop.updateQuantity(productAdded, -3));
        productRetrieved = ezShop.getProductTypeByBarCode(validBarCode);
        assertEquals(Integer.valueOf(2), productRetrieved.getQuantity());

       ezShop.reset();
    }

    @Test
    public void updatePositionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String description_other = "description_other";
        String validBarCode = "9788832360103";
        String validBarCode_other = "9788808182159";
        double pricePerUnit = 1.0;
        double pricePerUnit_other = 0.4;
        String note = "note";
        String note_other = "note_other";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        Integer productAdded_other = ezShop.createProductType(description_other, validBarCode_other, pricePerUnit_other, note_other);
        assertNotEquals(minus1, productAdded_other);
        assertThrows(InvalidProductIdException.class, () -> ezShop.updatePosition(null, "1-a-1"));
        assertThrows(InvalidProductIdException.class, () -> ezShop.updatePosition(0, "1-a-1"));
        assertThrows(InvalidLocationException.class, () -> ezShop.updatePosition(productAdded, null));
        assertThrows(InvalidLocationException.class, () -> ezShop.updatePosition(productAdded, ""));
        assertThrows(InvalidLocationException.class, () -> ezShop.updatePosition(productAdded, "notAValidFormat"));
        assertFalse(ezShop.updatePosition(Integer.MAX_VALUE, "1-a-1"));

       DB.alterJDBCUrl();
        assertFalse(ezShop.updatePosition(productAdded, "1-a-1"));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.updatePosition(productAdded, "1-a-1"));
        assertFalse(ezShop.updatePosition(productAdded_other, "1-a-1"));

       ezShop.reset();
    }

    @Test
    public void issueOrderTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidQuantityException, UnauthorizedException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductDescriptionException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        String invalidBarCode = "978020137";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.issueOrder(null, 50, 2));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.issueOrder("", 50, 2));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.issueOrder(invalidBarCode, 50, 2));
        assertEquals(minus1, ezShop.issueOrder(validBarCode, 50, 2));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertThrows(InvalidQuantityException.class, () -> ezShop.issueOrder(validBarCode, 0, 2));
        assertThrows(InvalidPricePerUnitException.class, () -> ezShop.issueOrder(validBarCode, 20, 0));

       DB.alterJDBCUrl();
        assertEquals(minus1, ezShop.issueOrder(validBarCode, 50, 2));
        DB.restoreJDBCUrl();

        Integer orderAdded = ezShop.issueOrder(validBarCode, 50, 1);
        assertNotEquals(minus1, orderAdded);
        List<Order> ordersRetrieved = ezShop.getAllOrders();
        assertNotNull(ordersRetrieved);
        assertEquals(1, ordersRetrieved.size());
        assertEquals(validBarCode, ordersRetrieved.get(0).getProductCode());
        assertEquals(Double.valueOf(1), Double.valueOf(ordersRetrieved.get(0).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(0).getQuantity());
        assertEquals("ISSUED", ordersRetrieved.get(0).getStatus());
        assertEquals(orderAdded, ordersRetrieved.get(0).getOrderId());

       ezShop.reset();
    }

    @Test
    public void payOrderForTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidQuantityException, UnauthorizedException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductDescriptionException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        String invalidBarCode = "978020137";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.payOrderFor(null, 0, 0));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.payOrderFor("", 0, 0));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.payOrderFor(invalidBarCode, 0, 0));
        assertThrows(InvalidQuantityException.class, () -> ezShop.payOrderFor(validBarCode, 0, 0));
        assertThrows(InvalidPricePerUnitException.class, () -> ezShop.payOrderFor(validBarCode, 50, 0));
        assertEquals(minus1, ezShop.payOrderFor(validBarCode, 50, 2));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertEquals(minus1, ezShop.payOrderFor(validBarCode, 50, 2));

       DB.alterJDBCUrl();
        assertEquals(minus1, ezShop.payOrderFor(validBarCode, 50, 2));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.recordBalanceUpdate(100));
        Integer orderAdded = ezShop.payOrderFor(validBarCode, 50, 2);
        assertNotEquals(minus1, orderAdded);
        List<Order> ordersRetrieved = ezShop.getAllOrders();
        assertNotNull(ordersRetrieved);
        assertEquals(1, ordersRetrieved.size());
        assertEquals(validBarCode, ordersRetrieved.get(0).getProductCode());
        assertEquals(Double.valueOf(2), Double.valueOf(ordersRetrieved.get(0).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(0).getQuantity());
        assertEquals("PAYED", ordersRetrieved.get(0).getStatus());
        assertEquals(orderAdded, ordersRetrieved.get(0).getOrderId());

       ezShop.reset();
    }

    @Test
    public void payOrderTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidOrderIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidOrderIdException.class, () -> ezShop.payOrder(null));
        assertThrows(InvalidOrderIdException.class, () -> ezShop.payOrder(0));
        assertFalse(ezShop.payOrder(Integer.MAX_VALUE));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        Integer orderAdded = ezShop.issueOrder(validBarCode, 50, 2);
        assertNotEquals(minus1, orderAdded);
        assertTrue(ezShop.deleteProductType(productAdded));
        assertFalse(ezShop.payOrder(orderAdded));
        productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        assertFalse(ezShop.payOrder(orderAdded));
        assertTrue(ezShop.recordBalanceUpdate(100));

       DB.alterJDBCUrl();
        assertFalse(ezShop.payOrder(orderAdded));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.payOrder(orderAdded));
        List<Order> ordersRetrieved = ezShop.getAllOrders();
        assertNotNull(ordersRetrieved);
        assertEquals(1, ordersRetrieved.size());
        assertEquals(validBarCode, ordersRetrieved.get(0).getProductCode());
        assertEquals(Double.valueOf(2), Double.valueOf(ordersRetrieved.get(0).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(0).getQuantity());
        assertEquals("PAYED", ordersRetrieved.get(0).getStatus());
        assertEquals(orderAdded, ordersRetrieved.get(0).getOrderId());

        assertTrue(ezShop.recordBalanceUpdate(100));
        assertFalse(ezShop.payOrder(orderAdded));
        assertTrue(ezShop.updatePosition(productAdded, "1-a-1"));
        assertTrue(ezShop.recordOrderArrival(orderAdded));
        assertFalse(ezShop.payOrder(orderAdded));

       ezShop.reset();
    }

    @Test
    public void recordOrderArrivalTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidLocationException, UnauthorizedException, InvalidOrderIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidQuantityException, InvalidProductIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description = "description";
        String validBarCode = "9788832360103";
        double pricePerUnit = 1.0;
        String note = "note";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertThrows(InvalidOrderIdException.class, () -> ezShop.recordOrderArrival(null));
        assertThrows(InvalidOrderIdException.class, () -> ezShop.recordOrderArrival(0));
        assertFalse(ezShop.recordOrderArrival(Integer.MAX_VALUE));
        Integer productAdded = ezShop.createProductType(description, validBarCode, pricePerUnit, note);
        assertNotEquals(minus1, productAdded);
        Integer orderAdded = ezShop.issueOrder(validBarCode, 50, 2);
        assertNotEquals(minus1, orderAdded);
        assertThrows(InvalidLocationException.class, () -> ezShop.recordOrderArrival(orderAdded));
        assertTrue(ezShop.updatePosition(productAdded, "1-a-1"));
        assertFalse(ezShop.recordOrderArrival(orderAdded));
        assertTrue(ezShop.recordBalanceUpdate(100));
        assertTrue(ezShop.payOrder(orderAdded));

       DB.alterJDBCUrl();
        assertFalse(ezShop.recordOrderArrival(orderAdded));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.recordOrderArrival(orderAdded));
        assertFalse(ezShop.recordOrderArrival(orderAdded));

       ezShop.reset();
    }

    @Test
    public void recordOrderArrivalRFIDTest() throws InvalidQuantityException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductDescriptionException, InvalidRFIDException, InvalidLocationException, InvalidOrderIdException, SQLException, UnauthorizedException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidProductIdException {
        EZShop ezshop = new EZShop();
        ezshop.reset();

        Integer cashierAdded = ezshop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezshop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezshop.login("shopmanager_user", "password"));

        assertThrows(InvalidOrderIdException.class, () -> ezshop.recordOrderArrivalRFID(null, ""));
        assertThrows(InvalidOrderIdException.class, () -> ezshop.recordOrderArrivalRFID(0, ""));

        ezshop.createProductType("testRecordOrder", "9788806222024", 1.2, "note");
        ezshop.recordBalanceUpdate(500);
        ezshop.payOrderFor("9788806222024", 10, 1.5);
        assertThrows(InvalidLocationException.class, () -> ezshop.recordOrderArrivalRFID(1, ""));
        ezshop.getProductTypeByBarCode(InventoryManagement.getOrder(InventoryManagement.getLasOrderId()).getProductCode()).setLocation("1-a-8");

        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(1, null));
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(1, ""));
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(1, "1234567"));
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(1, "ABCDEFGHIJ"));
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(1, "1234567891234"));

        assertEquals("PAYED", InventoryManagement.orderMap.get(1).getStatus());

        DB.alterJDBCUrl();
        assertFalse(ezshop.recordOrderArrivalRFID(1, "000000010000"));
        DB.restoreJDBCUrl();

        InventoryManagement.orderMap.get(1).setStatus("COMPLETED");
        assertFalse(ezshop.recordOrderArrivalRFID(1, "000000010000"));
        InventoryManagement.orderMap.get(1).setStatus("ISSUED");
        assertFalse(ezshop.recordOrderArrivalRFID(1, "000000010000"));

        InventoryManagement.orderMap.get(1).setStatus("PAYED");

        assertTrue(ezshop.recordOrderArrivalRFID(1, "000000010000"));

        ezshop.payOrderFor("9788806222024", 10, 1.5);
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(2, "000000010009"));
        assertThrows(InvalidRFIDException.class, () -> ezshop.recordOrderArrivalRFID(2, "000000009991"));

        ezshop.logout();
        assertNotNull(ezshop.login("cashier_user", "password"));
        assertThrows(UnauthorizedException.class, () -> ezshop.recordOrderArrivalRFID(1, "000000010000"));

        ezshop.reset();
    }

    @Test
    public void addProductToSaleTransactionRFIDTest() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidQuantityException, InvalidRFIDException, InvalidOrderIdException, InvalidTransactionIdException, SQLException, UnauthorizedException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        EZShop ezshop = new EZShop();

        ezshop.reset();

        // login as a user with permissions (all users)
        assertNotEquals(ezshop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezshop.login("user1", "pwd1"));

        assertThrows(InvalidTransactionIdException.class, () -> ezshop.addProductToSaleRFID(null, null));
        assertThrows(InvalidTransactionIdException.class, () -> ezshop.addProductToSaleRFID(0, null));

        assertThrows(InvalidRFIDException.class, () -> ezshop.addProductToSaleRFID(1, null));
        assertThrows(InvalidRFIDException.class, () -> ezshop.addProductToSaleRFID(1, ""));
        assertThrows(InvalidRFIDException.class, () -> ezshop.addProductToSaleRFID(1, "1234567"));
        assertThrows(InvalidRFIDException.class, () -> ezshop.addProductToSaleRFID(1, "ABCDEFGHIJ"));
        assertThrows(InvalidRFIDException.class, () -> ezshop.addProductToSaleRFID(1, "1234567891234"));

        Integer transactionId = ezshop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 12.0;
        Integer productID = ezshop.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(ezshop.updatePosition(productID, "1-a-3"));
        ezshop.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(ezshop.payOrderFor("9788832360103", pQuantity, 5)), java.util.Optional.ofNullable(1));
        assertTrue(ezshop.recordOrderArrivalRFID(1, "000000010000"));

        DB.alterJDBCUrl();
        assertFalse(ezshop.addProductToSaleRFID(transactionId, "000000010000"));
        DB.restoreJDBCUrl();

        assertTrue(ezshop.addProductToSaleRFID(transactionId, "000000010000"));

        ezshop.reset();
    }

    @Test
    public void getAllOrdersTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidQuantityException, InvalidOrderIdException, InvalidLocationException, InvalidProductIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        String description1 = "description1";
        String description2 = "description2";
        String validBarCode1 = "9788832360103";
        String validBarCode2 = "9788808182159";
        double pricePerUnit1 = 0.5;
        double pricePerUnit2 = 0.6;
        String note1 = "note1";
        String note2 = "note2";

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        Integer shopManagerAdded = ezShop.createUser("shopmanager_user", "password", "ShopManager");
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotEquals(minus1, shopManagerAdded);
        assertNotNull(ezShop.login("shopmanager_user", "password"));
        assertEquals(0, ezShop.getAllOrders().size());
        Integer productAdded1 = ezShop.createProductType(description1, validBarCode1, pricePerUnit1, note1);
        assertNotEquals(minus1, productAdded1);
        Integer productAdded2 = ezShop.createProductType(description2, validBarCode2, pricePerUnit2, note2);
        assertNotEquals(minus1, productAdded2);
        Integer orderAdded1 = ezShop.issueOrder(validBarCode1, 50, 1);
        assertNotEquals(minus1, orderAdded1);
        assertTrue(ezShop.recordBalanceUpdate(250));
        assertTrue(ezShop.payOrder(orderAdded1));
        assertTrue(ezShop.updatePosition(productAdded1, "1-a-1"));
        assertTrue(ezShop.recordOrderArrival(orderAdded1));
        Integer orderAdded2 = ezShop.payOrderFor(validBarCode2, 50, 2);
        assertNotEquals(minus1, orderAdded2);
        Integer orderAdded3 = ezShop.issueOrder(validBarCode1, 50, 2);
        assertNotEquals(minus1, orderAdded3);
        List<Order> ordersRetrieved = ezShop.getAllOrders();
        assertNotNull(ordersRetrieved);
        assertEquals(3, ordersRetrieved.size());
        assertEquals(orderAdded1, ordersRetrieved.get(0).getOrderId());
        assertEquals(validBarCode1, ordersRetrieved.get(0).getProductCode());
        assertEquals(Double.valueOf(1), Double.valueOf(ordersRetrieved.get(0).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(0).getQuantity());
        assertEquals("COMPLETED", ordersRetrieved.get(0).getStatus());
        assertEquals(orderAdded2, ordersRetrieved.get(1).getOrderId());
        assertEquals(validBarCode2, ordersRetrieved.get(1).getProductCode());
        assertEquals(Double.valueOf(2), Double.valueOf(ordersRetrieved.get(1).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(1).getQuantity());
        assertEquals("PAYED", ordersRetrieved.get(1).getStatus());
        assertEquals(orderAdded3, ordersRetrieved.get(2).getOrderId());
        assertEquals(validBarCode1, ordersRetrieved.get(2).getProductCode());
        assertEquals(Double.valueOf(2), Double.valueOf(ordersRetrieved.get(2).getPricePerUnit()));
        assertEquals(50, ordersRetrieved.get(2).getQuantity());
        assertEquals("ISSUED", ordersRetrieved.get(2).getStatus());

       ezShop.reset();
    }

    @Test
    public void defineCustomerTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidCustomerNameException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.defineCustomer("customerName"));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerNameException.class, () -> ezShop.defineCustomer(null));
        assertThrows(InvalidCustomerNameException.class, () -> ezShop.defineCustomer(""));

       DB.alterJDBCUrl();
        assertEquals(minus1, ezShop.defineCustomer("customerName"));
        DB.restoreJDBCUrl();

        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);

       ezShop.reset();
    }

    @Test
    public void modifyCustomerTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidCustomerIdException, InvalidCustomerNameException, UnauthorizedException, InvalidCustomerCardException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "customerName", "1234567890"));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.modifyCustomer(null, "newCustomerName", "1234567890"));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.modifyCustomer(0, "newCustomerName", "1234567890"));
        assertThrows(InvalidCustomerNameException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, null, "1234567890"));
        assertThrows(InvalidCustomerNameException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "", "1234567890"));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", null));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", ""));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", "notAnAdmissibleFormat"));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", "12345"));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", "123456789013"));
        assertFalse(ezShop.modifyCustomer(Integer.MAX_VALUE, "newCustomerName", "1234567890"));
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);

       DB.alterJDBCUrl();
        assertFalse(ezShop.modifyCustomer(customerAdded, "newCustomerName", "1234567890"));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.modifyCustomer(customerAdded, "newCustomerName", "1234567890"));

       ezShop.reset();
    }

    @Test
    public void deleteCustomerTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidCustomerIdException, UnauthorizedException, InvalidCustomerNameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.deleteCustomer(Integer.MAX_VALUE));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.deleteCustomer(null));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.deleteCustomer(0));
        assertFalse(ezShop.deleteCustomer(Integer.MAX_VALUE));
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);

       DB.alterJDBCUrl();
        assertFalse(ezShop.deleteCustomer(customerAdded));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.deleteCustomer(customerAdded));

       ezShop.reset();
    }

    @Test
    public void getCustomerTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidCustomerIdException, UnauthorizedException, InvalidCustomerNameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.getCustomer(Integer.MAX_VALUE));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.getCustomer(null));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.getCustomer(0));
        assertNull(ezShop.getCustomer(Integer.MAX_VALUE));
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);

       DB.alterJDBCUrl();
        assertNull(ezShop.getCustomer(customerAdded));
        DB.restoreJDBCUrl();

        assertNotNull(ezShop.getCustomer(customerAdded));

       ezShop.reset();
    }

    @Test
    public void getAllCustomersTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidCustomerNameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, ezShop::getAllCustomers);
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertEquals(0, ezShop.getAllCustomers().size());
        Integer customerAdded1 = ezShop.defineCustomer("customerName1");
        assertNotEquals(minus1, customerAdded1);
        Integer customerAdded2 = ezShop.defineCustomer("customerName2");
        assertNotEquals(minus1, customerAdded2);
        Integer customerAdded3 = ezShop.defineCustomer("customerName3");
        assertNotEquals(minus1, customerAdded3);

       DB.alterJDBCUrl();
        assertNull(ezShop.getAllCustomers());
        DB.restoreJDBCUrl();

        assertEquals(3, ezShop.getAllCustomers().size());
        assertEquals(customerAdded1, ezShop.getAllCustomers().get(0).getId());
        assertEquals("customerName1", ezShop.getAllCustomers().get(0).getCustomerName());
        assertEquals(customerAdded2, ezShop.getAllCustomers().get(1).getId());
        assertEquals("customerName2", ezShop.getAllCustomers().get(1).getCustomerName());
        assertEquals(customerAdded3, ezShop.getAllCustomers().get(2).getId());
        assertEquals("customerName3", ezShop.getAllCustomers().get(2).getCustomerName());

       ezShop.reset();
    }

    @Test
    public void createCardTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, ezShop::createCard);
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));

       DB.alterJDBCUrl();
        assertEquals("", ezShop.createCard());
        DB.restoreJDBCUrl();

        String createdCard = ezShop.createCard();
        assertNotNull(createdCard);
        assertNotEquals("", createdCard);
        assertTrue(Pattern.compile("\\d{10}").matcher(createdCard).matches());

       ezShop.reset();
    }

    @Test
    public void attachCardToCustomerTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidCustomerIdException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.attachCardToCustomer("1234567890", Integer.MAX_VALUE));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.attachCardToCustomer("1234567890", null));
        assertThrows(InvalidCustomerIdException.class, () -> ezShop.attachCardToCustomer("1234567890", 0));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.attachCardToCustomer(null, Integer.MAX_VALUE));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.attachCardToCustomer("", Integer.MAX_VALUE));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.attachCardToCustomer("notAnAdmissibleFormat", Integer.MAX_VALUE));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.attachCardToCustomer("12345", Integer.MAX_VALUE));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.attachCardToCustomer("12345678901235", Integer.MAX_VALUE));
        assertFalse(ezShop.attachCardToCustomer("1234567890", Integer.MAX_VALUE));
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);
        assertFalse(ezShop.attachCardToCustomer("1234567890", customerAdded));
        String cardAdded = ezShop.createCard();
        assertNotNull(cardAdded);

       DB.alterJDBCUrl();
        assertFalse(ezShop.attachCardToCustomer(cardAdded, customerAdded));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.attachCardToCustomer(cardAdded, customerAdded));

       ezShop.reset();
    }

    @Test
    public void modifyPointsOnCardTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer cashierAdded = ezShop.createUser("cashier_user", "password", "Cashier");
        assertThrows(UnauthorizedException.class, () -> ezShop.modifyPointsOnCard("1234567890", 20));
        Integer minus1 = -1;
        assertNotEquals(minus1, cashierAdded);
        assertNotNull(ezShop.login("cashier_user", "password"));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyPointsOnCard(null, 20));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyPointsOnCard("", 20));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyPointsOnCard("notAnAdmissibleFormat", 20));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyPointsOnCard("12345", 20));
        assertThrows(InvalidCustomerCardException.class, () -> ezShop.modifyPointsOnCard("123456789012345", 20));
        assertFalse(ezShop.modifyPointsOnCard("1234567890", 2));
        String cardAdded = ezShop.createCard();
        assertNotNull(cardAdded);
        assertFalse(ezShop.modifyPointsOnCard(cardAdded, 2));
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);
        assertTrue(ezShop.attachCardToCustomer(cardAdded, customerAdded));

       DB.alterJDBCUrl();
        assertFalse(ezShop.modifyPointsOnCard(cardAdded, 2));
        DB.restoreJDBCUrl();

        assertTrue(ezShop.modifyPointsOnCard(cardAdded, 5));
        assertFalse(ezShop.modifyPointsOnCard(cardAdded, -7));
        assertTrue(ezShop.modifyPointsOnCard(cardAdded, -2));

        Customer customer = ezShop.getCustomer(customerAdded);
        assertNotNull(customer);
        assertEquals(Integer.valueOf(3), customer.getPoints());

       ezShop.reset();
    }

    @Test
    public void resetTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidCustomerNameException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        Integer minus1 = -1;
        Integer adminAdded = ezShop.createUser("admin_user", "password", "Administrator");
        assertNotEquals(minus1, adminAdded);
        assertNotNull(ezShop.login("admin_user", "password"));
        Integer productAdded = ezShop.createProductType("description", "9788832360103", 1, "");
        assertNotEquals(minus1, productAdded);
        Integer customerAdded = ezShop.defineCustomer("customerName");
        assertNotEquals(minus1, customerAdded);
        String cardAdded = ezShop.createCard();
        assertNotEquals("", cardAdded);
        assertTrue(ezShop.updatePosition(productAdded, "1-a-1"));
        Integer orderAdded = ezShop.issueOrder("9788832360103", 20, 1);
        assertNotEquals(minus1, orderAdded);

        assertNotEquals(0, ezShop.getAllCustomers().size());
        assertNotEquals(0, ezShop.getAllOrders().size());
        assertNotEquals(0, ezShop.getAllProductTypes().size());
        assertNotEquals(0, ezShop.getAllUsers().size());
        assertNotEquals(0, InventoryManagement.productMap.size());
        assertNotEquals(0, InventoryManagement.orderMap.size());

       DB.alterJDBCUrl();
        ezShop.reset();
        DB.restoreJDBCUrl();

        assertNotEquals(0, ezShop.getAllCustomers().size());
        assertNotEquals(0, ezShop.getAllOrders().size());
        assertNotEquals(0, ezShop.getAllProductTypes().size());
        assertNotEquals(0, ezShop.getAllUsers().size());
        assertNotEquals(0, InventoryManagement.productMap.size());
        assertNotEquals(0, InventoryManagement.orderMap.size());

        ezShop.reset();

        assertEquals(0, ezShop.getAllCustomers().size());
        assertEquals(0, ezShop.getAllOrders().size());
        assertEquals(0, ezShop.getAllProductTypes().size());
        assertEquals(0, ezShop.getAllUsers().size());
        assertEquals(0, InventoryManagement.productMap.size());
        assertEquals(0, InventoryManagement.orderMap.size());

       ezShop.reset();
    }

    @Test
    public void startSaleTransactionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users) and check that operation is successful
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));
        Integer id = ezShop.startSaleTransaction();
        assertNotEquals(id, Integer.valueOf(-1));
        // check that transaction is properly stored
        assertTrue(PaymentController.getSaleTransaction(id).isOpen());

       ezShop.reset();

    }

    @Test
    public void getSaleTransactionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        // check exception for transaction with invalid id
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.getSaleTransaction(-1));

        // check null return value for a transaction that does not exist
        assertNull(ezShop.getSaleTransaction(1));

        // start a sale transaction
        Integer id = ezShop.startSaleTransaction();
        assertNotEquals(id, Integer.valueOf(-1));
        PaymentController.getSaleTransaction(id).setPrice(50.0);

        PaymentController.getSaleTransaction(id).close();
        // check that the method returns the required sale transaction
        SaleTransaction st = ezShop.getSaleTransaction(id);
        assertNotNull(st);
        assertEquals(st.getPrice(), 50.0,0.0001);

       ezShop.reset();
    }

    @Test
    public void addProductToSaleTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductIdException, InvalidQuantityException, InvalidLocationException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        // check exception if transaction id is invalid
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.addProductToSale(-1, "9788808182159", 10));

        // check if sale transaction does not exist
        assertFalse(ezShop.addProductToSale(1, "9788808182159", 10));

        // start a sale transaction
        Integer id = ezShop.startSaleTransaction();
        assertNotEquals(id, Integer.valueOf(-1));

        // check exception if product id is invalid
        assertThrows(InvalidProductCodeException.class, () -> ezShop.addProductToSale(id, "15", 10));

        // check if product does not exist
        assertFalse(ezShop.addProductToSale(id, "9788808182159", 10));

        // create product and update quantity
        Integer prod_id = ezShop.createProductType("product_test", "9788808182159", 12.0, "note_test");
        assertNotEquals(prod_id, Integer.valueOf(-1));
        assertTrue(ezShop.updatePosition(prod_id, "1-a-1"));
        assertTrue(ezShop.updateQuantity(prod_id, 10));

        // check exception if quantity is invalid
        assertThrows(InvalidQuantityException.class, () -> ezShop.addProductToSale(id, "9788808182159", -1));
        assertThrows(InvalidQuantityException.class, () -> ezShop.addProductToSale(id, "9788808182159", -11));

        // check successful case
        assertTrue(ezShop.addProductToSale(id, "9788808182159", 5));
        assertNotNull(PaymentController.getSaleTransaction(id).getEntries().get(0));
        assertEquals(PaymentController.getSaleTransaction(id).getEntries().get(0).getAmount(), 5);
        assertEquals(PaymentController.getSaleTransaction(id).getEntries().get(0).getBarCode(), "9788808182159");

       ezShop.reset();

    }

    @Test
    public void deleteProductFromSaleRFID() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException, InvalidRFIDException, InvalidOrderIdException, UnauthorizedException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
       EZShop ezshop = new EZShop();

        ezshop.reset();
        // login as a user with permission (all users)
        assertNotEquals(ezshop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezshop.login("user1", "pwd1"));

        Integer transactionId = ezshop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 20;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezshop.createProductType("testProduct#1", pBarCode, pPricePerUnit, "");
        assertTrue(ezshop.updatePosition(productID, "1-a-3"));
        ezshop.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(ezshop.payOrderFor(pBarCode, pQuantity, pPricePerUnit)), java.util.Optional.ofNullable(1));
        String RFID1 = "000000010000";
        String RFID1a = "000000010001";

        assertTrue(ezshop.recordOrderArrivalRFID(1, RFID1));

        Integer productID2 = ezshop.createProductType("testProduct#2", pBarCode2, pPricePerUnit2, "");
        assertTrue(ezshop.updatePosition(productID2, "1-b-3"));
        assertEquals(java.util.Optional.ofNullable(ezshop.payOrderFor(pBarCode2, pQuantity2, pPricePerUnit2)), java.util.Optional.ofNullable(2));
        String RFID2 = "000000020000";
        assertTrue(ezshop.recordOrderArrivalRFID(2, RFID2));

        //add item1 to the sale
        assertTrue(ezshop.addProductToSaleRFID(transactionId, RFID1));
        assertTrue(ezshop.addProductToSaleRFID(transactionId, RFID1a));

        //add item2 to the sale
        assertTrue(ezshop.addProductToSaleRFID(transactionId, RFID2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2 = Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);


        //now delete the product 1
        assertThrows(InvalidRFIDException.class, () -> ezshop.deleteProductFromSaleRFID(transactionId, ""));
        assertThrows(InvalidRFIDException.class, () -> ezshop.deleteProductFromSaleRFID(transactionId, null));
        assertThrows(InvalidRFIDException.class, () -> ezshop.deleteProductFromSaleRFID(transactionId, "011as11111"));

        assertThrows(InvalidTransactionIdException.class, () -> ezshop.deleteProductFromSaleRFID(null, RFID1));
        assertThrows(InvalidTransactionIdException.class, () -> ezshop.deleteProductFromSaleRFID(-1, RFID1));


        assertTrue(ezshop.deleteProductFromSaleRFID(transactionId, RFID1));

        //check whether the ticket entry has been updated or not
        TicketEntryObject ticketEntryAfterDelete = (TicketEntryObject) Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode);
        assertEquals(ticketEntryAfterDelete.getBarCode(), Objects.requireNonNull(ezshop.getProductTypeByBarCode(pBarCode)).getBarCode());
        assertEquals(ticketEntryAfterDelete.getAmount(), 1);
        assertFalse(ticketEntryAfterDelete.getRFIDs().stream().anyMatch(e -> e.equals(RFID1)));
        assertTrue(ticketEntryAfterDelete.getRFIDs().stream().anyMatch(e -> e.equals(RFID1a)));

        double correctPrice = pPricePerUnit + pPricePerUnit2;
        assertEquals((PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        //assert that inventory quantities are update after the delete
        assertEquals(Objects.requireNonNull(ezshop.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - 1));
        //add a little bit of product and delete all the remaining product
        assertTrue(ezshop.deleteProductFromSaleRFID(transactionId, RFID1a));
        assertEquals(Objects.requireNonNull(ezshop.getProductTypeByBarCode(pBarCode)).getQuantity(), pQuantity );
        correctPrice = pPricePerUnit2;
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getPrice(), correctPrice, 0.01);

        assertTrue(ezshop.addProductToSaleRFID(transactionId, RFID1a));

        //delete a product that doesn't belong to the transaction
        assertFalse(ezshop.deleteProductFromSaleRFID(transactionId, "000000400000"));

        //add a little bit of product, then disconnect db and try to remove it
        DB.alterJDBCUrl();
        assertFalse(ezshop.deleteProductFromSaleRFID(transactionId, RFID1a));
        DB.restoreJDBCUrl();

        assertEquals(Objects.requireNonNull(ezshop.getProductTypeByBarCode(pBarCode)).getQuantity(), Integer.valueOf(pQuantity - 1));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), 1);

        //now deletes product from inventory end tries to delete it
        assertTrue(ezshop.deleteProductType(Objects.requireNonNull(InventoryManagement.getProductTypeByBarCode(pBarCode)).getId()));
        assertFalse(ezshop.deleteProductFromSaleRFID(transactionId, RFID1a));
        assertEquals(Objects.requireNonNull(PaymentController.getSaleTransaction(transactionId)).getEntry(pBarCode).getAmount(), 1);

        ezshop.reset();
    }

    @Test
    public void deleteProductFromSaleTest() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException, UnauthorizedException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);

        //now delete the product 1
        int deletedQuantity = 20;
        assertTrue(ezShop.deleteProductFromSale(transactionId, pBarCode, deletedQuantity));

        //check whether the ticket entry has been updated or not
        TicketEntry ticketEntryAfterDelete= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertEquals(ticketEntryAfterDelete.getBarCode(),InventoryManagement.getProductTypeByBarCode(pBarCode).getBarCode());
        assertEquals(ticketEntryAfterDelete.getAmount(), addedQuantity-deletedQuantity);

        double correctPrice = pPricePerUnit*(addedQuantity-deletedQuantity) +pPricePerUnit2*addedQuantity2;
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), correctPrice, 0.01);

        //assert that inventory quantities are updated after the delete
        assertEquals(InventoryManagement.getProductTypeByBarCode(pBarCode).getQuantity(), Integer.valueOf(pQuantity - addedQuantity + deletedQuantity));
        //add a little bit of product and delete all the remaining product
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));
        assertTrue(ezShop.deleteProductFromSale(transactionId, pBarCode, 2* addedQuantity - deletedQuantity));
        assertNull(PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode));
        assertEquals(ezShop.getProductTypeByBarCode(pBarCode).getQuantity(), Integer.valueOf(pQuantity));

        correctPrice = pPricePerUnit2*addedQuantity2;
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), correctPrice, 0.01);

        assertTrue(ezShop.deleteSaleTransaction(transactionId));
        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }


    @Test
    public void applyDiscountRateToProductTest() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, InvalidDiscountRateException, SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);

        //check if current transaction price is correct
        double currentPrice = addedQuantity*pPricePerUnit + addedQuantity2*pPricePerUnit2;
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);


        //apply the discountRate to the product
        double discountRate1 = 0.2;
        double discountRate2 = 2.0;
        double discountRate3 = 0.0;
        double discountRate4 = 0.5;
        assertTrue(ezShop.applyDiscountRateToProduct(transactionId, pBarCode, discountRate1));
        currentPrice = addedQuantity*pPricePerUnit* (1-discountRate1) +addedQuantity2*pPricePerUnit2;
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, ()->ezShop.applyDiscountRateToProduct(transactionId,pBarCode2,discountRate2));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertTrue(ezShop.applyDiscountRateToProduct(transactionId, pBarCode2, discountRate1));
        currentPrice = addedQuantity*pPricePerUnit* (1-discountRate1) +addedQuantity2*pPricePerUnit2*(1-discountRate1);
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertTrue(ezShop.applyDiscountRateToProduct(transactionId, pBarCode2, discountRate3));
        currentPrice = addedQuantity*pPricePerUnit* (1-discountRate1) +addedQuantity2*pPricePerUnit2*(1-discountRate3);
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertTrue(ezShop.applyDiscountRateToProduct(transactionId, pBarCode2, discountRate4));
        currentPrice = addedQuantity*pPricePerUnit* (1-discountRate1) +addedQuantity2*pPricePerUnit2*(1-discountRate4);
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);

        assertTrue(ezShop.deleteSaleTransaction(transactionId));
        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }


    @Test
    public void applyDiscountRateToSale() throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidTransactionIdException, InvalidQuantityException, InvalidDiscountRateException, SQLException, InvalidPasswordException, InvalidUsernameException, InvalidRoleException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //get item1 related ticketEntry
        TicketEntry ticketEntryP1= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP1);

        //get item2 related ticketEntry
        TicketEntry ticketEntryP2= PaymentController.getSaleTransaction(transactionId).getEntry(pBarCode);
        assertNotNull(ticketEntryP2);

        //check if current transaction price is correct
        double currentPrice = addedQuantity*pPricePerUnit + addedQuantity2*pPricePerUnit2;
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);

        double saleDiscount1 = 1.0;
        double saleDiscount2 = 2.0;
        double saleDiscount3 = 0.2;
        double saleDiscount4 = 0.5;
        assertFalse(ezShop.applyDiscountRateToSale(transactionId+1, saleDiscount4));
        assertThrows(InvalidTransactionIdException.class, ()->ezShop.applyDiscountRateToSale(-1, saleDiscount1));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, ()->ezShop.applyDiscountRateToSale(transactionId, saleDiscount1));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        assertThrows(InvalidDiscountRateException.class, ()->ezShop.applyDiscountRateToSale(transactionId, saleDiscount2));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        currentPrice = (1-saleDiscount3)*(addedQuantity*pPricePerUnit + addedQuantity2*pPricePerUnit2);
        assertTrue(ezShop.applyDiscountRateToSale(transactionId, saleDiscount3));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);
        currentPrice = (1-saleDiscount4)*(addedQuantity*pPricePerUnit + addedQuantity2*pPricePerUnit2);
        assertTrue(ezShop.applyDiscountRateToSale(transactionId, saleDiscount4));
        assertEquals(PaymentController.getSaleTransaction(transactionId).getPrice(), currentPrice, 0.01);

        assertTrue(ezShop.deleteSaleTransaction(transactionId));
        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }

    @Test
    public void computePointsForSaleTest() throws InvalidPasswordException, InvalidRoleException, InvalidUsernameException, SQLException, UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidProductIdException, InvalidLocationException, InvalidQuantityException, InvalidTransactionIdException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // verify exception if transaction id is invalid
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.computePointsForSale(-1));

        // check value 0 if sale transaction does not exist
        assertEquals(ezShop.computePointsForSale(1), 0);

        double price1 = 55.5, price2 = 0.0, price3 = 110.0;
        // check if all three prices correspond to the right amount of points
        PaymentController.getSaleTransaction(transactionId).setPrice(price1);
        assertEquals(ezShop.computePointsForSale(transactionId), (int)price1/10);
        PaymentController.getSaleTransaction(transactionId).setPrice(price2);
        assertEquals(ezShop.computePointsForSale(transactionId), (int)price2/10);
        PaymentController.getSaleTransaction(transactionId).setPrice(price3);
        assertEquals(ezShop.computePointsForSale(transactionId), (int)price3/10);

       ezShop.reset();
    }


    @Test
    public void endSaleTransaction() throws InvalidQuantityException, InvalidProductCodeException, InvalidTransactionIdException, InvalidProductIdException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidLocationException, SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertThrows(InvalidTransactionIdException.class, ()->ezShop.endSaleTransaction(-1));
        assertFalse(ezShop.endSaleTransaction(transactionId+1));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertFalse(ezShop.endSaleTransaction(transactionId));

        assertTrue(PaymentController.getSaleTransaction(transactionId).isClosed());

        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }


    @Test
    public void deleteSaleTransaction() throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException, InvalidProductIdException, InvalidTransactionIdException, InvalidQuantityException, SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        //delete the transaction
        assertThrows(InvalidTransactionIdException.class, ()->ezShop.deleteSaleTransaction(-1));
        assertTrue(ezShop.deleteSaleTransaction(transactionId));
        assertFalse(ezShop.deleteSaleTransaction(transactionId));

        //check if products' quantities are correctly updated into db
        assertEquals(ezShop.getProductTypeByBarCode(pBarCode).getQuantity(), pQuantity);
        assertEquals(ezShop.getProductTypeByBarCode(pBarCode2).getQuantity(), pQuantity2);

        assertNull(ezShop.getSaleTransaction(transactionId));

        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }


    @Test
    public void receiveCashPaymentTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidTransactionIdException, InvalidPaymentException, SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 12.0;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 6.0;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertTrue(ezShop.endSaleTransaction(transactionId));

        assertEquals(ezShop.receiveCashPayment(transactionId, PaymentController.getSaleTransaction(transactionId).getPrice()-1),-1, 0.01);

        assertThrows(InvalidTransactionIdException.class,()-> ezShop.receiveCashPayment(-1, PaymentController.getSaleTransaction(transactionId).getPrice()));

        assertThrows(InvalidPaymentException.class,()-> ezShop.receiveCashPayment(transactionId, -1));

        //create last one transaction that won't fail
        assertEquals(ezShop.receiveCashPayment(transactionId, PaymentController.getSaleTransaction(transactionId).getPrice()+100),100, 0.01);

        assertEquals(ezShop.receiveCashPayment(transactionId, PaymentController.getSaleTransaction(transactionId).getPrice()),-1, 0.01);

        assertEquals(ezShop.getProductTypeByBarCode(pBarCode).getQuantity(), Integer.valueOf(pQuantity-addedQuantity));
        assertEquals(ezShop.getProductTypeByBarCode(pBarCode2).getQuantity(), Integer.valueOf(pQuantity2-addedQuantity2));
        assertTrue(PaymentController.getSaleTransaction(transactionId).isPayed());

        assertTrue(ezShop.deleteProductType(productID));
        assertTrue(ezShop.deleteProductType(productID2));

       ezShop.reset();
    }

    @Test
    public void receiveCreditCardPaymentTest() throws InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidTransactionIdException, InvalidPaymentException, InvalidCreditCardException, SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;

        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertTrue(ezShop.endSaleTransaction(transactionId));

        String validCreditCard = "4485370086510891";
        String invalidCreditCard = "4126178638225568";
        String insufficientCreditCard = "4716258050958645";

        assertThrows(InvalidTransactionIdException.class,()-> ezShop.receiveCreditCardPayment(-1,validCreditCard));

        assertThrows(InvalidCreditCardException.class,()-> ezShop.receiveCreditCardPayment(transactionId, invalidCreditCard));

        assertFalse(ezShop.receiveCreditCardPayment(transactionId, insufficientCreditCard));

        assertTrue(ezShop.receiveCreditCardPayment(transactionId, validCreditCard));

        assertEquals(ezShop.getProductTypeByBarCode(pBarCode).getQuantity(), Integer.valueOf(pQuantity-addedQuantity));
        assertEquals(ezShop.getProductTypeByBarCode(pBarCode2).getQuantity(), Integer.valueOf(pQuantity2-addedQuantity2));
        assertTrue(PaymentController.getSaleTransaction(transactionId).isPayed());
        
       ezShop.reset();
    }


    @Test
    public void startReturnTransactionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.startReturnTransaction(-1));
        // check error if sale transaction does not exist
        assertEquals(ezShop.startReturnTransaction(transactionId + 1), Integer.valueOf(-1));
        // check error if sale transaction is not payed
        assertEquals(ezShop.startReturnTransaction(transactionId), Integer.valueOf(-1));

        // add products and pay the transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));

        assertTrue(ezShop.endSaleTransaction(transactionId));

        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // check that return transaction is started correctly
        Integer ReturnId = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId, Integer.valueOf(-1));

       ezShop.reset();
    }

    @Test
    public void returnProductTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {

        EZShop ezShop = new EZShop();

        ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnProduct(-1, "9788832360103", 10));
        // check invalid product code exception
        assertThrows(InvalidProductCodeException.class, () -> ezShop.returnProduct(1, null, 10));
        assertThrows(InvalidProductCodeException.class, () -> ezShop.returnProduct(1, "", 10));
        // check error if return transaction does not exist
        assertFalse(ezShop.returnProduct(1, "9788832360103", 10));

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // check that return transaction is started correctly
        Integer ReturnId = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId, Integer.valueOf(-1));

        // check exception for invalid quantity of the product
        assertFalse(ezShop.returnProduct(ReturnId, pBarCode, addedQuantity + 1));

        // check that product is correctly added to return transaction
        assertTrue(ezShop.returnProduct(ReturnId, pBarCode, addedQuantity));

        ezShop.reset();
    }

    @Test
    public void returnProductTestRFID() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidRFIDException, InvalidOrderIdException {

        EZShop ezShop = new EZShop();

        ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnProductRFID(-1, "000000100000"));
        // check invalid product code exception
        assertThrows(InvalidRFIDException.class, () -> ezShop.returnProductRFID(1, null));
        assertThrows(InvalidRFIDException.class, () -> ezShop.returnProductRFID(1, ""));
        assertThrows(InvalidRFIDException.class, () -> ezShop.returnProductRFID(1, "122qqa2q"));

        // check error if return transaction does not exist
        assertFalse(ezShop.returnProductRFID(1, "000000100000"));

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 10;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 20;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        ezShop.recordBalanceUpdate(500);
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode, pQuantity, pPricePerUnit)), java.util.Optional.ofNullable(1));

        String RFID1 = "000000010000";
        String RFID1a = "000000010001";
        assertTrue(ezShop.recordOrderArrivalRFID(1, RFID1));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertEquals(java.util.Optional.ofNullable(InventoryManagement.payOrderFor(pBarCode2, pQuantity2, pPricePerUnit2)), java.util.Optional.ofNullable(2));
        String RFID2 = "000000020000";
        String RFID2a = "000000020001";
        assertTrue(ezShop.recordOrderArrivalRFID(2, RFID2));

        //add item1 to the sale
        int addedQuantity = 5;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        assertTrue(ezShop.addProductToSaleRFID(transactionId, RFID2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // check that return transaction is started correctly
        Integer ReturnId = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId, Integer.valueOf(-1));

        // check exception for invalid quantity of the product
        assertFalse(ezShop.returnProductRFID(ReturnId, RFID2a));
        assertTrue(ezShop.returnProductRFID(ReturnId, RFID2));

        assertFalse(ezShop.returnProductRFID(ReturnId, "000003000000"));

        // check that product is correctly added to return transaction
        assertFalse(ezShop.returnProductRFID(ReturnId, RFID1a));

        ezShop.reset();
    }

    @Test
    public void deleteReturnTransactionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnProduct(-1, "9788832360103", 10));
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnProduct(null, "9788832360103", 10));

        // check error if return transaction does not exist
        assertFalse(ezShop.deleteReturnTransaction(1));

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // check that return transaction is started correctly
        Integer ReturnId = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId, Integer.valueOf(-1));

        // check that transaction is properly deleted
        assertTrue(ezShop.deleteReturnTransaction(ReturnId));

       ezShop.reset();
    }

    @Test
    public void endReturnTransactionTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnProduct(-1, "9788832360103", 10));

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // check that return transaction is started correctly
        Integer ReturnId = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId, Integer.valueOf(-1));

        // check exception for invalid quantity of the product
        assertFalse(ezShop.returnProduct(ReturnId, pBarCode, addedQuantity + 1));

        // check that product is correctly added to return transaction
        assertTrue(ezShop.returnProduct(ReturnId, pBarCode, addedQuantity-5));

        // check error if return transaction does not exist
        assertFalse(ezShop.endReturnTransaction(ReturnId + 1, true));


        assertTrue(ezShop.endReturnTransaction(ReturnId, true));
        // check that return transaction was deleted
        assertFalse(ezShop.endReturnTransaction(ReturnId, true));

        // restart another return transaction
        Integer ReturnId2 = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId2, Integer.valueOf(-1));
        assertTrue(ezShop.returnProduct(ReturnId2, pBarCode2, addedQuantity2));

        // check that function works properly when input is okay
        assertTrue(ezShop.endReturnTransaction(ReturnId2, true));

        // restart another return transaction
        Integer ReturnId3 = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId3, Integer.valueOf(-1));
        assertTrue(ezShop.returnProduct(ReturnId3, pBarCode, 5));
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + 5 * pPricePerUnit + 1.0 );

        // check that transaction is deleted when commit = false
        assertTrue(ezShop.endReturnTransaction(ReturnId3, false));
        assertFalse(ezShop.endReturnTransaction(ReturnId3, true));


       ezShop.reset();
    }

    @Test
    public void returnCashPaymentTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnCashPayment(-1));
        // check error if return transaction does not exist
        assertEquals(ezShop.returnCashPayment(1), -1.0, 0.0001);

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // start, fill and end return transaction
        Integer ReturnId2 = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId2, Integer.valueOf(-1));
        assertTrue(ezShop.returnProduct(ReturnId2, pBarCode2, addedQuantity2));
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 + 1.0 );
        assertTrue(ezShop.endReturnTransaction(ReturnId2, true));

        // check error if balance is not enough
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 - 1.0 );
        assertEquals(ezShop.returnCashPayment(1), -1.0, 0.0001);

        // check that function works when balance is enough
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 + 1.0 );
        assertEquals(ezShop.returnCashPayment(1), -1.0, 0.0001);

       ezShop.reset();
    }

    @Test
    public void returnCreditCardPayment() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException, InvalidTransactionIdException, InvalidPaymentException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductCodeException, InvalidLocationException, InvalidProductIdException, InvalidQuantityException, InvalidCreditCardException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        Integer transactionId = ezShop.startSaleTransaction();
        //verifies that transaction has been created successfully
        assertNotEquals(transactionId, Integer.valueOf(-1));

        // check invalid return transaction id exception
        assertThrows(InvalidTransactionIdException.class, () -> ezShop.returnCreditCardPayment(-1, "4485370086510891"));
        // check error if return transaction does not exist
        assertEquals(ezShop.returnCreditCardPayment(1, "4485370086510891"), -1.0, 0.0001);

        // add products and pay the sale transaction
        String pBarCode = "9788832360103";
        Integer pQuantity = 100;
        double pPricePerUnit = 0.05;
        String pBarCode2 = "9788808182159";
        Integer pQuantity2 = 200;
        double pPricePerUnit2 = 0.10;
        Integer productID = ezShop.createProductType("testProduct#1", pBarCode , pPricePerUnit, "");
        assertTrue(ezShop.updatePosition(productID, "1-a-3"));
        assertTrue(ezShop.updateQuantity(productID, pQuantity));

        Integer productID2 = ezShop.createProductType("testProduct#2", pBarCode2 , pPricePerUnit2, "");
        assertTrue(ezShop.updatePosition(productID2, "1-b-3"));
        assertTrue(ezShop.updateQuantity(productID2, pQuantity2));

        //add item1 to the sale
        int addedQuantity = 50;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode, addedQuantity));

        //add item2 to the sale
        int addedQuantity2 = 70;
        assertTrue(ezShop.addProductToSale(transactionId, pBarCode2, addedQuantity2));
        assertTrue(ezShop.endSaleTransaction(transactionId));
        assertNotEquals(ezShop.receiveCashPayment(transactionId, 50.0), -1.0, 0.0001);

        // start, fill and end return transaction
        Integer ReturnId2 = ezShop.startReturnTransaction(transactionId);
        assertNotEquals(ReturnId2, Integer.valueOf(-1));
        assertTrue(ezShop.returnProduct(ReturnId2, pBarCode2, addedQuantity2));
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 + 1.0 );

        // check exception if card is not valid
        assertThrows(InvalidCreditCardException.class, () -> ezShop.returnCreditCardPayment(ReturnId2, "1111111111111111"));

        // check error if card is not present in database
        assertEquals(ezShop.returnCreditCardPayment(ReturnId2, "6271703435814254"), -1.0, 0.0001);

        // check error if balance is not enough
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 - 1.0 );
        assertEquals(ezShop.returnCreditCardPayment(ReturnId2, "4485370086510891"), -1.0, 0.0001);

        // check that function works when balance is enough
        ezShop.recordBalanceUpdate(- ezShop.computeBalance() + addedQuantity2 * pPricePerUnit2 + 1.0 );
        double price = addedQuantity2*pPricePerUnit2;
        assertEquals(ezShop.returnCreditCardPayment(ReturnId2, "4485370086510891"), price, 0.0001);

       // assertTrue(ezShop.endReturnTransaction(ReturnId2, false));

       ezShop.reset();
    }

    @Test
    public void recordBalanceUpdateTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {
        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user without permission 
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Cashier"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        // check that unauthorized exception is thrown
        assertThrows(UnauthorizedException.class, () -> ezShop.recordBalanceUpdate(50.0));

        // login as a user with permission 
        assertNotEquals(ezShop.createUser("user2", "pwd2", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user2", "pwd2"));

        double toBeAdded = 50.0;
        // check that balance cannot be negative
        assertFalse(ezShop.recordBalanceUpdate(-toBeAdded));
        // check that correct balance update is stored properly
        assertTrue(ezShop.recordBalanceUpdate(toBeAdded));
        BalanceOperationObject op = (BalanceOperationObject) AccountBook.getCreditsAndDebits(null, null).get(0);
        assertEquals(op.getDate(), LocalDate.now());
        assertEquals(op.getMoney(), toBeAdded, 0.0001);
        assertEquals(op.getType(), "Credit");
        assertEquals(AccountBook.computeBalance(), toBeAdded, 0.0001);

       ezShop.reset();
    }


    @Test
    public void getCreditsAndDebitsTest() throws SQLException, InvalidTransactionIdException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user without permission
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Cashier"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        // check that unauthorized exception is thrown
        assertThrows(UnauthorizedException.class, () -> ezShop.recordBalanceUpdate(50.0));

        // login as a user with permission
        assertNotEquals(ezShop.createUser("user2", "pwd2", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user2", "pwd2"));

        // Insert some transactions in the database
        SaleTransactionObject st1 = new SaleTransactionObject(1);
        SaleTransactionObject st2 = new SaleTransactionObject(2);
        SaleTransactionObject st3 = new SaleTransactionObject(3);
        double price1 = 50.0, price2 = 100.0, price3 = 150.0;
        LocalDate date1 = LocalDate.of(2021, 05, 18);
        LocalDate date2 = LocalDate.of(2021, 05, 20);
        LocalDate date3 = LocalDate.of(2021, 05, 22);
        st1.setPrice(price1); st1.setDate(date1);
        st2.setPrice(price2); st2.setDate(date2);
        st3.setPrice(price3); st3.setDate(date3);
        AccountBook.recordSaleTransaction(st1);
        AccountBook.recordSaleTransaction(st2);
        AccountBook.recordSaleTransaction(st3);
        // Store transactions also in a local collection
        List<SaleTransactionObject> insertedOps = new ArrayList<SaleTransactionObject>();
        insertedOps.add(st1); insertedOps.add(st2); insertedOps.add(st3);

        // Try to get all transactions by inserting null dates
        List<BalanceOperation> ops =  ezShop.getCreditsAndDebits(null, null);
        // Check that transactions are correct
        int index = 0;
        assertEquals(ops.size(), 3);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }

        // Check that the function works even if limit dates are inverted
        ops =  ezShop.getCreditsAndDebits(LocalDate.of(2021, 05, 24), LocalDate.of(2021, 05, 18));
        // Check that transactions are correct
        index = 0;
        assertEquals(ops.size(), 3);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }

        // Check that filter on dates work properly
        ops =  ezShop.getCreditsAndDebits(LocalDate.of(2021, 05, 18), LocalDate.of(2021, 05, 20));
        index = 0;
        assertEquals(ops.size(), 2);
        for (BalanceOperation op : ops){
            assertEquals(op.getDate(), ops.get(index).getDate());
            assertEquals(op.getMoney(), ops.get(index).getMoney(), 0.0001);
            assertEquals(op.getType(), ops.get(index).getType());
            index++;
        }
       ezShop.reset();
    }


    @Test
    public void computeBalanceTest() throws SQLException, InvalidTransactionIdException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, UnauthorizedException {

        EZShop ezShop = new EZShop();

       ezShop.reset();

        // login as a user with permission (all users)
        assertNotEquals(ezShop.createUser("user1", "pwd1", "Administrator"), Integer.valueOf(-1));
        assertNotNull(ezShop.login("user1", "pwd1"));

        // check that balance is 0 if no transaction is stored in database
        assertEquals(ezShop.computeBalance(), 0.0, 0.0001);
        // insert some transactions in the database
        SaleTransactionObject st1 = new SaleTransactionObject(1);
        SaleTransactionObject st2 = new SaleTransactionObject(2);
        double price1 = 50.0, price2 = 100.0;

        st1.setPrice(price1);
        st2.setPrice(price2);
        AccountBook.recordSaleTransaction(st1);
        AccountBook.recordSaleTransaction(st2);
        assertEquals(ezShop.computeBalance(), price1 + price2, 0.00001);

       ezShop.reset();
    }


}
