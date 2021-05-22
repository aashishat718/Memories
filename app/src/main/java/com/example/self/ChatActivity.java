package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import actions.SendMessage;
import util.JournalApi;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "Chat Activity";
    private EditText message;
    private TextView roomName,conversation;
    private ImageButton sendButton;

    private String currentUserName,rname;
    private String tempKey;
    private String chatUserName,chatMessage;

    private DatabaseReference root;

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message=findViewById(R.id.chat_message_id);
        roomName=findViewById(R.id.chat_roomName_id);
        sendButton=findViewById(R.id.chat_sendButton_id);
        conversation=findViewById(R.id.chat_showmsg_id);

        currentUserName = getIntent().getStringExtra("userName");
        final String rname1 = getIntent().getStringExtra("roomName");
        roomName.setText(new StringBuilder().append("In chat room : ").append(rname1).toString());
        int type = getIntent().getIntExtra("type",0);

        if(type == 0){
            rname = rname1;
            root = FirebaseDatabase.getInstance().getReference().child(rname);
            rootEvent();
        }
        else {
            rname = rname1 + "_chatRoom_" + currentUserName;
            root = FirebaseDatabase.getInstance().getReference().child(rname);
            root.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        rname = currentUserName + "_chatRoom_" + rname1;
                        root = FirebaseDatabase.getInstance().getReference().child(rname);
                    }
                    rootEvent();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (message.getText() != null) {
                    SendMessage.send(root,currentUserName,message.getText().toString());
                    message.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, R.string.type_a_message, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

    }

    private void rootEvent() {
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void appendChatConversation(DataSnapshot dataSnapshot) {

        StringBuffer stringBuffer = new StringBuffer();
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()){

            chatMessage = (String) ((DataSnapshot)i.next()).getValue();
            chatUserName = (String) ((DataSnapshot)i.next()).getValue();

            stringBuffer.append(chatUserName).append(" : ").append(chatMessage).append("\n");
            //conversation.append(chatUserName+" : "+chatMessage + "\n");
        }

        String completeString = stringBuffer.toString();
        ChatActivity.createLink(conversation,completeString,new ClickableSpan() {

            @Override
            public void onClick(@NonNull View view) {
                TextView tv = (TextView) view;
                Spanned s = (Spanned) tv.getText();
                int start = s.getSpanStart(this);
                int end = s.getSpanEnd(this);
                //Log.d(TAG, "onClick [" + s.subSequence(start, end) + "]");
                String locationMessage = s.subSequence(start,end).toString();
                handleLocationClick(locationMessage);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(ChatActivity.this,R.color.lightBlue));
                ds.setUnderlineText(true);
            }
        });

    }

    public static TextView createLink(TextView targetTextView, String completeString, ClickableSpan clickableAction) {

        SpannableString spannableString = new SpannableString(completeString);

        int starting = completeString.indexOf("https://");
        while(starting >= 0) {
            int ending = completeString.indexOf('\n',starting);
            spannableString.setSpan(clickableAction, starting, ending,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            starting = completeString.indexOf("https://",ending);
        }

        targetTextView.append(spannableString);
        targetTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return targetTextView;
    }

    private void handleLocationClick(final String locationMessage) {
        builder = new AlertDialog.Builder(ChatActivity.this);
        inflater = LayoutInflater.from(ChatActivity.this);
        View v = inflater.inflate(R.layout.map_app_list,null);

        builder.setView(v);
        dialog = builder.create();
        dialog.show();

        TextView memories = v.findViewById(R.id.app_list_memories_id);
        TextView googleMaps = v.findViewById(R.id.app_list_googlemaps_id);

        memories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,MapsActivity.class);
                intent.putExtra("previous activity",1);
                intent.putExtra("location message",locationMessage);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        googleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(locationMessage));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
                dialog.dismiss();
            }
        });
    }
}
