package ui;

import android.content.Context;
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

import com.example.self.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Journal;
import util.JournalApi;

public class FriendsViewAdapter extends RecyclerView.Adapter<FriendsViewAdapter.ViewHolder> {

    private static final String TAG = "FriendsViewAdapter";
    private Context context;
    private List<Journal> friendsJournalList;

    public FriendsViewAdapter(Context context, List<Journal> friendsJournalList) {
        this.context = context;
        this.friendsJournalList = friendsJournalList;
    }

    @NonNull
    @Override
    public FriendsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_journal_row,parent,false);
        return new ViewHolder(view,context);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Journal journal = friendsJournalList.get(position);
        String imageUrl;

        holder.titleText.setText(journal.getTitle());
        holder.thoughtText.setText(journal.getThoughts());
        holder.name.setText(journal.getUserName());
        holder.likeCount.setText(journal.getLikeCount());

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
        return friendsJournalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView titleText;
        public TextView thoughtText;
        public TextView name;
        public TextView likeCount;
        public TextView timestampText;

        public ImageButton likeButton;

        public ViewHolder(@NonNull View itemView,Context ctx) {
            super(itemView);

            context=ctx;

            imageView=itemView.findViewById(R.id.fjr_image_id);
            titleText=itemView.findViewById(R.id.fjr_title_id);
            thoughtText=itemView.findViewById(R.id.fjr_thoughts_id);
            timestampText=itemView.findViewById(R.id.fjr_timestamp_id);
            name=itemView.findViewById(R.id.fjr_username_id);
            likeCount=itemView.findViewById(R.id.fjr_likeCount_id);
            likeButton=itemView.findViewById(R.id.fjr_like_button_id);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position=getAdapterPosition();
                    updateLike(position,view);
                }
            });

        }

        private void updateLike(int position, final View view) {

            String currentUserName = JournalApi.getInstance().getUsername();
            Journal journal = friendsJournalList.get(position);
            ArrayList<String> lbl = journal.getLikedByList(); // getting local liked by list
            if(lbl == null)
                lbl = new ArrayList<>();

            if(lbl.contains(currentUserName)) {
                Toast.makeText(view.getContext(),"Already liked the post",Toast.LENGTH_SHORT)
                        .show();
            }
            else {

                lbl.add(currentUserName);
                int lc=Integer.parseInt(journal.getLikeCount()); // getting local like count
                ++lc;
                journal.setLikeCount(String.valueOf(lc));

                Map<String,Object> data = new HashMap<>();
                data.put("likedByList",lbl);
                data.put("likeCount",String.valueOf(lc));
                String docName = journal.getDocName();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Journal").document(docName)
                        .update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(view.getContext(),"Failed to like", Toast.LENGTH_SHORT)
                                        .show();
                                Log.d(TAG, "onFailure: " +e.toString());
                            }
                        });
            }
        }
    }
}
