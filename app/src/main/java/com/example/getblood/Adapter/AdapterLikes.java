package com.example.getblood.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getblood.DataModel.ModelUser;
import com.example.getblood.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterLikes  extends  RecyclerView.Adapter<AdapterLikes.MyHolder>{

    Context context;
    List<ModelUser> userList;

    public AdapterLikes(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_likes_users,parent,false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String email  =  userList.get(position).getEmailId();
        String name  = userList.get(position).getFullName();
        String profileUrl = userList.get(position).getProfileImage();

        try
        {
            Picasso.get().load(profileUrl).placeholder(R.drawable.baseline_account_circle_black_48).into(holder.profileImageView);
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }

        holder.personNameTextView.setText(name);
        holder.PersonEmailTextView.setText(email);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder
    {

        private TextView personNameTextView,PersonEmailTextView;
        private ImageView profileImageView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            personNameTextView = itemView.findViewById(R.id.personNameTextView);
            PersonEmailTextView = itemView.findViewById(R.id.personEmailTextView);
            profileImageView = itemView.findViewById(R.id.likesProfileImageView);


        }
    }
}
