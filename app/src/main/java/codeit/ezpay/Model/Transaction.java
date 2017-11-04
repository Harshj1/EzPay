package codeit.ezpay.Model;

import java.sql.Timestamp;

public class Transaction {

    private int balance;
    private String timestamp;
    private Type type;


    public Transaction(int balance, String timestamp, Type type)
    {
        this.balance = balance;
        this.timestamp = timestamp;
        this.type = type;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
