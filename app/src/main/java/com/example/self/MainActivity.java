package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class MainActivity extends AppCompatActivity {
    private Button getStarted;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();
                if(user != null){
                    String userId = user.getUid();
                    collectionReference.whereEqualTo("userId",userId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                    if(e != null) {
                                        return;
                                    }

                                    assert queryDocumentSnapshots != null;
                                    if(!queryDocumentSnapshots.isEmpty()){
                                        for(QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserID(snapshot.getString("userId"));
                                            journalApi.setUsername(snapshot.getString("userName"));
                                            journalApi.setFullName(snapshot.getString("fullName"));
                                            journalApi.setProfilePicUrl(snapshot.getString("profilePicUrl"));
                                            ArrayList<String> fl = (ArrayList<String>) snapshot.get("friendList");
                                            if(fl==null)
                                                fl=new ArrayList<>();
                                            ArrayList<String> rrl = (ArrayList<String>) snapshot.get("receivedRequestList");
                                            if(rrl==null)
                                                rrl=new ArrayList<>();
                                            journalApi.setFriendList(fl);
                                            journalApi.setReceivedRequestList(rrl);

                                            startActivity(new Intent(MainActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }

                                }
                            });
                }
                else{

                }
            }
        };

        getStarted = findViewById(R.id.start_button_id);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to login screen
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
