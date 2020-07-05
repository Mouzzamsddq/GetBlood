package com.example.getblood.Activities;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SelectBlood extends AppCompatActivity {
    private Spinner bloodGroup;
    Button nextBloodButton;
    private boolean found;
    public static final String EXTRA_FOUND="found";
    private String blood,firstName,lastName,date;
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";
    public static final String EXTRA_DATE_TEXT="com.example.getblood.Activities.EXTRA_DATE_TEXT";
    public static final String EXTRA_BLOOD_TEXT="com.example.getblood.Activities.EXTRA_BLOOD_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_blood);
        Toolbar toolbar = findViewById(R.id.toolbar);
        checkUserStatus();
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        bloodGroup=findViewById(R.id.inputBloodGroup);
        nextBloodButton=findViewById(R.id.nextBloodButton);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   alertDialog(SelectBlood.this);
            }
        });
        Intent intent=getIntent();
        firstName=intent.getStringExtra(DateOfBirth.EXTRA_FIRST_NAME);
        lastName=intent.getStringExtra(DateOfBirth.EXTRA_LAST_NAME);
        date=intent.getStringExtra(DateOfBirth.EXTRA_DATE_TEXT);
        found=intent.getBooleanExtra(DateOfBirth.EXTRA_FOUND,false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Blood Group");
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        nextBloodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int BloodGroup = bloodGroup.getSelectedItemPosition();
                blood = bloodGroup.getSelectedItem().toString();
                confirmDialogBox();
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
        alertDialog(SelectBlood.this);


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
                Intent intent = new Intent(getApplicationContext(), LogInOptions.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your Blood Group..!")
                .setMessage("Is "+blood+" your blood group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        openGenderActivity();
                        //close();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }
    public void openGenderActivity()
    {
        Intent intent= new Intent(getApplicationContext(),GenderActivity.class);
        intent.putExtra(EXTRA_BLOOD_TEXT,blood);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_LAST_NAME,lastName);
        intent.putExtra(EXTRA_DATE_TEXT,date);
        intent.putExtra(EXTRA_FOUND,found);
        startActivity(intent);
    }

    private void checkUserStatus()
    {
         FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
         if(user != null)
         {
             startActivity(new Intent(getApplicationContext() ,HomeActivity.class));
             finish();
         }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }
}
