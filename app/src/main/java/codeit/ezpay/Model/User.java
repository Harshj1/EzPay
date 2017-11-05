package codeit.ezpay.Model;

/**
 * Created by nikhi on 04-Nov-17.
 */

public class User {
    private String name;
    private String uid;
    private String mail_id;
    private String profile_url;
    private String creditcardnumber;

    public User(String name, String uid, String mail_id,String creditcardnumber) {
        this.name = name;
        this.uid = uid;
        this.mail_id = mail_id;
      //  this.profile_url=profile_url;
        this.creditcardnumber=creditcardnumber;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getMail_id() {
        return mail_id;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public String getCredit_card_number() {
        return creditcardnumber;
    }
}
