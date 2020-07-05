package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.Fragment.Chat_list_fragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class PostActivity extends AppCompatActivity {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 9001;
    private Spinner customBloodSpinner,userTypeSpinner,requestTypeSpinner;
    private ActionBar actionBar;
    private EditText desEditText,locEditText,contactEditText,titleEditText;
    private Button postButton;
    private ScrollView scView;
    private CountryCodePicker countryCodePicker;
    private DatabaseReference reference;
    private FirebaseUser user;

    //progressBar
    private ProgressDialog progressDialog;

    //userinfo
    String name,uid,email,dp;
    ArrayAdapter<String> bloodAdapter;
    //info of post to be editted
    String editTitle,editDescription,editUserType,editPostType,editBloodGroup,editContactNo,editLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = findViewById(R.id.toolbar);
        postButton = findViewById(R.id.postButton);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add new post");
        actionBar.setSubtitle(email);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        scView = findViewById(R.id.scView);
        FloatingActionButton fab = findViewById(R.id.fab);
       fab.hide();
        checkUserStatus();


        // get data through intent from previous activity's adapter
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        //validate if we came here to update i.e. came fro adapterPost
        if(isUpdateKey.equals("editPost"))
        {
            //update
            actionBar.setTitle("Update Post");
            postButton.setText("Update");
            loadPostData(editPostId);

        }
        else
        {
            //add
            actionBar.setTitle("Add New Post");
            postButton.setText("post");

        }

        progressDialog=new ProgressDialog(PostActivity.this);
        customBloodSpinner=findViewById(R.id.inputBloodGroup);
        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        titleEditText=findViewById(R.id.postTitle);
        requestTypeSpinner = findViewById(R.id.requestTypeSpinner);
        desEditText=findViewById(R.id.descriptionEditText);
        locEditText=findViewById(R.id.locationEditText);
        countryCodePicker=findViewById(R.id.postCcp);
        user = FirebaseAuth.getInstance().getCurrentUser();
        contactEditText=findViewById(R.id.postContactEditText);
        String[] userTypeArray = {"Donor","Recipient"};
        String [] requestTypeArray = {"want to donate","need blood"};
        ArrayAdapter<String> requestTypeAdapter = new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,requestTypeArray);
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,userTypeArray);
        String[] bloodArray = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};
        bloodAdapter = new ArrayAdapter<String>(this,R.layout.custom_blood_spinner,R.id.textSpinner,bloodArray);
        customBloodSpinner.setAdapter(bloodAdapter);
        userTypeSpinner.setAdapter(userTypeAdapter);
        requestTypeSpinner.setAdapter(requestTypeAdapter);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkConnection()) {
                    String title = titleEditText.getText().toString().trim();
                    String description = desEditText.getText().toString().trim();
                    String contact = contactEditText.getText().toString().trim();
                    String location = locEditText.getText().toString().trim();
                    String blood = customBloodSpinner.getSelectedItem().toString().trim();
                    String userType = userTypeSpinner.getSelectedItem().toString().trim();
                    String requestType = requestTypeSpinner.getSelectedItem().toString().trim();


                    if (isUpdateKey.equals("editPost")) {
                        if (validateFields(title, contact, location)) {
                            contact = "+" + countryCodePicker.getSelectedCountryCode() + contact;
                            beginUpdate(title, description, contact, location, blood, userType, requestType, editPostId);
                        }
                    } else if (validateFields(title, contact, location)) {
                        contact = "+" + countryCodePicker.getSelectedCountryCode() + contact;
                        uploadData(title, description, contact, location, blood, userType, requestType);
                        Toast.makeText(PostActivity.this, "Succsessful", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        // get some info of current user to include in post
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(user.getUid().equals(ds2.getKey()))
                        {
                            ModelUser data = ds2.getValue(ModelUser.class);
                            name = data.getFullName();
                            email= data.getEmailId();
                            dp = data.getProfileImage();
                            uid = data.getUID();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }

    private void beginUpdate(String title, String description, String contact, String location, String blood, String userType, String requestType,String editPostId) {
        progressDialog.setMessage("Updating post...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        // upload post to firebase database
        HashMap<String,Object> postHashMap = new HashMap<>();
        //put post info
        postHashMap.put("pTitle",title);
        postHashMap.put("pDescr",description);
        postHashMap.put("pLoc",location);
        postHashMap.put("contact",contact);
        postHashMap.put("uType",userType);
        postHashMap.put("reqType",requestType);
        postHashMap.put("blood",blood);

        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //put data in this ref
        ref.child(editPostId).updateChildren(postHashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "post updated...", Toast.LENGTH_SHORT).show();


                        //change title of action bar
                        actionBar.setTitle("Add new post");
                        //reset views
                        titleEditText.setText("");
                        desEditText.setText("");
                        locEditText.setText("");
                        contactEditText.setText("");
                        countryCodePicker.setCountryForPhoneCode(91);

                        // send notification
                        prepareNotification(""+editPostId, //since we are using timestamp for post id
                                ""+name+" added new post",
                                ""+title+"\n"+description,
                                "PostNotification",
                                "POST");

                        startActivity(new Intent(PostActivity.this,HomeActivity.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadPostData(String editPostId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //get data of post using id of post

        Query query = ref.orderByChild("pID").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    // get data
                    ModelPost data = ds.getValue(ModelPost.class);
                    editTitle = data.getpTitle();
                    editDescription = data.getpDescr();
                    editContactNo = data.getContact();
                    editUserType = data.getuType();
                    editPostType = data.getReqType();
                    editBloodGroup = data.getBlood();
                    editLocation = data.getpLoc();

                    String phone="";
                    char[] charArray=editContactNo.toCharArray();
                    for(int i=3;i<13;i++)
                    {
                        phone+=charArray[i];
                    }

                    //set data
                    desEditText.setText(editDescription);
                    locEditText.setText(editLocation);
                    contactEditText.setText(phone);
                    titleEditText.setText(editTitle);
                    if(editBloodGroup != null)
                    {
                        int spinnerPosition = bloodAdapter.getPosition(editBloodGroup);
                        customBloodSpinner.setSelection(spinnerPosition);
                    }
                    if(editUserType != null)
                    {
                        int spinnerPosition = bloodAdapter.getPosition(editUserType);
                        userTypeSpinner.setSelection(spinnerPosition);
                    }
                    if(editPostType != null)
                    {
                        int spinnerPosition = bloodAdapter.getPosition(editPostType);
                        requestTypeSpinner.setSelection(spinnerPosition);
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadData(String title, String description, String contact, String location, String blood, String userType, String requestType) {
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        //for post_id and post publish time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        // upload post to firebase database
        HashMap<String,Object> postHashMap = new HashMap<>();
        //put post info
        postHashMap.put("uid",uid);
        postHashMap.put("uName",name);
        postHashMap.put("uEmail",email);
        postHashMap.put("uDp",dp);
        postHashMap.put("pID",timeStamp);
        postHashMap.put("pTitle",title);
        postHashMap.put("pDescr",description);
        postHashMap.put("pLoc",location);
        postHashMap.put("contact",contact);
        postHashMap.put("uType",userType);
        postHashMap.put("reqType",requestType);
        postHashMap.put("blood",blood);
        postHashMap.put("pInterest","0");

        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //put data in this ref
        ref.child(timeStamp).setValue(postHashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added in database
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        //reset views
                        titleEditText.setText("");
                        desEditText.setText("");
                        locEditText.setText("");
                        contactEditText.setText("");
                        countryCodePicker.setCountryForPhoneCode(91);

                        // send notification
                        prepareNotification(""+timeStamp, //since we are using timestamp for post id
                                             ""+name+" added new post",
                                              ""+title+"\n"+description,
                                              "PostNotification",
                                              "POST");

                        startActivity(new Intent(PostActivity.this,HomeActivity.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed adding post in database
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu,menu);
        MenuItem item = menu.findItem(R.id.actionChats);
        item.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();

        switch (id)
        {

            case  R.id.home_notifications:
                startActivity(new Intent(getApplicationContext(),NotificationActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
             email = user.getEmail();
        }
        else
        {
            startActivity(new Intent(PostActivity.this,LogInOptions.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }


    private boolean validateFields(String title,String contact,String location)
    {

        if(title.isEmpty())
        {
            Toast.makeText(this, "Field can't be empty!", Toast.LENGTH_SHORT).show();
            titleEditText.requestFocus();

            return  false;


        }
        else if(!validatePhone(contact))
        {
            return false;

        }
        else if(contact.length()<10)
        {
            Toast.makeText(this, "please enter a valid mobile no!", Toast.LENGTH_SHORT).show();
            contactEditText.requestFocus();
            return  false;
        }
        else if(!Patterns.PHONE.matcher(contact).matches())
        {
            Toast.makeText(this, "please enter a valid mobile no!", Toast.LENGTH_SHORT).show();
            contactEditText.requestFocus();
            return  false;
        }


        else if(location.isEmpty())
        {
            Toast.makeText(this, "Field can't be empty!", Toast.LENGTH_SHORT).show();
            locEditText.requestFocus();
            return  false;

        }
        else
        {
            return true;
        }



    }
    public boolean validatePhone(String contact)
    {
        if(contact.isEmpty()) {
            Toast.makeText(this, "Field can't be empty!", Toast.LENGTH_SHORT).show();
            contactEditText.requestFocus();
            return false;
        }
        else if(!Patterns.PHONE.matcher(contact).matches())
        {
            Toast.makeText(this, "Please enter a valid mobile no!", Toast.LENGTH_SHORT).show();
            contactEditText.requestFocus();
            return false;
        }
        else if(contact.length()<10)
        {
            Toast.makeText(this, "Please enter a valid mobile no!", Toast.LENGTH_SHORT).show();
            contactEditText.requestFocus();
            return false;
        }
        else
        {
            return true;
        }

    }


    public void prepareNotification(String pId,String title , String description ,String notificationType , String notificationTopic) {
        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic ; // topic must match with what the receiver susubscribe to
        String NOTIFICATION_TITLE = title ;// e.g. Mouzzam Siddiqui added a new post
        String NOTIFICATION_MESSAGE = description; // content of post
        String NOTIFICATION_TYPE = notificationType; // now there are two notification types chat and post, so to differentiate in FirebaseMessaging.java class


        //prepared  JSON what to send and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try{
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("sender",uid); // uid of current user/sender
            notificationBodyJo.put("pID",pId); // post id
            notificationBodyJo.put("pTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription",NOTIFICATION_MESSAGE);

            notificationJo.put("to",NOTIFICATION_TOPIC); // where  to send

            notificationJo.put("data",notificationBodyJo); // combine data to be sent

        }
        catch (JSONException e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificationJo);


    }

    private void sendPostNotification(JSONObject notificationJo) {
        // send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       Log.d("FCM_RESPONSE","onResponse: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws  AuthFailureError {
                //put  required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key=AAAA6_DN_gQ:APA91bFTdw4HUmribaeBoVE8atU5YTp1tPXTBakPuOkZYN3ZCpar9Ab1wnGo7MybLhEFX3kF6YjuHfBbv0S62CIQpMRwnXmPkacVC03wxkTOjan4Lf0ETLDbrkEJ5IGwhwsd1xMHBUui");   // place your fcm key here

                return headers;
            }
        };


        // enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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

            Snackbar sb = Snackbar.make(scView,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.block_background_color));
            sb.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }






}

