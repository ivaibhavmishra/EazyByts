package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SeenRoom extends AppCompatActivity {

    private EditText messageInput;
    private Button sendMessageButton;
    private DatabaseReference messagesDatabaseReference;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize layout
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.parseColor("#FFFFFF")); // Set background color to white

        // Initialize Firebase
        String firebaseDatabaseUrl = "https://chat-app-bfc93-default-rtdb.asia-southeast1.firebasedatabase.app/"; // Replace with your Firebase database URL
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseDatabaseUrl);
        messagesDatabaseReference = database.getReference().child("ChatRooms").child("my_chat_room");

        // Initialize UI components
        messageInput = new EditText(this);
        sendMessageButton = new Button(this);

        // Set up message input
        messageInput.setHint("First register yourself to send the message");
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setTextColor(Color.BLACK);
        messageInput.setEnabled(false); // Disable the input field for guests
        RelativeLayout.LayoutParams messageInputParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        messageInputParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        messageInput.setLayoutParams(messageInputParams);

        // Set up send button
        sendMessageButton.setText("Send");
        sendMessageButton.setOnClickListener(v -> {
            Toast.makeText(SeenRoom.this, "Please register yourself to send messages.", Toast.LENGTH_SHORT).show();
        });
        RelativeLayout.LayoutParams sendButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        sendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        sendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        sendMessageButton.setLayoutParams(sendButtonParams);

        // Create RecyclerView
        recyclerView = new RecyclerView(this);
        RelativeLayout.LayoutParams recyclerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        recyclerViewParams.addRule(RelativeLayout.ABOVE, messageInput.getId());
        recyclerViewParams.setMargins(16, 16, 16, 16);

        recyclerView.setLayoutParams(recyclerViewParams);

        // Add components to layout
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
                Toast.makeText(SeenRoom.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
