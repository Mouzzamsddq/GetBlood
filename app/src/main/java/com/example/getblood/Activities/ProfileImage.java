package com.example.getblood.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.getblood.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ProfileImage extends AppCompatActivity {

    private SubsamplingScaleImageView profileImageView;
    String profileUrl,name;
    Bitmap myBitmap;
    SpinKitView spinKitView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        profileUrl = intent.getStringExtra("profileUrl");
        name = intent.getStringExtra("name");

        spinKitView = findViewById(R.id.SpinKit);

        profileImageView = findViewById(R.id.profileImageView);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(!profileUrl.isEmpty()) {


            new updateTask().execute();

        }

        if(name!=null) {
            actionBar.setTitle(name + " profile");
        }
        else
        {
            actionBar.setTitle("Sent image");
        }




        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
       fab.hide();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
    class updateTask extends  AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                java.net.URL url = new java.net.URL(profileUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                 myBitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            spinKitView.setVisibility(View.INVISIBLE);
            if (myBitmap != null) {
                profileImageView.setImage(ImageSource.bitmap(myBitmap));
            }
            else
            {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_portrait);
                profileImageView.setImage(ImageSource.bitmap(icon));
            }
        }
    }


}
