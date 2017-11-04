package codeit.ezpay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import codeit.ezpay.Model.ChatMessage;

public class UserProfile extends AppCompatActivity {

    TextView name,emailId,creditCardNumber,balance;
    FirebaseAuth firebaseAuth;
    ImageView userProfileImage;
    DatabaseReference ref;

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

            final FirebaseUser user= firebaseAuth.getInstance().getCurrentUser();

    //        Glide.with(userProfileImage.getContext()).load(user.getPhotoUrl().toString()).into(userProfileImage);
            name.setText(user.getDisplayName());
            emailId.setText(user.getEmail());
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
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(UserProfile.this, LoginActivity.class));
    }
}
