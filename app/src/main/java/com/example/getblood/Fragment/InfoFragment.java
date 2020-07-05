package com.example.getblood.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.getblood.Activities.DateOfBirth;
import com.example.getblood.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {


    private NavigationView navigationView;
    private FrameLayout fm;




    public InfoFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);


        navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.nav_info);
        fm=view.findViewById(R.id.frameInfo);
        checkConnection();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Information");
        YouTubePlayerView youTubePlayerView =view.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);





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
