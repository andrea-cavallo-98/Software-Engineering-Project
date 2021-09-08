package it.polito.ezshop.controllers;

import it.polito.ezshop.exceptions.InvalidCreditCardException;
import it.polito.ezshop.model.CreditCardObject;

import java.io.*;

public class PaymentGateway {

    public static boolean verifyCard(String creditCard){
        if (creditCard.length()!=15 && creditCard.length()!=16 && creditCard.length()!=19) return false;
        int sum = 0;
        boolean alternate = false;
        for (int i = creditCard.length() - 1; i >= 0; i--)
        {
            int n = Integer.parseInt(creditCard.substring(i, i + 1));
            if (alternate)
            {
                n *= 2;
                if (n > 9)
                {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public static boolean issuePayment(String creditCard, double price) throws InvalidCreditCardException {

        if(!verifyCard(creditCard)) throw new InvalidCreditCardException();
        CreditCardObject cc = getCreditCard(creditCard);
        if(cc == null) return false;
        if(cc.getBalance() >= price) {
            cc.updateBalance(-1 * price);
            return true;
            //return updateCreditCard(cc);
        }
        return false;
    }

    public static boolean issueTransfer(String creditCard, double money) throws InvalidCreditCardException {
        if(!verifyCard(creditCard)) throw new InvalidCreditCardException();
        CreditCardObject cc = getCreditCard(creditCard);
        return !(cc == null || money <= 0);
    }

    private static CreditCardObject getCreditCard(String creditCard){
        File file = new File("CreditCards.txt");
        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                while((st = br.readLine()) != null) {
                    if(st.charAt(0) == '#') continue;
                    String[] splitted = st.split(";");
                    String creditCardCode = splitted[0];
                    String creditCardBalance = splitted[1];
                    if(creditCardCode.equals(creditCard)) {
                        return new CreditCardObject(creditCardCode, Double.parseDouble(creditCardBalance));
                    }
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
        /*try {
            Connection connection = EZShop.getConnectionToDB();
            String query = "SELECT * FROM creditCards WHERE number=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, creditCard);
            ResultSet result = preparedStatement.executeQuery();
            if(result.next()) {
                double balance = result.getDouble("balance");
                return new CreditCardObject(creditCard, balance);
            }
            return null;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }*/
    }

    /*private static boolean updateCreditCard(CreditCardObject creditCard) {
        try {
            Connection conn = EZShop.getConnectionToDB();
            String query = "UPDATE creditCards SET balance=? WHERE number=?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setDouble(1, creditCard.getBalance());
            statement.setString(2,creditCard.getCreditCardCode());
            if(statement.executeUpdate()!=0){
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }*/
}
