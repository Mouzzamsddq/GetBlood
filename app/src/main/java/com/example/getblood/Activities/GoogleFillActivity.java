package com.example.getblood.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GoogleFillActivity extends AppCompatActivity {

    private static final Pattern Date_Pattern=Pattern.compile("^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$");
    private TextView gNameTextView,gEmailTextView;
    private Spinner gGenderSpinner,gBloodSpinner,gStateSpinner,gCitySpinner;
    private EditText gDoB,gContactNo,gPinCode;
    private CountryCodePicker gCcp;
    private Button gSubmitButton;
    private ImageView gProfileImageView;

    int cday, cmonth, cyear;
    String resultMessage;
    Integer userAge;
    Calendar currentDate;
    String date;

    ArrayList<String> listSpinner=new ArrayList<String>();
    ArrayList<String> listStateSpinner = new ArrayList<String>();
    ArrayList<String> newStateListSpinner =new ArrayList<String>();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_fill);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //init progress dialog
        progressDialog = new ProgressDialog(GoogleFillActivity.this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //navigation on toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 confirmDialogBox();
            }
        });



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();


        //init textView
        gNameTextView = findViewById(R.id.gUserNameTextView);
        gEmailTextView = findViewById(R.id.gUserEmailTextView);

        //init spinner
        gGenderSpinner = findViewById(R.id.gGenderSpinner);
        gBloodSpinner = findViewById(R.id.gBloodSpinner);
        gStateSpinner = findViewById(R.id.gSpinnerState);
        gCitySpinner = findViewById(R.id.gSpinnerCity);

        //init Button
        gSubmitButton = findViewById(R.id.gSubmitButton);


        //init edittext
        gDoB = findViewById(R.id.gDateEditText);
        gContactNo = findViewById(R.id.gContactEditText);
        gPinCode = findViewById(R.id.gPinEditText);

        //init ccp
        gCcp = findViewById(R.id.gCcp);

