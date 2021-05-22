package actions;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class SendMessage {
    public static void send(DatabaseReference root,String currentUserName,String message) {
        Map<String, Object> m = new HashMap<>();
        String tempKey = root.push().getKey();
        root.updateChildren(m);

        DatabaseReference message_root = root.child(tempKey);
        Log.d("send message", tempKey);
        Map<String, Object> m2 = new HashMap<>();
        m2.put("userName", currentUserName);
        m2.put("message", message);

        message_root.updateChildren(m2);
    }
}
