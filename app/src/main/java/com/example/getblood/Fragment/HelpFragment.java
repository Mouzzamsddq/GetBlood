package com.example.getblood.Fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.getblood.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends Fragment {
     private NavigationView navigationView;
     private ImageView myImageView;
     private FrameLayout fm;
     String myImageUri = "https://firebasestorage.googleapis.com/v0/b/get-blood-f8318.appspot.com/o/mouzzam.png?alt=media&token=456f343f-9aeb-4fde-83e1-e569a0ec24b7";
    public HelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  =  inflater.inflate(R.layout.fragment_help, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Help");
        navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_help);
        myImageView = view.findViewById(R.id.myImageView);
        fm= view.findViewById(R.id.frameHelp);
        checkConnection();
        try {
            Picasso.get().load(myImageUri).placeholder(R.drawable.baseline_account_circle_black_48).into(myImageView);
        }
        catch (Exception e)
        {
            myImageView.setImageResource(R.drawable.baseline_account_circle_black_48);
        }

        return view;
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
            sb.setBackgroundTint(ContextCompat.getColor(getContext(),R.color.block_background_color));
            sb.setTextColor(ContextCompat.getColor(getContext(),R.color.colorBlood));
            sb.show();
            return false;
        }


    }


}
