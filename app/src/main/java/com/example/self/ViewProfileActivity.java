package com.example.self;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import model.User;
import util.JournalApi;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private TextView usernameText,fullNameText;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        profilePic = findViewById(R.id.vp_imageView);
        usernameText = findViewById(R.id.vp_username);
        fullNameText = findViewById(R.id.vp_fullName);

        user = (User)getIntent().getSerializableExtra("User");
        if(user != null){
            usernameText.setText(String.format("Username: %s", user.getUserName()));
            fullNameText.setText(String.format("Name: %s", user.getFullName()));
            Picasso.get().load(user.getProfilePicUrl())
                    .placeholder(R.drawable.pexelsphoto)
                    .fit().into(profilePic);
        }
        else {
            usernameText.setText(String.format("Username: %s", JournalApi.getInstance().getUsername()));
            fullNameText.setText(String.format("Name: %s", JournalApi.getInstance().getFullName()));
            Picasso.get().load(JournalApi.getInstance().getProfilePicUrl())
                    .placeholder(R.drawable.pexelsphoto)
                    .fit().into(profilePic);
        }

    }
}
