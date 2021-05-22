package ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.example.self.ViewProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.User;
import util.JournalApi;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {

    private static final String TAG = "FindFriendsAdapter";
    private Context context;
    private List<User> peopleList;
    private int type;

    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    public FindFriendsAdapter(Context context, List<User> peopleList, int type) {
        this.context = context;
        this.peopleList = peopleList;
        this.type = type;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.find_friends_row,parent,false);

        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = peopleList.get(position);

        if(type==1)
            holder.requestButton.setVisibility(View.GONE);
        if(type==2)
            holder.requestButton.setText(R.string.accept);

        holder.userName.setText(user.getUserName());

    }

    @Override
    public int getItemCount() {
        return peopleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName,request;
        public Button requestButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context=ctx;
            userName = itemView.findViewById(R.id.ffr_username_id);
            request = itemView.findViewById(R.id.ffr_request_id);
            requestButton = itemView.findViewById(R.id.ffr_button_id);

            requestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = getAdapterPosition();
                    User user = peopleList.get(position);
                    if(type==2){
                        //accept friend request
                        acceptFriendRequest(user,view);
                    }

                    if(type == 3) {
                        // send friend request
                        sendFriendRequest(user, view);
                    }
                }
            });

            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    User user = peopleList.get(position);
                    Intent intent = new Intent(view.getContext(), ViewProfileActivity.class);
                    intent.putExtra("User",user);
                    view.getContext().startActivity(intent);
                }
            });

        }

        private void acceptFriendRequest(final User user, final View view) {

            final String currentUserName = JournalApi.getInstance().getUsername();
            ArrayList<String> mfl = JournalApi.getInstance().getFriendList();
            //ArrayList<String> mrl = JournalApi.getInstance().getReceivedRequestList();
            ArrayList<String> fl = user.getFriendList();

            if(fl == null){
                fl = new ArrayList<>();
            }
            fl.add(currentUserName);
            mfl.add(user.getUserName());
            final Map<String,Object> data = new HashMap<>();
            data.put("friendList",fl);

            Map<String,Object> data1 = new HashMap<>();
            data1.put("friendList",mfl);

            peopleList.remove(user.getUserName());
            final Map<String,Object> data2 = new HashMap<>();
            data2.put("receivedRequestList",peopleList);
            notifyDataSetChanged();

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserName)
                    .update(data1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            db.collection("Users").document(user.getUserName())
                                    .update(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            db.collection("Users").document(currentUserName)
                                                    .update(data2)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            //request.setText(R.string.accepted );
                                                            createChatRoom(user.getUserName(),currentUserName);
                                                            Snackbar.make(view,R.string.accepted, BaseTransientBottomBar.LENGTH_SHORT).show();

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

        }

        private void createChatRoom(String userName, String currentUserName) {

            String roomName = currentUserName+"_chatRoom_"+userName;
            Map<String, Object> map = new HashMap<>();
            map.put(roomName, "");
            root.updateChildren(map);

        }

        private void sendFriendRequest(User user, View view) {

            String currentUserName = JournalApi.getInstance().getUsername();

            ArrayList<String> rl = user.getReceivedRequestList();
            if(rl == null) {
                rl = new ArrayList<>();
            }

            if(rl.contains(currentUserName)){
                Toast.makeText(view.getContext(),"Request already sent" ,Toast.LENGTH_SHORT)
                        .show();
                request.setText(R.string.requested);
            }
            else {
                rl.add(currentUserName);

                Map<String, Object> data = new HashMap<>();
                data.put("receivedRequestList", rl);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users")
                        .document(user.getUserName())
                        .update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                request.setText(R.string.requested);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                request.setText(R.string.fail_text);
                            }
                        });
            }
        }
    }
}
