package com.example.getblood.Activities;

import android.app.admin.SystemUpdatePolicy;
import android.content.Intent;
import android.os.Bundle;

import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.DataModel.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    ImageView profileImageView;
    ImageButton moreButton;
    Button interestButton,messageButton,shareButton;
    TextView usernameTextView,pTimeTextView,pTitleTextView,pDescrTextView,pInterestTextView,userTypeTextView,requestTypeTextView,
            contactTextView,bloodTextView,locationTextView,pPublishedDateTextView;
    LinearLayout profileLayout;

    String pID,pTime,uid;
    boolean mProcessInterest = false;

    private DatabaseReference pInterestRef; // for like database node
    private DatabaseReference postsRef; // for post ref

    int pInterest = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pInterestRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostDetailActivity.this , HomeActivity.class));
                finish();
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);

        fab.hide();

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Query query = ref.orderByChild("pID").equalTo(pID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost post = ds.getValue(ModelPost.class);
                    //getData
                     uid = post.getUid();
                    String userName = post.getuName();
                    String pTitle = post.getpTitle();
                    String pDescr = post.getpDescr();
                    String userType = post.getuType();
                    String reqType = post.getReqType();
                    String blood = post.getBlood();
                    String contact = post.getContact();
                    String location = post.getpLoc();
                    String pID= post.getpID();
                    String profilePicture = post.getuDp();
                    String email = post.getuEmail();
                    String pInterest = post.getpInterest(); // contains a total no of interes for a post

                    //convert timestamp to dd/mm/yyyy mm:hh am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pID));
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

                    String title = userName+" post";
                    actionBar.setTitle(title);


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

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
                Query query = ref.orderByChild("pID").equalTo(pID);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
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
                            String pID = post.getpID();
                            String profilePicture = post.getuDp();
                            String email = post.getuEmail();
                            String pInterest = post.getpInterest(); // contains a total no of interes for a post

                            //convert timestamp to dd/mm/yyyy mm:hh am/pm
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(pID));
                            pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            sharePost(pTitle, pDescr, userType, location, contact, blood, userName, email, reqType, pTime);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


        interestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                 onPostInterestButtonClicked(pID);

            }
        });
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMessageButtonClicked();
            }
        });

       pInterestTextView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext() , LikesActivitty.class);
               intent.putExtra("postId",pID);
               startActivity(intent);

           }
       });

    }

    private void onMessageButtonClicked() {

        Intent intent = getIntent();
        pID = intent.getStringExtra("postId");
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.child(pID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelPost post = dataSnapshot.getValue(ModelPost.class);
                String uid  =   post.getUid();
                Intent intent1 = new Intent(PostDetailActivity.this, ChatActivity.class);
                intent1.putExtra("hisUID",uid);
                startActivity(intent1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void onPostInterestButtonClicked(String pID) {

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postRef.child(pID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ModelPost post = dataSnapshot.getValue(ModelPost.class);
                    pInterest=Integer.parseInt(post.getpInterest());
                    changeDatabase(pInterest);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void changeDatabase(int pInterest) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pInterestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(pID).hasChild(myUID))
                    {
                        // already like so remove like
                        postsRef.child(pID).child("pInterest").setValue(String.valueOf(pInterest-1));
                        pInterestRef.child(pID).child(myUID).removeValue();
                    }
                    else
                    {
                        //not Interest , interest it
                        postsRef.child(pID).child("pInterest").setValue(String.valueOf(pInterest+1));
                        pInterestRef.child(pID).child(myUID).setValue("Interested");
                        addToListNotifications(""+uid,""+pID,"interested in your post");
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


      setInterest();
      setInterestTextView();



    }


    private void setInterest() {

        Intent intent = getIntent();
        pID = intent.getStringExtra("postId");
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pInterestRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        pInterestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(pID).hasChild(myUID))
                {
                    // user has interested in this post
                    /* To indicate that the post is liked by this(signed in) user
                    change drawable left icon for  interest button
                    change text of interest button to "interested"*/
                    interestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_green_icon,0,0,0);
                    interestButton.setText("Interested");
                }
                else
                {
                    //user has not liked this post
                     /* To indicate that the post is not interest by this(signed in) user
                    change drawable left icon for  interest button
                    change text of "interested" button to "interest"*/
                    interestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thum_red_icon,0,0,0);
                    interestButton.setText("Interest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setInterestTextView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setInterest();
        setInterestTextView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInterest();
        setInterestTextView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setInterest();
        setInterestTextView();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setInterest();
        setInterestTextView();
    }


    private void setInterestTextView()
    {
        Intent intent = getIntent();
        pID = intent.getStringExtra("postId");
        pInterestTextView=findViewById(R.id.interestedTextView);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.child(pID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelPost post = dataSnapshot.getValue(ModelPost.class);
                String pInterest = post.getpInterest();
                pInterestTextView.setText(pInterest+ " interests");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private void sharePost(String pTitle, String pDescr, String userType, String location, String contact, String blood, String userName, String email,String reqType,String pTime) {
        // concatenate title and description to share
        String shareBody = pTitle + "\n" +pDescr;
        String content = "Username: "+userName +"\n"+"User Email: "+email+"\n"+"\n"+"User Type: "+userType + "\n"+"Request Type: "+reqType + "\n"+ "Blood Group: "+blood
                +"\n"+"Contact No: "+contact+"\n"+"Location: "+location +"\n"+"Posted at: "+pTime+".";
        shareBody = shareBody+"\n"+"\n"+content;

        String appName = "GetBlood"+"\n"+"Welcome  to smart blood bank";
        shareBody=appName+"\n"+"\n"+shareBody;

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject here"); // In case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT,shareBody); // text to share
        startActivity(Intent.createChooser(intent,"Share via")); // message to show  in share dialog
    }

    private void addToListNotifications(String hisUID, String pId , String notification)
    {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timestamp= ""+System.currentTimeMillis();
        HashMap<Object ,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timeStamp",timestamp);
        hashMap.put("pUid",hisUID);
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUID);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds1: ds.getChildren())
                    {
                        if(hisUID.equals(ds1.getKey()))
                        {
                            ds1.getRef().child("Notifications").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setInterest();
        setInterestTextView();

    }

}
