package com.example.getblood.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.getblood.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingActivity1 extends AppCompatActivity {


    SwitchCompat postSwitch;

    ActionBar actionBar;

    //used shared preferences to save the state of switch
    SharedPreferences sp;
    SharedPreferences.Editor editor ;  //to edit the value of shared pref

    //Contstant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST"; //assign any value but use  same for this kind of notification


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();

        postSwitch = findViewById(R.id.postSwitch);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //init sp
        sp = getSharedPreferences("Notification_SP",MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION,false);

        //if enabled check switch , otherwise uncheck switch - by default unchecked false

        if(isPostEnabled)
        {
            postSwitch.setChecked(true);
        }
        else
        {
            postSwitch.setChecked(false);
        }

        //implement switch change listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //edit switch state
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION,isChecked);
                editor.apply();
                if(isChecked)
                {
                    subscribePostNotification(); // call to subscribehello
                }
                else
                {
                    unsubscribePostNotification(); // call to unsubscribe
                }
            }

        });
    }
    private void unsubscribePostNotification() {
        //unsubscribe  to a topic (Post) to disable it's notification

        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notification";
                        if(!task.isSuccessful())
                        {
                            msg = "Unsubscription failed";
                        }
                        Toast.makeText(SettingActivity1.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void subscribePostNotification() {
        //subscribe to a topic (Post) to enable it's notification
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notification";
                        if(!task.isSuccessful())
                        {
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingActivity1.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}


