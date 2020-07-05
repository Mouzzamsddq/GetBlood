package com.example.getblood.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.getblood.Activities.ImagePickerActivity;
import com.example.getblood.Activities.FragmentChangeListener;
import com.example.getblood.Activities.ImagePickerActivity;
import com.example.getblood.Activities.LogInOptions;
import com.example.getblood.Activities.ProfileImage;
import com.example.getblood.Activities.SpecifiProfile;
import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class profileFragment extends Fragment {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    //"(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");
    private FirebaseDatabase mDatabase;
    private static final Pattern NAME_PATTERN=Pattern.compile("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    private FirebaseAuth mAuth;
    String updateUserType="";
    private boolean isDonor = false;
    SwipeRefreshLayout profileRefreshLayout;
    private FirebaseUser user;

    private RelativeLayout rl ;
    Uri pickedImgUri;
    LinearLayout linearLayout;
    NavigationView navigationView;
    String storagePath = "users/users_profile_image/";
    boolean found = false;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private static final String TAG = "TAG";
    public static final int REQUEST_IMAGE = 100;
    private View view;
    FloatingActionButton fab;
    private ImageView profileImageView, profileImagePlusView;
    private ProgressDialog pd;
    private DatabaseReference mRef;
    private RecyclerView userProfileRecyclerView;
   private TextView profileUserNameTextView,profileUserEmailTextView;
   List<ModelPost> postList;
    String profileUrl;
   AdapterPost adapterPost;
   ProgressDialog progressDialog;
   String uid,name,email;
   Button updateProfileButton,profileCancelButton;
   ScrollView sc;
    public profileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        sc = view.findViewById(R.id.scrollViewProfile);
        setHasOptionsMenu(true);
        checkUserStatus();
        pd = new ProgressDialog(getContext());
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        setHasOptionsMenu(true);
        rl = view.findViewById(R.id.profileLayout);
        linearLayout = view.findViewById(R.id.uploadLayout);
        mDatabase = FirebaseDatabase.getInstance();
        profileRefreshLayout = view.findViewById(R.id.profileRefreshLayout);
        mRef = mDatabase.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_profile);
        profileCancelButton = view.findViewById(R.id.profileCancelButton);
        updateProfileButton=view.findViewById(R.id.uploadProfileButton);
        userProfileRecyclerView = view.findViewById(R.id.fragmentProfileRecyclerView);
        fab = view.findViewById(R.id.fabProfileButton);
        mStorageRef = storage.getReferenceFromUrl("gs://get-blood-f8318.appspot.com");
        profileImageView = view.findViewById(R.id.profileImageView);
        profileImagePlusView = view.findViewById(R.id.imagePlusProfile);
        profileUserEmailTextView=view.findViewById(R.id.profileUserEmailTextView);
        profileUserNameTextView = view.findViewById(R.id.profileUserNameTextView);
        postList = new ArrayList<>();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot donorRecipientSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot usersSnapshot : donorRecipientSnapshot.getChildren()) {
                        if (user.getUid().equals(usersSnapshot.getKey())) {
                            Map<String, Object> data = (HashMap<String, Object>) usersSnapshot.getValue();
                             name = (String) data.get("FullName");
                            email = (String) data.get("emailId");
                            profileUserEmailTextView.setText(email);
                            profileUserNameTextView.setText(name);
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(name);
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

        profileCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkConnection()) {
                    linearLayout.setVisibility(View.INVISIBLE);
                    showOtherFragment();
                }

            }
        });


         loadMyPosts();

         profileRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 if(checkConnection()) {
                     showOtherFragment();
                     profileRefreshLayout.setRefreshing(false);
                 }
                 else
                 {
                     profileRefreshLayout.setRefreshing(false);
                 }
             }
         });
         profileImageView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(checkConnection()) {
                     if (profileUrl != null) {
                         Intent intent1 = new Intent(getContext(), ProfileImage.class);
                         intent1.putExtra("profileUrl", profileUrl);
                         intent1.putExtra("name", name);
                         startActivity(intent1);
                     }
                 }
             }
         });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkConnection()) {
                    if (pickedImgUri != null && !pickedImgUri.equals(Uri.EMPTY)) {
                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Updating profile...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        linearLayout.setVisibility(View.INVISIBLE);
                        View hView = navigationView.getHeaderView(0);
                        ImageView headerImageView = hView.findViewById(R.id.headerImageView);
                        try {
                            Picasso.get().load(pickedImgUri).placeholder(R.drawable.baseline_account_circle_black_48).into(headerImageView);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.baseline_account_circle_black_48).placeholder(R.drawable.baseline_account_circle_black_48)
                                    .into(headerImageView);
                        }
//                    TextView usernameTextView=(TextView)hView.findViewById(R.id.headerUsernameTextView);
                        uploadImageFirebase(pickedImgUri);

                    }


                }
            }
        });
        // Clearing older images from cache directory
        // don't call this line if you want to choose multiple images in the same activity
        // call this once the bitmap(s) usage is over
        ImagePickerActivity.clearCache(getContext());
