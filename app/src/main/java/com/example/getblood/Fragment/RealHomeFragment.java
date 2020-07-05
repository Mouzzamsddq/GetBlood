package com.example.getblood.Fragment;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.getblood.Activities.FragmentChangeListener;
import com.example.getblood.Activities.HomeActivity;
import com.example.getblood.Activities.LogInOptions;
import com.example.getblood.Activities.PostActivity;
import com.example.getblood.Adapter.AdapterPost;
import com.example.getblood.DataModel.ModelPost;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RealHomeFragment extends Fragment {
    private RecyclerView recyclerView;
    List<ModelPost>  postList;
    AdapterPost adapterPost;

    FirebaseAuth mAuth;
    NavigationView navigationView;
    List<ModelPost> filteredPostList;
    List<String> blockUserList;
    private ProgressDialog pd;

    Toolbar toolbar;

    private SwipeRefreshLayout homeRefreshLayout;
    private FrameLayout fm;
    public RealHomeFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Get Blood");
        filteredPostList = new ArrayList<>();
        blockUserList = new ArrayList<>();
        navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_home);
        View view =inflater.inflate(R.layout.fragment_real_home, container, false);


        mAuth = FirebaseAuth.getInstance();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postRecyclerView);
        homeRefreshLayout = view.findViewById(R.id.homeRefreshLayout);
        fm = view.findViewById(R.id.frameHome);
        checkConnection();

        homeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    if(checkConnection()) {
                        showOtherFragment();

                        homeRefreshLayout.setRefreshing(false);
                    }
                    else
                    {
                        homeRefreshLayout.setRefreshing(false);
                    }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newest post first,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();
        checkPost();
        checkSpecificPost();
        return  view;
    }

    private void showOtherFragment() {

        Fragment fr=new RealHomeFragment();
        FragmentChangeListener fc=(FragmentChangeListener)getContext();
        pd.hide();
        fc.replaceFragment(fr);

    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPost = new AdapterPost(getActivity(),postList,fm);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterPost);
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(String searchQuery)
    {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())
                    || modelPost.getBlood().toLowerCase().contains(searchQuery.toLowerCase()) ||
                     modelPost.getpLoc().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getuName().toLowerCase().contains(searchQuery.toLowerCase())){


                        postList.add(modelPost);
                    }


                    //adapter
                    adapterPost = new AdapterPost(getActivity(),postList,fm);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu_resource,menu);

        //search view to search post by post  title/description and blood group
        MenuItem item = menu.findItem(R.id.home_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        super.onCreateOptionsMenu(menu,inflater);

//        search listener
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
                    loadPosts();
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
                    loadPosts();
                }
                return false;
            }
        });
//
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();


        switch(id)
        {

            case R.id.home_addPost:
                startActivity(new Intent(getContext(), PostActivity.class));
                break;
            case R.id.home_search:
                break;


        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser() ;
        if(user!= null)
        {

        }
        else
        {
            startActivity(new Intent(getContext(), LogInOptions.class));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        checkUserStatus();
        checkSpecificPost();
    }
    private  void checkPost()
    {
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

                                            addPost(blockUserList);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addPost(List<String> blockUserList) {

        Log.d("AddPost",blockUserList.toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds1 : dataSnapshot.getChildren())
                {
                    ModelPost post = ds1.getValue(ModelPost.class);
                    String uid = post.getUid();
                    if(!blockUserList.contains(uid))
                    {
                         filteredPostList.add(post);
                    }
                    adapterPost = new AdapterPost(getActivity(),filteredPostList,fm);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private  void checkSpecificPost()
    {
        blockUserList.clear();
       String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
       DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
       ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(DataSnapshot ds1 : dataSnapshot.getChildren())
               {
                   for(DataSnapshot ds2 : ds1.getChildren())
                   {
                       ds2.getRef().child("BlockedUsers")
                               .addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                       for(DataSnapshot ds3 : dataSnapshot.getChildren())
                                       {
                                           if(myUID.equals(ds3.getKey()))
                                           {
                                               blockUserList.add(ds2.getKey());
                                           }
                                       }
                                       addSpecificPost(blockUserList);
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void addSpecificPost(List<String> blockUserList) {

       List<String> filteredBlockList = new ArrayList<>();
        Log.d("SpecifBlock",blockUserList.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_home);
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
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }

}
