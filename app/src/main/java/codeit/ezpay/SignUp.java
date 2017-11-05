package codeit.ezpay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import codeit.ezpay.Model.Type;
import codeit.ezpay.Model.User;

public class SignUp extends AppCompatActivity {


    private EditText inputEmail, inputPassword, inputName, inputCreditCardNumber;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    String name;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    DatabaseReference ref,userRef,transref;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        inputName=(EditText)findViewById(R.id.inputname);
        inputCreditCardNumber=(EditText)findViewById(R.id.inputcreditcardnumber);
        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);

        userRef=FirebaseDatabase.getInstance().getReference().child("users");

       // btnResetPassword = (Button) findViewById(R.id.btn_reset_password);


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this,SignIn.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                name=inputName.getText().toString().trim();
                String creditCardNumber=inputCreditCardNumber.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(name))
                {
                    Toast.makeText(getApplicationContext(),"Enter your Name!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(creditCardNumber)&&creditCardNumber.length()!=16)
                {
                    Toast.makeText(getApplicationContext(),"Enter valid Credit Card Details!",Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignUp.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
//                                    transref = ref.child(auth.getCurrentUser().getUid());
//                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
//                                    auth.getCurrentUser().updateProfile(profileChangeRequest);
//                                    userRef.push().setValue((new User(auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail())));
//                                    transref.child("transaction").push().setValue(new codeit.ezpay.Model.Transaction(5000,getTimeStamp(),new Type("Initial","Initial","Deposit",5000)));
//                                    startActivity(new Intent(SignUp.this, LoginActivity.class));
//                                    finish();
                                }
                            }
                        });

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    user.updateProfile(profileUpdates);
                    transref = ref.child(auth.getCurrentUser().getUid());
                    userRef.push().setValue((new User(name, auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail())));
                    transref.child("transaction").push().setValue(new codeit.ezpay.Model.Transaction(5000,getTimeStamp(),new Type("Initial","Initial","Deposit",5000)));
                    Intent intent = new Intent(SignUp.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }
    public String getTimeStamp(){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());;
        return timeStamp;
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth.addAuthStateListener(mAuthListener);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            auth.removeAuthStateListener(mAuthListener);
        }
    }
}
