package com.example.getblood.Activities;

import android.app.DatePickerDialog;
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

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.regex.Pattern;

public class DateOfBirth extends AppCompatActivity {
    private static final Pattern Date_Pattern=Pattern.compile("^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$");
    Button nextDateButton;
    Calendar currentDate;
    String date;
    private  boolean found;
    int cday, cmonth, cyear;
    String resultMessage;
    Integer userAge;
    String firstName,lastNAme;
    TextInputEditText dateEditText;
    public static  final String EXTRA_FOUND="found";
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";
    public static final String EXTRA_DATE_TEXT="com.example.getblood.Activities.EXTRA_DATE_TEXT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_of_birth2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        checkUserStatus();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Date Of Birth");
        nextDateButton=findViewById(R.id.nextDateButton);
        dateEditText=findViewById(R.id.dateEditText);
        Intent foundIntent=getIntent();
        found=foundIntent.getBooleanExtra(NameActivity.EXTRA_FOUND,false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog(DateOfBirth.this);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        //DatePicker Dialog code
        dateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (dateEditText.getRight() - dateEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        DatePickerDialog datePickerDialog = new DatePickerDialog(DateOfBirth.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int gyear, int gmonth, int gday) {
                                gmonth += 1;

                                if(gday<10 || gmonth<10)
                                {
                                    String gdays,gmonths;
                                    gdays=String.valueOf(gday);
                                    gmonths=String.valueOf(gmonth);
                                    if(gday<10) {
                                        gdays = "0" + gdays;
                                    }
                                    if(gmonth<10) {
                                        gmonths = "0" + gmonths;
                                    }
                                    resultMessage=gdays+"/"+gmonths+"/"+gyear;
                                    dateEditText.setText(resultMessage);
                                }
                                else {
                                    String resultMessage = gday + "/" + gmonth + "/" + gyear;
                                    dateEditText.setText(resultMessage);
                                }
                                if (gyear != 0 && gmonth != 0 && gday != 0 && cyear > gyear) {
                                    userAge = cyear - gyear;
                                }
                            /*int agem = cmonth - gmonth;
                            int aged = cday - gday;
                            aged = aged < 0 ? (aged * -1) : aged;
                            String resultMessage = "";
                            if (agem < 0) {
                                resultMessage = (agem * -1) + " month\nand " + aged + " days to complete\n" + agey + " years old";
                            } else {
                                resultMessage = "Your age is: " + agey + " years old\n" + agem + " month and " + aged + " days.";
                            }

                            dateEditText.setText(resultMessage);
                        } else {
                            dateEditText.setText("Please select a valid birthday !");
                        }*/
                            }
                        }, cyear, cmonth-1, cday);
                        datePickerDialog.show();



                        return true;
                    }
                }
                return false;
            }
        });
        currentDate = Calendar.getInstance();
        cday = currentDate.get(Calendar.DAY_OF_MONTH);
        cmonth = currentDate.get(Calendar.MONTH) + 1;
        cyear = currentDate.get(Calendar.YEAR);
        Intent intent=getIntent();
        firstName=intent.getStringExtra(NameActivity.EXTRA_FIRST_NAME);
        lastNAme=intent.getStringExtra(NameActivity.EXTRA_LAST_NAME);


        nextDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 date=dateEditText.getText().toString().trim();

                if(validateDate())
                {

                    confirmDialogBox();
                }
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
        alertDialog(DateOfBirth.this);


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
    public boolean validateDate()
    {
        if(!date.isEmpty()) {
            String[] dateArr = date.split("/");
            userAge = cyear - Integer.parseInt(dateArr[2]);
            Log.i("UserAge", String.valueOf(userAge));
        }

        String dateInput=dateEditText.getText().toString().trim();
        if(dateInput.isEmpty())
        {
            dateEditText.setError("Field can't be empty");
            return false;
        }else if(!Date_Pattern.matcher(dateInput).matches())
        {
            dateEditText.setError("Date must be in this pattern : dd/mm/yyyy");
            return false;
        }
        else if(userAge<18)
        {
            dateEditText.setError("User must be above 18!");
            return false;
        }
        else
        {
            dateEditText.setError(null);
            return true;
        }
    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your DOB..!")
                .setMessage("Is "+date+" your date of birth?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        openSelectBloodActivity();
                        //close();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }
    public void openSelectBloodActivity(){
        TextInputEditText dateEditText=findViewById(R.id.dateEditText);
        String date=dateEditText.getText().toString();
        Intent intent=new Intent(getApplicationContext(),SelectBlood.class);
        intent.putExtra(EXTRA_DATE_TEXT,date);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_LAST_NAME,lastNAme);
        intent.putExtra(EXTRA_FOUND,found);
        startActivity(intent);
        finish();
    }


    private  void checkUserStatus()
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
