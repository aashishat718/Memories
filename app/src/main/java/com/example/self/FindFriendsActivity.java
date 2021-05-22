package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import model.User;
import ui.FindFriendsAdapter;
import util.JournalApi;

public class FindFriendsActivity extends AppCompatActivity {

    private static final String TAG = "FindFriendsActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private StorageReference storageReference;

    private RecyclerView recyclerView;
    private FindFriendsAdapter findFriendsAdapter;

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private TextView noPerson;
    private TextView heading;
    private ProgressBar progressBar;

    private List<User> userList;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        userList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        noPerson = findViewById(R.id.ff_no_people_id);
        heading = findViewById(R.id.ff_heading_id);
        progressBar = findViewById(R.id.ff_progressBar);

        recyclerView=findViewById(R.id.ff_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        type= getIntent().getIntExtra("type",0);

        if(type == 2)
            heading.setText(R.string.friend_request_list);
        if(type == 3)
            heading.setText(R.string.choose_friends);

        findFriendsAdapter = new FindFriendsAdapter(FindFriendsActivity.this,userList, type);
        recyclerView.setAdapter(findFriendsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_chat_id:
                startActivity(new Intent(FindFriendsActivity.this,ChatRoomActivity.class));
                break;
            case R.id.action_friendsPost_id:
                startActivity(new Intent(FindFriendsActivity.this,FriendsJournalActivity.class));
                break;
            case R.id.action_add_id:
                //take user to add journal
                if(currentUser != null && firebaseAuth != null) {
                    startActivity(new Intent(FindFriendsActivity.this, PostJournalActivity.class));
                    //finish();
                }
                break;
            case R.id.action_myProfile_id:
                startActivity(new Intent(FindFriendsActivity.this, ViewProfileActivity.class));
                break;
            case R.id.action_signout_id:
                //sign out user
                signOut();
                break;
            case R.id.action_findFriend_id:
                //find friends and show
                Intent intent = new Intent(FindFriendsActivity.this,FindFriendsActivity.class);
                intent.putExtra("type",3);
                startActivity(intent);
                break;
            case R.id.action_myFriends_id:
                //show my friend list
                Intent intent2 = new Intent(FindFriendsActivity.this,FindFriendsActivity.class);
                intent2.putExtra("type",1);
                startActivity(intent2);
                break;
            case R.id.action_friendRequests_id:
                //show all friend requests
                Intent intent1 = new Intent(FindFriendsActivity.this,FindFriendsActivity.class);
                intent1.putExtra("type",2);
                startActivity(intent1);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {

        builder = new AlertDialog.Builder(FindFriendsActivity.this);
        inflater = LayoutInflater.from(FindFriendsActivity.this);
        View v = inflater.inflate(R.layout.confirmation_popup_signout,null);

        builder.setView(v);
        dialog = builder.create();
        dialog.show();

        TextView yesView = v.findViewById(R.id.conf_signOut_yes_id);
        TextView noView = v.findViewById(R.id.conf_signOut_no_id);

        yesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentUser != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(FindFriendsActivity.this, MainActivity.class));
                    finish();
                }
                dialog.dismiss();
            }
        });

        noView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(type == 3)
            makeFriends();
        if(type == 2)
            generateFriendRequestList();
        if(type == 1)
            generateFriendList();

    }

    private void generateFriendList() {

        progressBar.setVisibility(View.VISIBLE);
        ArrayList<String> fl = JournalApi.getInstance().getFriendList();// all friends

        if(fl.isEmpty()){
            noPerson.setText(R.string.no_one_here);
            noPerson.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        for(String un : fl){

            collectionReference.whereEqualTo("userName",un)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if(queryDocumentSnapshots != null){
                                for(QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                                    User user = snapshot.toObject(User.class);
                                    userList.add(user);
                                }

                                progressBar.setVisibility(View.INVISIBLE);
                                findFriendsAdapter.notifyDataSetChanged();
                            }
                            else {
                                noPerson.setText(R.string.no_one_here);
                                noPerson.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(FindFriendsActivity.this,"Problem in loading people",Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG, "onFailure: "+e.toString());
                        }
                    });
        }
    }

    private void generateFriendRequestList() {

        progressBar.setVisibility(View.VISIBLE);
        ArrayList<String> ru = JournalApi.getInstance().getReceivedRequestList();// all requested users

        if(ru.isEmpty()){
            noPerson.setText(R.string.no_one_here);
            noPerson.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        for(String un : ru){

            collectionReference.whereEqualTo("userName",un)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if(queryDocumentSnapshots != null){
                                for(QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                                    User user = snapshot.toObject(User.class);
                                    userList.add(user);
                                }

                                progressBar.setVisibility(View.INVISIBLE);
                                findFriendsAdapter.notifyDataSetChanged();
                            }
                            else {
                                noPerson.setText(R.string.no_one_here);
                                noPerson.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(FindFriendsActivity.this,"Problem in loading people",Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG, "onFailure: "+e.toString());
                        }
                    });
        }

    }

    private void makeFriends() {

        progressBar.setVisibility(View.VISIBLE);
        final String currentUserName = JournalApi.getInstance().getUsername();
        final ArrayList<String> cu = JournalApi.getInstance().getFriendList(); // all connected users

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots!=null){

                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                User user = snapshot.toObject(User.class);
                                if(!currentUserName.equals(user.getUserName()) && !cu.contains(user.getUserName()))
                                    userList.add(user);

                            }

                            if(userList.isEmpty()){
                                noPerson.setText(R.string.no_one_here);
                                noPerson.setVisibility(View.VISIBLE);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            findFriendsAdapter.notifyDataSetChanged();
                        }
                        else{
                            noPerson.setText(R.string.no_one_here);
                            noPerson.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(FindFriendsActivity.this,"Problem in loading people",Toast.LENGTH_LONG)
                                .show();
                        Log.d(TAG, "onFailure: "+e.toString());

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        userList.clear();
    }
}
