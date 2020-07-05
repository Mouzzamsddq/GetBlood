package com.example.getblood.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.places.model.PlaceSearchRequestParams;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.NotActiveException;

public class GenderActivity extends AppCompatActivity {
    RadioGroup radioGroup;
    RadioButton radioButton;
    private boolean  found ;
    public static  final  String EXTRA_FOUND="found";
    String firstName,lastName,date,radioButtonText,blood;
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";
    public static final String EXTRA_DATE_TEXT="com.example.getblood.Activities.EXTRA_DATE_TEXT";
    public static final String EXTRA_GENDER_TEXT="com.example.getblood.Activities.EXTRA_GENDER_TEXT";
    public static final String EXTRA_BLOOD_TEXT="com.example.getblood.Activities.EXTRA_BLOOD_TEXT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        radioGroup=findViewById(R.id.gender_radio_group);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(GenderActivity.this);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        Intent intent=getIntent();
        firstName=intent.getStringExtra(SelectBlood.EXTRA_FIRST_NAME);
        lastName=intent.getStringExtra(SelectBlood.EXTRA_LAST_NAME);
        date=intent.getStringExtra(SelectBlood.EXTRA_DATE_TEXT);
        blood=intent.getStringExtra(SelectBlood.EXTRA_BLOOD_TEXT);
        found=intent.getBooleanExtra(SelectBlood.EXTRA_FOUND,false);

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {
        alertDialog(GenderActivity.this);


    }
    public void checkedButton(View v)
    {
        int checkedRadioButtonId=radioGroup.getCheckedRadioButtonId();
        radioButton=findViewById(checkedRadioButtonId);
        radioButtonText=radioButton.getText().toString();
        Toast.makeText(this,"You selected:"+radioButton.getText().toString(), Toast.LENGTH_SHORT).show();
    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your gender..!")
                .setMessage("Are you sure want to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        openCommunicationDetails();
                        //close();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                }).show();
    }
    public void nextButton(View view)
    {
        confirmDialogBox();
    }
    public void openCommunicationDetails()
    {
        Intent intent=new Intent(getApplicationContext(),CommunicationDetails.class);
        intent.putExtra(EXTRA_GENDER_TEXT,radioButtonText);
        intent.putExtra(EXTRA_DATE_TEXT,date);
        intent.putExtra(EXTRA_LAST_NAME, lastName);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_BLOOD_TEXT,blood);
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
