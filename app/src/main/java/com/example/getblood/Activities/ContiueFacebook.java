package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContiueFacebook extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton fbLoginButton;
    private FirebaseAuth mFirebaseAuth;
    private boolean found;
    private AccessTokenTracker accessTokenTracker;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static final String TAG="Facebook Authentication";
    public static final String EXTRA_FOUND="found";
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contiue_facebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        progressDialog = new ProgressDialog(ContiueFacebook.this);
        progressDialog.setMessage("signing in ...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Continue to facebook");
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton=findViewById(R.id.fbLoginButton);
        if(fbLoginButton.getText().toString().equals("Log out"))
        {
            LoginManager.getInstance().logOut();
        }
        fbLoginButton.setReadPermissions("email","public_profile");
        mFirebaseAuth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d(TAG,"onSuccess"+ loginResult);
                        progressDialog.show();
                        handleFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d(TAG,"onCancel");

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(TAG,"onError"+ exception);
                    }
                });
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    updateUI(user);
                }else
                {
                    updateUI(null);
                }
            }
        };
        accessTokenTracker =new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null)
                {
                    mFirebaseAuth.signOut();
                }
            }
        };

    }
    private void handleFacebookToken(AccessToken token)
    {
        Log.d(TAG,"handleFacebookToken"+ token);

        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"sign in with credential successful");
                    FirebaseUser user=mFirebaseAuth.getCurrentUser();
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
                }else{
                    Log.d(TAG,"sign in with credential : failure",task.getException());
                    Toast.makeText(ContiueFacebook.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode,resultCode,data);
    }



    private void checkDataUpload(List<String> userList) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userList != null  && userList.contains(myUid))
        {
            progressDialog.dismiss();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            updateUI(user);

        }
        else
        {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String FullName=user.getDisplayName();
            String emailId=user.getEmail();
            String userId=user.getUid();
            String userType;
            found = getIntent().getBooleanExtra(EXTRA_FOUND,false);
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
            userData.put("Register","facebook");
            Task task3;


            if(found)
            {
                task3=ref.child("Recipient").child(user.getUid()).setValue(userData);
            }
            else
            {
                task3=ref.child("Donor").child(user.getUid()).setValue(userData);
            }
            task3.addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    progressDialog.dismiss();
                    Toast.makeText(ContiueFacebook.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ContiueFacebook.this, "Failed to register "+e, Toast.LENGTH_SHORT).show();
                }
            });
            updateUI(user);


        }


    }

    private void updateUI(FirebaseUser user)
    {
        if(user!=null)
        {
           checkGoogleFill();
        }
        else
        {
            Toast.makeText(this, "user not logged in..!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
        checkUserStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(authStateListener!=null)
        {
            mFirebaseAuth.removeAuthStateListener(authStateListener);

        }
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
                            if(Register!= null  && Register.equals("facebook"))
                            {
                                String pinCode = (String )data.get("pin");
                                if(pinCode.equals(""))
                                {
                                    overridePendingTransition(0,0);
                                    Intent intent = new Intent(getApplicationContext(),GoogleFillActivity.class);

                                    startActivity(intent);
                                    overridePendingTransition(0,0);
                                    finish();
                                }
                                else
                                {

                                    overridePendingTransition(0,0);
                                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(0,0);
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

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }
    }

}

