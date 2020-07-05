package com.example.getblood.Activities;

import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AddressActivity extends AppCompatActivity
{
    public  static final String EXTRA_FOUND="found";
    public static final String EXTRA_FIRST_NAME="com.example.getblood.Activities.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME="com.example.getblood.Activities.EXTRA_LAST_NAME";
    public static final String EXTRA_DATE_TEXT="com.example.getblood.Activities.EXTRA_DATE_TEXT";
    public static final String EXTRA_GENDER_TEXT="com.example.getblood.Activities.EXTRA_GENDER_TEXT";
    public static final String EXTRA_BLOOD_TEXT="com.example.getblood.Activities.EXTRA_BLOOD_TEXT";
    public static final String EXTRA_EMAIL_TEXT="com.example.getblood.Activities.EXTRA_EMAIL_TEXT";
    public static final String EXTRA_PHONE_TEXT="com.example.getblood.Activities.EXTRA_PHONE_TEXT";
    public static final String EXTRA_STATE_TEXT="com.example.getblood.Activities.EXTRA_STATE_TEXT";
    public static final String EXTRA_CITY_TEXT="com.example.getblood.Activities.EXTRA_CITY_TEXT";
    public static final String EXTRA_PIN_TEXT="com.example.getblood.Activities.EXTRA_PIN_TEXT";
    private Spinner spState,spCity;
    boolean  found;
    private String selectedCity,selectedState,pinCode;
    private TextInputEditText pinEditText;
    private Button nextMoreDetailsBitton;
    private String firstName,lastName,dob,gender,blood,phoneNo,emailId;
    // array lists
    // for the spinner in the format : City_no : City , State. Eg : 144 : New Delhi , India
    ArrayList<String> listSpinner=new ArrayList<String>();
    ArrayList<String> listStateSpinner = new ArrayList<String>();
    ArrayList<String> newStateListSpinner =new ArrayList<String>();
    // to store the city and state in the format : City , State. Eg: New Delhi , India

    AutoCompleteTextView act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkUserStatus();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("More Details");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog(AddressActivity.this);
            }
        });
        FloatingActionButton fab=findViewById(R.id.fab);
        fab.hide();
        Intent intent=getIntent();
        firstName=intent.getStringExtra(CommunicationDetails.EXTRA_FIRST_NAME);
        lastName=intent.getStringExtra(CommunicationDetails.EXTRA_LAST_NAME);
        dob=intent.getStringExtra(CommunicationDetails.EXTRA_DATE_TEXT);
        blood=intent.getStringExtra(CommunicationDetails.EXTRA_BLOOD_TEXT);
        gender=intent.getStringExtra(CommunicationDetails.EXTRA_GENDER_TEXT);
        phoneNo=intent.getStringExtra(CommunicationDetails.EXTRA_PHONE_TEXT);
        emailId=intent.getStringExtra(CommunicationDetails.EXTRA_EMAIL_TEXT);
        found=intent.getBooleanExtra(CommunicationDetails.EXTRA_FOUND,false);
        pinEditText=findViewById(R.id.pinEditText);
        nextMoreDetailsBitton=findViewById(R.id.moreDetailsNextButton);
        spCity=findViewById(R.id.spCity);
        spState=findViewById(R.id.spState);
        nextMoreDetailsBitton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCity=spCity.getSelectedItem().toString();
                selectedState=spState.getSelectedItem().toString();
                pinCode=pinEditText.getText().toString().trim();
                if(pinCode.isEmpty())
                {
                    pinEditText.setError("Field can't be empty");
                }
                else if (pinCode.length()<6)
                {
                    pinEditText.setError("Please enter the correct pin code");
                }
                else {
                    confirmDialogBox();
                }

            }
        });

        callAll();
    }

    public void callAll()
    {
        obj_list();
        addToSpinnerCity();
        addToSpinnerState();

    }

    // Get the content of cities.json from assets directory and store it as string
    public String getJson()
    {
        String json=null;
        try
        {
            // Opening cities.json file
            InputStream is = getAssets().open("cities.json");
            // is there any content in the file
            int size = is.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            is.read(buffer);
            // close the stream --- very important
            is.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return json;
        }
        return json;
    }

    // This add all JSON object's data to the respective lists
    void obj_list()
    {
        // Exceptions are returned by JSONObject when the object cannot be created
        try
        {
            // Convert the string returned to a JSON object
            JSONObject jsonObject=new JSONObject(getJson());
            // Get Json array
            JSONArray array=jsonObject.getJSONArray("array");
            // Navigate through an array item one by one
            for(int i=0;i<array.length();i++)
            {
                // select the particular JSON data
                JSONObject object=array.getJSONObject(i);
                String city=object.getString("name");
                String state=object.getString("state");
                // add to the lists in the specified format
                listSpinner.add(city);
                listStateSpinner.add(state);

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    // Add the data items to the spinner
    void addToSpinnerCity()
    {
        Collections.sort(listSpinner);
        Spinner spinner=(Spinner)findViewById(R.id.spCity);
        // Adapter for spinner
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,listSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    void addToSpinnerState()
    {
        Collections.sort(listStateSpinner);
        newStateListSpinner = removeDuplicates(listStateSpinner);
        Spinner spinner=(Spinner)findViewById(R.id.spState);
        // Adapter for spinner
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,newStateListSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
    public void confirmDialogBox()
    {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle("Confirm your Details!")
                .setMessage("Are you sure you want to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        openLoginPasswordActivity();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }
    public void openLoginPasswordActivity()
    {
        Intent intent=new Intent(AddressActivity.this,login_password.class);
        intent.putExtra(EXTRA_FIRST_NAME,firstName);
        intent.putExtra(EXTRA_LAST_NAME,lastName);
        intent.putExtra(EXTRA_GENDER_TEXT,gender);
        intent.putExtra(EXTRA_DATE_TEXT,dob);
        intent.putExtra(EXTRA_BLOOD_TEXT,blood);
        intent.putExtra(EXTRA_PHONE_TEXT,phoneNo);
        intent.putExtra(EXTRA_EMAIL_TEXT,emailId);
        intent.putExtra(EXTRA_FOUND,found);
        intent.putExtra(EXTRA_STATE_TEXT,selectedState);
        intent.putExtra(EXTRA_CITY_TEXT,selectedCity);
        intent.putExtra(EXTRA_PIN_TEXT,pinCode);
        startActivity(intent);
        finish();

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
        alertDialog(AddressActivity.this);


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

