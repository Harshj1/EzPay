package codeit.ezpay;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    TextView name,emailId,creditCardNumber,balance;
    FirebaseAuth firebaseAuth;
    ImageView userProfileImage;
    DatabaseReference ref,userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);

            name=(TextView)findViewById(R.id.name);
            emailId=(TextView)findViewById(R.id.emailid);
            creditCardNumber=(TextView)findViewById(R.id.creditcardnumber);
            balance=(TextView)findViewById(R.id.balance);
            userProfileImage=(ImageView)findViewById(R.id.user_profile_photo);

            ref = FirebaseDatabase.getInstance().getReference();
            ref.keepSynced(true);

        userRef=FirebaseDatabase.getInstance().getReference().child("users");

            final FirebaseUser user= firebaseAuth.getInstance().getCurrentUser();
        String[] nameArray = user.getDisplayName().split(" ");
        ColorGenerator colorGenerator = ColorGenerator.MATERIAL;

        String[] strArray = user.getDisplayName().split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
            name.setText(builder.toString());
            emailId.setText(user.getEmail());
        String alpha = String.valueOf(user.getDisplayName().charAt(0));
        Log.e("hi",alpha);
        //int fontSize = (int) (height * 0.07);
        int color = colorGenerator.getRandomColor();
        TextDrawable textDrawable =
                TextDrawable.builder()
                        .beginConfig()
                        .textColor(Color.parseColor("#FFFFFF"))
                        .useFont(Typeface.DEFAULT)
                        .fontSize(200)
                        .bold()
                        .toUpperCase()
                        .endConfig()
                        .buildRound(alpha,color);
        userProfileImage.setImageDrawable(textDrawable);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ref.child(firebaseAuth.getInstance().getCurrentUser().getUid()).child("transaction").orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    balance.setText("Balance: "+snapshot.child("balance").getValue().toString());
                    Log.e("Tag",snapshot.child("balance").getValue().toString());
                    break;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    creditCardNumber.setText("Credit Card Number: "+snapshot.child("credit_card_number").getValue().toString());
                    break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }



        });
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(UserProfile.this, LoginActivity.class));
    }
}
