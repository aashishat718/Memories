package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import actions.SendMessage;
import model.Journal;
import util.JournalApi;

public class ChatRoomActivity extends AppCompatActivity {

    private Button addRoom;
    private EditText roomName;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> roomList = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private String currentUserName;

    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    private HashMap<String,Integer> rnt = new HashMap<>(); // room name type, 0 for public room, 1 for friend room

    private DatabaseReference chatRoot; // for sending location
    private int previousActivity; // 1 for mapActivity, 0 for any other activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        previousActivity = getIntent().getIntExtra("previous activity",0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        currentUserName = JournalApi.getInstance().getUsername();

        addRoom = findViewById(R.id.cr_addRoom_id);
        roomName = findViewById(R.id.cr_roomName_id);
        listView = findViewById(R.id.cr_listView_id);

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,roomList);
        listView.setAdapter(arrayAdapter);

        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String rn = roomName.getText().toString().trim() + " (Public room)";
                if(!rn.isEmpty()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(rn, "");
                    root.updateChildren(map);
                    roomName.setText("");
                }
                else{
                    Toast.makeText(ChatRoomActivity.this,R.string.empty_field_na,Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String rn = ((TextView)view).getText().toString();
                if(previousActivity == 0) {
                    Intent intent = new Intent(ChatRoomActivity.this, ChatActivity.class);
                    intent.putExtra("roomName", rn);
                    intent.putExtra("userName", currentUserName);
                    intent.putExtra("type", rnt.get(rn));
                    startActivity(intent);
                }
                else {
                    sendLocationFromMap(rn);
                    Toast.makeText(ChatRoomActivity.this,"Location sent",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set= new HashSet<>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()){
                    String rn = ((DataSnapshot)i.next()).getKey(); // rn == room name
                    if(rn.endsWith(" (Public room)")) {
                        set.add(rn);
                        rnt.put(rn,0);
                    }
                }
                ArrayList<String> friendList = JournalApi.getInstance().getFriendList();
                for(String friends : friendList){
                    set.add(friends);
                    rnt.put(friends,1);
                }
                roomList.clear();
                roomList.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendLocationFromMap(final String roomName) {
        final String message = getIntent().getStringExtra("Alert Location Message");
        if(roomName.endsWith(" (Public room)")){
            SendMessage.send(root.child(roomName),currentUserName,message);
        }
        else {
            final String[] rname = {roomName + "_chatRoom_" + currentUserName};
            chatRoot = root.child(rname[0]);
            chatRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        rname[0] = currentUserName + "_chatRoom_" + roomName;
                        chatRoot = root.child(rname[0]);
                    }
                    SendMessage.send(chatRoot,currentUserName,message);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
