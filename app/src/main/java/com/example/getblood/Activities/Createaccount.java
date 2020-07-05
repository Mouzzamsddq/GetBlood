package com.example.getblood.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.jar.Attributes;

public class Createaccount extends AppCompatActivity {
    private Context context;
    private Button nextButton;
    private boolean found;
    public Createaccount activity;
    public static final String EXTRA_FOUND = "found";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nextButton = findViewById(R.id.nextCreateButton);
        actionBar.setTitle("Create account");
        Intent foundIntent = getIntent();
        found = foundIntent.getBooleanExtra(LoginViaEmail.EXTRA_FOUND, false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(Createaccount.this);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent nameIntent = new Intent(Createaccount.this, NameActivity.class);
                nameIntent.putExtra(EXTRA_FOUND, found);
                startActivity(nameIntent);
            }
        });


    }

    public void alreadyHaveAccount(View v) {
        startActivity(new Intent(getApplicationContext(), LogInOptions.class));
        finish();
    }

    void alertDialog(final Context context) {
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
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
        alertDialog(Createaccount.this);


    }
}
