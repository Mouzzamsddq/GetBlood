package com.example.getblood.Activities;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.getblood.Adapter.AdapterChat;
import com.example.getblood.Adapter.AdapterChatList;
import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.Adapter.AdapterUser;
import com.example.getblood.DataModel.ModelChat;
import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.Fragment.Chat_list_fragment;
import com.example.getblood.R;
import com.example.getblood.notification.Data;
import com.example.getblood.notification.Sender;
import com.example.getblood.notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class ChatActivity extends AppCompatActivity {

    boolean change = false;
    public static  final String EXTRA_FOUND="found";
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_BACK_IMAGE=50;
    private static final String TAG = "TAG";
    //Initialize views
    FirebaseAuth mAuth;
    Uri pickedImgUri;
    private boolean notify =false;
    Toolbar toolbar;
    boolean isSpecificBlock = false;
    List<String> specificBlockUserList;
    RecyclerView  recyclerView;
    ImageView profileImageView,blockImageView,backNavigationImageView;
    TextView nameTextView,userStatusTextView;
    EditText messageEditText;
    ImageButton sendButton,attatchButton;
    String fileName = "";






    String hisUID;
    String myUID;


    private RelativeLayout rl;

    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    String hisImage;

    // for checking user have seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    //volley request queue for notification
    private RequestQueue requestQueue;
   


    List<String> blockUserList;

    //permission constant
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;
    //iimage pick constant
    private static final int  IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE =400;

    //permission array;
    String[] cameraPermission;
    String[] storagePermission;


    //image picked will be samed in this uri
    Uri image_uri = null;

    ProgressDialog pd;
    MenuItem item2;
    boolean check = false;


    //chat layout declaration
    LinearLayout chatLayout,blockedLayout;
    Button unblockedButtonLayout;
    TextView blockedText;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        blockUserList = new ArrayList<>();
        specificBlockUserList = new ArrayList<>();
        ActionBar actionBar = getSupportActionBar();
        toolbar.setTitle("");
        pd= new ProgressDialog(ChatActivity.this);
        rl = findViewById(R.id.relativeChat);
        recyclerView = findViewById(R.id.chatRecyclerView);
        profileImageView = findViewById(R.id.chatProfileImageView);
        nameTextView = findViewById(R.id.chatNameTextView);
        userStatusTextView = findViewById(R.id.userStatusTextView);
        messageEditText = findViewById(R.id.messageEditText);
        rl = findViewById(R.id.relativeChat);
        sendButton = findViewById(R.id.sendButton);
        mDatabase = FirebaseDatabase.getInstance();
        attatchButton = findViewById(R.id.attatchButton);
        backNavigationImageView = findViewById(R.id.backNavigationImageView);


        //init chat layout
        chatLayout = findViewById(R.id.chatLayout);
        blockedLayout = findViewById(R.id.blockedLayout);
        unblockedButtonLayout = findViewById(R.id.unblockButtonLayout);
        blockedText = findViewById(R.id.blockedText);
        userStatusTextView.setSelected(true);


        new readMessageBackground().execute();
        new seenMessageBackground().execute();
        setBackground();

         new chatActivityBackground().execute();

   }


    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getApplicationContext());
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }
    private void showSettingsDialog1() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getApplicationContext());
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().toString(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(ChatActivity.this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }
    private void showImagePickerOptions1() {
        ImagePickerActivity.showImagePickerOptions(ChatActivity.this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent1();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent1();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(ChatActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }
    private void launchCameraIntent1() {
        Intent intent = new Intent(ChatActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_BACK_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getApplicationContext(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent1() {
        Intent intent = new Intent(getApplicationContext(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_BACK_IMAGE);
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                pickedImgUri = uri;
                String imageUri=compressImage(pickedImgUri.toString());
                         try {
                             sendImageMessage(Uri.parse(imageUri));
                         } catch (IOException e) {
                             e.printStackTrace();
                         }





            }
        }
        else
        {
            if(requestCode == REQUEST_BACK_IMAGE)
            {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getParcelableExtra("path");
                    Toast.makeText(this, ""+uri.toString(), Toast.LENGTH_SHORT).show();
                    pickedImgUri = uri;
                    String imageUri=compressImage(pickedImgUri.toString());






                    //get Bitmap from image uri
                    Bitmap bitmap = null;
                    FileOutputStream outputStream = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), pickedImgUri);



                        outputStream  = openFileOutput(fileName+myUID+".jpg",MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(outputStream != null)
                        {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                    Bitmap bitmap1 = null;
                    InputStream inputStream = null;

                    try {
                        inputStream = openFileInput(fileName);
                        bitmap1 = BitmapFactory.decodeStream(inputStream);



                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    Drawable dr = new BitmapDrawable(bitmap);
                    recyclerView.setBackground(dr);













                }
            }
            if(requestCode == 438)
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    Uri uri = data.getData();
                   String fileName = getFileName(uri);
                   new sendDocumentFileBackGround().execute(uri.toString(),fileName);

                }
            }
        }
    }


    public class sendDocumentFileBackGround extends AsyncTask<String,Void,Void>
    {


        @Override
        protected Void doInBackground(String... strings) {


            new uploadPdfBackground().execute(strings[0],strings[1]);








            return null;
        }
    }

    public class uploadPdfBackground extends AsyncTask<String,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar sb = Snackbar.make(rl,"Sending pdf...",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.show();
        }



        @Override
        protected Void doInBackground(String... strings) {

            notify = true;
            String timeStamp = ""+System.currentTimeMillis();
            String fileNameAndPath = "ChatPdf/"+strings[1]+timeStamp;

            Uri uri = getUriToDrawable(ChatActivity.this,R.drawable.pdf);


            //add image uri and other info to database
            DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

            String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = getIntent();
            String hisUID = intent.getStringExtra("hisUID");
            //setup required data
            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("sender",myUID);
            hashMap.put("receiver",hisUID);
            hashMap.put("message",uri.toString());
            hashMap.put("timestamp",timeStamp);
            hashMap.put("type","pdf");
            hashMap.put("isSeen",false);
            hashMap.put("fileName",strings[1]);

            //put the data into firebase
            databaseReference.child("chats").push().setValue(hashMap);



            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            mStorageRef.child(fileNameAndPath).putFile(Uri.parse(strings[0])).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful())
                    {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("chats");
                        Query query =dbRef.orderByChild("timestamp").equalTo(timeStamp);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("message", downloadUri);
                                    ds.getRef().updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar sb = Snackbar.make(rl,"Pdf sent...",Snackbar.LENGTH_LONG);
                                            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
                                            sb.show();

                                            DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child("users");
                                            ds.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot d1 : dataSnapshot.getChildren())
                                                    {
                                                        for(DataSnapshot d2 : d1.getChildren())
                                                        {
                                                            if(myUID.equals(d2.getKey()))
                                                            {
                                                                ModelUser user = d2.getValue(ModelUser.class);

                                                                if(notify)
                                                                {
                                                                    new sendNotificationDocument().execute(user.getFullName());

                                                                }
                                                                notify = false;
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                                            //create chatlist node/child in Firebase Database
                                            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                                                    .child(myUID)
                                                    .child(hisUID);
                                            chatRef1.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.exists())
                                                    {
                                                        chatRef1.child("id").setValue(hisUID);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            //create chatlist node/child in Firebase Database
                                            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                                                    .child(hisUID)
                                                    .child(myUID);
                                            chatRef2.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.exists())
                                                    {
                                                        chatRef2.child("id").setValue(myUID);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(ChatActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });




            //send notification











            return null;
        }

    }



    private void setBackground()
   {
       Bitmap bitmap = null;
       InputStream inputStream = null;
       String myUID =  FirebaseAuth.getInstance().getCurrentUser().getUid();
       try {
           inputStream = openFileInput(fileName+myUID+".jpg");
           bitmap = BitmapFactory.decodeStream(inputStream);



       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }


       Drawable dr = new BitmapDrawable(bitmap);
       recyclerView.setBackground(dr);


   }


    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void loadProfile(String url) {
        Log.d(TAG, "Image cache path: " + url);

        Picasso.get().load(url).placeholder(R.drawable.baseline_account_circle_black_48)
                .into(profileImageView);
        profileImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
    }







    private void sendImageMessage(Uri pickedImgUri) throws IOException {
        notify=true;
//        progressDialog
        ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage("Sending image...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        /* chats node will be created that will contain all images sent via chat*/
//        progressDialog.show();
        //get Bitmap from image uri
        new uploadChatPhoto().execute(pickedImgUri);


    }


    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver() != null && chat.getSender() != null) {
                        if (chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID)) {
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void readMessage() {
//        chatList = new ArrayList<>();
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
//        dbRef.child("chats").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot ds : dataSnapshot.getChildren()) {
//
//
//                    ModelChat chat = ds.getValue(ModelChat.class);
//                    if(chat.getSender() != null  && chat.getSender() != null) {
//
//                        if(chat.getSender().equals(hisUID) && chat.getReceiver().equals(myUID)  ||
//                              chat.getSender().equals(myUID) && chat.getReceiver().equals(hisUID)) {
//
//                            chatList.add(chat);
//
//
//                        }
//                        AdapterChat adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
//                        adapterChat.notifyDataSetChanged();
//                        recyclerView.setAdapter(adapterChat);
//
//                    }
//
//
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private  void readMessage()
    {
        chatList = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    chatList.clear();
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getSender() != null && chat.getSender() != null) {

                        if (chat.getSender().equals(hisUID) && chat.getReceiver().equals(myUID) ||
                                chat.getSender().equals(myUID) && chat.getReceiver().equals(hisUID)) {

                            String messageTime = chat.getTimestamp();
                            //convert timestamp to dd/mm/yyyy mm:hh am/pm
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(messageTime));
                            String time= DateFormat.format("dd/MM/yyyy hh:mm:ss",calendar).toString();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                            try {
                                Date d = sdf.parse(time);
                                mRef.child("clearChat").child(myUID+hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            HashMap<String, Object> clearChat = (HashMap<String, Object>) dataSnapshot.getValue();
                                            String clearChatTimeStamp = (String) clearChat.get("timestamp");
                                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                            calendar.setTimeInMillis(Long.parseLong(clearChatTimeStamp));
                                            String clearTime = DateFormat.format("dd/MM/yyyy hh:mm:ss", calendar).toString();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                                            try {
                                                Date d1 = sdf.parse(clearTime);
                                                if (d.after(d1)) {
                                                    chatList.add(chat);


                                                }
                                                AdapterChat adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                                                adapterChat.notifyDataSetChanged();
                                                recyclerView.setAdapter(adapterChat);


                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        else
                                        {
                                            chatList.add(chat);
                                            AdapterChat adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                                            adapterChat.notifyDataSetChanged();
                                            recyclerView.setAdapter(adapterChat);
                                        }





                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            } catch (ParseException e) {
                                e.printStackTrace();
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

    private void clearChat()
    {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUID);
        hashMap.put("receiver",hisUID);
        hashMap.put("timestamp",timeStamp);
        String clearChatUid = myUID+hisUID;
        databaseReference.child("clearChat").child(clearChatUid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                   AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                   builder.setTitle("Clear Chat");
                   builder.setMessage("To clear the messages,Tap on clear");
                   builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           finish();
                           overridePendingTransition(0, 0);
                           Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                           intent.putExtra("hisUID",hisUID);
                           startActivity(intent);
                           overridePendingTransition(0, 0);

                       }

                   });
               builder.create().show();
            }

        });


        deleteChatListNode();



    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUID);
        hashMap.put("receiver",hisUID);
        hashMap.put("message",message);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("isSeen",false);
        hashMap.put("type","text");
        databaseReference.child("chats").push().setValue(hashMap);


        new sendMessageRemainingTask().execute(message);





    }

    private void sendNotification(String hisUID, String fullName, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query =allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Token token = ds.getValue(Token.class);
                    Data data =new Data(""+myUID,""+fullName+": "+""+message,"New Message",hisUID,"ChatNotification",R.drawable.baseline_account_circle_black_48);

                    Sender sender = new Sender(data, token.getToken());


                    // for json token object
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // response of the request
                                        Log.d("JSON_RESPONSE","onResponse: " + response.toString());

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE","onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String,String> headers = new HashMap<>();
                                headers.put("Content-Type","application/json");
                                headers.put("Authorization","key=AAAA6_DN_gQ:APA91bFTdw4HUmribaeBoVE8atU5YTp1tPXTBakPuOkZYN3ZCpar9Ab1wnGo7MybLhEFX3kF6YjuHfBbv0S62CIQpMRwnXmPkacVC03wxkTOjan4Lf0ETLDbrkEJ5IGwhwsd1xMHBUui");


                                return headers;
                            }
                        };

                        //add this request to  queue
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null)
        {

        }
        else
        {
            startActivity(new Intent(getApplicationContext(),Donor.class));
            finish();

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
                        if(user.getUid().equals(dataSnapshot2.getKey()))
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
    private void checkTypingStatus(String  typing)
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
                        if(user.getUid().equals(dataSnapshot2.getKey()))
                        {
                            Map<String,Object> data = (HashMap<String,Object>)dataSnapshot2.getValue();
                            String userType =(String) data.get("userType");
                            //update value of online status of current user
                            if(userType!= null && userType.equals("Donor"))
                            {
                                databaseReference.child("Donor").child(user.getUid()).child("typingTo").setValue(typing);
                            }
                            else
                            {
                                databaseReference.child("Recipient").child(user.getUid()).child("typingTo").setValue(typing);
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
        checkUserStatus();
        // set onlineStatus
        checkOnlineStatus("online");
        checkLastSeen(hisUID);
        setBackground();

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //set offline with Last Seen timestamp
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        checkLastSeen(hisUID);
        setBackground();
        userRefForSeen.removeEventListener(seenListener);

    }

    @Override
    protected void onResume() {
        //set Online
        checkOnlineStatus("online");
        checkTypingStatus("noOne");
        checkLastSeen(hisUID);
        setBackground();

        super.onResume();
    }



//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.chat_activity_menu,menu);
          Intent intent = getIntent();
           boolean isBlocked = intent.getBooleanExtra("Blocked", false);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
          MenuItem menuItem = menu.findItem(R.id.chat_block);
          item2 = menu.findItem(R.id.chat_block);
          if(isBlocked) {
              menuItem.setTitle("Unblock");
          }



      //  search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s))
                {
                    searchPost(s);
                }
                else
                {
                    readMessage();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchPost(s);
                }
                else
                {
                    readMessage();
                }
                return false;
            }
        });
