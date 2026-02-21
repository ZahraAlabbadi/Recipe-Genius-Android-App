package com.example.project_1;

import com.google.android.gms.common.api.ApiException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_1.Data.DatabaseHelper;
import com.example.project_1.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

public class LogIn extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    Button googleSignInButton; // Google Sign-In button
    TextView textViewRegister;
    DatabaseHelper db;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        db = new DatabaseHelper(this);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
//        googleSignInButton = findViewById(R.id.buttonGoogleLogin); // Google Sign-In button
        textViewRegister = findViewById(R.id.textViewRegister);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Request email from the user
                .build();

//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Handle Google Sign-In button click
//        googleSignInButton.setOnClickListener(view -> signInWithGoogle());

        textViewRegister.setOnClickListener(v -> startActivity(new Intent(LogIn.this, register.class)));

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Boolean res = db.checkUser(email, password);
            if (res) {
                // Save the logged-in user's email in SharedPreferences
                saveLoggedInUserEmail(email);

                // Redirect to MainActivity
                Intent chat = new Intent(LogIn.this, MainActivity.class);
                startActivity(chat);
                finish();
            } else {
                Toast.makeText(LogIn.this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
            }
        });

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            // Redirect to ForgetPasswordActivity
            Intent intent = new Intent(LogIn.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully
            String email = account.getEmail();
            String displayName = account.getDisplayName();

            Log.d("GoogleSignIn", "Email: " + email + ", Name: " + displayName);

            // Save the logged-in user's email in SharedPreferences
            saveLoggedInUserEmail(email);

            // Redirect to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } catch (ApiException e) {
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save the logged-in user's email in SharedPreferences.
     */
    private void saveLoggedInUserEmail(String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logged_in_user_email", email);
        editor.apply();
    }
}
