package com.example.getblood.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.regex.Pattern;

public class LoginViaEmail extends AppCompatActivity {
    private Button loginViaEmailButton;
    FirebaseAuth mAuth;
    ProgressDialog pd;
    RelativeLayout rl;
    public static final String EXTRA_FOUND="boolean";
//    private static final Pattern PASSWORD_PATTERN =
//            Pattern.compile("^" +
//                    "(?=.*[0-9])" +         //at least 1 digit
//                    "(?=.*[a-z])" +         //at least 1 lower case letter
//                    "(?=.*[A-Z])" +         //at least 1 upper case letter
//                    //"(?=.*[a-zA-Z])" +      //any letter
//                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
//                    "(?=\\S+$)" +           //no white spaces
//                    ".{6,}" +               //at least 4 characters
//                    "$");

    TextInputEditText textInputEmail;
    TextInputEditText textInputPassword;
    private boolean found;
    public void signUpButton(View v)
    {
        Intent createAccount=new Intent(getApplicationContext(),Createaccount.class);
        createAccount.putExtra(EXTRA_FOUND,found);
        startActivity(createAccount);

        finish();

    }



    public void forgottenPassword(View view)
    {
        Intent mainPage=new Intent(getApplicationContext(), ForgotPassword.class);
        startActivity(mainPage);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_via_email);
        checkUserStatus();
        rl = findViewById(R.id.relative);
        textInputEmail=findViewById(R.id.emailEditText);
        textInputPassword=findViewById(R.id.passwordEditText);
        loginViaEmailButton=findViewById(R.id.loginViaEmailButton);
        found=getIntent().getBooleanExtra(LogInOptions.EXTRA_FOUND,false);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        mAuth = FirebaseAuth.getInstance();
        loginViaEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = textInputEmail.getText().toString();
                final String password = textInputPassword.getText().toString();
                try {
                    if (validateEmail() && validatePassword()) {
                        if (checkConnection()) {

                            pd.show();
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(LoginViaEmail.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (!task.isSuccessful()) {
                                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                    Toast.makeText(LoginViaEmail.this, "Invalid Password!", Toast.LENGTH_SHORT).show();

                                                } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {

                                                    Toast.makeText(LoginViaEmail.this, "Email not in use", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(LoginViaEmail.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            pd.dismiss();
                                        }
                                    });
                        }
                        } else {
                            Toast.makeText(getApplicationContext(), "Please fill all the field.", Toast.LENGTH_LONG).show();
                        }

                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }


        });

    }
    public boolean validateEmail()
    {
        String emailInput=textInputEmail.getText().toString().trim();
        if(emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty");
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
    public boolean validatePassword()
    {
        String passwordInput=textInputPassword.getText().toString().trim();
        if(passwordInput.isEmpty())
        {
            textInputPassword.setError("Field can't be empty");
            return false;
        }
        else
        {
            textInputPassword.setError(null);
            return true;
        }
    }
    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null )
        {
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }
    }
    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }


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
            Snackbar sb = Snackbar.make(rl,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }

}
