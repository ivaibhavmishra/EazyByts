package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendMessageButton;
    private TextView lastMessageTextView;
    private DatabaseReference messagesDatabaseReference;
    private FirebaseAuth firebaseAuth;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the user is authenticated
        if (firebaseAuth.getCurrentUser() == null) {
            // Redirect guest users to SeenRoom
            Intent intent = new Intent(ChatRoomActivity.this, SeenRoom.class);
            startActivity(intent);
            finish(); // Finish the current activity so the user cannot go back to it
            return;
        }

        // Initialize layout
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.parseColor("#FFFFFF")); // Set background color to white

        String firebaseDatabaseUrl = "https://chat-app-bfc93-default-rtdb.asia-southeast1.firebasedatabase.app/"; // Replace with your Firebase database URL
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseDatabaseUrl);
        messagesDatabaseReference = database.getReference().child("ChatRooms").child("my_chat_room");

        // Initialize UI components
        messageInput = new EditText(this);
        sendMessageButton = new Button(this);
        lastMessageTextView = new TextView(this);

        // Generate IDs for views
        int messageInputId = View.generateViewId();
        int sendMessageButtonId = View.generateViewId();
        int lastMessageTextViewId = View.generateViewId();

        // Set IDs for the components
        messageInput.setId(messageInputId);
        sendMessageButton.setId(sendMessageButtonId);
        lastMessageTextView.setId(lastMessageTextViewId);

        // Set up message input
        messageInput.setHint("Type a message");
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setTextColor(Color.BLACK);
        RelativeLayout.LayoutParams messageInputParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        messageInputParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        messageInputParams.addRule(RelativeLayout.LEFT_OF, sendMessageButtonId); // Adjust input field to make room for the send button
        messageInput.setLayoutParams(messageInputParams);

        // Set up send button
        sendMessageButton.setText("Send");
        RelativeLayout.LayoutParams sendButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        sendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        sendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        sendMessageButton.setLayoutParams(sendButtonParams);

        // Set up last message TextView
        lastMessageTextView.setTextColor(Color.BLACK);
        lastMessageTextView.setTextSize(16);
        lastMessageTextView.setPadding(16, 16, 16, 16);
        RelativeLayout.LayoutParams lastMessageTextViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lastMessageTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lastMessageTextView.setLayoutParams(lastMessageTextViewParams);

        // Create RecyclerView
        recyclerView = new RecyclerView(this);
        RelativeLayout.LayoutParams recyclerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        recyclerViewParams.addRule(RelativeLayout.ABOVE, messageInputId);
        recyclerViewParams.addRule(RelativeLayout.BELOW, lastMessageTextViewId);
        recyclerView.setLayoutParams(recyclerViewParams);

        // Add components to layout
        layout.addView(lastMessageTextView);
        layout.addView(recyclerView);
        layout.addView(messageInput);
        layout.addView(sendMessageButton);

        // Set layout as content view
        setContentView(layout);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList); // Pass the messageList to the adapter
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch existing messages
        fetchMessages();

        // Send message on button click
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        String email = firebaseAuth.getCurrentUser().getEmail();
        String messageId = messagesDatabaseReference.push().getKey();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("userId", userId);
        messageMap.put("email", email);
        messageMap.put("message", message);
        Log.d("ChatRoomActivity", "Sending message: " + message);
        messagesDatabaseReference.child(messageId).setValue(messageMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChatRoomActivity", "Message sent successfully.");
                        messageInput.setText("");
                        Toast.makeText(ChatRoomActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                        updateLastMessageUI(message);
                    } else {
                        Log.e("ChatRoomActivity", "Failed to send message", task.getException());
                        Toast.makeText(ChatRoomActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchMessages() {
        messagesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear(); // Clear the list before adding new messages
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("userId").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    messageList.add(new Message(userId, email, message));
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLastMessageUI(String lastMessage) {
        lastMessageTextView.setText("Last message: " + lastMessage);
    }
}
