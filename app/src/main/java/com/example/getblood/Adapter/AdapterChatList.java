package com.example.getblood.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.Activities.ChatActivity;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList  extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUser> userList; // get user info
    private HashMap<String,Object> lastMessageMap;

    //constructor


    public AdapterChatList(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout row_chatList.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chat_list,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUId = userList.get(position).getUID();
        String  userImage = userList.get(position).getProfileImage();
        String userName = userList.get(position).getFullName();
        String lastMessage = (String) lastMessageMap.get(hisUId);
        checkBlockedUsers(hisUId,holder);
        //set data
        holder.nameTextView.setText(userName);
        if(!holder.lastMessageTextView.getText().toString().equals("Blocked")) {
            if (lastMessage == null || lastMessage.equals("default")) {
                holder.lastMessageTextView.setVisibility(View.GONE);
            } else {
                holder.lastMessageTextView.setVisibility(View.VISIBLE);
                holder.lastMessageTextView.setText(lastMessage);
            }
        }
        try
        {
            Picasso.get().load(userImage).placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);
        }
        catch (Exception e)
        {
            Picasso.get().load(userImage).placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);
        }
        //set online status of other users in chat List
        if(userList.get(position).getOnlineStatus().equals("online"))
        {
            //online
            holder.onlineStatusImageView.setImageResource(R.drawable.circle_online);
        }
        else
        {
            //offline
            holder.onlineStatusImageView.setImageResource(R.drawable.circle_offline);
        }

        //handle click  of user in chat List
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID",hisUId);
                if(holder.lastMessageTextView.getText().toString().equals("Blocked"))
                {
                    intent.putExtra("Blocked",true);
                }
                else
                {
                    intent.putExtra("Blocked",false);
                }
                context.startActivity(intent);
            }
        });


    }

    public void setLastMessageMap(String userId, String lastMessage)
    {
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();  //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //view of the chat list.xml
        ImageView profileImageView,onlineStatusImageView;
        TextView nameTextView,lastMessageTextView;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init view
            profileImageView = itemView.findViewById(R.id.chatListProfileImageView);
            onlineStatusImageView=itemView.findViewById(R.id.onlineStatusImageView);
            nameTextView = itemView.findViewById(R.id.chatListUserNameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);

        }
    }
    private void checkBlockedUsers(String hisUID, MyHolder holder)
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
                                                        holder.lastMessageTextView.setVisibility(View.VISIBLE);
                                                         holder.lastMessageTextView.setText("Blocked");
                                                        holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.ITALIC);

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
}
