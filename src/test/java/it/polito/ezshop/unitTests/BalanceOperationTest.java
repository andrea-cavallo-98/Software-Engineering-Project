package it.polito.ezshop.unitTests;

import it.polito.ezshop.model.BalanceOperationObject;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class BalanceOperationTest {

    @Test
    public void testGetBalanceId(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);
        assertEquals(op.getBalanceId(), id);
    }

    @Test
    public void testGetDate(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);
        assertEquals(op.getDate(), date);
    }

    @Test
    public void testGetMoney(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);
        assertEquals(op.getMoney(), money, 0.0001);
    }

    @Test
    public void testGetType(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);
        assertEquals(op.getType(), type);
    }

    @Test
    public void testSetBalanceId(){
        int id = 12, newId = 50;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);

        op.setBalanceId(newId);
        assertEquals(op.getBalanceId(), newId);
    }

    @Test
    public void testSetDate(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        LocalDate newDate = LocalDate.of(2021, 05, 25);
        double money = 50.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);

        op.setDate(newDate);

        assertEquals(op.getDate(), newDate);
    }

    @Test
    public void testSetMoney(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0, newMoney = 100.0;
        String type = "Sale Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);

        op.setMoney(newMoney);

        assertEquals(op.getMoney(), newMoney, 0.0001);
    }

    @Test
    public void testSetType(){
        int id = 12;
        LocalDate date = LocalDate.of(2021, 05, 20);
        double money = 50.0;
        String type = "Sale Transaction", newType = "Order Transaction";
        BalanceOperationObject op = new BalanceOperationObject(id, date, money, type);

        op.setType(newType);

        assertEquals(op.getType(), newType);
    }


}
