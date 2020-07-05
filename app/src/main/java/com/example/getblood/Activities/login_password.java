package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.load.resource.gif.GifDrawableEncoder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class login_password extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    //"(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");
    TextInputEditText textInputPassword, reTextInputPassword;
    Button nextPassword;
    String userId;
    FirebaseStorage storage;
    FirebaseDatabase mDatabase;
    private ScrollView sc;
    DatabaseReference mRef;
    StorageReference mStorageRef;
    FirebaseFirestore firestore;
    String firstName, lastName, date, gender, blood, phoneInput, emailInput, passwordInput, rePasswordInput,state,city,pinCode;
    FirebaseAuth mAuth;
    private boolean found;
    public static final String EXTRA_FOUND="found";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        textInputPassword = findViewById(R.id.signUpPassword);
        reTextInputPassword = findViewById(R.id.reEnterPassword);
        nextPassword = findViewById(R.id.passwordNextButton);
        checkUserStatus();
        sc= findViewById(R.id.scPass);
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReferenceFromUrl("gs://get-blood-f8318.appspot.com");
        setSupportActionBar(toolbar);
        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(login_password.this);
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Password");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        Intent intent = getIntent();
        firstName = intent.getStringExtra(AddressActivity.EXTRA_FIRST_NAME);
        lastName = intent.getStringExtra(AddressActivity.EXTRA_LAST_NAME);
        date = intent.getStringExtra(AddressActivity.EXTRA_DATE_TEXT);
        gender = intent.getStringExtra(AddressActivity.EXTRA_GENDER_TEXT);
        blood = intent.getStringExtra(AddressActivity.EXTRA_BLOOD_TEXT);
        phoneInput = intent.getStringExtra(AddressActivity.EXTRA_PHONE_TEXT);
        emailInput = intent.getStringExtra(AddressActivity.EXTRA_EMAIL_TEXT);
        found=intent.getBooleanExtra(AddressActivity.EXTRA_FOUND,false);
        state=intent.getStringExtra(AddressActivity.EXTRA_STATE_TEXT);
        city=intent.getStringExtra(AddressActivity.EXTRA_CITY_TEXT);
        pinCode=intent.getStringExtra(AddressActivity.EXTRA_PIN_TEXT);
        nextPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkConnection()) {
                    if (validatePassword()) {
                        startUserRegisteration();
                    }
                }
            }
        });

    }

    public boolean validatePassword() {
        passwordInput = textInputPassword.getText().toString().trim();
        rePasswordInput = reTextInputPassword.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPassword.setError("Password is too weak");
            return false;
        } else if (!rePasswordInput.isEmpty()) {
            if (!passwordInput.equals(rePasswordInput)) {
                Toast.makeText(this, "password must be same", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else if (!passwordInput.isEmpty()) {
            if (rePasswordInput.isEmpty()) {
                reTextInputPassword.setError("Field can't be empty");
                return false;
            }
            return true;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {
        alertDialog(login_password.this);


    }

    public void alertDialog(final Context context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        final TextView continueCreatingAcc = mView.findViewById(R.id.continueCreatingAccount);
        final TextView stopCreatingAcc = mView.findViewById(R.id.stopCreatingAccount);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        continueCreatingAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        stopCreatingAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Donor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void startUserRegisteration() {

        String FullName = firstName + " " + lastName;
        String DOB = date;
        String Blood = blood;
        String Gender = gender;
        String contactNo = phoneInput;
        String emailId = emailInput;
        ProgressDialog progressDialog = new ProgressDialog(login_password.this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(login_password.this, task -> {
                    if (task.isSuccessful()) {
                        if (!found) {
                            userId = mAuth.getCurrentUser().getUid();
                            Map<String, String> userData = new HashMap<>();
                            userData.put("FullName", FullName);
                            userData.put("DateOfBirth", DOB);
                            userData.put("Blood", Blood);
                            userData.put("Gender", Gender);
                            userData.put("Contact", contactNo);
                            userData.put("emailId", emailId);
                            userData.put("UID", userId);
                            userData.put("profileImage", "0");
                            userData.put("state",state);
                            userData.put("city",city);
                            userData.put("userType","Donor");
                            userData.put("pin",pinCode);
                            userData.put("onlineStatus","online");
                            userData.put("typingTo","noOne");
                            userData.put("Register","firebase");
                            progressDialog.setMessage("Saving user data...");
                            Task<Void> task1 = mRef.child("Donor").child(userId).setValue(userData);
                            task1.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(login_password.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                                     intent.putExtra(EXTRA_FOUND,found);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(login_password.this, "Error Occured " + e, Toast.LENGTH_SHORT).show();

                                }
                            });
                        } else {
                            userId = mAuth.getCurrentUser().getUid();
                            Map<String, String> userData = new HashMap<>();
                            userData.put("FullName", FullName);
                            userData.put("DateOfBirth", DOB);
                            userData.put("Blood", Blood);
                            userData.put("Gender", Gender);
                            userData.put("Contact", contactNo);
                            userData.put("emailId", emailId);
                            userData.put("UID", userId);
                            userData.put("profileImage", "");
                            userData.put("state",state);
                            userData.put("city",city);
                            userData.put("userType","Recipient");
                            userData.put("pin",pinCode);
                            userData.put("onlineStatus","online");
                            userData.put("typingTo","noOne");
                            progressDialog.setMessage("Saving user data...");
                            Task<Void> task1 = mRef.child("Recipient").child(userId).setValue(userData);
                            task1.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(login_password.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                                    intent.putExtra(EXTRA_FOUND,found);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(login_password.this, "Error Occured " + e, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthUserCollisionException) {
                    progressDialog.dismiss();
                    Toast.makeText(login_password.this, "Email already exists,try with another email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Donor.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            startActivity(new Intent(getApplicationContext() , HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    //

    public boolean checkConnection()
    {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if(null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return  true;

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                return  true;
            }
            return  true;
        }
        else
        {
            Snackbar sb = Snackbar.make(sc,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }

}
