package com.example.getblood.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.Activities.Donor;
import com.example.getblood.Activities.ProfileImage;
import com.example.getblood.Activities.SplashScreen;
import com.example.getblood.DataModel.ModelChat;
import com.example.getblood.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.logging.LogRecord;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
     private static  final int MSG_TYPE_LEFT = 0;
     private static final int MSG_TYPE_RIGHT= 1;
     Context context;
     List<ModelChat> chatList;
     String imageUrl;


     FirebaseUser user;
    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right , parent , false);
            return  new MyHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_left , parent , false);
            return  new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp= chatList.get(position).getTimestamp();
        String type= chatList.get(position).getType();
        String pdfFileName = chatList.get(position).getFileName();
        String pdfUri = chatList.get(position).getMessage();






        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        if(type.equals("text"))
        {
            //text message
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageImageView.setVisibility(View.GONE);

            holder.messageTextView.setText(message);

        }
        else if(type.equals("image"))
        {
            //Image message
            holder.messageTextView.setVisibility(View.GONE);
            holder.messageImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_image_black_24dp).into(holder.messageImageView);

        }
        else
        {
            //pdf message
            holder.messageTextView.setVisibility(View.GONE);
            holder.messageImageView.setVisibility(View.GONE);
            holder.pdfRelativeLayout.setVisibility(View.VISIBLE);
            holder.pdfNameTextView.setText(pdfFileName);
            holder.pdfRelativeLayout.setTag(pdfUri);
        }

        //set data
        holder.messageTextView.setText(message);
        holder.timeTextView.setText(dateTime);
        try
        {
            Picasso.get().load(imageUrl).into(holder.profileImageView);
        }
        catch (Exception e)
        {

        }

        //click to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //show delete message confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position,holder);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();

                return false;
            }
        });

        //set seen/ delivered status of message
        if(position==chatList.size()-1)
        {
//            Toast.makeText(context, "one", Toast.LENGTH_SHORT).show();
            if(chatList.get(position).isSeen())
            {
                holder.isSeenTextView.setText("Seen");

            }
            else
            {
                holder.isSeenTextView.setText("Delivered");

            }
        }
        else
        {
            holder.isSeenTextView.setVisibility(View.GONE);
//            Toast.makeText(context, "four", Toast.LENGTH_SHORT).show();
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullImage(position);
            }
        });


        holder.pdfRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pdfUri = holder.pdfRelativeLayout.getTag().toString();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(pdfUri));
                context.startActivity(intent);
            }
        });

        holder.pdfRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //show delete message confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position,holder);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();




                return false;
            }
        });



    }


    private void openFullImage(int position)
    {
        String msgType = chatList.get(position).getType();
        if(msgType!= null && msgType.equals("image"))
        {
            String msgTimeStamp = chatList.get(position).getTimestamp();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("chats");
            Query query =dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        String imageUri = chat.getMessage();
                        try
                        {
                            Intent intent1 = new Intent(context, ProfileImage.class);
                            intent1.putExtra("profileUrl", imageUri);
                            context.startActivity(intent1);
                        }
                        catch (Exception e)
                        {
                            e.getMessage();
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

    }

    private void deleteMessage(int position,MyHolder holder) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();



        /* Logic:
        * Get timestamp of clicked message
        * Compare the timestamp of the clicked message with all message in chats
        * where both value matches delete that messages
         */
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("chats");
        Query query =dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    /* If you want to allow sender to delete only his message then
                    compare sender value with current user's UID
                    if they match means its the message of sender that is trying to delete
                     */
                    if(ds.child("sender").getValue().equals(myUID))
                    {
                        /* we can do one of two things here
                         * 1) Remove the message from chats
                         * 2) Set the value of message "This message was deleted..."
                         * so do whatever you want
                         */

                        // Remove the message from chats
                      //  ds.getRef().removeValue(); // now test this

                        //2) Set the the value of message "This message was deleted..." //test this
                        if(ds.child("type").getValue().equals("text")) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("message", "This message was deleted...");
                            ds.getRef().updateChildren(hashMap);
                            Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                        }
                        else if(ds.child("type").getValue().equals("image"))
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("message","This message was deleted...");
                            hashMap.put("type","text");
                            ds.getRef().updateChildren(hashMap);
                            Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("message","This message was deleted...");
                            hashMap.put("type","text");
                            ds.getRef().updateChildren(hashMap);
                            Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current signed in user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(user.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views
        ImageView profileImageView,messageImageView;
        TextView messageTextView,timeTextView,isSeenTextView;
        LinearLayout messageLayout; //for click listener to show delete
        RelativeLayout pdfRelativeLayout;
        TextView pdfNameTextView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            //init views
            profileImageView = itemView.findViewById(R.id.profileImageView);
            messageTextView=itemView.findViewById(R.id.messageTextView);
            messageImageView=itemView.findViewById(R.id.messageIv);
            timeTextView=itemView.findViewById(R.id.timeTextView);
            isSeenTextView=itemView.findViewById(R.id.isSeenTextView);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            pdfRelativeLayout = itemView.findViewById(R.id.pdfRelativeLayout);
            pdfNameTextView = itemView.findViewById(R.id.pdfNameTextView);




        }
    }
}