//

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        switch(id)
        {
            case R.id.chat_block:
                if(checkConnection()) {
                    onBlockClicked(item);
                }
                break;

            case R.id.chat_profile:
                Intent intent = getIntent();
                String hisUID = intent.getStringExtra("hisUID");
                openProfileActivity(hisUID);
                break;
            case R.id.chat_clearChat:
                new clearChatTask().execute();
                break;

            case R.id.callChat:
                findContactNo();
                break;

            case R.id.changeBackground:
                new changeBackgroundTask().execute();
                break;




        }
        return super.onOptionsItemSelected(item);
    }


    public class changeBackgroundTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            Dexter.withActivity(ChatActivity.this)
                    .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                showImagePickerOptions1();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog1();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();




            return null;
        }
    }

    private void openProfileActivity(String hisUID) {
        boolean isChat=true;
        Intent intent1 = getIntent();
        boolean isBlocked = intent1.getBooleanExtra("Blocked",false);
        String name = nameTextView.getText().toString().trim();
        Intent intent = new Intent(ChatActivity.this,SpecifiProfile.class);
        intent.putExtra("uid",hisUID);
        intent.putExtra("isChat",isChat);
        intent.putExtra("name",name);
        intent.putExtra("Blocked",isBlocked);
        startActivity(intent);

    }

    private void onBlockClicked(MenuItem item) {

                if(item.getTitle().equals("Unblock"))
                {
                    unBlockedUser(hisUID,item,false);
                }
                else
                {
                    blockedUser(hisUID,item);
                }


    }






    private void blockedUser(String hisUID,MenuItem item) {
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
                                             item.setTitle("Unblock");
                                             check=true;
                                             chatLayout.setVisibility(View.GONE);
                                             blockedLayout.setVisibility(View.VISIBLE);
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
    private void unBlockedUser(String hisUID,MenuItem item,boolean blocked) {
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
                                                                        item.setTitle("Block");

                                                                            blockedLayout.setVisibility(View.GONE);
                                                                            chatLayout.setVisibility(View.VISIBLE);

                                                                        Toast.makeText(ChatActivity.this, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //failed to block
                                                                Toast.makeText(ChatActivity.this, "failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
     private void confirmDialogBox(String hisUID,MenuItem item2)
     {
          String name = nameTextView.getText().toString().trim();
         androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(ChatActivity.this)
                 .setTitle("Block!")
                 .setMessage(name+ " is blocked, Unblock the user for start messaging..!")
                 .setPositiveButton("Unblock", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {

                         unBlockedUsers(hisUID,item2);

                     }
                 }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {

                     }
                 });
           alertDialog.create().show();

     }
        private void unBlockedUsers(String hisUID,MenuItem item2)
        {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        for(DataSnapshot ds1 : ds.getChildren())
                        {
                            if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ds1.getKey()))
                            {
                                ds1.getRef().child("BlockedUsers")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                 for(DataSnapshot ds : dataSnapshot.getChildren())
                                                 {
                                                     if(ds.exists())
                                                     {
                                                         if(hisUID.equals(ds.getKey()))
                                                         {
                                                             ds.getRef().removeValue()
                                                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                         @Override
                                                                         public void onSuccess(Void aVoid) {
                                                                             item2.setTitle("Block");

                                                                             Toast.makeText(ChatActivity.this, "Unblocked Successfully", Toast.LENGTH_SHORT).show();

                                                                         }
                                                                     }) .addOnFailureListener(new OnFailureListener() {
                                                                 @Override
                                                                 public void onFailure(@NonNull Exception e) {
                                                                     Toast.makeText(ChatActivity.this, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();

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
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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

        // if the blockuserlist is  not empty, and this listb contains the specific user UID it means the specifi user blocked by user
//        // then show snackbar with one action Button
        if(!blockUserList.isEmpty())
        {
            if(blockUserList.contains(hisUId))
            {
                chatLayout.setVisibility(View.GONE);
                blockedLayout.setVisibility(View.VISIBLE);
                DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("users");
                data.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            for(DataSnapshot ds1 : ds.getChildren())
                            {
                                if(hisUId!= null && hisUId.equals(ds1.getKey()))
                                {

                                    ModelUser user = ds1.getValue(ModelUser.class);
                                    String specifUserName = user.getFullName();
                                    if(specifUserName != null)
                                    {
                                        String setText = specifUserName+ " is blocked by you.";
                                        blockedText.setText(setText);
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





    }


    private void confirmSpecificDialogBox(String hisUID)
    {
        String name = nameTextView.getText().toString().trim();
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(ChatActivity.this)
                .setTitle("Block!")
                .setMessage("You blocked by "+name + ", can't send the message !")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                          return;

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        alertDialog.create().show();

    }
    private void isBlockedOrNot(String hisUid,String message)
    {
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
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                if(ds.exists())
                                                {
//
                                                    confirmSpecificDialogBox(hisUid);
                                                    return;

                                                }

                                            }
//                                            checkBlockOrNot(hisUID,message);


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
    private void checkLastSeen(String hisUid)
    {
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
//
                                                      userStatusTextView.setVisibility(View.INVISIBLE);
                                                      Picasso.get().load(R.drawable.baseline_account_circle_black_48)
                                                              .placeholder(R.drawable.baseline_account_circle_black_48).into(profileImageView);
                                                }
                                                else
                                                {
                                                    userStatusTextView.setVisibility(View.VISIBLE);
                                                    Picasso.get().load(hisImage).placeholder(R.drawable.baseline_account_circle_black_48)
                                                            .into(profileImageView);
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


    public class readMessageBackground extends  AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            readMessage();
            return null;
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }

    private void findContactNo()
    {
        Intent intent = getIntent();
         String hisUID = intent.getStringExtra("hisUID");

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
                            ModelUser specificUser = ds2.getValue(ModelUser.class);
                            String contactNo = specificUser.getContact();
                            callAction(contactNo);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void callAction(String contactNo) {
        Uri number = Uri.parse("tel:"+contactNo);
        Intent callIntent = new Intent(Intent.ACTION_DIAL,number);
        startActivity(callIntent);
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

    public class uploadChatPhoto extends AsyncTask<Uri,Uri,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            Snackbar sb = Snackbar.make(rl,"Sending image...",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
            sb.show();

        }

        @Override
        protected Void doInBackground(Uri... uris) {



            String timeStamp = ""+System.currentTimeMillis();
            String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;



            Uri uri = getUriToDrawable(ChatActivity.this,R.drawable.ic_image_chat);


            //add image uri and other info to database
            DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();


            //setup required data
            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("sender",myUID);
            hashMap.put("receiver",hisUID);
            hashMap.put("message",uri.toString());
            hashMap.put("timestamp",timeStamp);
            hashMap.put("type","image");
            hashMap.put("isSeen",false);

            //put the data into firebase
            databaseReference.child("chats").push().setValue(hashMap);







            //get Bitmap from image uri
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), pickedImgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,0,baos);
            byte[] data = baos.toByteArray();  // convert images to byte
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get uri of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();
//
                            if(uriTask.isSuccessful())
                            {
                                //add image uri and other info to database
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("chats");
                                Query query =dbRef.orderByChild("timestamp").equalTo(timeStamp);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot ds : dataSnapshot.getChildren())
                                        {
                                            HashMap<String,Object> hashMap = new HashMap<>();
                                            hashMap.put("message",downloadUri);
                                            ds.getRef().updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Snackbar sb = Snackbar.make(rl,"Image sent...",Snackbar.LENGTH_LONG);
                                                    sb.setBackgroundTint(ContextCompat.getColor(getApplicationContext(),R.color.colorBlood));
                                                    sb.show();
                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                                //send notification
                                DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child("users");
                                ds.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot d1 : dataSnapshot.getChildren())
                                        {
                                            for(DataSnapshot d2 : d1.getChildren())
                                            {
                                                if(myUID.equals(d2.getKey()))
                                                {
                                                    ModelUser user = d2.getValue(ModelUser.class);

                                                    if(notify)
                                                    {
                                                        new sendNotificationBackground().execute(user.getFullName());

                                                    }
                                                    notify = false;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                                //create chatlist node/child in Firebase Database
                                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                                        .child(myUID)
                                        .child(hisUID);
                                chatRef1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists())
                                        {
                                            chatRef1.child("id").setValue(hisUID);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //create chatlist node/child in Firebase Database
                                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                                        .child(hisUID)
                                        .child(myUID);
                                chatRef2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists())
                                        {
                                            chatRef2.child("id").setValue(myUID);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed
//                        progressDialog.dismiss();
                        }
                    });


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }


    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    //test code
    public static final Uri getUriToDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId) );
        return imageUri;
    }


    public class sendMessageBackground extends AsyncTask<String,Void,Void>
    {

        @Override
        protected Void doInBackground(String... strings) {

            return null;
        }
    }

    public class seenMessageBackground extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            seenMessage();
            return null;
        }
    }


    public class updateUserInfoBackground extends  AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot ds2 : userSnapshot.getChildren()) {
                            if (hisUID!= null && hisUID.equals(ds2.getKey())) {
                                //get Data
                                Map<String, Object> data = (HashMap<String, Object>) ds2.getValue();
                                String name = (String) data.get("FullName");
                                hisImage = (String) data.get("profileImage");
                                String typingStatus = (String) data.get("typingTo");
                                //check typing status
                                if (typingStatus.equals(myUID)) {
                                    userStatusTextView.setText("typing...");
                                } else {
                                    //get value of online status
                                    String onlineStatus = (String) data.get("onlineStatus");
                                    if (onlineStatus.equals("online")) {
                                        userStatusTextView.setText(onlineStatus);
                                    } else {
                                        // convert timestamp to proper time and date
                                        //convert time stamp to dd/mm/yyyy hh:mm am/pm
                                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                                        userStatusTextView.setText("Last seen at: " + dateTime);


                                    }
                                }


                                //set data
                                nameTextView.setText(name);
                                try {
                                    if (!hisImage.isEmpty()) {
                                        Picasso.get().load(hisImage).placeholder(R.drawable.baseline_account_circle_black_48)
                                                .into(profileImageView);
                                    }
                                } catch (Exception e) {
                                    Picasso.get().load(R.drawable.baseline_account_circle_black_48).into(profileImageView);
                                }


                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            return null;
        }
    }



    public class sendPicBackground extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            Dexter.withActivity(ChatActivity.this)
                    .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                showImagePickerOptions();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

            return null;
        }
    }

  public class sendNotificationBackground extends AsyncTask<String,Void,Void>
  {

      @Override
      protected Void doInBackground(String... strings) {


          sendNotification(hisUID,strings[0],"Sent you a photo ...");
          return null;
      }
  }


  public class sendNotificationDocument extends AsyncTask<String,Void,Void>
  {

      @Override
      protected Void doInBackground(String... strings) {

          sendNotification(hisUID,strings[0],"Sent you a pdf...");

          return null;
      }
  }



  public class chatActivityBackground extends  AsyncTask<Void,Void,Void>
  {

      @Override
      protected Void doInBackground(Void... voids) {

          //intent to pass the uid so we get the reciever profile and name
          Intent intent = getIntent();
          hisUID = intent.getStringExtra("hisUID");
         unblockedButtonLayout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 unBlockedUser(hisUID,item2,true);


             }
         });


         checkBlockOrNot(hisUID);




          backNavigationImageView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  finish();
              }
          });

          toolbar.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  boolean isChat=true;
                  Intent intent1 = getIntent();
                  boolean isBlocked =  intent1.getBooleanExtra("Blocked",false);
                  String name = nameTextView.getText().toString().trim();
                  Intent intent = new Intent(ChatActivity.this,SpecifiProfile.class);
                  intent.putExtra("uid",hisUID);
                  intent.putExtra("isChat",isChat);
                  intent.putExtra("name",name);
                  intent.putExtra("Blocked",isBlocked);
                  startActivity(intent);
                  finish();

              }
          });

          mRef = mDatabase.getReference().child("users");
          myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


          //init permission arrays
          cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
          storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

          //layout(Linear layout for recycler view

          LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
          linearLayoutManager.setStackFromEnd(true);
          //recycler view properties
          recyclerView.setHasFixedSize(true);
          recyclerView.setLayoutManager(linearLayoutManager);

          //create api service
          requestQueue = Volley.newRequestQueue(getApplicationContext());





          //search user to get the user info
          new updateUserInfoBackground().execute();

          //click button to send message
          sendButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if(checkConnection()) {


                      String message = messageEditText.getText().toString();
                      notify = true;
                      //get Text from edit Text
//                String message = messageEditText.getText().toString().trim();
                      //check text is empty or not
                      if (TextUtils.isEmpty(message)) {
                          //text Empty
                          Toast.makeText(ChatActivity.this, "Cann't send the empty message...", Toast.LENGTH_SHORT).show();
                      } else {
                          //text not empty
                          new sendMessageTask().execute(message);
                      }
                      //reset edit Text after sending message
                      messageEditText.setText("");

                  }

              }



          });

          //check editText change Listener
          messageEditText.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

              }

              @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                  if (charSequence.toString().trim().length() == 0) {
                      checkTypingStatus("noOne");
                  } else {
                      checkTypingStatus(hisUID);// uid of receiver
                  }
              }

              @Override
              public void afterTextChanged(Editable editable) {

              }
          });



          //click button to import image
          attatchButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  //show Image pick dialog
                  if (checkConnection()) {

                      alertDialog(ChatActivity.this);


                  }
              }
          });











          return null;
      }
  }



  public void  deleteChatListNode()
  {
      String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
      Intent intent = getIntent();
      String hisUID = intent.getStringExtra("hisUID");
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ChatList");
      ref.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for(DataSnapshot ds1 : dataSnapshot.getChildren())
              {
                  if(ds1.exists())
                  {
                      if(myUID.equals(ds1.getKey())) {

                         for(DataSnapshot ds2 : ds1.getChildren())
                         {
                             if(ds2.exists())
                             {
                                 if(hisUID.equals(ds2.getKey()))
                                 {
                                     ds2.getRef().removeValue();

                                 }
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



    public void alertDialog(final Context context) {
        final androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.attatch_button_dialog, null);
        final TextView selectImageFiles = mView.findViewById(R.id.selectImageTextView);
        final TextView selectPdfFiles = mView.findViewById(R.id.selectPdfTextView);

        alert.setView(mView);

        final androidx.appcompat.app.AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        selectImageFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                new sendPicBackground().execute();
            }
        });
        selectPdfFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.setType("application/pdf");
                startActivityForResult(intent1.createChooser(intent1, "Select PDF File"),438);
            }
        });
        alertDialog.show();
    }


  public class sendMessageRemainingTask extends  AsyncTask<String,Void,Void>
  {



      @Override
      protected Void doInBackground(String... strings) {

          DatabaseReference database1= FirebaseDatabase.getInstance().getReference().child("users");
          database1.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  for(DataSnapshot ds1 : dataSnapshot.getChildren())
                  {
                      for(DataSnapshot ds2 : ds1.getChildren())
                      {
                          if(myUID.equals(ds2.getKey()))
                          {
                              ModelUser user = ds2.getValue(ModelUser.class);
                              if(notify)
                              {
                                  sendNotification(hisUID,user.getFullName(),strings[0]);
                              }
                              notify = false;
                          }
                      }
                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });
          //create chatlist node/child in Firebase Database
          DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                  .child(myUID)
                  .child(hisUID);
          chatRef1.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if(!dataSnapshot.exists())
                  {
                      chatRef1.child("id").setValue(hisUID);
                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });

          //create chatlist node/child in Firebase Database
          DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                  .child(hisUID)
                  .child(myUID);
          chatRef2.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if(!dataSnapshot.exists())
                  {
                      chatRef2.child("id").setValue(myUID);
                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });



          return null;
      }
  }

  public class clearChatTask extends AsyncTask<Void,Void,Void>
  {




      @Override
      protected Void doInBackground(Void... voids) {
          clearChat();
          return null;
      }
  }


  public class sendMessageTask extends AsyncTask<String,Void,Void>
  {

      @Override
      protected Void doInBackground(String... strings) {

          sendMessage(strings[0]);

          return null;
      }
  }





    private void searchPost(String searchQuery)
    {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getSender() != null && modelChat.getReceiver() != null)
                    {
                        if(modelChat.getReceiver().equals(hisUID) && modelChat.getSender().equals(myUID) ||
                           modelChat.getReceiver().equals(myUID) && modelChat.getSender().equals(hisUID))
                        {
                            if(modelChat.getMessage().toLowerCase().contains(searchQuery.toLowerCase())){


                                chatList.add(modelChat);
                            }
                        }
                    }



                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }




}





