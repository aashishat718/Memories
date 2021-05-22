package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Button loginButton, crtAccButton;
    private AutoCompleteTextView emailText;
    private EditText passwordText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //connection to firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_button_id);
        crtAccButton = findViewById(R.id.create_acc_button_id);
        emailText = findViewById(R.id.login_emailText_id);
        passwordText = findViewById(R.id.login_password_id);
        progressBar = findViewById(R.id.login_progress_id);

        firebaseAuth = FirebaseAuth.getInstance();

        crtAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email= emailText.getText().toString().trim();
                String password= passwordText.getText().toString().trim();
                if(!email.isEmpty() && !password.isEmpty()) {
                    loginEmailPasswordUser(email,password);
                }
                else{
                    Toast.makeText(LoginActivity.this, R.string.empty_field_na ,Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void loginEmailPasswordUser(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            if (user == null) {
//                                throw new AssertionError();
                                return;
                            }
                            String currentUserId = user.getUid();

                            collectionReference.whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                            if(e != null) return;

                                            assert queryDocumentSnapshots != null;
                                            if(!queryDocumentSnapshots.isEmpty()){

                                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUsername(snapshot.getString("userName"));
                                                    journalApi.setFullName(snapshot.getString("fullName"));
                                                    journalApi.setUserID(snapshot.getString("userId"));
                                                    journalApi.setProfilePicUrl(snapshot.getString("profilePicUrl"));

                                                    ArrayList<String> fl = (ArrayList<String>) snapshot.get("friendList");
                                                    if(fl==null)
                                                        fl=new ArrayList<>();
                                                    ArrayList<String> rrl = (ArrayList<String>) snapshot.get("receivedRequestList");
                                                    if(rrl==null)
                                                        rrl=new ArrayList<>();
                                                    journalApi.setFriendList(fl);
                                                    journalApi.setReceivedRequestList(rrl);
//                                                    journalApi.setFriendList((ArrayList<String>) snapshot.get("friendList"));
//                                                    journalApi.setFriendRequest((ArrayList<String>) snapshot.get("friendRequest"));

                                                    // go to list post journal activity
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
                                                    finish();
                                                }

                                            }

                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "onFailure: "+e.toString());
                            String message = Objects.requireNonNull(e.getMessage());
                            Toast.makeText(LoginActivity.this, message ,Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
    }
}
