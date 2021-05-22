package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import model.Journal;
import ui.RecyclerViewAdapter;
import util.JournalApi;

public class JournalListActivity extends AppCompatActivity {

    private static final String TAG = "JournalListActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");
    private StorageReference storageReference;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private TextView noThought;

    private List<Journal> journalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        journalList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noThought = findViewById(R.id.ajl_no_thoughts_text_id);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewAdapter = new RecyclerViewAdapter(JournalListActivity.this,journalList);
        recyclerView.setAdapter(recyclerViewAdapter);

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
                startActivity(new Intent(JournalListActivity.this,ChatRoomActivity.class));
                break;
            case R.id.action_friendsPost_id:
                startActivity(new Intent(JournalListActivity.this,FriendsJournalActivity.class));
                break;
            case R.id.action_add_id:
                //take user to add journal
                if(user != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                    //finish();
                }
                break;
            case R.id.action_myProfile_id:
                startActivity(new Intent(JournalListActivity.this, ViewProfileActivity.class));
                break;
            case R.id.action_signout_id:
                //sign out user
                signOut();
                break;
            case R.id.action_findFriend_id:
                //find friends and show
                Intent intent = new Intent(JournalListActivity.this,FindFriendsActivity.class);
                intent.putExtra("type",3);
                startActivity(intent);
                break;
            case R.id.action_myFriends_id:
                //show my friend list
                Intent intent2 = new Intent(JournalListActivity.this,FindFriendsActivity.class);
                intent2.putExtra("type",1);
                startActivity(intent2);
                break;
            case R.id.action_friendRequests_id:
                //show all friend requests
                Intent intent1 = new Intent(JournalListActivity.this,FindFriendsActivity.class);
                intent1.putExtra("type",2);
                startActivity(intent1);
                break;
            case R.id.action_map_id:
                // move to map activity
                Intent intent3 = new Intent(JournalListActivity.this,MapsActivity.class);
                startActivity(intent3);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {

        builder = new AlertDialog.Builder(JournalListActivity.this);
        inflater = LayoutInflater.from(JournalListActivity.this);
        View v = inflater.inflate(R.layout.confirmation_popup_signout,null);

        builder.setView(v);
        dialog = builder.create();
        dialog.show();

        TextView yesView = v.findViewById(R.id.conf_signOut_yes_id);
        TextView noView = v.findViewById(R.id.conf_signOut_no_id);

        yesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
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

        collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(QueryDocumentSnapshot snapshots: queryDocumentSnapshots){
                                Journal journal = snapshots.toObject(Journal.class);
                                journalList.add(journal);
                            }

                            //invoke recycler view

//                            recyclerViewAdapter = new RecyclerViewAdapter(JournalListActivity.this,journalList);
//                            recyclerView.setAdapter(recyclerViewAdapter);

                            //here we called recycler view in OnCreate()

                            recyclerViewAdapter.notifyDataSetChanged();

                        }
                        else {
                            noThought.setVisibility(View.VISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(JournalListActivity.this, "Something went wrong", Toast.LENGTH_SHORT)
                                .show();
                        noThought.setText(e.getMessage());
                        noThought.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        journalList.clear();
    }
}