//        init profile Image View
          gProfileImageView = findViewById(R.id.gProfileImageView);

        //spinner blood data
        String[] bloodArray = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,bloodArray);
        gBloodSpinner.setAdapter(bloodAdapter);


        //Spinner gender data
        String[] genderArray = {"Male","Female","Others"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.custom_blood_spinner,R.id.textSpinner,genderArray);
        gGenderSpinner.setAdapter(genderAdapter);

        //Spinner

        //set userInfo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            gNameTextView.setText(user.getDisplayName());
            gEmailTextView.setText(user.getEmail());
            actionBar.setTitle(user.getDisplayName());
            actionBar.setSubtitle(user.getEmail());

            try
            {
                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.baseline_account_circle_black_48).into(gProfileImageView);
            }
            catch (Exception e)
            {
                gProfileImageView.setImageResource(R.drawable.baseline_account_circle_black_48);
            }
        }


        // call method to set the data in state and city spinner
        callAll();


        //when click username and useremail, toast will appear

        gNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GoogleFillActivity.this, "You can't edit this field !", Toast.LENGTH_SHORT).show();
            }
        });
        gEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GoogleFillActivity.this, "You can't edit this field !", Toast.LENGTH_SHORT).show();
            }
        });

        gProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GoogleFillActivity.this, "You can't change the profile picture here!", Toast.LENGTH_SHORT).show();
            }
        });


        gDoB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (gDoB.getRight() - gDoB.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        DatePickerDialog datePickerDialog = new DatePickerDialog(GoogleFillActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                                    gDoB.setText(resultMessage);
                                }
                                else {
                                    String resultMessage = gday + "/" + gmonth + "/" + gyear;
                                    gDoB.setText(resultMessage);
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

                            gDoB.setText(resultMessage);
                        } else {
                            gDoB.setText("Please select a valid birthday !");
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


        gSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                date=gDoB.getText().toString().trim();
                String pinCode = gPinCode.getText().toString().trim();
                String contact = gContactNo.getText().toString().trim();
                if(validateDate())
                {
                    if(validatePhone()) {
                        if (validatePinCode()) {
                            progressDialog.setMessage("updating...");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            String updatedContact = "+"+gCcp.getSelectedCountryCode() + contact;
                            String gender = gGenderSpinner.getSelectedItem().toString();
                            String blood = gBloodSpinner.getSelectedItem().toString();
                            String state = gStateSpinner.getSelectedItem().toString();
                            String city = gCitySpinner.getSelectedItem().toString();
                            uploadtoFirebase(date,pinCode,updatedContact,gender,blood,state,city);
                        }
                    }
                }
            }
        });





    }

    private void uploadtoFirebase(String date, String pinCode, String updatedContact, String gender, String blood, String state, String city) {



        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref  = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for(DataSnapshot ds1 : dataSnapshot.getChildren())
                       {
                           for(DataSnapshot ds2 : ds1.getChildren())
                           {
                               if(myUID.equals(ds2.getKey()))
                               {
                                   Map<String,Object> user = (HashMap<String, Object>)ds2.getValue();
                                   String userType = (String)user.get("userType");
                                   Map<String,Object> data  = new HashMap<>();
                                   data.put("DateOfBirth", date);
                                   data.put("Blood", blood);
                                   data.put("Gender", gender);
                                   data.put("Contact", updatedContact);
                                   data.put("state",state);
                                   data.put("city",city);
                                   data.put("pin",pinCode);
                                   if(userType!= null && userType.equals("Donor"))
                                   {
                                        ref.child("Donor").child(myUID).child("DateOfBirth").setValue(date);
                                       ref.child("Donor").child(myUID).child("Blood").setValue(blood);
                                       ref.child("Donor").child(myUID).child("Gender").setValue(gender);
                                       ref.child("Donor").child(myUID).child("Contact").setValue(updatedContact);
                                       ref.child("Donor").child(myUID).child("state").setValue(state);
                                       ref.child("Donor").child(myUID).child("city").setValue(city);
                                       ref.child("Donor").child(myUID).child("pin").setValue(pinCode)
                                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {

                                                       progressDialog.dismiss();
                                                       Toast.makeText(GoogleFillActivity.this, "Data successfully updated...!", Toast.LENGTH_SHORT).show();
                                                       startActivity(new Intent(GoogleFillActivity.this,HomeActivity.class));
                                                       finish();

                                                   }
                                               }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               progressDialog.dismiss();
                                               Toast.makeText(GoogleFillActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                           }
                                       });

                                   }
                                   else {

                                       ref.child("Recipient").child(myUID).child("DateOfBirth").setValue(date);
                                       ref.child("Recipient").child(myUID).child("Blood").setValue(blood);
                                       ref.child("Recipient").child(myUID).child("Gender").setValue(gender);
                                       ref.child("Recipient").child(myUID).child("Contact").setValue(updatedContact);
                                       ref.child("Recipient").child(myUID).child("state").setValue(state);
                                       ref.child("Recipient").child(myUID).child("city").setValue(city);
                                       ref.child("Recipient").child(myUID).child("pin").setValue(pinCode)
                                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {

                                                       progressDialog.dismiss();
                                                       Toast.makeText(GoogleFillActivity.this, "Data successfully updated...!", Toast.LENGTH_SHORT).show();
                                                       startActivity(new Intent(GoogleFillActivity.this, HomeActivity.class));
                                                       finish();

                                                   }
                                               }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               progressDialog.dismiss();
                                               Toast.makeText(GoogleFillActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                   }



                               }
                           }
                       }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressDialog.dismiss();
                Toast.makeText(GoogleFillActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    public boolean validateDate()
    {
        String dateInput=gDoB.getText().toString().trim();

        if(!date.isEmpty()) {
            if(Date_Pattern.matcher(dateInput).matches()) {


                String[] dateArr = date.split("/");
                userAge = cyear - Integer.parseInt(dateArr[2]);
                Log.i("UserAge", String.valueOf(userAge));
            }
        }


        if(dateInput.isEmpty())
        {
            gDoB.setError("Field can't be empty");
            return false;
        }else if(!Date_Pattern.matcher(dateInput).matches())
        {
            gDoB.setError("Date must be in this pattern : dd/mm/yyyy");
            return false;
        }
        else if(userAge<18)
        {
            gDoB.setError("User must be above 18!");
            return false;
        }
        else
        {
            gDoB.setError(null);
            return true;
        }
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
        Spinner spinner=(Spinner)findViewById(R.id.gSpinnerCity);
        // Adapter for spinner
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,listSpinner);
        spinner.setAdapter(adapter);
    }
    void addToSpinnerState()
    {
        Collections.sort(listStateSpinner);
        newStateListSpinner = removeDuplicates(listStateSpinner);
        Spinner spinner=(Spinner)findViewById(R.id.gSpinnerState);
        // Adapter for spinner
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,newStateListSpinner);
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
                .setTitle("Google...!")
                .setMessage("Please fill all the details...!")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {




                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
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
        checkGoogleFill();


    }


    private void checkGoogleFill()
    {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2: ds1.getChildren())
                    {
                        if(myUID != null  && myUID.equals(ds2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String, Object>)ds2.getValue();
                            String Register = (String)data.get("Register");
                            if(Register!= null  && Register.equals("google"))
                            {
                                String pinCode = (String )data.get("pin");
                                if(pinCode.equals(""))
                                {

                                   confirmDialogBox();
                                }
                                else
                                {
                                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                    finish();
                                }

                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private boolean validatePinCode()
    {
        String pinCode=gPinCode.getText().toString().trim();
        if(pinCode.isEmpty())
        {
            gPinCode.setError("Field can't be empty");
            return false;
        }
        else if (pinCode.length()<6)
        {
            gPinCode.setError("Please enter the correct pin code");
            return false;
        }
        else {
            gPinCode.setError(null);
            return true;
        }
    }


    public boolean validatePhone()
    {
        String phoneInput=gContactNo.getText().toString().trim();
        if(phoneInput.isEmpty()) {
            gContactNo.setError("Field can't be empty");
            return false;
        }
        else if(!Patterns.PHONE.matcher(phoneInput).matches())
        {
            gContactNo.setError("Please enter a valid phone no!");
            return false;
        }
        else if(phoneInput.length()<10)
        {
            gContactNo.setError("Please enter a valid phone no!");
            return false;
        }
        else
        {
            gContactNo.setError(null);
            return true;
        }

    }


}
