package it.polito.ezshop.unitTests;

import static org.junit.Assert.*;
import it.polito.ezshop.controllers.PaymentGateway;
import org.junit.Test;

public class PaymentGatewayUnitTest {

    @Test
    public void verifyLength(){
        String valid_14 = "50490336563862";
        String valid_15 = "504903365638622";
        String valid_16 = "8996930288404152";
        String valid_17 = "15677504760139510";
        String valid_18 = "836861119076864068";
        String valid_19 = "4676417395153922236";
        String valid_20 = "22072169452226569039";
        assertFalse(PaymentGateway.verifyCard(valid_14));
        assertTrue(PaymentGateway.verifyCard(valid_15));
        assertTrue(PaymentGateway.verifyCard(valid_16));
        assertFalse(PaymentGateway.verifyCard(valid_17));
        assertFalse(PaymentGateway.verifyCard(valid_18));
        assertTrue(PaymentGateway.verifyCard(valid_19));
        assertFalse(PaymentGateway.verifyCard(valid_20));
    }

    @Test
    public void verifyValidity(){
        String valid_15 = "504903365638622";
        String invalid_15 = "504901125638622";
        String valid_16 = "8996930288404152";
        String valid_19 = "4676417395153922236";
        String invalid_16 = "1234567812345678";
        String invalid_19 = "4676417395153842230";
        assertTrue(PaymentGateway.verifyCard(valid_15));
        assertFalse(PaymentGateway.verifyCard(invalid_15));
        assertTrue(PaymentGateway.verifyCard(valid_16));
        assertFalse(PaymentGateway.verifyCard(invalid_16));
        assertTrue(PaymentGateway.verifyCard(valid_19));
        assertFalse(PaymentGateway.verifyCard(invalid_19));
    }

    @Test
    public void verifyCombinations() {
        String valid_15 = "504903365638622";
        String valid_16 = "8996930288404152";
        String valid_17 = "15677504760139510";
        String valid_18 = "836861119076864068";
        String valid_19 = "4676417395153922236";
        String valid_20 = "22072169452226569039";
        String invalid_15 = "504903365638620";
        String invalid_16 = "8996930288404150";
        String invalid_17 = "15677504760139519";
        String invalid_18 = "836861119076864066";
        String invalid_19 = "4676417395153922237";
        String invalid_20 = "22072169452226569031";

        assertTrue(PaymentGateway.verifyCard(valid_15));
        assertFalse(PaymentGateway.verifyCard(invalid_15));
        assertTrue(PaymentGateway.verifyCard(valid_16));
        assertFalse(PaymentGateway.verifyCard(invalid_16));
        assertFalse(PaymentGateway.verifyCard(valid_17));
        assertFalse(PaymentGateway.verifyCard(invalid_17));
        assertFalse(PaymentGateway.verifyCard(valid_18));
        assertFalse(PaymentGateway.verifyCard(invalid_18));
        assertTrue(PaymentGateway.verifyCard(valid_19));
        assertFalse(PaymentGateway.verifyCard(invalid_19));
        assertFalse(PaymentGateway.verifyCard(valid_20));
        assertFalse(PaymentGateway.verifyCard(invalid_20));
    }
}
