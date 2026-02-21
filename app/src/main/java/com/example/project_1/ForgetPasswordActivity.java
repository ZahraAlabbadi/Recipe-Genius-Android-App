package com.example.project_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_1.Data.DatabaseHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnCheckEmail;
    private Button btnReturnToLogin;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        etEmail = findViewById(R.id.etEmail);
        btnCheckEmail = findViewById(R.id.btnResetPassword); // Button to check email


        // Initialize the DatabaseHelper
        db = new DatabaseHelper(this);

        // Return to login interface
        ImageButton buttonBack = findViewById(R.id.buttonBackForget);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the login page
                Intent intent = new Intent(ForgetPasswordActivity.this, LogIn.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
        // Handle Check Email Button Click
        btnCheckEmail.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgetPasswordActivity.this, "Enter your email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(ForgetPasswordActivity.this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the email exists in the database
            boolean emailExists = db.checkEmailExists(email);
            if (emailExists) {
                // Redirect to SpecialInfoVerificationActivity
                Intent intent = new Intent(ForgetPasswordActivity.this, SpecialInfoVerificationActivity.class);
                intent.putExtra("email", email); // Pass the email
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ForgetPasswordActivity.this, "Email does not exist in the database!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Validates an email address format
     *
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
