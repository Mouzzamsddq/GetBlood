package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity implements TextWatcher {
    public static final String HELLO = "HELLO";
    EditText ed1, ed2, ed3, ed4, ed5, ed6;
    String verificationId;
    Button verifyButton;
    TextView timerTextView;
    TextView otpSentTextView, resendOtpView;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    ProgressDialog pd;
    private boolean check=false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationStateChangedCallbacks;
    PhoneAuthProvider.ForceResendingToken token;
    String s = "XXXXXXX";
    String userOtp;
    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Contact Verification");
        verifyButton = findViewById(R.id.verifyButton);
        resendOtpView = findViewById(R.id.resendOtpView);
        timerTextView = findViewById(R.id.timerTextView);
        mAuth = FirebaseAuth.getInstance();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CommunicationDetails.class));
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage("Sending...");
        pd.setCanceledOnTouchOutside(true);
        ed1 = findViewById(R.id.ed1);
        ed2 = findViewById(R.id.ed2);
        ed3 = findViewById(R.id.ed3);
        ed4 = findViewById(R.id.ed4);
        ed5 = findViewById(R.id.ed5);
        ed6 = findViewById(R.id.ed6);
        otpSentTextView = findViewById(R.id.otpSentView);
        ed1.addTextChangedListener(this);
        ed2.addTextChangedListener(this);
        ed3.addTextChangedListener(this);
        ed4.addTextChangedListener(this);
        ed5.addTextChangedListener(this);
        ed6.addTextChangedListener(this);
        resendOtpView.setEnabled(false);
        progressBar = findViewById(R.id.progress_bar);
        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra(CommunicationDetails.EXTRA_PHONE_TEXT);
        char[] phoneArr = phoneNumber.toCharArray();
        for (int i = phoneArr.length - 3; i < phoneArr.length; i++) {
            s = s + phoneArr[i];
        }
        Log.d(HELLO, "Editted String-->" + s);
//        requestOTP(phoneNumber, s);
//        verifyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressBar.setVisibility(View.VISIBLE);
//                userOtp = ed1.getText().toString() + ed2.getText().toString() + ed3.getText().toString() + ed4.getText().toString() + ed5.getText().toString() + ed6.getText().toString();
//
//                if (!userOtp.isEmpty() && userOtp.length() == 6) {
//                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, userOtp);
//                    verifyAuth(credential);
//                } else {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(OTPActivity.this, "Please enter a valid OTP!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        resendOtpView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ed1.setEnabled(true);
//                ed2.setEnabled(true);
//                ed3.setEnabled(true);
//                ed4.setEnabled(true);
//                ed5.setEnabled(true);
//                ed6.setEnabled(true);
//                verifyButton.setEnabled(true);
//                resendVerificationCode(phoneNumber, token);
//
//            }
//        });
//
    }

//    public void updateTimer(int secondLeft) {
//        check=false;
//        int minutes = secondLeft / 60;
//        int seconds = secondLeft - minutes * 60;
//        String secondString = Integer.toString(seconds);
//        if (seconds <= 9) {
//            secondString = "0" + secondString;
//        }
//        if (secondLeft < 10) {
//            timerTextView.setTextColor(getResources().getColor(R.color.colorBlood));
//            timerTextView.setText(Integer.toString(minutes) + ":" + secondString);
//        }
//        timerTextView.setText(Integer.toString(minutes) + ":" + secondString);
//    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 1) {
            if (ed1.length() == 1) {
                ed2.requestFocus();
            }
            if (ed2.length() == 1) {
                ed3.requestFocus();
            }
            if (ed3.length() == 1) {
                ed4.requestFocus();
            }
            if (ed4.length() == 1) {
                ed5.requestFocus();
            }
            if (ed5.length() == 1) {
                ed6.requestFocus();
            }
        } else if (s.length() == 0) {
            if (ed6.length() == 0) {
                ed5.requestFocus();
            }
            if (ed5.length() == 0) {
                ed4.requestFocus();
            }
            if (ed4.length() == 0) {
                ed3.requestFocus();
            }
            if (ed3.length() == 0) {
                ed3.requestFocus();
            }
            if (ed2.length() == 0) {
                ed1.requestFocus();
            }
        }

    }
//
//    public void requestOTP(String phoneNum, String setText) {
//        pd.show();
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNum, 60L, TimeUnit.SECONDS, this, verificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            @Override
//            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);
//                verificationId = s;
//                token = forceResendingToken;
//                pd.dismiss();
//                timerTextView.setVisibility(View.VISIBLE);
//                countDownTimer = new CountDownTimer(60000, 1000) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//
//                        updateTimer((int) millisUntilFinished / 1000);
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        timerTextView.setVisibility(View.INVISIBLE);
//                        ed1.setEnabled(false);
//                        ed2.setEnabled(false);
//                        ed3.setEnabled(false);
//                        ed4.setEnabled(false);
//                        ed5.setEnabled(false);
//                        ed6.setEnabled(false);
//                        timerTextView.setTextColor(getResources().getColor(R.color.colorBlack));
//                    }
//                }.start();
//                otpSentTextView.setVisibility(View.VISIBLE);
//                otpSentTextView.setText("OTP code has been sent to " + setText);
//
//
//            }
//
//            @Override
//            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
//                super.onCodeAutoRetrievalTimeOut(s);
//                Toast.makeText(OTPActivity.this, "Time Out!", Toast.LENGTH_SHORT).show();
//                verifyButton.setEnabled(false);
//                resendOtpView.setEnabled(true);
//
//            }
//
//            @Override
//            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//
//            }
//
//            @Override
//            public void onVerificationFailed(@NonNull FirebaseException e) {
//                Toast.makeText(OTPActivity.this, "OTP could not be send to this number!" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void verifyAuth(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    timerTextView.setVisibility(View.INVISIBLE);
//                    Toast.makeText(OTPActivity.this, "phone number verified!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent();
//                    intent.putExtra("Button_text", "verified");
//                    setResult(RESULT_OK, intent);
//                    finish();
//                } else {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(OTPActivity.this, "Invalid OTP!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    private void resendVerificationCode(String phoneNumber,
//                                        PhoneAuthProvider.ForceResendingToken token) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                phoneNumber,        // Phone number to verify
//                60,                 // Timeout duration
//                TimeUnit.SECONDS,   // Unit of timeout
//                this,               // Activity (for callback binding)
//                verificationStateChangedCallbacks,         // OnVerificationStateChangedCallbacks
//                token);
//
//
//    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {
        startActivity(new Intent(OTPActivity.this,CommunicationDetails.class));
        finish();


    }


}