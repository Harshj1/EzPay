package codeit.ezpay.Model;

/**
 * Created by Harsh on 11/4/2017.
 */

public class Type {

    private String to;
    private String from;
    private String type;
    private int amount;

    public Type(String to, String from, String type, int amount)
    {
        this.from = from;
        this.to = to;
        this.type = type;
        this.amount = amount;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
