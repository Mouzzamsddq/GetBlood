package com.example.getblood.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.getblood.Adapter.AdapterLikes;
import com.example.getblood.Adapter.AdapterUser;
import com.example.getblood.DataModel.ModelUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LikesActivitty extends AppCompatActivity {

    String postId;

    private RecyclerView recyclerView;

    private List<ModelUser> userList;
    private AdapterLikes adapterLikes;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes_activitty);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user  = firebaseAuth.getCurrentUser();
        String email = user.getEmail();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("People who interested...");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setSubtitle(email);

        userList = new ArrayList<>();

        recyclerView = findViewById(R.id.likeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FloatingActionButton fab = findViewById(R.id.fab);
       fab.hide();


       //get the postId
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");


        //get the list of uid  of users who interested in your post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Likes");
        ref.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userList.clear();
                for(DataSnapshot ds :dataSnapshot.getChildren())
                {
                    String hisUID = ""+ds.getRef().getKey();

                    //get the user info from each id
                    getUsers(hisUID);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void getUsers(String hisUID) {
        //get information of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(hisUID != null && hisUID.equals(ds2.getKey()))
                        {
                            Log.d("getUSers",""+hisUID);
                            ModelUser user = ds2.getValue(ModelUser.class);
                            userList.add(user);
                        }
                        adapterLikes = new AdapterLikes(LikesActivitty.this, userList);
                        recyclerView.setAdapter(adapterLikes);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed(); // go to the previous Activity
        return super.onNavigateUp();
    }
}
