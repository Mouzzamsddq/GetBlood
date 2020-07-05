package com.example.getblood.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.getblood.Activities.Donor;
import com.example.getblood.Activities.FragmentChangeListener;
import com.example.getblood.Activities.NameActivity;
import com.example.getblood.Adapter.AdapterChatList;
import com.example.getblood.DataModel.ModelChat;
import com.example.getblood.DataModel.ModelChatList;
import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Chat_list_fragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    NavigationView navigationView;
    private FrameLayout fm;

    List<ModelChatList> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatList adapterChatList;
    private SwipeRefreshLayout chatRefreshLayout;

    public Chat_list_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat_list_fragment, container, false);

        //init views
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fm = view.findViewById(R.id.chatFrame);
        checkConnection();
        recyclerView = view.findViewById(R.id.chatListRecyclerView);
        navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_chats);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");
        chatRefreshLayout = view.findViewById(R.id.chatRefreshLayout);
        chatRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showOtherFragment();
                chatRefreshLayout.setRefreshing(false);
            }
        });

        chatlistList = new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for(DataSnapshot  ds : dataSnapshot.getChildren())
                {
                    ModelChatList chatList = ds.getValue(ModelChatList.class);
                    chatlistList.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void loadChats() {

        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for(DataSnapshot ds2 : ds.getChildren()) {
                        ModelUser user = ds2.getValue(ModelUser.class);
                        for (ModelChatList chatList : chatlistList) {
                            if (user.getUID() != null && user.getUID().equals(chatList.getId())) {
                                userList.add(user);
                                break;
                            }
                        }
                        //adapter
                        adapterChatList = new AdapterChatList(getContext(), userList);
                        //set adapter
                        recyclerView.setAdapter(adapterChatList);
                        //set last messsage
                        for (int i = 0; i < userList.size(); i++) {
                            lastMessage(userList.get(i).getUID());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage ="default";
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat==null)
                    {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender==null || receiver == null)
                    {
                        continue;
                    }
                    if(chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){
                        if(chat.getType().equals("image"))
                        {
                            theLastMessage = "Sent a photo";
                        }
                        else if(chat.getType().equals("pdf"))
                        {
                            theLastMessage = "Sent a pdf";
                        }
                        else {
                            theLastMessage = chat.getMessage();
                        }

                    }
                }
                adapterChatList.setLastMessageMap(userId,theLastMessage);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null)
        {

        }
        else
        {
            startActivity(new Intent(getContext(), Donor.class));

        }
    }

    private void showOtherFragment() {

        Fragment fr = new Chat_list_fragment();
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

            Snackbar sb = Snackbar.make(fm,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }



}
