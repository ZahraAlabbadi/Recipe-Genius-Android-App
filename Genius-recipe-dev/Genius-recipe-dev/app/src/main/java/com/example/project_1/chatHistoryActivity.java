package com.example.project_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class chatHistoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Message> messageList;
    MassageAdapter messageAdapter;
    ImageButton backButton; // Declare the back button
    ImageButton deleteButton; // Declare the delete button
    private String currentUserId; // To store the logged-in user's email or ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        recyclerView = findViewById(R.id.chat_history_recyclerview);
        backButton = findViewById(R.id.back_button_icon); // Initialize the back button
        deleteButton = findViewById(R.id.clear_chat_button); // Initialize the delete button

        // Get the current user's unique email or ID
        currentUserId = getLoggedInUserId();

        if (currentUserId == null) {
            // Redirect to login if no user is logged in
            Intent intent = new Intent(chatHistoryActivity.this, LogIn.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            // Navigate back to the MainActivity
            Intent intent = new Intent(chatHistoryActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the current activity
        });

        // Set click listener for the delete button with confirmation dialog
        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Load chat history for the current user
        messageList = loadChatHistory(currentUserId);
        if (messageList == null) {
            messageList = new ArrayList<>();
        }

        messageAdapter = new MassageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Method to get the logged-in user's ID or email
    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("logged_in_user_email", null);
    }

    // Method to load chat history for the current user
    private List<Message> loadChatHistory(String userId) {
        List<Message> messages = new ArrayList<>();
        try {
            String fileName = getChatHistoryFileName(userId);
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            Gson gson = new Gson();
            Type type = new TypeToken<List<Message>>() {}.getType();
            messages = gson.fromJson(json, type);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Method to show a confirmation dialog before clearing chat history
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Chat History");
        builder.setMessage("Are you sure you want to delete all chat history? This action cannot be undone.");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Clear chat history
            clearChatHistory(currentUserId);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });
        builder.create().show();
    }

    // Method to clear chat history for the current user
    private void clearChatHistory(String userId) {
        // Clear the message list
        messageList.clear();
        messageAdapter.notifyDataSetChanged();

        // Delete the chat history file for the current user
        String fileName = getChatHistoryFileName(userId);
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(this, "Chat history deleted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete chat history.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No chat history to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get the file name for storing chat history based on the user's ID
    private String getChatHistoryFileName(String userId) {
        return userId + "_chat_history.json";
    }
}
