package codeit.ezpay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import codeit.ezpay.Model.ChatMessage;
import codeit.ezpay.Model.Transaction;
import codeit.ezpay.Model.Type;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity implements AIListener {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    public static final int RC_SIGN_IN=9001;
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    ImageView userImage;
    TextView userName;
    DatabaseReference ref,chatref,transref,transref1,typeref,userRef;
    FirebaseRecyclerAdapter<ChatMessage,chat_rec> adapter;
    Boolean flagFab = true;
    private AIService aiService;
    private int userFoundFlag = 0;
    String balance1=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},1);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_custom_view_home);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        editText = (EditText)findViewById(R.id.editText);
        addBtn = (RelativeLayout)findViewById(R.id.addBtn);
        userImage=(CircleImageView)findViewById(R.id.userImage);
        userName=(TextView)findViewById(R.id.userName);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        final String[] deposit = new String[1];

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);

        mFirebaseAuth=FirebaseAuth.getInstance();

        final AIConfiguration config = new AIConfiguration("2770b09ff4da4b5fb3389446ad519eaa",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();

        final FirebaseUser user1= mFirebaseAuth.getCurrentUser();


        userRef=FirebaseDatabase.getInstance().getReference().child("users");

        //Glide.with(userImage.getContext()).load(mFirebaseAuth.getCurrentUser().getPhotoUrl().toString()).into(userImage);
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,UserProfile.class));
                finish();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,UserProfile.class));
                finish();
            }
        });



        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, mFirebaseAuth.getCurrentUser().getDisplayName());
                    chatref.child("chat").push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest,Void,AIResponse>(){

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {
                                Result result = response.getResult();
                                final String reply = result.getFulfillment().getSpeech();
                                Log.e("LOG",reply.toString());
                                if(result.getAction().equals("balance"))
                                {
                                    getBalance(reply);
                                }
                                else if(result.getAction().equals("pay") || result.getAction().equals("payemail"))
                                {
                                    getPay(reply,result);
                                }
                                else if(result.getAction().equals("history-email") || result.getAction().equals("history-company"))
                                {
                                    getHistoryEmail(reply,result);
                                }
                                else if(result.getAction().equals("history-gen"))
                                {
                                    getHistoryGen();
                                }
                                else if(result.getAction().equals("deposit"))
                                {
                                    deposit[0] = result.getStringParameter("deposited_money");
                                    Log.e("DEPOSIT","amt:"+deposit[0]);
                                    ChatMessage chatMessage = new ChatMessage(reply, "bot");
                                    chatref.child("chat").push().setValue(chatMessage);
                                }
                                else if(result.getAction().equals("cc"))
                                {
                                    getDeposit(deposit[0],result);
                                }
                                else if(result.getAction().equals("statement"))
                                {
                                    getStatement(result);
                                }
                                else{
                                    deposit[0] = null;
                                    Log.e("Tag",reply);
                                    ChatMessage chatMessage = new ChatMessage(reply, "bot");
                                    chatref.child("chat").push().setValue(chatMessage);
                                }
                            }
                        }
                    }.execute(aiRequest);
                }
                else {
                    aiService.startListening();
                }
                editText.setText("");
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView)findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);
                if (s.toString().trim().length()!=0 && flagFab){
                    ImageViewAnimatedChange(LoginActivity.this,fab_img,img);
                    flagFab=false;
                }
                else if (s.toString().trim().length()==0){
                    ImageViewAnimatedChange(LoginActivity.this,fab_img,img1);
                    flagFab=true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
//        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//
//                final FirebaseUser user= firebaseAuth.getCurrentUser();
//                if(user!=null)
//                {
//                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            for(DataSnapshot snapshot : dataSnapshot.getChildren())
//                            {
//                                // Log.e("TAG",snapshot.toString());
//                                if(snapshot.child("uid").getValue().toString().equals(user.getUid())) {
//                                    userFoundFlag = 1;
//                                }
//                            }
//
//                            if(userFoundFlag==0)
//                            {
//                                userRef.push().setValue((new User(user.getDisplayName(), user.getUid(), user.getEmail(),user.getPhotoUrl().toString())));
//                                transref.child("transaction").push().setValue(new codeit.ezpay.Model.Transaction(5000,getTimeStamp(),new Type("Initial","Initial","Deposit",5000)));
//                            }
//                        }
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
                    chatref = ref.child(mFirebaseAuth.getCurrentUser().getUid());
                    transref = ref.child(mFirebaseAuth.getCurrentUser().getUid());
                    typeref = ref.child(mFirebaseAuth.getCurrentUser().getUid());
                    adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class,R.layout.msglist,chat_rec.class,chatref.child("chat")) {
                        @Override
                        protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
                            if (model.getMsgUser().equals(mFirebaseAuth.getCurrentUser().getDisplayName())) {

                                viewHolder.rightText.setText(model.getMsgText());

                                viewHolder.rightText.setVisibility(View.VISIBLE);
                                viewHolder.leftText.setVisibility(View.GONE);
                            }
                            else {

                                viewHolder.leftText.setText(model.getMsgText());

                                viewHolder.rightText.setVisibility(View.GONE);
                                viewHolder.leftText.setVisibility(View.VISIBLE);
                            }
                        }
                    };

                    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onItemRangeInserted(int positionStart, int itemCount) {
                            super.onItemRangeInserted(positionStart, itemCount);

                            int msgCount = adapter.getItemCount();
                            int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                            Log.e("COUNT","Last:"+lastVisiblePosition);
                            Log.e("COUNT","First:"+positionStart);
                            if (lastVisiblePosition == -1 ||
                                    (positionStart >= (msgCount - 1) ||
                                            lastVisiblePosition == (positionStart - 1))) {
                                recyclerView.scrollToPosition(positionStart);
                            }
                        }
                    });

                    recyclerView.setAdapter(adapter);



        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String[] upperString = new String[1];
        final String[] alpha = new String[1];
        final ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(LoginActivity.this, SignIn.class));
                    finish();

                }
                else
                {
                    userName.setText(mFirebaseAuth.getCurrentUser().getDisplayName());
                }
            }
        };
    }

    public String getTimeStamp(){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());;
        return timeStamp;
    }

    private String modifyDateLayout(String inputDate) throws ParseException{
        Date date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").parse(inputDate);
        return new SimpleDateFormat("d MMM yyyy, hh:mm aaa").format(date);
    }

    private void getBalance(final String reply)
    {
        transref.child("transaction").orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.e("TAG",snapshot.toString());
                    balance1=snapshot.child("balance").getValue().toString();
                    Log.e("Tag",snapshot.child("balance").getValue().toString());
                    break;
                }
                ChatMessage chatMessage = new ChatMessage(reply+" "+balance1, "bot");
                chatref.child("chat").push().setValue(chatMessage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getHistoryGen()
    {
        final int[] count = {0};
        final boolean[] historyexist = {false};
        DatabaseReference typeref;
        typeref = transref.child("transaction");
//                                    final ArrayList<ChatMessage> chatMessage = new ArrayList<ChatMessage>();
        typeref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String timestamp = null;
                    try {
                        timestamp = modifyDateLayout(snapshot.child("timestamp").getValue().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String amount = snapshot.child("type").child("amount").getValue().toString();
                    String type;
                    String receiver = snapshot.child("type").child("to").getValue().toString();
                    String message;
                    if(snapshot.child("type").child("type").getValue().toString().equals("Deposit"))
                    {
                        type = "received";
                        message = "You " + type + " Rs."+ amount + " from " +receiver+ " at " + timestamp ;
                    }
                    else if(snapshot.child("type").child("to").getValue().toString().equals("BANK"))
                    {
                        type = "credited";
                        message = "You " + type + " Rs."+ amount + " at " + timestamp ;
                    }
                    else
                    {
                        type = "paid";
                        message = "You " + type + " Rs."+ amount + " to " +receiver+ " at " + timestamp ;
                    }
                    if(count[0] == 0 )
                    {
                        type = "were credited with";
                        message = "You " + type + " Rs."+ amount + " at " + timestamp ;
                        count[0]++;
                    }
                    ChatMessage chatMessage = new ChatMessage(message, "bot");
                    chatref.child("chat").push().setValue(chatMessage);
                    historyexist[0] = true;
                }
                if(historyexist[0]){
                }
                else
                {
                    ChatMessage chatMessage = new ChatMessage("No history exists", "bot");
                    chatref.child("chat").push().setValue(chatMessage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHistoryEmail(String reply,ai.api.model.Result result)
    {
        final String receiver = result.getStringParameter("receiver");
        Log.e("History","Rec "+receiver);
        final boolean[] historyexist = {false};
        DatabaseReference typeref;
        typeref = transref.child("transaction");
//                                    final ArrayList<ChatMessage> chatMessage = new ArrayList<ChatMessage>();
        typeref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
//                                                Log.e("History",snapshot.child("type").child("to").getValue().toString());
                    if(snapshot.child("type").child("to").getValue().toString().equals(receiver))
                    {
//                                                    Log.e("Histroy",snapshot.toString());
                        String timestamp = null;
                        try {
                            timestamp = modifyDateLayout(snapshot.child("timestamp").getValue().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String amount = snapshot.child("type").child("amount").getValue().toString();
                        String type;
                        if(snapshot.child("type").child("type").getValue().toString().equals("Deposit"))
                        {
                            type = "received";
                        }
                        else
                        {
                            type = "paid";
                        }
                        String message = "You " + type + " Rs."+ amount + " at " + timestamp ;
                        ChatMessage chatMessage = new ChatMessage(message, "bot");
                        chatref.child("chat").push().setValue(chatMessage);
                        historyexist[0] = true;
                    }
                }
                if(historyexist[0]){
                }
                else
                {
                    ChatMessage chatMessage = new ChatMessage("No history exists", "bot");
                    chatref.child("chat").push().setValue(chatMessage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getStatement(ai.api.model.Result result)
    {
        final boolean[] historyexist = {false};
        final int[] count = {0};
        DatabaseReference typeref;
        typeref = transref.child("transaction");
        String duration_unit = result.getComplexParameter("duration").get("amount").toString();
        Log.e("Duration unit",duration_unit);
        String duration_time = result.getComplexParameter("duration").get("unit").getAsString();
        Log.e("Duration time",duration_time);
        String timestamp = getTimeStamp();
        final String[] transtime = new String[1];
        final SimpleDateFormat datetimeFormatter = new SimpleDateFormat(
                "yyyy.MM.dd.HH.mm.ss");
        try {
            Date currtime = datetimeFormatter.parse(timestamp);
            final Calendar cal = Calendar.getInstance();
            final Calendar modcal = Calendar.getInstance();
            cal.setTime(currtime);
            if(duration_time.equals("h"))
            {
                cal.add(Calendar.HOUR,Integer.parseInt("-"+duration_unit));
                Log.e("TIME",cal.getTime().toString());
            }
            if(duration_time.equals("day"))
            {
                cal.add(Calendar.DATE,Integer.parseInt("-"+duration_unit));
                Log.e("TIME",cal.getTime().toString());
            }
            if(duration_time.equals("min"))
            {
                cal.add(Calendar.MINUTE,Integer.parseInt("-"+duration_unit));
                Log.e("TIME",cal.getTime().toString());
            }
            typeref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
//                                                Log.e("History",snapshot.child("type").child("to").getValue().toString());
                        transtime[0] = snapshot.child("timestamp").getValue().toString();
                        try {
                            modcal.setTime(datetimeFormatter.parse(transtime[0]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(modcal.after(cal))
                        {
//                                                    Log.e("Histroy",snapshot.toString());
                            String timestamp = null;
                            try {
                                timestamp = modifyDateLayout(transtime[0]);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String amount = snapshot.child("type").child("amount").getValue().toString();
                            String receiver = snapshot.child("type").child("to").getValue().toString();
                            String type;
                            String message;
                            if(snapshot.child("type").child("type").getValue().toString().equals("Deposit"))
                            {
                                type = "received";
                                message = "You " + type + " Rs."+ amount + " from " +receiver+ " at " + timestamp ;
                            }
                            else if(snapshot.child("type").child("to").getValue().toString().equals("BANK"))
                            {
                                type = "credited";
                                message = "You " + type + " Rs."+ amount + " at " + timestamp ;
                            }
                            else
                            {
                                type = "paid";
                                message = "You " + type + " Rs."+ amount + " to " +receiver+ " at " + timestamp ;
                            }
                            if(count[0] == 0 )
                            {
                                type = "were credited with";
                                message = "You " + type + " Rs."+ amount + " at " + timestamp ;
                                count[0]++;
                            }
                            ChatMessage chatMessage = new ChatMessage(message, "bot");
                            chatref.child("chat").push().setValue(chatMessage);
                            historyexist[0] = true;
                        }
                    }
                    if(historyexist[0]){
                    }
                    else
                    {
                        ChatMessage chatMessage = new ChatMessage("No history exists", "bot");
                        chatref.child("chat").push().setValue(chatMessage);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void getDeposit(String deposit, ai.api.model.Result result)
    {
        if(deposit != null)
        {
            Log.e("DEPOSIT",deposit);
            final String[] cc = new String[1];
            final boolean[] match = new boolean[1];
            final String[] currbalance = new String[2];
            currbalance[1] = deposit;
            cc[0] = result.getStringParameter("cc");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        if(snapshot.child("credit_card_number").getValue().toString().equals(cc[0])) {
                            match[0] = true;
                            break;
                        }
                    }
                    if(match[0])
                    {
                        transref.child("transaction").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    Log.e("TAG", snapshot.toString());
                                    currbalance[0] = snapshot.child("balance").getValue().toString();
                                    Log.e("Success", currbalance[0]);
                                    break;
                                }
                                Log.e("Success", String.valueOf(Integer.parseInt(currbalance[0])));
                                currbalance[0]= String.valueOf(Integer.parseInt(currbalance[0])+Integer.parseInt(currbalance[1]));
                                transref.child("transaction").push().setValue(new Transaction(Integer.parseInt(currbalance[0]),getTimeStamp(),new Type("BANK",mFirebaseAuth.getCurrentUser().getEmail(),"Credit",Integer.parseInt(currbalance[1]))));
                                Log.e("Success",currbalance[0]);
                                ChatMessage chatMessage = new ChatMessage("Successfully transferred Rs."+currbalance[1]+"\nYour current balance is Rs."+currbalance[0], "bot");
                                chatref.child("chat").push().setValue(chatMessage);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    else
                    {
                        ChatMessage chatMessage = new ChatMessage("Wrong credit card number.", "bot");
                        chatref.child("chat").push().setValue(chatMessage);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else
        {
            ChatMessage chatMessage = new ChatMessage("Please try depositing money again.", "bot");
            chatref.child("chat").push().setValue(chatMessage);
        }
    }

    private void getPay(String reply, ai.api.model.Result result)
    {
        final String payee = result.getStringParameter("receiver");
        final String amount = result.getStringParameter("amount");
        Log.e("GET","JI"+amount);
        final String[] payeeUID = new String[1];
        final boolean[] userfound = {false};
        final String[] currbalance = new String[2];
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.child("mail_id").getValue().toString().equals(payee)) {
                        payeeUID[0] = snapshot.child("uid").getValue().toString();
                        userfound[0] = true;
                        break;
                    }
                }
                if(userfound[0])
                {
                    transref.child("transaction").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Log.e("TAG", snapshot.toString());
                                currbalance[0] = snapshot.child("balance").getValue().toString();
                                Log.e("Success", currbalance[0]);
                                break;
                            }
                            Log.e("Success", String.valueOf(Integer.parseInt(currbalance[0])));
                            if(Integer.parseInt(currbalance[0])>=Integer.parseInt(amount))
                            {
                                currbalance[0]= String.valueOf(Integer.parseInt(currbalance[0])-Integer.parseInt(amount));
                                transref.child("transaction").push().setValue(new Transaction(Integer.parseInt(currbalance[0]),getTimeStamp(),new Type(payee,mFirebaseAuth.getCurrentUser().getEmail(),"Withdrawn",Integer.parseInt(amount))));
                                Log.e("Success",currbalance[0]);
                                ref.child(payeeUID[0]).child("transaction").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            currbalance[1] = snapshot.child("balance").getValue().toString();
                                            Log.e("Balance", currbalance[1]);
                                            break;
                                        }
                                        currbalance[1] = String.valueOf(Integer.parseInt(currbalance[1]) + Integer.parseInt(amount));
                                        ref.child(payeeUID[0]).child("transaction").push().setValue(new Transaction(Integer.parseInt(currbalance[1]),getTimeStamp(),new Type(mFirebaseAuth.getCurrentUser().getEmail(),payee,"Deposit",Integer.parseInt(amount))));
                                        ChatMessage chatMessage = new ChatMessage("Money succesfully transferred.\nYour account balance is: "+currbalance[0], "bot");
                                        chatref.child("chat").push().setValue(chatMessage);
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
//                                                            ref.child(payeeUID[0]).child("transaction").push().setValue(new codeit.ezpay.Model.Transaction(Integer.parseInt(amount),getTimeStamp(),new Type(mFirebaseAuth.getCurrentUser().getEmail(),payee,"Transfer")));
                            }
                            else
                            {
                                ChatMessage chatMessage = new ChatMessage("Insufficient balance, please deposit in your account.\nYour account balance is: "+currbalance[0], "bot");
                                chatref.child("chat").push().setValue(chatMessage);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                else
                {
                    ChatMessage chatMessage = new ChatMessage("Please check the receiver's id.", "bot");
                    chatref.child("chat").push().setValue(chatMessage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        chatref.child("chat").push().setValue(chatMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item ) {
        int id = item.getItemId();
        if (id == R.id.sign_out_menu) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
            return true;
        }
//        else if( id == R.id.tutorial){
//            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {

            // notify user you are online

        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            Toast.makeText(LoginActivity.this,"Please check Internet connection",Toast.LENGTH_LONG).show();
        }
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

        final String[] deposit = new String[1];

        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message,mFirebaseAuth.getCurrentUser().getDisplayName() );
        chatref.child("chat").push().setValue(chatMessage0);


        final String reply = result.getFulfillment().getSpeech();
        Log.e("LOG",reply);
        if(result.getAction().equals("balance"))
        {
            getBalance(reply);
        }
        else if(result.getAction().equals("pay") || result.getAction().equals("payemail"))
        {
            getPay(reply,result);
        }
        else if(result.getAction().equals("history-email") || result.getAction().equals("history-company"))
        {
            getHistoryEmail(reply,result);
        }
        else if(result.getAction().equals("history-gen"))
        {
            getHistoryGen();
        }
        else if(result.getAction().equals("deposit"))
        {
            deposit[0] = result.getStringParameter("deposited_money");
            Log.e("DEPOSIT","amt:"+deposit[0]);
            ChatMessage chatMessage = new ChatMessage(reply, "bot");
            chatref.child("chat").push().setValue(chatMessage);
        }
        else if(result.getAction().equals("cc"))
        {
            getDeposit(deposit[0],result);
        }
        else if(result.getAction().equals("statement"))
        {
            getStatement(result);
        }
        else{
            deposit[0] = null;
            Log.e("Tag",reply);
            ChatMessage chatMessage = new ChatMessage(reply, "bot");
            chatref.child("chat").push().setValue(chatMessage);
        }
    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

}
