package com.example.getblood.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

public class PostDetail extends AppCompatActivity {


    ImageView profileImageView;
    ImageButton moreButton;
    Button interestButton,messageButton,shareButton;
    TextView usernameTextView,pTimeTextView,pTitleTextView,pDescrTextView,pInterestTextView,userTypeTextView,requestTypeTextView,
            contactTextView,bloodTextView,locationTextView,pPublishedDateTextView;
    LinearLayout profileLayout;

    String pID,pTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        //init views
        profileImageView = findViewById(R.id.uProfilePicture);
        interestButton = findViewById(R.id.interestedButton);
        messageButton=findViewById(R.id.messageButton);
        shareButton= findViewById(R.id.shareButton);
        moreButton = findViewById(R.id.moreButton);

        //init text Views
        usernameTextView=findViewById(R.id.uNameTextView);
        pTimeTextView=findViewById(R.id.pTimeTextView);
        pTitleTextView = findViewById(R.id.postTitleTextView);
        pDescrTextView=findViewById(R.id.postDescriptionTextView);
        pInterestTextView=findViewById(R.id.interestedTextView);
        userTypeTextView=findViewById(R.id.pUserTypeTextView);
        requestTypeTextView=findViewById(R.id.pTypeTextView);
        contactTextView= findViewById(R.id.pContactTextView);
        bloodTextView = findViewById(R.id.postBloodTextView);
        locationTextView= findViewById(R.id.pLocationTextView);
        pPublishedDateTextView=findViewById(R.id.publishedTimeTextView);
        profileLayout=findViewById(R.id.profileLayout);

        Intent intent = getIntent();
        pID = intent.getStringExtra("postId");

        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Query query = ref.orderByChild("pID").equalTo(pID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost post = ds.getValue(ModelPost.class);
                    //getData
                    String uid = post.getUid();
                    String userName = post.getuName();
                    String pTitle = post.getpTitle();
                    String pDescr = post.getpDescr();
                    String userType = post.getuType();
                    String reqType = post.getReqType();
                    String blood = post.getBlood();
                    String contact = post.getContact();
                    String location = post.getpLoc();
                    String timeStamp= post.getpID();
                    String profilePicture = post.getuDp();
                    String email = post.getuEmail();
                    String pInterest = post.getpInterest(); // contains a total no of interes for a post

                    //convert timestamp to dd/mm/yyyy mm:hh am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timeStamp));
                    pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


                    //set data
                    usernameTextView.setText(userName);
                    pTimeTextView.setText(pTime);
                    pTitleTextView.setText(pTitle);
                    pDescrTextView.setText(pDescr);
                    userTypeTextView.setText(userType);
                    requestTypeTextView.setText(reqType);
                    bloodTextView.setText(blood);
                    contactTextView.setText(contact);
                    locationTextView.setText(location);
                    pPublishedDateTextView.setText(pTime);
                    pInterestTextView.setText(pInterest + " Interests"); // ex : 100 interested


                    //set User dp
                    try
                    {
                        Picasso.get().load(profilePicture).placeholder(R.drawable.baseline_account_circle_black_48).into(profileImageView);
                    }
                    catch (Exception e)
                    {

                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
