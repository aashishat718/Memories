package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class UpdateJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private static final String TAG = "UpdateJournalActivity";
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
    private StorageReference storageReference;

    private Uri imageUri;
    private Journal journal_rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_journal);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();

        progressBar=findViewById(R.id.update_progressBar_id);
        titleText=findViewById(R.id.update_title_id);
        thoughtText=findViewById(R.id.update_thoughts_id);
        saveButton=findViewById(R.id.update_saveButton_id);
        usernameText=findViewById(R.id.update_username_id);
        addPhotoButton=findViewById(R.id.update_camera_id);
        imageView=findViewById(R.id.update_imageView_id);

        progressBar.setVisibility(View.INVISIBLE);
        if(JournalApi.getInstance() != null){
            currentUserId=JournalApi.getInstance().getUserID();
            currentUsername=JournalApi.getInstance().getUsername();

            usernameText.setText(currentUsername);
        }

        journal_rec = (Journal)getIntent().getSerializableExtra("Journal");
        if(journal_rec != null){
            titleText.setText(journal_rec.getTitle());
            thoughtText.setText(journal_rec.getThoughts());
            //use picasso to download and show image
            Picasso.get().load(journal_rec.getImageUrl())
                    .placeholder(R.drawable.pexelsphoto)
                    .fit().into(imageView);

        }

        addPhotoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

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
            case R.id.update_camera_id:
                //add new image if desired
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
            case R.id.update_saveButton_id:
                //update item
                updateJournal();
                break;
        }

    }

    private void updateJournal() {

        progressBar.setVisibility(View.VISIBLE);
        String title = titleText.getText().toString().trim();
        String thought = thoughtText.getText().toString().trim();

        String timeStamp = journal_rec.getStrTimeStamp();
        //for updating image
        final StorageReference filepath = storageReference.child("journal_images")
                .child("my_image_" + timeStamp);

        final Map<String,Object> data = new HashMap<>();
        data.put("title",title);
        data.put("thoughts",thought);
        final DocumentReference journalRef = db.collection("Journal").document(journal_rec.getDocName());

        if(imageUri != null) {

            // compress the image
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] d = baos.toByteArray();

            filepath.putBytes(d).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String newImageUrl = uri.toString();
                            data.put("imageUrl", newImageUrl);
                            journalRef.update(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(UpdateJournalActivity.this, JournalListActivity.class));
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(UpdateJournalActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                                            .show();
                                    Log.d(TAG, "onFailure: "+e.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: "+e.toString());
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UpdateJournalActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "onFailure: "+e.toString());
                }
            });
        }
        else {


            journalRef.update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(UpdateJournalActivity.this, JournalListActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateJournalActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "onFailure: "+e.toString());
                }
            });
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
