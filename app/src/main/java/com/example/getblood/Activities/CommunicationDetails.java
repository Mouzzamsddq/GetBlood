package com.example.getblood.Activities;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.StrictMode;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.grpc.InternalChannelz;

public class CommunicationDetails extends AppCompatActivity {
    public static final String TAG = "TAG";
    SmsRetrieverClient client ;
    private boolean found;
    private static final int CREDENTIAL_PICKER_REQUEST = 1;
    protected TextInputEditText textInputPhone;
    protected TextInputEditText textInputEmail;
    GoogleApiClient apiClient;
    private Spinner spinner;
    public  static final String EXTRA_FOUND="found";
    FirebaseAuth mAuth;
    String verificationId;
    private Button nextButton;
    ProgressDialog pd;
    boolean check=false;
    PhoneAuthProvider.ForceResendingToken token;
    CountryCodePicker codePicker;
    Button sendOtpButton;
    String firstName,lastName,date,gender,blood,phoneInput,emailInput;
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";
    public static final String EXTRA_DATE_TEXT="com.example.getblood.Activities.EXTRA_DATE_TEXT";
    public static final String EXTRA_GENDER_TEXT="com.example.getblood.Activities.EXTRA_GENDER_TEXT";
    public static final String EXTRA_BLOOD_TEXT="com.example.getblood.Activities.EXTRA_BLOOD_TEXT";
    public static final String EXTRA_EMAIL_TEXT="com.example.getblood.Activities.EXTRA_EMAIL_TEXT";
    public static final String EXTRA_PHONE_TEXT="com.example.getblood.Activities.EXTRA_PHONE_TEXT";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nextButton=findViewById(R.id.nextBigButton);
        textInputPhone = findViewById(R.id.phoneEditText);
        textInputEmail = findViewById(R.id.emailEditText);
        pd = new ProgressDialog(this);
        pd.setMessage("Sending...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        codePicker=findViewById(R.id.countryCodePicker);
        Intent intent = getIntent();
        firstName = intent.getStringExtra(GenderActivity.EXTRA_FIRST_NAME);
        lastName = intent.getStringExtra(GenderActivity.EXTRA_LAST_NAME);
        date = intent.getStringExtra(GenderActivity.EXTRA_DATE_TEXT);
        gender = intent.getStringExtra(GenderActivity.EXTRA_GENDER_TEXT);
        blood = intent.getStringExtra(GenderActivity.EXTRA_BLOOD_TEXT);
        found= intent.getBooleanExtra(GenderActivity.EXTRA_FOUND,false);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(CommunicationDetails.this);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();



     nextButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {


             if(validatePhone()) {
                 phoneInput="+"+codePicker.getSelectedCountryCode()+phoneInput;
                 if (validateEmail()) {
                     confirmDialogBox();
                 } else {
                     return;
                 }
             }
             else
                 return;

         }
     });

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
        alertDialog(CommunicationDetails.this);


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
    public boolean validateEmail()
    {
        emailInput=textInputEmail.getText().toString().trim();

        if(emailInput.isEmpty())
        {
            textInputEmail.setError("Field can't be empty!");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches())
        {
            textInputEmail.setError("Please enter a valid email address!");
            return false;
        }
        else
        {
            textInputEmail.setError(null);
            return true;
        }

    }
    public boolean validatePhone()
    {
       phoneInput=textInputPhone.getText().toString().trim();
        if(phoneInput.isEmpty()) {
            textInputPhone.setError("Field can't be empty");
            return false;
        }
        else if(!Patterns.PHONE.matcher(phoneInput).matches())
        {
            textInputPhone.setError("Please enter a valid phone no!");
            return false;
        }
        else if(phoneInput.length()<10)
        {
            textInputPhone.setError("Please enter a valid phone no!");
            return false;
        }
        else
        {
            textInputPhone.setError(null);
            return true;
        }

    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your Details!")
                .setMessage("Are you sure you want to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                       openAddressActivity();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }

    public void openAddressActivity() {
        Intent intent = new Intent(getApplicationContext(), AddressActivity.class);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_LAST_NAME,lastName);
        intent.putExtra(EXTRA_GENDER_TEXT,gender);
        intent.putExtra(EXTRA_DATE_TEXT,date);
        intent.putExtra(EXTRA_BLOOD_TEXT,blood);
        intent.putExtra(EXTRA_PHONE_TEXT,phoneInput);
        intent.putExtra(EXTRA_EMAIL_TEXT,emailInput);
        intent.putExtra(EXTRA_FOUND,found);


        startActivity(intent);
        finish();
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

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }
}
