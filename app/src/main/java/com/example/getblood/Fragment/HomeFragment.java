package com.example.getblood.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.getblood.Activities.FragmentChangeListener;
import com.example.getblood.Activities.HomeActivity;
import com.example.getblood.Adapter.AdapterUser;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment {
   private RecyclerView recyclerView;
   private ProgressDialog pd;
   AdapterUser adapterUser;
   NavigationView navigationView;
   List<ModelUser> userList;
   private FrameLayout fm;
    List<String> blockedList ;
    SwipeRefreshLayout userRefreshLayout;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_home, container, false);
        pd= new ProgressDialog(getContext());
        pd.setMessage("Loading...");
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        setHasOptionsMenu(true); // to show menu option in fragment
        fm = view.findViewById(R.id.userHome);
        checkConnection();
       navigationView = getActivity().findViewById(R.id.navigation_view);
       navigationView.setCheckedItem(R.id.nav_users);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Users");
       userRefreshLayout = view.findViewById(R.id.userRefreshLayout);
       userRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {

               showOtherFragment();
               userRefreshLayout.setRefreshing(false);

           }
       });
        recyclerView=view.findViewById(R.id.usersRecyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        userList = new ArrayList<>();
        blockedList = new ArrayList<>();

        //getAllUsers;
        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        pd.show();
        FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named users containing user info

        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference().child("users");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for ( DataSnapshot uidSnapShot : ds.getChildren())
                    {
                        ModelUser modelUser = uidSnapShot.getValue(ModelUser.class);

                        //get all users except current sign in user

                        if(modelUser.getUID()!=null && !modelUser.getUID().equals(firebaseUser.getUid()))
                        {

                            userList.add(modelUser);
                        }

                        //adapter

                        adapterUser = new AdapterUser(getActivity(),userList);
                        //set adapter to recycle view
                        recyclerView.setAdapter(adapterUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        pd.dismiss();
    }

    private void searchUsers(String query) {
        FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named users containing user info

        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference().child("users");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    for ( DataSnapshot uidSnapShot : ds.getChildren())
                    {
                        ModelUser modelUser = uidSnapShot.getValue(ModelUser.class);

                        /* condition to fullfill search:
                        * 1) User not Current user
                        * 2) The user name or email contains text entered in search view (case Insensitive)
                         */

                        //get all search users except current sign in user

                        if(!modelUser.getUID().equals(firebaseUser.getUid()))
                        {
                            if(modelUser.getFullName().toLowerCase().contains(query.toLowerCase()) || modelUser.getBlood().toLowerCase().contains(query.toLowerCase())) {
                                userList.add(modelUser);
                            }
                        }
                        //adapter

                        adapterUser = new AdapterUser(getActivity(),userList);
                        //refresh Adapter
                        adapterUser.notifyDataSetChanged();
                        //set adapter to recycle view
                        recyclerView.setAdapter(adapterUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void  onCreateOptionsMenu(Menu menu,MenuInflater inflater) {

        inflater.inflate(R.menu.my_action_bar_menu,menu);
        //Search View
        MenuItem item = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        super.onCreateOptionsMenu(menu,inflater);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                // if search query is not empty then search

                if(!TextUtils.isEmpty(s.trim()))
                {
                    //search text contains text,search it
                    searchUsers(s);

                }
                else
                {
                    //search text empty , get all users
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // if search query is not empty then search

                if(!TextUtils.isEmpty(s.trim()))
                {
                    //search text contains text,search it
                    searchUsers(s);

                }
                else
                {
                    //search text empty , get all users
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
            case R.id.actionSearch:
                Toast.makeText(getActivity(), "New Item first", Toast.LENGTH_SHORT).show();
                break;
//            case R.id.actionChats:
//                Toast.makeText(getContext(), "New Item Second", Toast.LENGTH_SHORT).show();
//                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showOtherFragment() {

        Fragment fr = new HomeFragment();
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
            pd.dismiss();
            Snackbar sb = Snackbar.make(fm,"No Internet Connection",Snackbar.LENGTH_LONG);
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.block_background_color));
            sb.setTextColor(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }




}
