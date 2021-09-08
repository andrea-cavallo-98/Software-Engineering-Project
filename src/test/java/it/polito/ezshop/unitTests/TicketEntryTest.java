package it.polito.ezshop.unitTests;
import static org.junit.Assert.*;
import it.polito.ezshop.model.TicketEntryObject;
import org.junit.Test;

public class TicketEntryTest {

    @Test
    public void testBarCode(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        Integer amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te.getBarCode(), barCode);

        String barCode2 = "9788808182159";
        te.setBarCode(barCode2);
        assertEquals(te.getBarCode(), barCode2);
    }

    @Test
    public void testDescription(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        Integer amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te.getProductDescription(), description);

        String description2 = "Orange Juice";
        te.setProductDescription(description2);
        assertEquals(te.getProductDescription(), description2);
    }

    @Test
    public void testAmount(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        int amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te.getAmount(), amount);

        int amount2 = 78;
        te.setAmount(amount2);
        assertEquals(te.getAmount(), amount2);

    }

    @Test
    public void testPricePerUnit(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        Integer amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te.getPricePerUnit(), pricePerUnit, 0.01);

        double price2 = 3.1;
        te.setPricePerUnit(price2);
        assertEquals(te.getPricePerUnit(), price2,0.01);

    }

    @Test
    public void testDiscountRate(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        Integer amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te.getDiscountRate(), discountRate, 0.01);

        double discount2 = 0.5;
        te.setDiscountRate(discount2);
        assertEquals(te.getDiscountRate(), discount2,0.01);
    }

    @Test
    public void testEqualsOperator(){
        String description = "Apple Juice";
        String barCode = "9788808182159";
        Integer amount = 45;
        double pricePerUnit = 2.5;
        double discountRate = 0.25;
        TicketEntryObject te = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);
        TicketEntryObject te1 = new TicketEntryObject(description, barCode, amount, discountRate, pricePerUnit);

        assertEquals(te, te1);

        String barCode2 = "9788832360103";
        te.setBarCode(barCode2);
        assertNotEquals(te,te1);

    }
}
