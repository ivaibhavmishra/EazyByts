package com.example.myapplication;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private FirebaseAuth firebaseAuth;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
        this.firebaseAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth here
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new TextView for each message
        TextView textView = new TextView(parent.getContext());
        textView.setPadding(16, 16, 16, 16);
        return new MessageViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        TextView textView = holder.textView;

        if (message.getUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
            // Right-align message for the current user
            textView.setBackgroundColor(Color.BLUE);
            textView.setTextColor(Color.WHITE);
            textView.setText("You: " + message.getMessage());
            textView.setGravity(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            // Left-align message for others
            textView.setBackgroundColor(Color.parseColor("#D3D3D3"));
            textView.setTextColor(Color.BLACK);
            textView.setText(message.getEmail() + ": " + message.getMessage());
            textView.setGravity(View.TEXT_ALIGNMENT_VIEW_START);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MessageViewHolder(@NonNull TextView itemView) {
            super(itemView);
            textView = itemView;
        }
    }
}
