package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPassword extends AppCompatActivity {
    Button resetbtn;
    EditText useremail;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Reset Password");
        FloatingActionButton fab = findViewById(R.id.fab);
         fab.hide();


        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        useremail = findViewById(R.id.resetEmailEditText);
        findViewById(R.id.resetPasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = mAuth.getCurrentUser();

                final String email = useremail.getText().toString();

                if(validateEmail())
                {
                    pd.show();
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getApplicationContext(), "We have sent an email to "+" '"+ email +"'. Please check your email.", Toast.LENGTH_LONG)
                                                .show();
                                        startActivity(new Intent(getApplicationContext(), LoginViaEmail.class));
                                        //useremail.setText(null);
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(), "Sorry, There is something went wrong. please try again some time later.", Toast.LENGTH_LONG)
                                                .show();
                                        useremail.setText(null);
                                    }
                                    pd.dismiss();
                                }
                            });
                }
            }
        });
    }
    public boolean validateEmail()
    {
        String emailInput=useremail.getText().toString().trim();
        if(emailInput.isEmpty()) {
            useremail.setError("Field can't be empty,email required!");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches())
        {
            useremail.setError("Please enter a valid email address!");
            return false;
        }
        else
        {
            useremail.setError(null);
            return true;
        }

    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}
