package it.polito.ezshop.unitTests;
import static org.junit.Assert.*;

import it.polito.ezshop.model.CreditCardObject;
import org.junit.Test;

public class CreditCardTest {

    @Test
    public void testGetCode(){
        String ccCode = "5555333322227777";
        CreditCardObject cc = new CreditCardObject(ccCode);
        assertEquals(cc.getCreditCardCode(), ccCode);
    }

    @Test
    public void testGetBalance(){
        String ccCode =  "5555333322227777";
        double balance = 816629.02;
        CreditCardObject cc = new CreditCardObject(ccCode, balance);

        assertEquals(cc.getBalance(), balance, 0.01);
    }

    @Test
    public void testUpdateBalance(){
        String ccCode =  "5555333322227777";
        double balance = 816629.02;
        double balanceDelta = 221.05;

        CreditCardObject cc = new CreditCardObject(ccCode, balance);

        assertEquals(cc.updateBalance(balanceDelta), balance + balanceDelta, 0.01);
        assertEquals(cc.getBalance(), balance + balanceDelta, 0.01);

        assertEquals(cc.updateBalance(-balanceDelta), balance , 0.01);
        assertEquals(cc.getBalance(), balance , 0.01);
    }
}
