package com.example.getblood.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.DataModel.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecifiProfile extends AppCompatActivity {


    List<String> blockUserList;
    List<ModelPost>filteredPostList;
    boolean isBlocked;
    String profileUrl,name,myUID;
    boolean isChat;
    DatabaseReference ref;
    List<String> blockList;
    private String hisUId;
    LinearLayout linearLayout;
    private RelativeLayout rl;
    RecyclerView recyclerView;
    String textProfileBlockButton;
    private ImageView profileImageView;
    private TextView profileUserNameTextView,profileUserEmailTextView;
    private RecyclerView specificProfileRecyclerView;
    private Button profileMessageButton, profileBlockButton;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;
    FrameLayout fm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specifi_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        blockList = new ArrayList<>();
         linearLayout = findViewById(R.id.linear);
        blockUserList = new ArrayList<>();
        filteredPostList = new ArrayList<>();
        rl = findViewById(R.id.profileFragment);
        checkConnection();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        recyclerView = findViewById(R.id.specifProfileRecyclerView);
        Intent intent = getIntent();
        fm = new FrameLayout(getApplicationContext());
        isChat = intent.getBooleanExtra("isChat",false);
        hisUId = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");
        checkUserStatus();
        profileBlockButton= findViewById(R.id.profileBlockButton);
        profileMessageButton=findViewById(R.id.profileMessageButton);
        specificProfileRecyclerView = findViewById(R.id.specifProfileRecyclerView);
        profileUserEmailTextView=findViewById(R.id.specificProfileUserEmailTextView);
        profileUserNameTextView = findViewById(R.id.specificProfileUserNameTextView);
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        textProfileBlockButton = profileBlockButton.getText().toString();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = getIntent();
                boolean isHome = intent1.getBooleanExtra("isHome",false);
                if(isHome)
                {
                    finish();
                }
                else
                {
                    checkBlockOrNot(hisUId);
                }





            }
        });
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post  first ,for this load from list
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycler view

        specificProfileRecyclerView.setLayoutManager(layoutManager);
        if(isChat)
        {
            Intent intent1 = getIntent();
            isBlocked = intent1.getBooleanExtra("Blocked",false);

        }


        profileMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                checkBlockOrNot(hisUId);


            }
        });


        profileImageView = findViewById(R.id.profileImageView);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();

        //fetching details from database
         ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(hisUId.equals(ds2.getKey()))
                        {
                            Map<String, Object> data = (HashMap<String, Object>) ds2.getValue();
                            name = (String) data.get("FullName");
                            String email = (String) data.get("emailId");

                            actionBar.setTitle(name);
                            actionBar.setSubtitle(email);
                            profileUserNameTextView.setText(name);
                            profileUserEmailTextView.setText(email);

                            profileUrl = (String) data.get("profileImage");
                            if (!profileUrl.isEmpty()) {
                                Picasso.get().load(profileUrl).placeholder(R.drawable.baseline_account_circle_black_48).into(profileImageView);
                            }


                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkConnection()) {
                    Intent intent1 = new Intent(SpecifiProfile.this, ProfileImage.class);
                    intent1.putExtra("profileUrl", profileUrl);
                    intent1.putExtra("name", name);
                    startActivity(intent1);
                    finish();
                }
            }
        });
        profileBlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkConnection()) {
                    if (profileBlockButton.getText().toString().equals("Block")) {
                        blockedUser(hisUId);
                    } else {
                        unBlockedUser(hisUId);
                    }
                }
            }
        });

       postList = new ArrayList<>();
        Log.d("BlockedList",blockList.toString());
        isBlockedOrNot(hisUId);

    }

    private void checkBlockOrNot(String hisUId) {
        blockUserList.clear();
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds1 : ds.getChildren())
                    {
                        if(myUID.equals(ds1.getKey()))
                        {
                            ds1.getRef().child("BlockedUsers")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds2 : dataSnapshot.getChildren())
                                            {
                                                blockUserList.add(ds2.getKey());
                                            }

                                            checkMessageBlock(blockUserList,hisUId);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

    }

    private void checkMessageBlock(List<String> blockUserList, String hisUId) {

        if(blockUserList.isEmpty())
        {
            Intent intent = new Intent(SpecifiProfile.this,ChatActivity.class);
            intent.putExtra("hisUID",hisUId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Blocked",false);
            startActivity(intent);
            finish();

        }
        else
        {
            if(blockUserList.contains(hisUId))
            {
                Intent intent = new Intent(SpecifiProfile.this,ChatActivity.class);
                intent.putExtra("hisUID",hisUId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Blocked",true);
                startActivity(intent);
                finish();


            }
            else
            {

                Intent intent = new Intent(SpecifiProfile.this,ChatActivity.class);
                intent.putExtra("hisUID",hisUId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Blocked",false);
                startActivity(intent);
                finish();

            }
        }
    }

    private void loadSpecificUserPosts() {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post  first ,for this load from list
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycler view

        specificProfileRecyclerView.setLayoutManager(layoutManager);


        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(hisUId);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost myPosts = ds.getValue(ModelPost.class);


                        //add to list


                          postList.add(myPosts);
                          //adapter
                          adapterPost = new AdapterPost(getApplicationContext(), postList,rl);
                          //set the adapter to recycler view
                          specificProfileRecyclerView.setAdapter(adapterPost);





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SpecifiProfile.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchSpecificUserPost(String searchQuery)
    {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post  first ,for this load from list
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycler view

        specificProfileRecyclerView.setLayoutManager(layoutManager);


        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(hisUId);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                            || myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getBlood().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpLoc().toLowerCase().contains(searchQuery.toLowerCase()) || myPosts.getuName()
                    .toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        //add to list
                        postList.add(myPosts);
                    }



                    //adapter
                    adapterPost = new AdapterPost(getApplicationContext(),postList,rl);
                    //set the adapter to recycler view
                    specificProfileRecyclerView.setAdapter(adapterPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void blockedUser(String hisUID) {
        //block the user ,by adding the uid to current user's "Blocked User Node";


        //put values in hashmap to put in db
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(myUID.equals(ds2.getKey()))
                        {
                            ds2.getRef().child("BlockedUsers").child(hisUID).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            profileBlockButton.setText("Unblock");
                                            checkPost();
                                            Toast.makeText(getApplicationContext(), "Blocked Successfully", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }
    private void unBlockedUser(String hisUID) {
//        unblock the user by removing uid from current user's "BlockedUsers" node

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(myUID.equals(ds2.getKey()))
                        {
                            ds2.getRef().child("BlockedUsers")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds  : dataSnapshot.getChildren())
                                                if (ds.exists()) {
                                                    if (hisUID.equals(ds.getKey())) {
                                                        //remove blocked user data from current users blocked users list
                                                        ds.getRef().removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        //unblocked successfully

                                                                        ProgressDialog pd = new ProgressDialog(SpecifiProfile.this);
                                                                        pd.setCancelable(false);
                                                                        pd.setCanceledOnTouchOutside(false);
                                                                        pd.setMessage("Loading...");
                                                                        pd.show();
                                                                       Handler handler = new Handler();
                                                                       handler.postDelayed(new Runnable() {
                                                                           @Override
                                                                           public void run() {
                                                                               checkPost();
                                                                               pd.dismiss();
                                                                               profileBlockButton.setText("Block");
                                                                               Toast.makeText(SpecifiProfile.this, "Unblocked Successfully", Toast.LENGTH_SHORT).show();

                                                                           }
                                                                       },2000);



                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //failed to block
                                                                Toast.makeText(SpecifiProfile.this, "failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser() ;
        if(user!= null)
        {
            uid = user.getUid();
        }
        else
        {
            startActivity(new Intent(SpecifiProfile.this,LogInOptions.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        isChat = intent.getBooleanExtra("isChat",false);
        hisUId = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");
        checkUserStatus();
        isBlockedOrNot(hisUId);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu_resource,menu);
        MenuItem item = menu.findItem(R.id.home_addPost);
        item.setVisible(false);
        Intent intent = getIntent();
        boolean isBlocked = intent.getBooleanExtra("Blocked",false);
        if(isBlocked)
        {
            MenuItem item1 = menu.findItem(R.id.home_search);
            item1.setVisible(false);
        }

        //searchview of search user specific post
        MenuItem item1 = menu.findItem(R.id.home_search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item1);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if(!TextUtils.isEmpty(s))
                {
                    //search
                    searchSpecificUserPost(s);
                }
                else
                {
                    loadSpecificUserPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                //called whenever user type letter
                if(!TextUtils.isEmpty(s))
                {
                    searchSpecificUserPost(s);
                }
                else
                {
                    loadSpecificUserPosts();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        switch(id)
        {
            case R.id.home_search:
                Toast.makeText(SpecifiProfile.this, "Profile Search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.home_addPost:
                Toast.makeText(SpecifiProfile.this, "profile Add post", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private  void checkPost()
    {
        blockUserList.clear();
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds1 : ds.getChildren())
                    {
                        if(myUID.equals(ds1.getKey()))
                        {
                            ds1.getRef().child("BlockedUsers")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds2 : dataSnapshot.getChildren())
                                            {
                                                blockUserList.add(ds2.getKey());
                                            }

                                            addPost(blockUserList);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

    }

    private void addPost(List<String> blockUserList) {
        Intent intent = getIntent();

        hisUId = intent.getStringExtra("uid");


        //init post list
        if(!blockUserList.isEmpty()) {

            if (blockUserList.contains(hisUId)) {
                profileBlockButton.setText("Unblock");
                specificProfileRecyclerView.setVisibility(View.GONE);

                linearLayout.setVisibility(View.VISIBLE);
                TextView textView = findViewById(R.id.textShow);
                textView.setText(name + " is blocked");
                profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(SpecifiProfile.this, "Unblock "+name + " to see the profile picture", Toast.LENGTH_SHORT).show();
                    }
                });


            }
            else
            {

                linearLayout.setVisibility(View.GONE);
                specificProfileRecyclerView.setVisibility(View.VISIBLE);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
                Query query = ref.orderByChild("uid").equalTo(hisUId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        filteredPostList.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            ModelPost postData = ds.getValue(ModelPost.class);
                            filteredPostList.add(postData);
                            adapterPost = new AdapterPost(SpecifiProfile.this,filteredPostList,rl);
                            specificProfileRecyclerView.setAdapter(adapterPost);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        }
        else
        {

            linearLayout.setVisibility(View.GONE);
            specificProfileRecyclerView.setVisibility(View.VISIBLE);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
            Query query = ref.orderByChild("uid").equalTo(hisUId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    filteredPostList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        ModelPost postData = ds.getValue(ModelPost.class);
                        filteredPostList.add(postData);
                        adapterPost = new AdapterPost(SpecifiProfile.this,filteredPostList,rl);
                        specificProfileRecyclerView.setAdapter(adapterPost);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }





    }
    private void isBlockedOrNot(String hisUid)
    {
        //first check if sender(current user) is blocked by receiver or not
        //Logic : If uid of the sender(current user) exists in "BlockedUsers" of receiver then sender(current user) is blocked,otherwise not
        //uif blocked then just display a message e.g. "you are blocked by that user,can't send message"
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(hisUid != null && hisUid.equals(ds2.getKey()))
                        {

                            ds2.getRef().child("BlockedUsers").orderByChild("uid").equalTo(myUID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                if(ds.exists())
                                                {
                                                    profileImageView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Toast.makeText(SpecifiProfile.this, "You blocked by "+name + ", can't see profile picture", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    specificProfileRecyclerView.setVisibility(View.GONE);
                                                    profileMessageButton.setVisibility(View.INVISIBLE);
                                                    profileBlockButton.setVisibility(View.INVISIBLE);
                                                    linearLayout.setVisibility(View.VISIBLE);
                                                    TextView textView = findViewById(R.id.textShow);
                                                    textView.setText("You blocked by "+name);
                                                    // blocked , don't proceed further
                                                    return;
                                                }
                                            }
                                            profileMessageButton.setVisibility(View.VISIBLE);
                                            profileBlockButton.setVisibility(View.VISIBLE);
                                            checkPost();


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent1 = getIntent();
        boolean isHome = intent1.getBooleanExtra("isHome",false);
        if(isHome)
        {
            finish();
        }
        else
        {
            checkBlockOrNot(hisUId);
        }

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
            Snackbar sb = Snackbar.make(rl,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
            sb.show();
            return false;
        }


    }

}



