package com.example.getblood.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.MemoryCategory;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.Fragment.Chat_list_fragment;
import com.example.getblood.Fragment.HelpFragment;
import com.example.getblood.Fragment.HomeFragment;
import com.example.getblood.Fragment.InfoFragment;
import com.example.getblood.Fragment.RealHomeFragment;
import com.example.getblood.Fragment.profileFragment;
import com.example.getblood.R;
import com.example.getblood.notification.Token;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener,FragmentChangeListener{
    private static final String TAG = "CHECK" ;
    private DrawerLayout mDrawerLayout;
    private TextView usernameTextView;
    private TextView emailTextView;
    FirebaseAuth mAuth;
    Boolean check = false;
    FirebaseUser user;
    NavigationView navigationView;
    String mUid;
    int position = 0;
    String profileUrl;
    ActionBar actionBar;
    String name;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    View hView;
    private FrameLayout fm;
    private ImageView headerImageView;
    private ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_layout);

        pd=new ProgressDialog(getApplicationContext());
        pd.setMessage("Loading....");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        mDatabase=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        fm = findViewById(R.id.fragment_container);
        mRef=mDatabase.getReference().child("users");
        mDrawerLayout=findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        hView=navigationView.getHeaderView(0);
        headerImageView=hView.findViewById(R.id.headerImageView);
        usernameTextView=(TextView)hView.findViewById(R.id.headerUsernameTextView);
        emailTextView=(TextView)hView.findViewById(R.id.headerEmailTextView);
        Toolbar toolbar=findViewById(R.id.toolbar_tb);
        setSupportActionBar(toolbar);
        checkGoogleFill();
        checkFacebookFill();
         actionBar=getSupportActionBar();
         headerImageView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(checkConnection()) {
                     if (profileUrl != null) {
                         Intent intent1 = new Intent(HomeActivity.this, ProfileImage.class);
                         intent1.putExtra("profileUrl", profileUrl);
                         intent1.putExtra("name", name);
                         startActivity(intent1);
                     }
                 }

             }
         });

        new updateHeaderTask().execute();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,new RealHomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        checkUserStatus();


    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private void updateHeader()  {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot donorRecipientSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot donorsSnapshot : donorRecipientSnapshot.getChildren()) {
                        if (user.getUid().equals(donorsSnapshot.getKey())) {
                            Map<String, Object> data = (HashMap<String, Object>) donorsSnapshot.getValue();
                             name = (String) data.get("FullName");
                            String email = (String) data.get("emailId");
                            usernameTextView.setText(name);
                            emailTextView.setText(email);
                            usernameTextView.setVisibility(View.VISIBLE);
                            emailTextView.setVisibility(View.VISIBLE);
                            profileUrl = (String) data.get("profileImage");
                            try
                            {
                                Picasso.get().load(profileUrl).placeholder(R.drawable.baseline_account_circle_black_48).into(headerImageView);
                            }
                            catch (Exception e)
                            {
                                headerImageView.setImageResource(R.drawable.baseline_account_circle_black_48);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        pd.dismiss();
        }



    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else

        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_logout:
                if(checkConnection()) {
                    mAuth.signOut();
                    checkUserStatus();
                }
                break;
            case R.id.nav_users:
                //add the home fragment'
                actionBar.setTitle("Users");
                position=1;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new HomeFragment(),"user").addToBackStack("users")
                        .commit();
                break;

            case R.id.nav_profile:
                position=2;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new profileFragment(),"profile").addToBackStack("Profile")
                        .commit();
                break;
            case R.id.nav_home:
                position = 3;
//                Toast.makeText(this, "This is share item", Toast.LENGTH_SHORT).show();
                actionBar.setTitle("Get Blood");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new RealHomeFragment(),"home").addToBackStack("home")
                        .commit();
                break;
            case R.id.nav_chats:
                actionBar.setTitle("Chats");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new Chat_list_fragment(),"Chat").addToBackStack("chats")
                        .commit();
                break;
            case R.id.nav_setting:
                startActivity(new Intent(HomeActivity.this , SettingActivity1.class));
                break;

            case R.id.nav_info:
                actionBar.setTitle("Information");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new InfoFragment()).addToBackStack("Info")
                        .commit();
                break;
            case R.id.nav_help:

                actionBar.setTitle("Help");
                navigationView.setCheckedItem(R.id.nav_help);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new HelpFragment()).addToBackStack("Help")
                        .commit();
                break;
            case R.id.nav_nearest_hospital:
                if(checkConnection()) {
                    openGoogleMap();
                }
                break;


        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openGoogleMap() {


        navigationView.setCheckedItem(R.id.nav_home);
        String uri = "geo:0,0?q=nearest hospitals";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);

    }

    private void showHelpFragment() {

        Fragment fr = new HelpFragment();
        FragmentChangeListener fc = (FragmentChangeListener) HomeActivity.this;
        fc.replaceFragment(fr);
    }


    public class updateHeaderTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            updateHeader();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        switch(id)
        {

            case R.id.actionChats:
                 actionBar.setTitle("Chats");
                 position = 4;
                 navigationView.setCheckedItem(R.id.nav_chats);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,new Chat_list_fragment(),"Chat").addToBackStack("Chats")
                        .commit();
                break;


            case R.id.home_notifications:
                startActivity(new Intent(HomeActivity.this , NotificationActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus()
    {
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null)
        {
          mUid = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUid);
            editor.apply();

            //update token
            //update Token
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else
        {
            startActivity(new Intent(getApplicationContext(),Donor.class));
            finish();

        }
    }
    public void updateToken(String token)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUid).setValue(mToken);
    }
    Fragment getCurrentFragment()
    {
        androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        return currentFragment;
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString());
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

    private void showOtherFragment() {

        Fragment fr = new InfoFragment();
        FragmentChangeListener fc = (FragmentChangeListener) HomeActivity.this;
        fc.replaceFragment(fr);
    }



    private void checkGoogleFill()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2: ds1.getChildren())
                    {
                        if( mUid.equals(ds2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String, Object>)ds2.getValue();
                            String Register = (String)data.get("Register");
                            if(Register!= null  && Register.equals("google"))
                            {
                                String pinCode = (String )data.get("pin");
                                if(pinCode.equals(""))
                                {

                                    startActivity(new Intent(getApplicationContext(),GoogleFillActivity.class));
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

    @Override
    protected void onStart() {
        super.onStart();
        checkGoogleFill();
        checkFacebookFill();
        checkUserStatus();
    }



    private void checkFacebookFill()
    {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2: ds1.getChildren())
                    {
                        if(mUid != null  && mUid.equals(ds2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String, Object>)ds2.getValue();
                            String Register = (String)data.get("Register");
                            if(Register!= null  && Register.equals("facebook"))
                            {
                                String pinCode = (String )data.get("pin");
                                if(pinCode.equals(""))
                                {

                                    startActivity(new Intent(getApplicationContext(),GoogleFillActivity.class));
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
            Snackbar sb = Snackbar.make(fm,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
            sb.show();
            return false;
        }


    }


    private void checkOnlineStatus(String  status)
    {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren())
                    {
                        if( user != null && user.getUid().equals(dataSnapshot2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String,Object>)dataSnapshot2.getValue();
                            String userType =(String) data.get("userType");
                            //update value of online status of current user
                            if(userType != null && userType.equals("Donor"))
                            {
                                databaseReference.child("Donor").child(user.getUid()).child("onlineStatus").setValue(status);
                            }
                            else
                            {
                                databaseReference.child("Recipient").child(user.getUid()).child("onlineStatus").setValue(status);
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



}

