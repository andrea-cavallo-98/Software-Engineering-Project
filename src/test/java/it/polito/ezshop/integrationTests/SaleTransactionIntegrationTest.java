package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.model.SaleTransactionObject;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class SaleTransactionIntegrationTest {


    @Test
    public void testGetTicketNumber(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        assertEquals(saleTransactionObject.getTicketNumber(), Integer.valueOf(0));

        Integer newTicketNumber = 2;
        saleTransactionObject.setTicketNumber(newTicketNumber);
        assertEquals(saleTransactionObject.getTicketNumber(), newTicketNumber);

    }

    @Test
    public void testGetTicketEntry() {
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        TicketEntryObject ticketEntry = new TicketEntryObject("Apple Juice", "9788847057227", 1, 0.0, 2.0);
        TicketEntryObject comparable = new TicketEntryObject("Apple Juice", "9788847057227", 1, 0.0, 2.0);

        saleTransactionObject.addEntry(ticketEntry);

        //verify that the correct product is returned
        //TODO: comparator between ticketentries
        assertEquals(saleTransactionObject.getEntry("9788847057227"), comparable);

        //verify null when a ticket entry doesn't exist
        assertNull(saleTransactionObject.getEntry("9788808182159"));


    }

    @Test
    public void testGetTicketEntries(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        List<TicketEntry> entries = new ArrayList<>();

        //generate some ticket entries
        TicketEntryObject t1 = new TicketEntryObject("Apple Juice", "9788808182159", 1, 0.0, 2.0);
        TicketEntryObject t1_comparable = new TicketEntryObject("Apple Juice", "9788808182159", 1, 0.0, 2.0);

        TicketEntryObject t2 = new TicketEntryObject("Orange Juice", "9788806222024", 1, 0.0, 3.0);
        TicketEntryObject t2_comparable = new TicketEntryObject("Orange Juice", "9788806222024", 1, 0.0, 3.0);

        TicketEntryObject t3 = new TicketEntryObject("Peanuts Butter", "9788832360103", 1, 0.0, 5.0);
        TicketEntryObject t3_comparable = new TicketEntryObject("Peanuts Butter", "9788832360103", 1, 0.0, 5.0);

        TicketEntryObject t4 = new TicketEntryObject("Rice", "9788847057227", 1, 0.0, 5.0);
        TicketEntryObject t4_comparable = new TicketEntryObject("Rice", "9788847057227", 1, 0.0, 5.0);

        //add them to a list
        entries.add(t1);
        entries.add(t2);
        entries.add(t3);
        entries.add(t4);

        //push the list to saleTransactionObject
        saleTransactionObject.setEntries(entries);

        //verifies that returned list contains pushed items
        List<TicketEntry> returnedItems = saleTransactionObject.getEntries();

        assertTrue(returnedItems.contains(t1_comparable));
        assertTrue(returnedItems.contains(t2_comparable));
        assertTrue(returnedItems.contains(t3_comparable));
        assertTrue(returnedItems.contains(t4_comparable));
    }

    @Test
    public void testDeleteTicketEntry(){
        SaleTransactionObject saleTransactionObject = new SaleTransactionObject(0);
        List<TicketEntry> entries = new ArrayList<>();

        //generate some ticket entries
        TicketEntryObject t1 = new TicketEntryObject("Apple Juice", "9788808182159", 1, 0.0, 2.0);
        TicketEntryObject t1_comparable = new TicketEntryObject("Apple Juice", "9788808182159", 1, 0.0, 2.0);

        TicketEntryObject t2 = new TicketEntryObject("Orange Juice", "9788806222024", 1, 0.0, 3.0);
        TicketEntryObject t3 = new TicketEntryObject("Peanuts Butter", "9788832360103", 1, 0.0, 5.0);
        TicketEntryObject t4 = new TicketEntryObject("Rice", "9788847057227", 1, 0.0, 5.0);

        String invalidBarCode = "9788847057289";

        //add them to a list
        entries.add(t1);
        entries.add(t2);
        entries.add(t3);
        entries.add(t4);

        //push the list to saleTransactionObject
        saleTransactionObject.setEntries(entries);

        //now delete an object
        assertTrue(saleTransactionObject.deleteEntry(t1.getBarCode()));
        //try to delete an object is not into the sale transaction
        assertFalse(saleTransactionObject.deleteEntry(invalidBarCode));

        //assert that the returned list doesn't contain the deleted object
        assertFalse(saleTransactionObject.getEntries().contains(t1_comparable));
    }


}
