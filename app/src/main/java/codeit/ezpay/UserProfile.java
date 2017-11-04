package codeit.ezpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfile extends AppCompatActivity {

    TextView name,emailId,creditCardNumber,balance;
    FirebaseAuth firebaseAuth;
    ImageView userProfileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);

            name=(TextView)findViewById(R.id.name);
            emailId=(TextView)findViewById(R.id.emailid);
            creditCardNumber=(TextView)findViewById(R.id.creditcardnumber);
            balance=(TextView)findViewById(R.id.balance);
            userProfileImage=(ImageView)findViewById(R.id.user_profile_photo);


            final FirebaseUser user= firebaseAuth.getInstance().getCurrentUser();

    //        Glide.with(userProfileImage.getContext()).load(user.getPhotoUrl().toString()).into(userProfileImage);
            name.setText(user.getDisplayName());
            emailId.setText(user.getEmail());
    }
}
