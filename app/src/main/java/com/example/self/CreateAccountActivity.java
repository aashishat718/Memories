package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

import static java.lang.Boolean.FALSE;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";
    private static final int GALLERY_CODE = 1;
    private Button loginButton, crtAccButton;
    private EditText usernameText,fullNameText,passwordText;
    private AutoCompleteTextView emailText;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private ImageView imageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private StorageReference storageReference;

    private Uri imageUri;

    //connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        usernameText = findViewById(R.id.crtAcc_username_id);
        fullNameText = findViewById(R.id.crtAcc_fullName_id);
        emailText = findViewById(R.id.crtAcc_emailText_id);
        passwordText = findViewById(R.id.crtAcc_password_id);
        progressBar = findViewById(R.id.crtAcc_progress_id);
        imageView = findViewById(R.id.crtAcc_imageView_id);
        addPhotoButton = findViewById(R.id.crtAcc_camera_id);
        loginButton = findViewById(R.id.crtAcc_login_button_id);
        crtAccButton = findViewById(R.id.crtAcc_create_account_button_id);

        authStateListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null){
                    //user already logged in
                }
                else{
                    //no user yet
                }

            }
        };

        crtAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();
                String username = usernameText.getText().toString().trim();
                String fullName = fullNameText.getText().toString().trim();
                if(!email.isEmpty()&&!password.isEmpty()&&!username.isEmpty()&&!fullName.isEmpty() && imageUri != null) {
                    if(username.indexOf('_') == -1)
                        checkUserNameAvailable(email,password,username,fullName,view);
                    else
                        Toast.makeText(CreateAccountActivity.this, "username can't contain _ " , Toast.LENGTH_SHORT)
                                .show();
                }
                else
                    Toast.makeText(CreateAccountActivity.this, R.string.empty_field_na , Toast.LENGTH_SHORT)
                            .show();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateAccountActivity.this,LoginActivity.class));
                finish();
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
            }
        });
    }

    private void checkUserNameAvailable(final String email, final String password, final String username, final String fullName, final View view) {

        progressBar.setVisibility(View.VISIBLE);
        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        boolean flag = true;
                        for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            String un = (String) Objects.requireNonNull(documentSnapshot.get("userName"));
                            if(un.equals(username)) {
                                flag=false;
                                break;
                            }
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                        if(flag)
                            createEmailAccount(email,password,username,fullName);
                        else
                            Snackbar.make(view,"Username already taken. Try another" , BaseTransientBottomBar.LENGTH_LONG)
                                    .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.toString());
                        Toast.makeText(CreateAccountActivity.this,e.getMessage(),Toast.LENGTH_LONG)
                                .show();
                    }
                });

    }

    private void createEmailAccount(String email, String password, final String username, final String fullName){

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(fullName)) {
            progressBar.setVisibility(View.VISIBLE);

            final StorageReference filepath = storageReference.child("profile_pictures")
                    .child(username+"_dp");

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                // take user to add journal activity

                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId = currentUser.getUid();
                                final ArrayList<String> friendList = new ArrayList<>();
                                final ArrayList<String> receivedRequestList = new ArrayList<>();

                                //create a user map in order to create a user in collection
                                final Map<String,Object> userObj = new HashMap<>();
                                userObj.put("userId",currentUserId);
                                userObj.put("userName",username);
                                userObj.put("fullName",fullName);
                                userObj.put("friendList",friendList);
                                userObj.put("receivedRequestList",receivedRequestList);

                                //compress profile picture
                                imageView.setDrawingCacheEnabled(true);
                                imageView.buildDrawingCache();
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] d = baos.toByteArray();

                                //save to database

                                filepath.putBytes(d)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final String profilePicUrl = uri.toString();
                                                        userObj.put("profilePicUrl",profilePicUrl);

                                                        collectionReference.document(username).set(userObj)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        progressBar.setVisibility(View.INVISIBLE);

                                                                        JournalApi journalApi=JournalApi.getInstance();
                                                                        journalApi.setUserID(currentUserId);
                                                                        journalApi.setUsername(username);
                                                                        journalApi.setFullName(fullName);
                                                                        journalApi.setFriendList(friendList);
                                                                        journalApi.setProfilePicUrl(profilePicUrl);
                                                                        journalApi.setReceivedRequestList(receivedRequestList);

                                                                        Intent intent = new Intent(CreateAccountActivity.this,
                                                                                JournalListActivity.class);
                                                                        intent.putExtra("userName",username);
                                                                        intent.putExtra("userId",currentUserId);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Toast.makeText(CreateAccountActivity.this,e.getMessage(),Toast.LENGTH_LONG)
                                                                                .show();
                                                                        Log.d(TAG, "onFailure: "+e.toString());
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //could not find image
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(CreateAccountActivity.this, R.string.couldnt_load_image ,Toast.LENGTH_LONG)
                                                                .show();
                                                        Log.d(TAG, "onFailure: "+e.toString());
                                                    }
                                                });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(CreateAccountActivity.this, e.getMessage() ,Toast.LENGTH_LONG)
                                                        .show();
                                                Log.d(TAG, "onFailure: "+e.toString());
                                            }
                                        });

                            }
                            else
                            {
                                //some thing went wrong
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAccountActivity.this,"Something went wrong",Toast.LENGTH_LONG)
                                        .show();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(CreateAccountActivity.this,e.getMessage(),Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG, "onFailure: "+e.toString());
                        }
                    });
        }
        else{
            Toast.makeText(CreateAccountActivity.this, R.string.empty_field_na , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK){
            if(data != null){
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
