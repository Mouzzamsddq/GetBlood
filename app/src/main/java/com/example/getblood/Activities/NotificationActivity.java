package com.example.getblood.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.getblood.Adapter.AdapterNotification;
import com.example.getblood.DataModel.ModelNotifications;
import com.example.getblood.DataModel.ModelUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    String email;
    private RelativeLayout rlLayout;
    private RecyclerView recyclerView;
    private ArrayList<ModelNotifications> notificationsArrayList;
    private AdapterNotification adapterNotification;
    private SwipeRefreshLayout notificationSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rlLayout = findViewById(R.id.rlLayout);
        checkConnection();
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
        actionBar.setSubtitle(email);

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        // init recycler view
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationSwipeRefreshLayout = findViewById(R.id.notificationRefreshLayout);
        notificationSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(checkConnection()) {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                    overridePendingTransition(0, 0);
                    notificationSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        //show newest post first,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        getAllNotifications();

    }

    private void getAllNotifications() {
        notificationsArrayList = new ArrayList<>();

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(myUID.equals(ds2.getKey()))

                        {
                            ModelUser user = ds2.getValue(ModelUser.class);
                            String userType = user.getUserType();

                            getNotification(userType);


                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getNotification(String userType) {


        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        if(userType!=null  && userType.equals("Donor"))
        {
            reference.child("Donor").child(myUID).child("Notifications")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        Map<Object, String> data = (HashMap<Object, String>) ds.getValue();
                        String senderUid = data.get("sUid");
                        if (senderUid != null && !senderUid.equals(myUID))

                        {
                            ModelNotifications model = ds.getValue(ModelNotifications.class);
                            notificationsArrayList.add(model);
                        }





                    }
                    adapterNotification = new AdapterNotification(getApplicationContext(), notificationsArrayList);
                    recyclerView.setAdapter(adapterNotification);
//

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {

            reference.child("Recipient").child(myUID).child("Notifications").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        ModelNotifications modelNotifications = ds.getValue(ModelNotifications.class);

                        notificationsArrayList.add(modelNotifications);
                    }
                    adapterNotification = new AdapterNotification(NotificationActivity.this,notificationsArrayList);
                    recyclerView.setAdapter(adapterNotification);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

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

            Snackbar sb = Snackbar.make(rlLayout,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
            sb.show();
            return false;
        }


    }


}
