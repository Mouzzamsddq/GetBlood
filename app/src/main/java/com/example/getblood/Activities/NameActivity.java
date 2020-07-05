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

import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.jar.Attributes;
import java.util.regex.Pattern;

public class NameActivity extends AppCompatActivity {
    private static final Pattern NAME_PATTERN=Pattern.compile("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    protected TextInputEditText firstNameEditText;
    protected TextInputEditText lastNameEditText;
    protected  String firstNameInput;
    protected  String lastNameInput;
    private boolean found;
    public static  final String EXTRA_FOUND="found";
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
               alertDialog(NameActivity.this);
            }
        });
        firstNameEditText=findViewById(R.id.firstNameEditText);
        lastNameEditText=findViewById(R.id.lastNameEditText);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Name");
        Intent foundIntent=getIntent();
        found=foundIntent.getBooleanExtra(Createaccount.EXTRA_FOUND,found);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
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
        alertDialog(NameActivity.this);


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
    public boolean validateFirstName()
    {
        firstNameInput=firstNameEditText.getText().toString().trim();
        if(firstNameInput.isEmpty()) {
            firstNameEditText.setError("Field can't be empty");
            return false;
        }
        else if(!NAME_PATTERN.matcher(firstNameInput).matches())
        {
            firstNameEditText.setError("Please enter alphabets only!");
            return false;
        }
        else
        {
            firstNameEditText.setError(null);
            return true;
        }


    }
    public boolean validateLastName() {
        lastNameInput = lastNameEditText.getText().toString().trim();
        if (lastNameInput.isEmpty()) {
            lastNameEditText.setError("Field can't be empty");
            return false;
        } else if (!NAME_PATTERN.matcher(lastNameInput).matches()) {
            lastNameEditText.setError("Please enter alphabets only!");
            return false;
        } else {
            lastNameEditText.setError(null);
            return true;
        }
    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your Name..!")
                .setMessage("Is "+firstNameEditText.getText().toString().trim()+" "+lastNameEditText.getText().toString().trim()+" your name?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        openDateOfBirthActivity();
                        //close();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }
    public void nextButton(View v)
    {
        if(validateFirstName())
        {
            if(validateLastName())
            {
              confirmDialogBox();
            }
            else
                return;
        }
        else
            return;
    }
    public void  openDateOfBirthActivity()
    {
        TextInputEditText editTextFirstName=findViewById(R.id.firstNameEditText);
        String firstName=editTextFirstName.getText().toString();
        TextInputEditText editTextLastName=findViewById(R.id.lastNameEditText);
        String lastName=editTextLastName.getText().toString();
        Intent intent=new Intent(this,DateOfBirth.class);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_LAST_NAME,lastName);
        intent.putExtra(EXTRA_FOUND,found);
        startActivity(intent);
        finish();

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


}
