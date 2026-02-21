package com.example.project_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_HISTORY = 1; // Request code for history activity
    private String currentUserId; // Store the logged-in user's email or ID

    RecyclerView recyclerView;
    TextView WelcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList; // Stores the current chat session
    List<Message> historyMessageList; // Stores the entire chat history
    MassageAdapter messageAdapter;
    
// To use this app, you need an OpenAI API key.
// Replace the placeholder string below with your actual API key.
  // 👇 Replace YOUR_API_KEY_HERE with your actual OpenAI API key
private String apiKey = "YOUR_API_KEY_HERE";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton logoutButton = findViewById(R.id.logout_btn);
        ImageButton historyButton = findViewById(R.id.history_btn);

        // Get the current user's email or ID
        currentUserId = getLoggedInUserId();

        if (currentUserId == null) {
            // Redirect to login if no user is logged in
            Toast.makeText(this, "No user logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LogIn.class);
            startActivity(intent);
            finish();
            return;
        }

        messageList = loadMessageList();

        // Initialize messageList if it was not restored
        if (messageList == null) {
            messageList = new ArrayList<>();
        }

        // Load complete chat history for the logged-in user
        historyMessageList = loadChatHistory(currentUserId);
        if (historyMessageList == null) {
            historyMessageList = new ArrayList<>();
        }

        recyclerView = findViewById(R.id.recyclerview);
        WelcomeTextView = findViewById(R.id.welcome);
        messageEditText = findViewById(R.id.edited_message);
        sendButton = findViewById(R.id.send_btn);

        // Set up RecyclerView and Adapter
        if (messageAdapter == null) {
            messageAdapter = new MassageAdapter(messageList);
            recyclerView.setAdapter(messageAdapter);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setStackFromEnd(true);
            recyclerView.setLayoutManager(llm);
        }

        // Logout button
        logoutButton.setOnClickListener(v -> {
            // Clear the current chat session and save the history
            messageList.clear();
            saveChatHistory(currentUserId);
            clearMessageList(); // Delete the file storing messageList

            // Navigate to the login screen
            Intent intent = new Intent(MainActivity.this, LogIn.class);
            startActivity(intent);
            finish();
        });

        // History button
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, chatHistoryActivity.class);
            intent.putExtra("messageList", new ArrayList<>(historyMessageList)); // Pass the chat history
            startActivityForResult(intent, REQUEST_CODE_HISTORY);
        });

        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (question.toLowerCase().contains("food") ||
                    question.toLowerCase().contains("recipe") ||
                    question.toLowerCase().contains("recipes") ||
                    question.toLowerCase().contains("make") ||
                    question.toLowerCase().contains("makes") ||
                    question.toLowerCase().contains("ingredients") ||
                    question.toLowerCase().contains("ingredient")) {
                addToChat(question, Message.SEND_BY_ME);
                messageEditText.setText("");
                callAPI(question);
                WelcomeTextView.setVisibility(View.GONE);
            } else {
                addToChat(question, Message.SEND_BY_ME);
                messageEditText.setText("");
                addToChat("If you need assistance with recipes, could you kindly provide the list of ingredients you'd like to use? I'm here to help with creating a recipe based on your selected ingredients.", Message.SEND_BY_BOT);
                WelcomeTextView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_HISTORY && resultCode == RESULT_OK) {
            // Reload the updated history list if necessary
            List<Message> updatedHistory = (List<Message>) data.getSerializableExtra("updatedHistory");
            if (updatedHistory != null) {
                historyMessageList.clear();
                historyMessageList.addAll(updatedHistory);
                saveChatHistory(currentUserId); // Persist the updated history
            }

//            // Check if messageList is not empty and hide the welcome message
//            if (!messageList.isEmpty()) {
//                WelcomeTextView.setVisibility(View.GONE);
//            }

            // Refresh the adapter to reflect any changes
            messageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMessageList(); // Save the current messageList to the file
    }

    void addToChat(String message, String sentBy) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy, timestamp)); // Add timestamp
            historyMessageList.add(new Message(message, sentBy, timestamp)); // Add timestamp
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());

            // Save the overall chat history
            saveChatHistory(currentUserId);
        });
    }


    void addResponse(String response) {
        addToChat(response, Message.SEND_BY_BOT);
    }

    void callAPI(String question) {
        JSONArray messagesArray = new JSONArray();
        try {
            messagesArray.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant specializing in recipe recommendations."));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (Message message : messageList) {
            String role = message.getSentBy().equals(Message.SEND_BY_ME) ? "user" : "assistant";
            try {
                messagesArray.put(new JSONObject()
                        .put("role", role)
                        .put("content", message.getMessage()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            messagesArray.put(new JSONObject()
                    .put("role", "user")
                    .put("content", question));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-4o-mini");
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 1000);
            jsonBody.put("temperature", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
            addResponse("Failed to create request body: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray choicesArray = jsonObject.getJSONArray("choices");
                        String result = choicesArray.getJSONObject(0).getJSONObject("message").getString("content");
                        runOnUiThread(() -> addResponse(result.trim()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> addResponse("Failed to parse response: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> {
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "null response";
                            addResponse("Failed to load response: " + response.code() + ", " + errorBody);
                        } catch (IOException e) {
                            addResponse("Failed to load response: " + response.code() + ", unable to parse error body.");
                        }
                    });
                }
            }
        });
    }

    private void saveMessageList() {
        Gson gson = new Gson();
        String json = gson.toJson(messageList); // Convert messageList to JSON
        try {
            FileOutputStream fos = openFileOutput("current_message_list.json", MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Message> loadMessageList() {
        List<Message> messages = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput("current_message_list.json");
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

    private void clearMessageList() {
        deleteFile("current_message_list.json");
    }

    private void saveChatHistory(String userId) {
        Gson gson = new Gson();
        String json = gson.toJson(historyMessageList);
        try {
            String fileName = getChatHistoryFileName(userId);
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private String getChatHistoryFileName(String userId) {
        return userId + "_chat_history.json";
    }

    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("logged_in_user_email", null);
    }
}
