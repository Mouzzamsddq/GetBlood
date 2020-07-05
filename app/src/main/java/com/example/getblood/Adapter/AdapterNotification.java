package com.example.getblood.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.DataModel.ModelNotifications;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends  RecyclerView.Adapter<AdapterNotification.MyHolder> {


    private Context context;
    private ArrayList<ModelNotifications> notificationsArrayList;


    public AdapterNotification(Context context, ArrayList<ModelNotifications> notificationsArrayList) {
        this.context = context;
        this.notificationsArrayList = notificationsArrayList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        // get and set data to view

        //get data
        ModelNotifications model = notificationsArrayList.get(position);
        String notification = model.getNotification();
        String timeStamp = model.getTimeStamp();
        String senderUid = model.getsUid();
        String pId = model.getpId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds1.getChildren())
                    {
                        if(senderUid!= null && senderUid.equals(ds2.getKey()))
                        {
                            ModelUser user = ds2.getValue(ModelUser.class);
                            String name = user.getFullName();
                            String image = user.getProfileImage();
                            String email = user.getEmailId();

                            //add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to views
                            holder.usernameTextView.setText(name);
                            try
                            {
                                Picasso.get().load(image).placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);

                            }
                            catch (Exception e )
                            {
                                holder.profileImageView.setImageResource(R.drawable.baseline_account_circle_black_48);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //convert timestamp to dd/mm/yyyy mm:hh am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
          String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


        //set to views
        holder.notificationTextView.setText(notification);
        holder.timeStampTextView.setText(pTime);



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent();
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });

        //long press to show  delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //delete notification
                         DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("users");
                         ref.addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                 for(DataSnapshot ds1 : dataSnapshot.getChildren())
                                 {
                                     for(DataSnapshot ds2 : ds1.getChildren())
                                     {
                                         if(myUID.equals(ds2.getKey()))
                                         {
                                             ds2.getRef().child("notifications").child(timeStamp).removeValue()
                                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                         @Override
                                                         public void onSuccess(Void aVoid) {
                                                             //deleted
                                                             Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();


                                                         }
                                                     })
                                                     .addOnFailureListener(new OnFailureListener() {
                                                         @Override
                                                         public void onFailure(@NonNull Exception e) {

                                                             //failed
                                                             Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationsArrayList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder
    {

        private TextView usernameTextView,timeStampTextView,notificationTextView;
        private ImageView profileImageView;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.notNameTextView);
            timeStampTextView = itemView.findViewById(R.id.notTimeTextView);
            notificationTextView= itemView.findViewById(R.id.notificationsTextView);

            profileImageView = itemView.findViewById(R.id.notProfileImageView);



        }
    }
}
