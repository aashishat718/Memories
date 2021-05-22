package ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.PostJournalActivity;
import com.example.self.R;
import com.example.self.UpdateJournalActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

import model.Journal;

import static androidx.core.content.FileProvider.getUriForFile;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private Context context;
    private List<Journal> journalList;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    public RecyclerViewAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.journal_row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        holder.titleText.setText(journal.getTitle());
        holder.thoughtText.setText(journal.getThoughts());
        holder.name.setText(journal.getUserName());
        holder.likeCount.setText(new StringBuilder().append(journal.getLikeCount()).append(" like").toString());

        imageUrl=journal.getImageUrl();

        //use picasso to download and show image
        Picasso.get().load(imageUrl)
                .placeholder(R.drawable.pexelsphoto)
                .fit().into(holder.imageView);

        String time_ago = (String) DateUtils.getRelativeTimeSpanString(
                journal.getTimeAdded().getSeconds()*1000);
        holder.timestampText.setText(time_ago);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imageView;
        public TextView titleText;
        public TextView thoughtText;
        public TextView name;
        public TextView timestampText;
        public TextView likeCount;
        public ImageButton shareButton,deleteButton,editButton;

        String userId,userName;

        public ViewHolder(@NonNull View itemView,Context ctx) {
            super(itemView);

            context=ctx;

            imageView=itemView.findViewById(R.id.jr_image_id);
            titleText=itemView.findViewById(R.id.jr_title_id);
            thoughtText=itemView.findViewById(R.id.jr_thoughts_id);
            timestampText=itemView.findViewById(R.id.jr_timestamp_id);
            name=itemView.findViewById(R.id.jr_username_id);
            likeCount=itemView.findViewById(R.id.jr_likeCount_id);
            shareButton=itemView.findViewById(R.id.jr_share_button_id);
            deleteButton=itemView.findViewById(R.id.jr_delete_button_id);
            editButton= itemView.findViewById(R.id.jr_edit_button_id);

            shareButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            editButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int position=getAdapterPosition();
            Journal journal = journalList.get(position);

            switch (view.getId()){

                case R.id.jr_edit_button_id:
                    //edit journal
                    Intent intent = new Intent(view.getContext(), UpdateJournalActivity.class);
                    intent.putExtra("Journal",journal);
                    view.getContext().startActivity(intent);
                    break;
                case R.id.jr_share_button_id:
                    // share my thought
                    shareThought(journal,view);
                    break;
                case R.id.jr_delete_button_id:
                    // delete this thought
                    deleteThought(position,view);
                    break;
            }

        }

        private void shareThought(final Journal journal, final View view) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            String timeStamp = journal.getStrTimeStamp();

            final StorageReference filepath = storageReference.child("journal_images")
                    .child("my_image_" + timeStamp);

            try {
                final File localFile = File.createTempFile("images",".jpg");
                filepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        Uri shareImageUri = getUriForFile(view.getContext(),
                                "com.example.self" ,localFile);
                        view.getContext().grantUriPermission("com.example.self",shareImageUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);

                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        shareIntent.putExtra(Intent.EXTRA_STREAM,shareImageUri);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, journal.getTitle());
                        shareIntent.putExtra(Intent.EXTRA_TEXT,journal.getThoughts());
                        shareIntent.setType("image/jpg");
                        view.getContext().startActivity(Intent.createChooser(shareIntent, "Share via"));

                        view.getContext().revokeUriPermission(shareImageUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(view.getContext(),e.getMessage(),Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG, "onFailure: "+e.toString());
                        }
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        private void deleteThought(final int position, final View view) {

            builder = new AlertDialog.Builder(context);
            inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.confirmation_popup_delete_item,null);

            builder.setView(v);
            dialog = builder.create();
            dialog.show();

            TextView yesView = v.findViewById(R.id.conf_delete_yes_id);
            TextView noView = v.findViewById(R.id.conf_delete_no_id);

            yesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Journal journal = journalList.get(position);
                    //String timeStamp = journal.getStrTimeStamp();
                    String docName = journal.getDocName();
                    String imageUrl = journal.getImageUrl();

                    final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                    db.collection("Journal").document(docName)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(view,"Document deleted successfully", BaseTransientBottomBar.LENGTH_LONG)
                                                    .show();
                                            journalList.remove(position);
                                            notifyDataSetChanged();
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Snackbar.make(view ,"Something went wrong" , BaseTransientBottomBar.LENGTH_LONG)
                                                            .show();
                                                    //Log.d( TAG , "onFailure: "+e.toString());
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(view ,"Something went wrong" , BaseTransientBottomBar.LENGTH_LONG)
                                            .show();
                                    //Log.d( TAG , "onFailure: "+e.toString());
                                }
                            });

                    dialog.dismiss();
                }
            });
            noView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    dialog.dismiss();
                }
            });
        }
    }
}
