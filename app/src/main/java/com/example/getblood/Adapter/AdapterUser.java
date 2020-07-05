package com.example.getblood.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.Activities.ChatActivity;
import com.example.getblood.Activities.SpecifiProfile;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterUser extends  RecyclerView.Adapter<AdapterUser.MyHolder> {
     Context context;
     List<ModelUser> userList;
     FirebaseAuth firebaseAuth;
     String myUID;
    //click to block/unblock user
//        holder.blockImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(userList.get(position).isBlocked())
//                {
//                    unBlockedUser(hisUID);
//                }
//                else
//                {
//                    blockedUser(hisUID);
//                }
//            }
//        });
//    }
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
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                if(ds.exists())
                                                {
                                                    Toast.makeText(context, "You're blocked by that user, can't send message", Toast.LENGTH_SHORT).show();
                                                    // blocked , don't proceed further
                                                    return;
                                                }
                                            }
                                            // not blocked , start activity
                                            Intent intent = new Intent(context,ChatActivity.class);
                                            intent.putExtra("hisUID",hisUid);
                                            context.startActivity(intent);

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

    private void unBlockedUser(String hisUID) {
        //unblock the user by removing uid from current user's "BlockedUsers" node

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
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                if (ds.exists()) {
                                                    if (hisUID.equals(ds.getKey())) {
                                                        //remove blocked user data from current users blocked users list
                                                        ds.getRef().removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        //unblocked successfully
                                                                        Toast.makeText(context, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //failed to block
                                                                Toast.makeText(context, "failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void checkBlockedUsers(String hisUID,MyHolder holder,String name)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String myUid = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(myUid.equals(ds2.getKey()))
                        {
                            ds2.getRef().child("BlockedUsers").
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                if(ds.exists())
                                                {
                                                    if(hisUID.equals(ds.getKey()))
                                                    {
                                                        holder.userLayout.setBackground(ContextCompat.getDrawable(
                                                                context,R.color.block_background_color
                                                        ));
                                                        holder.itemView.setClickable(true);
                                                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                                            @Override
                                                            public boolean onLongClick(View view) {

                                                                confirmDialogBox(name,hisUID);
                                                                return true;
                                                            }
                                                        });
                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Toast.makeText(context,  name+" is blocked, can't send the message!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        holder.contactTextView.setText("Blocked,Press to unblock");
                                                        holder.contactTextView.setTypeface(holder.contactTextView.getTypeface(), Typeface.ITALIC);
                                                        holder.contactTextView.setTextColor(ContextCompat.getColor(context,R.color.colorBlack));
                                                        holder.nameTextView.setTextColor(ContextCompat.getColor(context,R.color.colorBlack));
                                                        holder.bloodTextView.setTextColor(ContextCompat.getColor(context,R.color.colorBlack));
                                                        holder.userTypeTextView.setTextColor(ContextCompat.getColor(context,R.color.colorBlack));
                                                        holder.contactTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                                                        holder.locationTextView.setVisibility(View.INVISIBLE);
                                                        holder.contactNameTextView.setTextColor(ContextCompat.getColor(context,R.color.colorBlack));


                                                    }



                                                }
//
                                            }

//
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
    List<String> blockedList;

     boolean isBlocked;

    public AdapterUser(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUID = firebaseAuth.getCurrentUser().getUid();
        blockedList = new ArrayList<>();
        isBlocked = false;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.request_list_item,parent,false);
        return new MyHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // get Data
        String hisUID = userList.get(position).getUID();
        String blood = userList.get(position).getBlood();
        String name =  userList.get(position).getFullName();
        String contact = userList.get(position).getContact();
        String location = userList.get(position).getCity() + ", " + userList.get(position).getState();
        String userType = userList.get(position).getUserType();
        String profileImage = userList.get(position).getProfileImage();


        //set data
        holder.nameTextView.setText(name);
        holder.userTypeTextView.setText(userType);
        holder.bloodTextView.setText(blood);
        String s = "XXXXXXX";
        char[] phoneArr = contact.toCharArray();
        for (int i = phoneArr.length - 3; i < phoneArr.length; i++) {
            s = s + phoneArr[i];
        }
        holder.contactTextView.setText(s);
        holder.locationTextView.setText(location);
        checkBlockedUsers(hisUID,holder,name);
        if(holder.contactTextView.getText().toString().equals("Blocked,Press to unblock"))
        {
            isBlocked = true;
        }

        try {
            Picasso.get().load(profileImage)
                    .placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);
        } catch (Exception e) {

        }

//        holder.blockImageView.setImageResource(R.drawable.ic_chat_check_green);
//        //check if each user if is blocked or not
//        checkIsBlocked(hisUID, holder, position);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //profile clicked
                            /*click to  got to profile Activity with uid , this uid is of clicked user
                             * which will be used to show user specific profile
//                             */
                            Intent intent = new Intent(context, SpecifiProfile.class);
                            intent.putExtra("uid", hisUID);
//                            intent.putExtra("isBlocked",isBlocked);
                            intent.putExtra("isChat",false);
                            context.startActivity(intent);
                        }
                        if (which == 1) {
                            //chat clicked
                            /* clicked user from user list start chatting/messaging
                             */
//                            isBlockedOrNot(hisUID);
//                            Intent intent = new Intent(context, ChatActivity.class);
//                            intent.putExtra("hisUID", hisUID);
//                            context.startActivity(intent);
                              isBlockedOrNot(hisUID);
                        }
                    }
                });
                builder.create().show();
//                Intent intent = new Intent(context, ChatActivity.class);
//                intent.putExtra("hisUID",hisUID);
//                context.startActivity(intent);
            }
        });
    }

    public void confirmDialogBox(String name,String hisUID)
    {
        androidx.appcompat.app.AlertDialog alertbox = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Blocked!")
                .setMessage("Unblock "+name+" for start messaging")
                .setPositiveButton("Unblock", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {


                        onUnBlockClick(hisUID);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }

    private void onUnBlockClick(String hisUID) {
        unBlockedUser(hisUID);
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    //View holder class
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileImageView;
        TextView bloodTextView,nameTextView,contactTextView,locationTextView,userTypeTextView,contactNameTextView;
        LinearLayout userLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init the views
            profileImageView=itemView.findViewById(R.id.profileImageView);
            bloodTextView=itemView.findViewById(R.id.bloodTextView);
            nameTextView=itemView.findViewById(R.id.nameTextView);
            locationTextView=itemView.findViewById(R.id.locationTextView);
            contactTextView=itemView.findViewById(R.id.contactTextView);
            userTypeTextView=itemView.findViewById(R.id.userTypeTextView);
            userLayout = itemView.findViewById(R.id.userLayout);
            contactNameTextView = itemView.findViewById(R.id.contactNameTextView);


        }
    }



}
