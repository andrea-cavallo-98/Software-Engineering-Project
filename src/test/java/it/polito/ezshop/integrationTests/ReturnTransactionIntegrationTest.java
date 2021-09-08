package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.model.ReturnTransactionObject;
import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class ReturnTransactionIntegrationTest {


    @Test
    public void testGetEntry(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);;

        TicketEntryObject ticketEntry = new TicketEntryObject("Apple Juice", "9788832360103", 1, 0.0, 2.0);
        TicketEntryObject ticketEntry_comparable = new TicketEntryObject("Apple Juice", "9788832360103", 1, 0.0, 2.0);

        returnTransactionObject.addItem(ticketEntry);

        //verify that the correct product is returned
        assertEquals(returnTransactionObject.getItem("9788832360103"), ticketEntry_comparable);

        //verify null when a ticket entry doesn't exist
        assertNull(returnTransactionObject.getItem("9788808182159"));
    }

    @Test
    public void testGetEntries(){
        ReturnTransactionObject returnTransactionObject = new ReturnTransactionObject(0,0);;
        Map<String, TicketEntryObject> entries = new HashMap<>();

        //generate some ticket entries
        TicketEntryObject t1 = new TicketEntryObject("Apple Juice", "123456789123", 1, 0.0, 2.0);
        TicketEntryObject t1_comparable = new TicketEntryObject("Apple Juice", "123456789123", 1, 0.0, 2.0);

        TicketEntryObject t2 = new TicketEntryObject("Orange Juice", "123456789124", 1, 0.0, 3.0);
        TicketEntryObject t2_comparable = new TicketEntryObject("Orange Juice", "123456789124", 1, 0.0, 3.0);

        TicketEntryObject t3 = new TicketEntryObject("Peanuts Butter", "123456789125", 1, 0.0, 5.0);
        TicketEntryObject t3_comparable = new TicketEntryObject("Peanuts Butter", "123456789125", 1, 0.0, 5.0);

        TicketEntryObject t4 = new TicketEntryObject("Rice", "123456789126", 1, 0.0, 5.0);
        TicketEntryObject t4_comparable = new TicketEntryObject("Rice", "123456789126", 1, 0.0, 5.0);

        //add them to a list
        entries.put(t1.getBarCode(), t1);
        entries.put(t2.getBarCode(), t2);
        entries.put(t3.getBarCode(), t3);
        entries.put(t4.getBarCode(), t4);

        //push the list to saleTransactionObject
        returnTransactionObject.setReturnedItems(entries);

        //verifies that returned list contains pushed items
        List<TicketEntryObject> returnedItems = returnTransactionObject.getReturnedItems();

        assertTrue(returnedItems.contains(t1_comparable));
        assertTrue(returnedItems.contains(t2_comparable));
        assertTrue(returnedItems.contains(t3_comparable));
        assertTrue(returnedItems.contains(t4_comparable));
    }




}