//
        profileImagePlusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(getActivity())
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
            }

        });

        // fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
        return view;
    }


    private void showEditProfileDialog() {
        /* show dialog containing options
        * edit username
        * edit contact
        * change password
         */

        //options to show in dialog
        String options[] ={"Edit Name", "Edit Phone", "Change Password"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i ==0)
                {
                    //edit name clicked
                        pd.setMessage("Updating username...");
                        //calling method and pass key "name" as paarmeter to update its value in database
                        showNamePhoneUpdateDialog("FullName");

                }
                if(i==1)
                {
                    //edit phone clicked
                        pd.setMessage("Updating Contact no...");
                        showNamePhoneUpdateDialog("Contact");

                }
                if(i==2)
                {
                    //edit  password clicked
                    pd.setMessage("Changing Password");
                    showChangePasswordDialog();
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
//        password change layout with custom  password having current passwrod , new password and update password button

          //inflate layout for dialog
          View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);
          TextInputEditText currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        TextInputEditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        Button updatePasswordButton = view.findViewById(R.id.updatePasswordButton);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setView(view); // set view to dialog

        AlertDialog dialog = builder.create();
        dialog.show();


        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPassword = currentPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                if (checkConnection()) {
                    if (validatePassword(currentPassword, currentPasswordEditText)) {
                        if (validatePassword(newPassword, newPasswordEditText)) {
                            dialog.dismiss();
                            updatePassword(currentPassword, newPassword);
                        }
                    }
                }
                else
                {
                    dialog.dismiss();
                }
            }
        });




    }

    private void updatePassword(String currentPassword, String newPassword) {
        pd.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        // before chamging the password reauthenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(),currentPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // successfully authenticated , begin update
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password updated
                                        pd.dismiss();
                                        Toast.makeText(getContext(), "Password Updated...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //authentication failed
                        pd.dismiss();
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);

        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // add edit text
        CountryCodePicker countryCodePicker = new CountryCodePicker(getActivity());
        countryCodePicker.setCountryForPhoneCode(91);

        TextInputLayout textInputLayout = new TextInputLayout(getActivity());
        textInputLayout.setPadding(3,3,3,3);
        textInputLayout.setErrorEnabled(true);

        TextInputEditText editText = new TextInputEditText(getActivity());
        editText.setHint("Enter "+key);
        if(key.equals("Contact")) {

            linearLayout.setPadding(10,10,10,10);

           LinearLayout linearLayout1 = new LinearLayout(getActivity());
           linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams. WRAP_CONTENT ,
                    LinearLayout.LayoutParams. WRAP_CONTENT ) ;
            layoutParams.setMargins( 0 , 50 , 0 , 0 ) ;

           editText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_phone_red_icon,0);
           editText.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.block_background_color));
           editText.setInputType(InputType.TYPE_CLASS_PHONE);
           linearLayout1.addView(countryCodePicker,layoutParams);

            editText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorBlood));
            editText.setHintTextColor(ContextCompat.getColor(getActivity(),R.color.colorBlood));
           textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
           textInputLayout.setPadding(5,8,5,5);
           editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
           textInputLayout.addView(editText);
           linearLayout1.addView(textInputLayout);
           linearLayout.addView(linearLayout1);
        }
        else
        {
            builder.setTitle("Update Name");
            linearLayout.setPadding(10,10,10,10);
            editText.setHintTextColor(ContextCompat.getColor(getActivity(),R.color.colorBlood));
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.block_background_color));
            editText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_person_red_icon,0);
            editText.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorBlood));
            textInputLayout.addView(editText);
            linearLayout.addView(textInputLayout);



        }


        builder.setView(linearLayout);

        //add buttons to dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (checkConnection()) {
                    String value = editText.getText().toString().trim();
                    String countryCode = "+" + countryCodePicker.getSelectedCountryCode();
                    if (checkConnection()) {
                        if (key.equals("FullName")) {
                            if (validateName(value)) {
                                pd.show();
                                View hView = navigationView.getHeaderView(0);
                                TextView usernameTextView = (TextView) hView.findViewById(R.id.headerUsernameTextView);
                                usernameTextView.setText(value);
                                profileUserNameTextView.setText(value);
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(value);
                                uploadDataToFirebase(key, value);
                            }
                        } else {

                            if (validatePhone(value)) {
                                value = countryCode + value;
                                pd.show();
                                uploadDataToFirebase(key, value);
                            }


                        }
                    }
                }
            }
        });
        //add cancel button to dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void uploadDataToFirebase(String key, String value) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot donorRecipient : dataSnapshot.getChildren()) {
                            for (DataSnapshot usersSnapshot : donorRecipient.getChildren()) {
                                if (user.getUid().equals(usersSnapshot.getKey())) {
                                    Map<String, Object> data = (HashMap<String, Object>) usersSnapshot.getValue();
                                    String userType = (String) data.get("userType");
                                    if (userType.equals("Donor")) {
                                            mRef.child("Donor").child(user.getUid()).child(key).setValue(value);
                                            found = false;
                                            pd.dismiss();
                                            if(key.equals("FullName"))
                                            {

                                                Toast.makeText(getContext(), "Username successfully updated..!", Toast.LENGTH_SHORT).show();
                                                updateDataOfPostNode(key,value);
                                            }
                                            else
                                            {
                                                Toast.makeText(getContext(), "Phone no successfully updated..!", Toast.LENGTH_SHORT).show();
                                            }





                                    } else {

                                            mRef.child("Recipient").child(user.getUid()).child(key).setValue(value);
                                            found = true;
                                            pd.dismiss();
                                        if(key.equals("FullName"))
                                        {
                                            Toast.makeText(getContext(), "Username successfully updated..!", Toast.LENGTH_SHORT).show();
                                            updateDataOfPostNode(key,value);
                                        }
                                        else
                                        {
                                            Toast.makeText(getContext(), "Phone no successfully updated..!", Toast.LENGTH_SHORT).show();
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

    private void updateDataOfPostNode(String key, String value) {
        String myUID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Query query = ref.orderByChild("uid").equalTo(myUID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(DataSnapshot ds : dataSnapshot.getChildren())
               {
                   if(key.equals("FullName")) {
                       ref.child(ds.getKey()).child("uName").setValue(value);
                   }

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadMyPosts() {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post  first ,for this load from list
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycler view

        userProfileRecyclerView.setLayoutManager(layoutManager);


        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
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
                    adapterPost = new AdapterPost(getActivity(),postList,rl);
                    //set the adapter to recycler view
                    userProfileRecyclerView.setAdapter(adapterPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(String searchQuery) {
        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post  first ,for this load from list
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycler view

        userProfileRecyclerView.setLayoutManager(layoutManager);


        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
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
                   myPosts.getpLoc().toLowerCase().contains(searchQuery.toLowerCase()) || myPosts.getuName().toLowerCase().contains(searchQuery.toLowerCase()))

                   {
                       //add to list
                       postList.add(myPosts);
                   }



                    //adapter
                    adapterPost = new AdapterPost(getActivity(),postList,rl);
                    //set the adapter to recycler view
                    userProfileRecyclerView.setAdapter(adapterPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadProfile(String url) {
        Log.d(TAG, "Image cache path: " + url);

        Picasso.get().load(url).placeholder(R.drawable.baseline_account_circle_black_48)
                .into(profileImageView);
        profileImageView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.transparent));
    }

//    private void loadProfileDefault() {
//        GlideApp.with(this).load(R.drawable.baseline_account_circle_black_48)
//                .into(profileImageView);
//        profileImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.profile_default_tint));
//    }


    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(getContext(), new ImagePickerActivity.PickerOptionListener() {
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

    private void launchCameraIntent() {
        Intent intent = new Intent(getContext(), ImagePickerActivity.class);
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

    private void launchGalleryIntent() {
        Intent intent = new Intent(getContext(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                pickedImgUri = uri;
                linearLayout.setVisibility(View.VISIBLE);
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);


                    // loading profile image from local cache
                    loadProfile(uri.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



        private void uploadImageFirebase(Uri imageUri) {

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot donorRecipient : dataSnapshot.getChildren()) {
                        for (DataSnapshot usersSnapshot : donorRecipient.getChildren()) {
                            if (user.getUid().equals(usersSnapshot.getKey())) {
                                Map<String, Object> data = (HashMap<String, Object>) usersSnapshot.getValue();
                                updateUserType = (String) data.get("userType");


                                if (updateUserType != null && updateUserType.equals("Donor")) {
                                    Toast.makeText(getContext(), updateUserType, Toast.LENGTH_SHORT).show();

                                storagePath = storagePath + "Donor_profile/profile_" + user.getUid() + ".png";
                                mStorageRef = mStorageRef.child(storagePath);
                                isDonor = true;
                            }
                             else {
                                    storagePath = storagePath + "Recipient_profile/profile_" + user.getUid() + ".png";
                                    mStorageRef = mStorageRef.child(storagePath);
                                    isDonor = false;



                            }



                                mStorageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Picasso.get().load(uri).into(profileImageView);
                                                Map<String, Object> results = new HashMap<>();
                                                results.put("profileImage", uri.toString());
                                                Task task2;
                                                if (isDonor) {
                                                    task2 = mRef.child("Donor").child(user.getUid()).updateChildren(results);
                                                } else {
                                                    task2 = mRef.child("Recipient").child(user.getUid()).updateChildren(results);
                                                }
                                                task2.addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                                                        updateUserPostProfile(uri.toString());
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), "Error in uploading image", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
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

    private void updateUserPostProfile(String imageUri) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Query query = ref.orderByChild("uid").equalTo(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {

                        ref.child(ds.getKey()).child("uDp").setValue(imageUri);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        Uri uri = Uri.fromParts("package", getActivity().toString(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu_resource,menu);
        MenuItem item = menu.findItem(R.id.home_addPost);
        item.setVisible(false);

        //searchview of search user specific post
        MenuItem item1 = menu.findItem(R.id.home_search);
        SearchView searchView= (SearchView)MenuItemCompat.getActionView(item1);
        super.onCreateOptionsMenu(menu, inflater);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if(!TextUtils.isEmpty(s))
                {
                    //search
                    searchMyPosts(s);
                }
                else
                {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                //called whenever user type letter
                if(!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }
                else
                {
                    loadMyPosts();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        switch(id)
        {
            case R.id.home_search:
                Toast.makeText(getContext(), "Profile Search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.home_addPost:
                Toast.makeText(getContext(), "profile Add post", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
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
            startActivity(new Intent(getContext(),LogInOptions.class));
        }
    }
    public boolean validateName(String name) {
        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            Toast.makeText(getContext(), "Please enter alphabet only", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
    public boolean validatePhone(String Phone)
    {
        if(Phone.isEmpty()) {
            Toast.makeText(getActivity(), "Field can't be empty", Toast.LENGTH_SHORT).show();
            return false;

        }
        else if(!Patterns.PHONE.matcher(Phone).matches())
        {
            Toast.makeText(getContext(), "Please enter a valid phone no!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(Phone.length()<10)
        {
            Toast.makeText(getContext(), "Please enter a valid phone no!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {

            return true;
        }

    }
    public boolean validatePassword(String passwordInput, TextInputEditText textInputPassword) {
        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPassword.setError("Password is too weak");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }
    private void showOtherFragment() {

        Fragment fr = new profileFragment();
        FragmentChangeListener fc = (FragmentChangeListener) getContext();
        fc.replaceFragment(fr);
    }
    public boolean checkConnection()
    {
        ConnectivityManager manager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

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
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }
    private boolean checkAllConnection()
    {

        ConnectivityManager manager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

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
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }

    }
}
