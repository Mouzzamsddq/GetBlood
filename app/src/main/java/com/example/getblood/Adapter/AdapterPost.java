package com.example.getblood.Adapter;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.Activities.ChatActivity;
import com.example.getblood.Activities.DateOfBirth;
import com.example.getblood.Activities.FragmentChangeListener;
import com.example.getblood.Activities.LikesActivitty;
import com.example.getblood.Activities.PostActivity;
import com.example.getblood.Activities.SpecifiProfile;
import com.example.getblood.DataModel.ModelPost;
import com.example.getblood.Fragment.profileFragment;
import com.example.getblood.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.List;
import java.util.Locale;

public class AdapterPost  extends  RecyclerView.Adapter<AdapterPost.MyHolder>{

       Context context;
       List<ModelPost> postList;
       View view;

       String myUID;
       String pTime;

       private DatabaseReference pInterestRef; // for like database node
       private DatabaseReference postsRef; // for post ref


        boolean mProcessInterest = false;
    public AdapterPost(Context context, List<ModelPost> postList,View view) {
        this.context = context;
        this.postList = postList;
        this.view=view;
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pInterestRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout row_post.xml

        View view = LayoutInflater.from(context).inflate(R.layout.row_post,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //getData
        String uid = postList.get(position).getUid();
        String userName = postList.get(position).getuName();
        String pTitle = postList.get(position).getpTitle();
        String pDescr = postList.get(position).getpDescr();
        String userType = postList.get(position).getuType();
        String reqType = postList.get(position).getReqType();
        String blood = postList.get(position).getBlood();
        String contact = postList.get(position).getContact();
        String location = postList.get(position).getpLoc();
        String timeStamp= postList.get(position).getpID();
        String profilePicture = postList.get(position).getuDp();
        String email = postList.get(position).getuEmail();
        String pInterest = postList.get(position).getpInterest(); // contains a total no of interes for a post

        //set interest for each post
        setInterest(holder,timeStamp);


        //convert timestamp to dd/mm/yyyy mm:hh am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


        //set data
        holder.usernameTextView.setText(userName);
        holder.pTimeTextView.setText(pTime);
        holder.pTitleTextView.setText(pTitle);
        holder.pDescrTextView.setText(pDescr);
        holder.userTypeTextView.setText(userType);
        holder.requestTypeTextView.setText(reqType);
        holder.bloodTextView.setText(blood);
        holder.contactTextView.setText(contact);
        holder.locationTextView.setText(location);
        holder.pPublishedDateTextView.setText(pTime);
        holder.pInterestTextView.setText(pInterest + " Interests"); // ex : 100 interested

        if(myUID.equals(uid))
        {
            holder.messageButton.setVisibility(View.GONE);
        }
        else
        {

        }


        //set User dp
        try
        {
            Picasso.get().load(profilePicture).placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);
        }
        catch (Exception e)
        {

        }
        //handle button click
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreButton,uid,myUID,timeStamp);
            }
        });

        holder.interestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //get total no of likes for the post, whose like button clicked
                // if currently signed in user has not liked it before
                // increase value by  1 , otherwise decrease value by 1
                // get the id of the post clicked
                if(checkConnection()) {
                    String postId = postList.get(position).getpID();
                    setInterest(holder, postId);


                    changeDatabase(postId, holder, uid);
                }

            }

        });
        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID",uid);
                context.startActivity(intent);
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sharePost(pTitle,pDescr,userType,location,contact,blood,userName,email,reqType,pTime);
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* click to go to the Specific profile activity with uid,this uid of clicked user
                which will be used to show user specific data/posts;
                 */
                if(myUID.equals(uid))
                {
                     showOtherFragment();
                }
                else
                {

                    Intent intent = new Intent(context, SpecifiProfile.class);
                    intent.putExtra("uid",uid);
                    intent.putExtra("isHome",true);
                    context.startActivity(intent);

                }



            }
        });

        holder.pInterestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , LikesActivitty.class);
                intent.putExtra("postId",timeStamp);
                context.startActivity(intent);
            }
        });



        holder.pInterestTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (holder.pInterestTextView.getRight() - holder.pInterestTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if(checkConnection()) {
                            OpenGoogleMap(location);
                        }

                        return true;
                    }
                }
                return false;
            }
        });

   holder.contactTextView.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           if(checkConnection())
           {
               callContact(contact);
           }
       }
   });







    }

    private void callContact(String contact) {

        Uri number = Uri.parse("tel:"+contact);
        Intent callIntent = new Intent(Intent.ACTION_DIAL,number);
        context.startActivity(callIntent);

    }


    public boolean checkConnection()
    {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
            Snackbar sb = Snackbar.make(view,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(context,R.color.colorBlood));
            sb.show();
            return false;
        }


    }


    private void OpenGoogleMap(String location) {

        String uri = "geo:0,0?q="+location;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);




    }

    private void addToListNotifications(String hisUID,String pId , String notification ,MyHolder holder)
    {
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

        setInterestTextView(holder,pId);
    }

    private void changeDatabase(String postId,MyHolder holder,String uid) {

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              ModelPost post = dataSnapshot.getValue(ModelPost.class);
              String pInterest = post.getpInterest();
              changePinterest(Integer.parseInt(pInterest),postId,holder,uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void changePinterest(int pInterest,String postId,MyHolder holder,String uid) {

        pInterestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUID))
                {
                    // already like so remove like
                    postsRef.child(postId).child("pInterest").setValue(String.valueOf(pInterest-1));
                    pInterestRef.child(postId).child(myUID).removeValue();
                }
                else
                {
                    //not Interest , interest it
                    postsRef.child(postId).child("pInterest").setValue(String.valueOf(pInterest+1));
                    pInterestRef.child(postId).child(myUID).setValue("Interested"); // set any value

                    addToListNotifications(""+uid,""+postId,"interested in your post",holder);
                }
                setInterestTextView(holder,postId);


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
        context.startActivity(Intent.createChooser(intent,"Share via")); // message to show  in share dialog
    }

    private void setInterestTextView(MyHolder holder,String pID)
    {

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.child(pID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelPost post = dataSnapshot.getValue(ModelPost.class);
                String pInterest = post.getpInterest();
                holder.pInterestTextView.setText(pInterest+ " interests");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setInterest(MyHolder holder, String postId) {
        pInterestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUID))
                {
                    // user has interested in this post
                    /* To indicate that the post is liked by this(signed in) user
                    change drawable left icon for  interest button
                    change text of interest button to "interested"*/
                     holder.interestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_green_icon,0,0,0);
                     holder.interestButton.setText("Interested");
                }
                else
                {
                    //user has not liked this post
                     /* To indicate that the post is not interest by this(signed in) user
                    change drawable left icon for  interest button
                    change text of "interested" button to "interest"*/
                    holder.interestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thum_red_icon,0,0,0);
                    holder.interestButton.setText("Interest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton moreButton, String uid, String myUID, String timeStamp) {
        //creating popup menu currently having options delete,we will add more options

        PopupMenu popupMenu = new PopupMenu(context,moreButton, Gravity.END);

        //show delete option in oly post of currently signed in user
        if(uid.equals(myUID)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }
        else
        {
            Toast.makeText(context, "You can edit or delete only your own post !", Toast.LENGTH_SHORT).show();
        }



        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(id==0)
                {
                    //delete is clicked
                    if(checkConnection()) {
                        beginDelete(timeStamp);
                    }
                }
                else if(id == 1)
                {
                    // edit is clicked
                    //start add post activitty with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",timeStamp);
                    context.startActivity(intent);

                }
                return false;
            }
        });
        //show menu
        popupMenu.show();


    }

    private void beginDelete(String timeStamp) {

        deletePost(timeStamp);
    }
    public void showOtherFragment()
    {
        Fragment fr=new profileFragment();
        FragmentChangeListener fc=(FragmentChangeListener)context;
        fc.replaceFragment(fr);
    }

    private void deletePost(String timeStamp) {
        //progress bar
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();



        /*steps
          1) Delete data from database
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Query query = ref.orderByChild("pID").equalTo(timeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ds.getRef().removeValue(); /// remove values from firebase

                    //deleted
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder
    {
        //view from row_post.xml
        ImageView profileImageView;
        ImageButton moreButton;
        Button interestButton,messageButton,shareButton;
        TextView usernameTextView,pTimeTextView,pTitleTextView,pDescrTextView,pInterestTextView,userTypeTextView,requestTypeTextView,
        contactTextView,bloodTextView,locationTextView,pPublishedDateTextView;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileImageView = itemView.findViewById(R.id.uProfilePicture);
            interestButton = itemView.findViewById(R.id.interestedButton);
            messageButton=itemView.findViewById(R.id.messageButton);
            shareButton= itemView.findViewById(R.id.shareButton);
            moreButton = itemView.findViewById(R.id.moreButton);

            //init text Views
            usernameTextView=itemView.findViewById(R.id.uNameTextView);
            pTimeTextView=itemView.findViewById(R.id.pTimeTextView);
            pTitleTextView = itemView.findViewById(R.id.postTitleTextView);
            pDescrTextView=itemView.findViewById(R.id.postDescriptionTextView);
            pInterestTextView=itemView.findViewById(R.id.interestedTextView);
            userTypeTextView=itemView.findViewById(R.id.pUserTypeTextView);
            requestTypeTextView=itemView.findViewById(R.id.pTypeTextView);
            contactTextView= itemView.findViewById(R.id.pContactTextView);
            bloodTextView = itemView.findViewById(R.id.postBloodTextView);
            locationTextView= itemView.findViewById(R.id.pLocationTextView);
            pPublishedDateTextView=itemView.findViewById(R.id.publishedTimeTextView);
            profileLayout=itemView.findViewById(R.id.profileLayout);



        }
    }
}


