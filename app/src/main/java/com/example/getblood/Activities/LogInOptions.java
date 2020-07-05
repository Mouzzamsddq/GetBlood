package com.example.getblood.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogInOptions extends AppCompatActivity {
    private Button signInButton;
    private boolean found;
    Button signInViaMail;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mRef;
    private Button signOut;
    public static final String EXTRA_FOUND="found";

    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN=1;
    private String TAG="LogInOption";
    FirebaseAuth mAuth;

    public void facebookLogin(View v)
    {
        found=getIntent().getBooleanExtra(Donor.EXTRA_FOUND,false);
        Intent facebookLogin=new Intent(getApplicationContext(),ContiueFacebook.class);
        facebookLogin.putExtra(EXTRA_FOUND,found);
        startActivity(facebookLogin);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();

        mDataBase=FirebaseDatabase.getInstance();
        mRef=mDataBase.getReference().child("users");
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Sign in options");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        found=getIntent().getBooleanExtra(Donor.EXTRA_FOUND,false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        signInViaMail=findViewById(R.id.signInViaEmail);
//
        signInViaMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInOptions=new Intent(getApplicationContext(),LoginViaEmail.class);
                signInOptions.putExtra(EXTRA_FOUND,found);
                startActivity(signInOptions);
                finish();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
         signInButton=findViewById(R.id.signInButton);
        mAuth=FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
         signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 signIn();
            }
        });

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount user) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + user.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(user.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LogInOptions.this, "Logged in", Toast.LENGTH_SHORT).show();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    List<String> userList = new ArrayList<>();
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        for(DataSnapshot ds1 : ds.getChildren())
                                        {
                                            userList.add(ds1.getKey());
                                        }
                                    }
                                    Log.d("userList",userList.toString());
                                    checkDataUpload(userList);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            // Sign in success, update UI with the signed-in user's information


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LogInOptions.this, "You are not able to login in to google", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void checkDataUpload(List<String> userList) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userList != null  && userList.contains(myUid))
        {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            updateUI(user);

        }
        else
        {
            FirebaseUser user = mAuth.getCurrentUser();
            String FullName=user.getDisplayName();
            String emailId=user.getEmail();
            String userId=user.getUid();
            String userType;
            if(found)
            {
                 userType = "Recipient";
            }
            else
            {
                 userType = "Donor";
            }
            String profileImage=user.getPhotoUrl().toString();
            Map<String, String> userData = new HashMap<>();
            userData.put("FullName", FullName);
            userData.put("DateOfBirth", "");
            userData.put("Blood", "");
            userData.put("Gender", "");
            userData.put("Contact", "");
            userData.put("emailId", emailId);
            userData.put("UID", userId);
            userData.put("profileImage", profileImage);
            userData.put("state","");
            userData.put("city","");
            userData.put("userType",userType);
            userData.put("pin","");
            userData.put("onlineStatus","online");
            userData.put("typingTo","noOne");
            userData.put("Register","google");
            Task task3;
            if(found)
            {
                task3=mRef.child("Recipient").child(user.getUid()).setValue(userData);
            }
            else
            {
                task3=mRef.child("Donor").child(user.getUid()).setValue(userData);
            }
            task3.addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Toast.makeText(LogInOptions.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LogInOptions.this, "Failed to register "+e, Toast.LENGTH_SHORT).show();
                }
            });
            updateUI(user);


        }


    }

    private void updateUI(FirebaseUser user) {
//        GoogleSignInAccount user = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (user != null) {
            checkGoogleFill();
        }else
        {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }


        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
        else
        {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }


    private void checkGoogleFill()
    {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2: ds1.getChildren())
                    {
                        if(myUID != null  && myUID.equals(ds2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String, Object>)ds2.getValue();
                            String Register = (String)data.get("Register");
                            if(Register!= null  && Register.equals("google"))
                            {
                                String pinCode = (String )data.get("pin");
                                if(pinCode.equals(""))
                                {
                                    startActivity(new Intent(getApplicationContext(),GoogleFillActivity.class));
                                    finish();
                                }
                                else
                                {
                                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                    finish();
                                }

                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}



