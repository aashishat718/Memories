package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private static final String TAG = "PostJournalActivity";
    private Button saveButton;
    private ProgressBar progressBar;
    private EditText titleText;
    private EditText thoughtText;
    private TextView usernameText;
    private ImageView addPhotoButton;
    private ImageView imageView;

    private String currentUserId;
    private String currentUsername;

    //fire base
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");
    private StorageReference storageReference;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        // using journal api so this is not needed
//        Bundle bundle = getIntent().getExtras();
//        if(bundle != null){
//            String username=bundle.getString("username");
//            String userID=bundle.getString("userID");
//        }

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference= FirebaseStorage.getInstance().getReference();

        progressBar=findViewById(R.id.post_progressBar_id);
        titleText=findViewById(R.id.post_title_id);
        thoughtText=findViewById(R.id.post_thoughts_id);
        saveButton=findViewById(R.id.post_saveButton_id);
        usernameText=findViewById(R.id.post_username_id);
        addPhotoButton=findViewById(R.id.post_camera_id);
        imageView=findViewById(R.id.post_imageView_id);

        firebaseAuth=FirebaseAuth.getInstance();

        addPhotoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);
        if(JournalApi.getInstance() != null){
            currentUserId=JournalApi.getInstance().getUserID();
            currentUsername=JournalApi.getInstance().getUsername();

            usernameText.setText(currentUsername);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){

                }
                else{

                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post_saveButton_id:
                //save journal
                saveJournal();
                break;
            case R.id.post_camera_id:
                //get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
        }

    }

    private void saveJournal() {
        final String title = titleText.getText().toString().trim();
        final String thoughts = thoughtText.getText().toString().trim();
        final String timeStamp = String.valueOf(Timestamp.now().getSeconds());

        if(!TextUtils.isEmpty(title)&&!TextUtils.isEmpty(thoughts)
            && imageUri != null){

            progressBar.setVisibility(View.VISIBLE);

            // compress the image
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            final StorageReference filepath = storageReference.child("journal_images")
                                    .child(currentUsername + "my_image_" + timeStamp);

            /*

                To upload image without compressing use :

                filepath.putFile(imageUri)
                    .addOnSuccess.....

                and rest code is same as below

             */


            //upload the image
            filepath.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                             //TODO: create journal object, invoke collection reference ,save journal object

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    String docName = currentUsername + "_" + "doc" + "_" + timeStamp;
                                    ArrayList<String> likedByList = new ArrayList<>();

                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThoughts(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setUserName(currentUsername);
                                    journal.setUserId(currentUserId);
                                    journal.setDocName(docName);
                                    journal.setStrTimeStamp(timeStamp);
                                    journal.setLikeCount("0");
                                    journal.setLikedByList(likedByList);
                                    journal.setTimeAdded(new Timestamp(new Date()));

                                    //collectionReference.add(journal);

                                    collectionReference.document(docName).set(journal)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostJournalActivity.this,e.getMessage(),Toast.LENGTH_LONG)
                                                    .show();
                                            //Log.d(TAG, "onFailure: "+e.toString());
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //could not find image

                                    Toast.makeText(PostJournalActivity.this, R.string.couldnt_load_image ,Toast.LENGTH_LONG)
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
                        }
                    });

        }
        else{
            Toast.makeText(PostJournalActivity.this, R.string.empty_field_na ,Toast.LENGTH_SHORT)
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
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }

    }
}
