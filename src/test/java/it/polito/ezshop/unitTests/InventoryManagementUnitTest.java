package it.polito.ezshop.unitTests;

import it.polito.ezshop.controllers.InventoryManagement;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryManagementUnitTest {
    @Test
    public void isBarcodeValidTest() throws InvalidProductCodeException {
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid(null));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid(""));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("123456"));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("12345678901234567"));
        assertThrows(InvalidProductCodeException.class, () -> InventoryManagement.isBarcodeValid("stringstring"));
        assertTrue(InventoryManagement.isBarcodeValid("9788808182159"));
    }
}
